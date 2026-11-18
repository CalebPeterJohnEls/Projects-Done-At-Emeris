
// Handles two-factor authentication (2FA) using TOTP (time-based one-time passwords):
// setup, enable, login with a code, disable and status.

const jwt = require("jsonwebtoken"); // signs and verifies the temporary 2FA login token
const { recordEvent } = require("../utils/securityEvents.js"); // saves security events (e.g. failed 2FA) for monitoring
const QRCode = require("qrcode"); // turns the TOTP link into a QR code the user scans
const { generateSecret, generateURI, verify } = require("otplib"); // TOTP functions from otplib v13 (otplib, 2026)
const User = require("../models/userModel.js");
const logger = require("../utils/logger.js");
// Helpers: AES-256-GCM encryption for the secret, and bcrypt-hashed one-time recovery codes
const { encrypt, decrypt, generateRecoveryCodes, consumeRecoveryCode } = require("../utils/twoFactor.js");
const { createSessionToken } = require("../utils/sessionTokens.js"); // issues the normal session JWT
const { isTokenCurrent } = require("../utils/sessions.js"); // checks the token is still valid for this user

// Five wrong codes lock the second factor for 15 minutes. NIST allows limits on
// consecutive failed attempts and permits stricter limits than its maximum
// (Temoshok et al., 2025). This also slows brute-forcing of 6-digit codes
// (OWASP, 2026n).
const MAX_FAILS = 5;
const LOCK_MS = 15 * 60 * 1000;

// A TOTP code is exactly 6 digits (IETF, 2011). Anything else is treated as a recovery code.
const isTotp = (code) => /^\d{6}$/.test(code);

// Checks a 6-digit code against the secret and returns its time step, or null if invalid.
const checkTotp = async (secret, code, lastStep) => {
    // epochTolerance of 30 seconds allows one time step of clock drift or delay
    // either side, as RFC 6238 advises (IETF, 2011; otplib, 2026).
    const result = await verify({ secret, token: code, epochTolerance: 30 });
    // Each code may only be used once, so a code from the same or an earlier
    // time step is rejected (Temoshok et al., 2025).
    if (!result.valid || result.timeStep <= (lastStep || 0)) return null;
    return result.timeStep;
};

// Checks the second factor (TOTP or recovery code) and applies the lockout rules.
// Returns "locked", "ok" or "bad".
const checkSecondFactor = async (user, code) => {
    const tf = user.twoFactor;
    // Refuse to check anything while the account's second factor is locked
    if (tf.lockedUntil && tf.lockedUntil > Date.now()) return "locked";

    let ok = false;
    if (isTotp(code)) {
        // The secret is stored encrypted, so it is decrypted only for the check
        // (OWASP, 2026q).
        const step = await checkTotp(decrypt(tf.secret), code, tf.lastUsedStep);
        if (step) {
            tf.lastUsedStep = step; // remember the step so this code cannot be replayed
            ok = true;
        }
    } else {
        // Recovery codes are single-use and stored hashed (OWASP, 2026g;
        // Temoshok et al., 2025)
        ok = await consumeRecoveryCode(user, code);
    }

    if (ok) {
        // Success resets the failure counter and any lock
        tf.failedAttempts = 0;
        tf.lockedUntil = undefined;
    } else {
        // Failure counts towards the lockout
        tf.failedAttempts += 1;
        if (tf.failedAttempts >= MAX_FAILS) {
            tf.lockedUntil = new Date(Date.now() + LOCK_MS);
            tf.failedAttempts = 0;
        }
    }
    await user.save();
    return ok ? "ok" : "bad";
};

// Step 1 of enrolment: create a secret and QR code for the logged-in user.
const setupTwoFactor = async (req, res) => {
    // "+twoFactor" loads the field, which is hidden from queries by default
    const user = await User.findById(req.user._id).select("+twoFactor");
    if (user.twoFactor.enabled) {
        return res.status(409).json({ message: "Two-factor authentication is already enabled." });
    }
    const secret = generateSecret(); // random shared secret (otplib, 2026)
    // Stored as "pending" (encrypted) until the user proves their app works
    user.twoFactor.pendingSecret = encrypt(secret);
    await user.save();
    // otpauth:// link that authenticator apps read from the QR code (otplib, 2026)
    const uri = generateURI({ issuer: "HustleHub+", label: user.email, secret });
    // Only the user ID and IP are logged, never the secret (OWASP, 2026l)
    logger.info(`2FA setup started for user ${user._id} from ${req.ip}`, "AUTH");
    // QR enrolment is the method OWASP recommends (OWASP, 2026g)
    return res.status(200).json({ qrDataUrl: await QRCode.toDataURL(uri), secret });
};

// Step 2 of enrolment: confirm a code from the app, then switch 2FA on.
const enableTwoFactor = async (req, res) => {
    const user = await User.findById(req.user._id).select("+twoFactor");
    const tf = user.twoFactor;
    const code = String(req.body.code || "").trim();
    if (!tf.pendingSecret || !isTotp(code)) {
        return res.status(400).json({ message: "Start setup first and enter the 6-digit code." });
    }
    // Proves the user's authenticator app holds the same secret (lastStep is 0
    // because no code has been used yet)
    const step = await checkTotp(decrypt(tf.pendingSecret), code, 0);
    if (!step) {
        logger.warn(`2FA enable failed for user ${user._id} from ${req.ip}`, "AUTH");
        return res.status(401).json({ message: "Invalid code." });
    }
    // Recovery codes are issued at setup, shown once, and only hashes are kept
    // (OWASP, 2026g; Temoshok et al., 2025)
    const { codes, hashes } = await generateRecoveryCodes();
    tf.secret = tf.pendingSecret; // pending secret becomes the active secret
    tf.pendingSecret = undefined;
    tf.enabled = true;
    tf.recoveryCodes = hashes;
    tf.lastUsedStep = step; // the confirmation code cannot be reused
    await user.save();
    logger.info(`2FA enabled for user ${user._id} from ${req.ip}`, "AUTH");
    return res.status(200).json({ recoveryCodes: codes });
};

// Second step of login: the user already passed the password step and now sends
// the temporary token plus a TOTP or recovery code.
const loginTwoFactor = async (req, res) => {
    let payload;
    try {
        // The temporary token uses a different secret from normal session
        // tokens, so it cannot be used as a session (OWASP, 2026c)
        payload = jwt.verify(String(req.body.tempToken || ""), process.env.JWT_SECRET + ":2fa");
    } catch {
        // Generic message so nothing useful is revealed to an attacker (OWASP, 2026a)
        return res.status(401).json({ message: "Invalid or expired login attempt. Please log in again." });
    }
    const user = await User.findById(payload.id).select("+twoFactor");
    // The token must be for the 2FA step, belong to an existing user with 2FA on,
    // and still be current (not revoked by a password change or sign-out)
    if (
        payload.purpose !== "2fa" ||
        !user ||
        !user.twoFactor.enabled ||
        !isTokenCurrent(payload, user)
    ) {
        return res.status(401).json({
            message: "Invalid or expired login attempt. Please log in again.",
        });
    }
    const outcome = await checkSecondFactor(user, String(req.body.code || "").trim());
    if (outcome === "locked") {
        // Lockouts are logged as security events for monitoring (OWASP, 2026l)
        logger.warn(`2FA locked out user ${user._id} from ${req.ip}`, "SECURITY");
        await recordEvent(req, user._id, "two_factor_failed");
        return res.status(429).json({ message: "Too many attempts. Try again in 15 minutes." });
    }
    if (outcome !== "ok") {
        logger.warn(`2FA code rejected for user ${user._id} from ${req.ip}`, "AUTH");
        await recordEvent(req, user._id, "two_factor_failed");
        return res.status(401).json({ message: "Invalid code." });
    }
    logger.info(`Login success with 2FA for user ${user._id} (${user.role}) from ${req.ip}`, "AUTH");
    await recordEvent(req, user._id, "login_success");
    // Only now is the real session token issued, so passing the password step
    // alone never gives access
    return res.status(200).json({
        _id: user._id,
        username: user.username,
        email: user.email,
        role: user.role,
        token: await createSessionToken(req, user),
    });
};

// Turns 2FA off. Needs both the password and a valid code.
const disableTwoFactor = async (req, res) => {
    const user = await User.findById(req.user._id).select("+twoFactor");
    if (!user.twoFactor.enabled) {
        return res.status(400).json({ message: "Two-factor authentication is not enabled." });
    }
    // Re-authentication is required before a factor is changed (OWASP, 2026g).
    // The code is only checked if the password is correct.
    const passwordOk = await user.matchPassword(String(req.body.password || ""));
    const outcome = passwordOk ? await checkSecondFactor(user, String(req.body.code || "").trim()) : "bad";
    if (outcome !== "ok") {
        logger.warn(`2FA disable rejected for user ${user._id} from ${req.ip}`, "SECURITY");
        // One message for a wrong password or wrong code, so an attacker cannot
        // tell which was wrong (OWASP, 2026a)
        return res.status(401).json({ message: "Invalid password or code." });
    }
    user.twoFactor = {}; // clears the secret, recovery codes and counters
    await user.save();
    logger.info(`2FA disabled for user ${user._id} from ${req.ip}`, "AUTH");
    return res.status(200).json({ message: "Two-factor authentication disabled." });
};

// Tells the frontend whether 2FA is currently on (used by the /security page).
const getTwoFactorStatus = async (req, res) => {
    const user = await User.findById(req.user._id).select("+twoFactor");
    return res.status(200).json({ enabled: !!(user.twoFactor && user.twoFactor.enabled) });
};

module.exports = { setupTwoFactor, enableTwoFactor, loginTwoFactor, disableTwoFactor, getTwoFactorStatus, checkSecondFactor };

/*
References

IETF, 2011. RFC 6238: TOTP: Time-Based One-Time Password Algorithm [online]. <https://datatracker.ietf.org/doc/rfc6238> [Accessed 23 October 2026].

otplib, 2026. Getting started [online]. <https://otplib.yeojz.dev/guide/getting-started> [Accessed 23 October 2026].

OWASP, 2026a. Authentication Cheat Sheet [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html> [Accessed 4 September 2026].

OWASP, 2026c. JSON Web Token Cheat Sheet [online]. <https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html> [Accessed 4 September 2026].

OWASP, 2026g. Multifactor Authentication Cheat Sheet [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html> [Accessed 23 October 2026].

OWASP, 2026l. Logging Cheat Sheet [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html> [Accessed 9 November 2026].

OWASP, 2026n. Credential Stuffing Prevention Cheat Sheet [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Credential_Stuffing_Prevention_Cheat_Sheet.html> [Accessed 9 November 2026].

OWASP, 2026q. Cryptographic Storage Cheat Sheet [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html> [Accessed 9 November 2026].

Temoshok, D., Fenton, J., Choong, Y., Lefkovitz, N., Regenscheid, A., Galluzzo, R. and Richer, J., 2025. Digital Identity Guidelines: Authentication and Authenticator Management. NIST Special Publication 800-63B-4 [online]. <https://csrc.nist.gov/pubs/sp/800/63/b/4/final> [Accessed 9 November 2026].
*/