const LoginEvent = require("../models/loginEventModel.js");
const logger = require("./logger.js");

const MAX_USER_AGENT = 200;

// Records one security event for an account. It never throws: failing to write
// an activity row must not break a login or a password change.
const recordEvent = async (req, userId, type) => {
    try {
        await LoginEvent.create({
            user: userId,
            type,
            ip: String((req && req.ip) || "").slice(0, 64),
            userAgent: String((req && req.headers && req.headers["user-agent"]) || "").slice(0, MAX_USER_AGENT),
        });
    } catch (error) {
        logger.error(`Could not record ${type} event: ${error.message}`, "AUTH", error);
    }
};

module.exports = { recordEvent, MAX_USER_AGENT };