const User = require("../models/userModel.js");
const jwt = require("jsonwebtoken");
const { body, validationResult } = require("express-validator");
const logger = require("../utils/logger.js"); // writes security events to the log files (IIE, 2026)
const { recordEvent } = require("../utils/securityEvents.js");   // top
const { createSessionToken } = require("../utils/sessionTokens.js");


// Backup check for a valid email shape (something@something.something) (IIE, 2026)
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// Signs a JWT with the user's id and role, valid for 7 days (OWASP, 2026c; IIE, 2026)
const generateToken = (id, role, tokenVersion = 0) => {
    return jwt.sign({ id, role, tv: tokenVersion }, process.env.JWT_SECRET, { expiresIn: "7d" });
};

// Signs a 5 minute token that only proves the password step passed. It uses a
// different secret, so it can never be accepted as a normal session token (OWASP, 2026c; OWASP, 2026g)
const signTempToken = (user) =>
  jwt.sign(
    {
      id: String(user._id),
      purpose: "2fa",
      tv: user.tokenVersion ?? 0,
    },
    process.env.JWT_SECRET + ":2fa",
    {
      expiresIn: "5m",
      algorithm: "HS256",
    }
  );

// Runs after the validation rules below; stops the request with a generic
// 400 if any field failed a check (OWASP, 2026e)
const handleValidationErrors = (req, res, next) => {
    const errors = validationResult(req);
    if (!errors.isEmpty()) {
        // the rejected request is logged without the values the user typed (OWASP, 2026e)
        logger.warn(`Validation failed on ${req.method} ${req.originalUrl} from ${req.ip}`, "VALIDATION");
        return res.status(400).json({
            message: "Invalid input supplied",
            errors: errors.array().map((e) => ({ field: e.path, message: e.msg })),
        });
    }
    next();
};

const registerValidationRules = [
    body("username")
        .trim() // removes accidental leading/trailing spaces (Validator.js, 2026)
        .isLength({ min: 3, max: 30 })
        .withMessage("Username must be between 3 and 30 characters")
        .matches(/^[a-zA-Z0-9_]+$/)
        .withMessage("Username may only contain letters, numbers and underscores")
        .escape(), // encodes HTML-unsafe characters to reduce stored-XSS risk (OWASP, 2026b)
    body("email")
        .trim()
        .isEmail()
        .withMessage("A valid email address is required")
        .normalizeEmail(), // stops the same address being registered in slightly different forms (Validator.js, 2026; Express-validator, 2026)
    body("password")
        // minimum length and character composition rules for a stored credential (OWASP, 2026a; Stack Overflow, 2010)
        .isLength({ min: 8 })
        .withMessage("Password must be at least 8 characters long")
        .matches(/\d/)
        .withMessage("Password must contain at least one number")
        .matches(/[A-Z]/)
        .withMessage("Password must contain at least one uppercase letter"),
        // character-class rules like these are a common but debated approach - length matters more than complexity in some guidance (Stack Overflow, 2018)
    body("role")
        .optional()
        // only client/freelancer allowed here - stops someone self-assigning admin (OWASP, 2026c; OWASP, 2021)
        .isIn(["client", "freelancer"])
        .withMessage("Role must be either client or freelancer"),
];

const loginValidationRules = [
    body("email").trim().isEmail().withMessage("A valid email address is required").normalizeEmail(),
    body("password").notEmpty().withMessage("Password is required"),
];

const registerUser = async (req, res) => {
    try {
        const { username, email, password, role } = req.body;

        if (!username || !email || !password) {
            return res.status(400).json({ message: "Please ensure you enter all required information" });
        }

        // never trust a client-supplied role directly - only freelancer can
        // be chosen, everything else defaults to client (OWASP, 2021; OWASP, 2026c)
        const safeRole = role === "freelancer" ? "freelancer" : "client";

        const existingUser = await User.findOne({ email });
        if (existingUser) {
            // the email itself is not logged, only the IP, to avoid storing personal data in logs (OWASP, 2026e)
            logger.warn(`Registration attempted with an email that already exists, from ${req.ip}`, "AUTH");
            // 409 = resource conflict, this email is already registered (Mozilla, 2026)
            return res.status(409).json({ message: "Unable to register with the supplied details" });
        }

        // password hashing happens automatically in the model's pre-save hook,
        // never handled as plaintext here (OWASP, 2026d)
        const user = await User.create({ username, email, password, role: safeRole });

        // successful sign-ups are logged by id and role only (OWASP, 2026a)
        logger.info(`New ${user.role} registered (id: ${user._id}) from ${req.ip}`, "AUTH");

        return res.status(201).json({
            _id: user._id,
            username: user.username,
            email: user.email,
            role: user.role,
            token: await createSessionToken(req, user),
        });
    } catch (error) {
        // full error logged server-side only, generic message sent to client (OWASP, 2026e)
        logger.error(`Registration error: ${error.message}`, "AUTH", error);
        return res.status(500).json({ message: "An unexpected error occurred. Please try again later." });
    }
};

const loginUser = async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ message: "Ensure all required information is present." });
        }

        // the 2FA fields are hidden by default, so they are asked for here (OWASP, 2026g)
        const user = await User.findOne({ email }).select("+twoFactor");

        // same message and status whether the email doesn't exist or the
        // password is wrong, so nobody can tell which one failed (OWASP, 2026a; Mozilla, 2026)
        if (!user) {
            // failed logins are logged so repeated attempts can be spotted (OWASP, 2026a)
            logger.warn(`Failed login (unknown email) from ${req.ip}`, "AUTH");
            return res.status(401).json({ message: "Invalid email or password" });
        }

        // compares the hash of the entered password against the stored hash (OWASP, 2026d)
        const isMatch = await user.matchPassword(password);

        if (!isMatch) {
            // the password is never logged, only the account id and IP (OWASP, 2026e)
            logger.warn(`Failed login (wrong password) for user ${user._id} from ${req.ip}`, "AUTH");
            await recordEvent(req, user._id, "login_failed");
            return res.status(401).json({ message: "Invalid email or password" });
        }

        // with 2FA on, no session token is issued until the second factor is checked (OWASP, 2026g; IETF, 2011)
        if (user.twoFactor && user.twoFactor.enabled) {
            logger.info(`Password accepted, 2FA required for user ${user._id} from ${req.ip}`, "AUTH");
            return res.status(200).json({ twoFactorRequired: true, tempToken: signTempToken(user) });
        }

        logger.info(`Login success for user ${user._id} (${user.role}) from ${req.ip}`, "AUTH");
        await recordEvent(req, user._id, "login_success");
        return res.status(200).json({
            _id: user._id,
            username: user.username,
            email: user.email,
            role: user.role,
            token: await createSessionToken(req, user),
        });
    } catch (error) {
        logger.error(`Login error: ${error.message}`, "AUTH", error);
        return res.status(500).json({ message: "An unexpected error occurred. Please try again later." });
    }
};

module.exports = {
    registerUser,
    loginUser,
    generateToken,
    registerValidationRules,
    loginValidationRules,
    handleValidationErrors,
};

/*
References:
Express-validator, 2026. Guides. [online] Available at: <https://express-validator.github.io/docs/category/guides> [Accessed 4 September 2026].
IETF, 2011. RFC 6238: TOTP: Time-Based One-Time Password Algorithm. [online] Available at: <https://datatracker.ietf.org/doc/rfc6238> [Accessed 23 October 2026].
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
Mozilla, 2026. HTTP response status codes. [online] Available at: <https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status> [Accessed 4 September 2026].
OWASP, 2026a. Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026b. Cross Site Scripting Prevention Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026c. JSON Web Token Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2021. OWASP Top 10:2021 - A07 Identification and Authentication Failures. [online] Available at: <https://owasp.org/Top10/2021/A07_2021-Identification_and_Authentication_Failures/> [Accessed 4 September 2026].
OWASP, 2026d. Password Storage Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026g. Multifactor Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html> [Accessed 23 October 2026].
Stack Overflow, 2010. Regular expression for password validation. [online] Available at: <https://stackoverflow.com/questions/2370015/regular-expression-for-password-validation> [Accessed 4 September 2026].
Stack Overflow, 2018. "Reference" - Password validation. [online] Available at: <https://stackoverflow.com/questions/48345922/reference-password-validation> [Accessed 4 September 2026].
Validator.js, 2026. Validators. [online] Available at: <https://github.com/validatorjs/validator.js#validators> [Accessed 4 September 2026].
*/