const crypto = require("crypto");
const bcrypt = require("bcryptjs");

// This file handles the two-factor authentication (2FA) secrets and recovery codes.
// - encrypt/decrypt: protect the 2FA secret before it is stored in the database.
// - generateRecoveryCodes/consumeRecoveryCode: one-time backup codes if the user loses their authenticator app.

// AES-256-GCM encrypts the data AND adds a tag that proves it was not changed (OWASP, 2026q).
// These sizes are fixed so encrypt and decrypt always agree.
const IV_LENGTH = 12;   // 12-byte IV (a random "starting value", the recommended size for GCM)
const TAG_LENGTH = 16;  // full 16-byte authentication tag, so it cannot be shortened by an attacker

// The encryption key lives in an environment variable, not in the code (OWASP, 2026r).
// It is stored as hex text, so we turn it back into bytes here.
const key = () => Buffer.from(process.env.TWOFA_ENC_KEY, "hex");

const encrypt = (plain) => {
    // A new random IV for every encryption, so the same text never encrypts the same way twice (OWASP, 2026q).
    const iv = crypto.randomBytes(IV_LENGTH);

    // Set up AES-256-GCM with our key and IV; authTagLength forces the full-size tag (OWASP, 2026q).
    const cipher = crypto.createCipheriv("aes-256-gcm", key(), iv, { authTagLength: TAG_LENGTH });

    // Encrypt the text into bytes.
    const data = Buffer.concat([cipher.update(plain, "utf8"), cipher.final()]);

    // Save as "iv.tag.data" in hex, so decrypt can split it back apart later.
    return [iv, cipher.getAuthTag(), data].map((b) => b.toString("hex")).join(".");
};

const decrypt = (stored) => {
    // Split the saved text back into its three parts.
    const [iv, tag, data] = stored.split(".").map((h) => Buffer.from(h, "hex"));

    // Reject values that are missing a part or have the wrong sizes, before decrypting anything.
    if (!iv || !tag || !data || iv.length !== IV_LENGTH || tag.length !== TAG_LENGTH) {
        throw new Error("Invalid encrypted value");
    }

    // Tell Node the exact tag size to expect, so a shortened (forged) tag is refused (OWASP, 2026q).
    const decipher = crypto.createDecipheriv("aes-256-gcm", key(), iv, { authTagLength: TAG_LENGTH });

    // Give Node the tag; final() throws an error if the data was changed after encryption.
    decipher.setAuthTag(tag);
    return Buffer.concat([decipher.update(data), decipher.final()]).toString("utf8");
};

const generateRecoveryCodes = async () => {
    // Make 8 random codes using the secure random generator, not Math.random (OWASP, 2026q).
    // Each code is 5 random bytes shown as 10 hex characters.
    const codes = Array.from({ length: 8 }, () => crypto.randomBytes(5).toString("hex"));

    // Hash every code with bcrypt before storing it, like a password (OWASP, 2026d).
    // Only the hashes are saved; the plain codes are shown to the user once.
    const hashes = await Promise.all(codes.map((c) => bcrypt.hash(c, 10)));
    return { codes, hashes };
};

const consumeRecoveryCode = async (user, code) => {
    const hashes = user.twoFactor.recoveryCodes;

    // Compare the entered code against each stored hash.
    for (let i = 0; i < hashes.length; i++) {
        if (await bcrypt.compare(code, hashes[i])) {
            // Remove the used hash so each recovery code works only once (OWASP, 2026g; Temoshok et al., 2025).
            hashes.splice(i, 1);
            return true;
        }
    }
    // No match: the code is wrong or was already used.
    return false;
};

module.exports = { encrypt, decrypt, generateRecoveryCodes, consumeRecoveryCode };

/*
References

OWASP, 2026d. Password Storage Cheat Sheet [online]. https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html [Accessed 4 September 2026].

OWASP, 2026g. Multifactor Authentication Cheat Sheet [online]. https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html [Accessed 23 October 2026].

OWASP, 2026q. Cryptographic Storage Cheat Sheet [online]. https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html [Accessed 9 November 2026].

OWASP, 2026r. Secrets Management Cheat Sheet [online]. https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html [Accessed 9 November 2026].

Temoshok, D., Fenton, J., Choong, Y., Lefkovitz, N., Regenscheid, A., Galluzzo, R. and Richer, J., 2025. Digital Identity Guidelines: Authentication and Authenticator Management. NIST Special Publication 800-63B-4 [online]. https://csrc.nist.gov/pubs/sp/800/63/b/4/final [Accessed 9 November 2026].
*/