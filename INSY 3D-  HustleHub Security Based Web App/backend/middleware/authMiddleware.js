const { isTokenCurrent } = require('../utils/sessions.js');
const jwt = require('jsonwebtoken');
const User = require('../models/userModel.js');
const logger = require('../utils/logger.js');
const mongoose = require("mongoose");
const Session = require("../models/sessionModel.js");

// Checks the JWT on protected routes and attaches the authenticated user
// to req.user for later handlers to use (OWASP, 2026c; IIE, 2026).
const validateAuth = async (req, res, next) => {
    const authHeader = req.headers.authorization;

    // Reject missing or incorrectly formatted authorization headers.
    // Every authentication failure must return a response (IIE, 2026).
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        logger.warn(
            `Auth rejected: Missing or malformed token. (${req.method} ${req.originalUrl})`,
            'SECURITY'
        );

        return res.status(401).json({
            message: 'Not authorized, no valid token provided.'
        });
    }

    try {
        const token = authHeader.split(' ')[1];

        // Verify the token's signature and expiry (OWASP, 2026c; IIE, 2026).
        if (!token || !process.env.JWT_SECRET) {
            throw new Error('Token or JWT secret is missing.');
        }

        const decoded = jwt.verify(token, process.env.JWT_SECRET, {algorithms: ["HS256"],});

        // Find the user associated with the token and exclude the password.
        req.user = await User.findById(decoded.id).select('-password');

        // Reject tokens belonging to users who no longer exist.
        if (!req.user) {
            logger.warn(
                `Auth failure: Token belongs to a non-existing user. (${req.method} ${req.originalUrl})`,
                'SECURITY'
            );

            return res.status(401).json({
                message: 'Requesting user no longer exists.'
            });
        }
        if (!isTokenCurrent(decoded, req.user)) {
            logger.warn(`Auth failure: Token was issued before the session was ended. (${req.method} ${req.originalUrl})`, 'SECURITY');
            return res.status(401).json({ message: 'Your session has ended. Please log in again.' });
}
if (
  typeof decoded.sid !== "string" ||
  !mongoose.isObjectIdOrHexString(decoded.sid)
) {
  return res.status(401).json({
    message: "Your session has ended. Please log in again.",
  });
}

const session = await Session.findOne({
  _id: decoded.sid,
  user: req.user._id,
  tokenVersion: req.user.tokenVersion ?? 0,
  revokedAt: null,
  expiresAt: { $gt: new Date() },
}).lean();

if (!session) {
  return res.status(401).json({
    message: "Your session has ended. Please log in again.",
  });
}

req.sessionId = String(session._id);

// Update at most once per minute for this session.
const now = new Date();
await Session.updateOne(
  {
    _id: session._id,
    revokedAt: null,
    lastSeenAt: { $lt: new Date(now.getTime() - 60_000) },
  },
  {
    $set: { lastSeenAt: now },
  }
);

        // Continue to the next middleware or route handler.
        return next();

    } catch (error) {
        // Fail closed: invalid, expired, or malformed tokens are rejected.
        // Do not expose token-validation details to the client (OWASP, 2026e).
        logger.warn(
            `Auth failure: Invalid or expired token. (${req.method} ${req.originalUrl})`,
            'SECURITY'
        );

        return res.status(401).json({
            message: 'Not authorized, failed to validate token.'
        });
    }
};

// Role-based access control restricts routes to authorised user roles.
// Requires validateAuth to run first (OWASP, 2021; IIE, 2026).
const validateRole = (...roles) => {
    return (req, res, next) => {
        // Only authenticated users with an allowed role may continue.
        if (!req.user || !roles.includes(req.user.role)) {
            const username = req.user?.username || 'Unknown user';

            logger.warn(
                `Auth forbidden: ${username} attempted unauthorised access. (${req.method} ${req.originalUrl})`,
                'SECURITY'
            );

            // 403 means access is forbidden; 401 means authentication failed.
            return res.status(403).json({
                message: 'Forbidden: you do not have permission to do this.'
            });
        }

        return next();
    };
};

module.exports = { validateAuth, validateRole };

/*
References:

The Independent Institute of Education (IIE), 2026.
Information Systems 3D [INSY7314/W Module Manual].
The Independent Institute of Education: Unpublished.

Mozilla, 2026. HTTP response status codes. [online]
Available at: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status
[Accessed 4 September 2026].

OWASP, 2026c. JSON Web Token Cheat Sheet. [online]
Available at: https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html
[Accessed 4 September 2026].

OWASP, 2021. OWASP Top 10:2021 - A07 Identification and Authentication Failures. [online]
Available at: https://owasp.org/Top10/2021/A07_2021-Identification-and-Authentication-Failures/
[Accessed 4 September 2026].

OWASP, 2026e. REST Security Cheat Sheet. [online]
Available at: https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html
[Accessed 4 September 2026].
*/

