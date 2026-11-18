const User = require("../models/userModel.js");
const LoginEvent = require("../models/loginEventModel.js");
const logger = require("../utils/logger.js");
const bcrypt = require("bcryptjs");
const Session = require("../models/sessionModel.js");
const { createSessionToken } = require("../utils/sessionTokens.js");
const twoFactor = require("./twoFactorController.js");
const { recordEvent } = require("../utils/securityEvents.js");
const { describeDevice } = require("../utils/sessions.js");

const ACTIVITY_LIMIT = 10;

// Both actions below bump the user's tokenVersion, which invalidates every
// token issued before now. The caller gets a fresh token back so the device
// they are using stays signed in (OWASP, 2026c).
const currentVersionFilter = (user) => {
  const version = user.tokenVersion ?? 0;

  return version === 0
    ? {
        _id: user._id,
        $or: [
          { tokenVersion: 0 },
          { tokenVersion: { $exists: false } },
        ],
      }
    : {
        _id: user._id,
        tokenVersion: version,
      };
};

// POST /api/account/password
// Needs the current password, and the 2FA code when 2FA is on. Signs out every
// other device.
const changePassword = async (req, res) => {
    try {
        const user = await User.findById(req.user._id).select("+twoFactor");
        const currentPassword = String(req.body.currentPassword || "");
        const newPassword = String(req.body.newPassword || "");

        if (!(await user.matchPassword(currentPassword))) {
            logger.warn(`Password change rejected (wrong current password) for user ${user._id} from ${req.ip}`, "SECURITY");
            return res.status(401).json({ message: "Current password is incorrect." });
        }

        // a stolen session token alone is not enough: with 2FA on, the code is needed too
        if (user.twoFactor && user.twoFactor.enabled) {
            const outcome = await twoFactor.checkSecondFactor(user, String(req.body.code || "").trim());
            if (outcome === "locked") {
                logger.warn(`Password change blocked, 2FA locked for user ${user._id} from ${req.ip}`, "SECURITY");
                return res.status(429).json({ message: "Too many attempts. Try again in 15 minutes." });
            }
            if (outcome !== "ok") {
                logger.warn(`Password change rejected (bad 2FA code) for user ${user._id} from ${req.ip}`, "SECURITY");
                return res.status(401).json({ message: "Invalid code." });
            }
        }

        if (await user.matchPassword(newPassword)) {
            return res.status(400).json({ message: "Choose a new password that is different from your current one." });
        }

        // findOneAndUpdate does not run the pre-save hashing hook,
        // so hash explicitly here.
        const passwordHash = await bcrypt.hash(newPassword, 12);

        const updatedUser = await User.findOneAndUpdate(
            {
                ...currentVersionFilter(user),
                password: user.password,
            },
            {
                $set: { password: passwordHash },
                $inc: { tokenVersion: 1 },
            },
            { new: true }
        );

        if (!updatedUser) {
            return res.status(409).json({
                message: "Your account changed during this request. Please log in again.",
            });
        }

        const token = await createSessionToken(req, updatedUser);

        await recordEvent(req, user._id, "password_changed");
        logger.info(`Password changed and other sessions ended for user ${user._id} from ${req.ip}`, "AUTH");

        return res.status(200).json({
            message: "Password changed. You have been signed out of all other devices.",
            token,
        });
    } catch (error) {
        logger.error(`Change password error: ${error.message}`, "AUTH", error);
        return res.status(500).json({ message: "An unexpected error occurred. Please try again later." });
    }
};

// POST /api/account/logout-all
// Ends every session except the one making the request. The password is
// required so a stolen token cannot quietly lock the real owner out.
const logoutAll = async (req, res) => {
    try {
        const user = await User.findById(req.user._id);

        if (!(await user.matchPassword(String(req.body.currentPassword || "")))) {
            logger.warn(`Sign-out-everywhere rejected (wrong password) for user ${user._id} from ${req.ip}`, "SECURITY");
            return res.status(401).json({ message: "Current password is incorrect." });
        }

        const updatedUser = await User.findOneAndUpdate(
            currentVersionFilter(user),
            { $inc: { tokenVersion: 1 } },
            { new: true }
        );

        if (!updatedUser) {
            return res.status(409).json({
                message: "Your account changed during this request. Please log in again.",
            });
        }

const token = await createSessionToken(req, updatedUser);

        await recordEvent(req, user._id, "sessions_revoked");
        logger.info(`All other sessions ended for user ${user._id} from ${req.ip}`, "AUTH");

        return res.status(200).json({
            message: "You have been signed out of all other devices.",
            token,
        });
    } catch (error) {
        logger.error(`Log out all error: ${error.message}`, "AUTH", error);
        return res.status(500).json({ message: "An unexpected error occurred. Please try again later." });
    }
};

const getSessions = async (req, res) => {
  try {
    const sessions = await Session.find({
      user: req.user._id,
      tokenVersion: req.user.tokenVersion ?? 0,
      revokedAt: null,
      expiresAt: { $gt: new Date() },
    })
      .sort({ lastSeenAt: -1 })
      .lean();

    return res.status(200).json(
      sessions.map((session) => ({
        _id: String(session._id),
        device: describeDevice(session.userAgent),
        ip: session.ip,
        createdAt: session.createdAt,
        lastSeenAt: session.lastSeenAt,
        expiresAt: session.expiresAt,
        isCurrent: String(session._id) === req.sessionId,
      }))
    );
  } catch (error) {
    logger.error(`Get sessions error: ${error.message}`, "AUTH", error);

    return res.status(500).json({
      message: "Could not load your signed-in sessions.",
    });
  }
};

const logoutCurrent = async (req, res) => {
  try {
    await Session.updateOne(
      {
        _id: req.sessionId,
        user: req.user._id,
        revokedAt: null,
      },
      {
        $set: { revokedAt: new Date() },
      }
    );

    return res.status(204).end();
  } catch (error) {
    logger.error(`Logout error: ${error.message}`, "AUTH", error);

    return res.status(500).json({
      message: "Could not sign out. Please try again.",
    });
  }
};

// GET /api/account/activity
// The caller's own recent events. The filter comes from req.user, never from
// the URL, so nobody can read another account's history.
const getActivity = async (req, res) => {
    try {
        const events = await LoginEvent.find({ user: req.user._id })
            .sort({ createdAt: -1 })
            .limit(ACTIVITY_LIMIT)
            .lean();

        return res.status(200).json(
            events.map((e) => ({
                _id: e._id,
                type: e.type,
                ip: e.ip,
                device: describeDevice(e.userAgent),
                createdAt: e.createdAt,
            }))
        );
    } catch (error) {
        logger.error(`Get activity error: ${error.message}`, "AUTH", error);
        return res.status(500).json({ message: "An unexpected error occurred. Please try again later." });
    }
};

module.exports = {
  changePassword,
  logoutAll,
  getActivity,
  getSessions,
  logoutCurrent,
};