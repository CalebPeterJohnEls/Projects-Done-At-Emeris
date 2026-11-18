const jwt = require("jsonwebtoken");
const Session = require("../models/sessionModel.js");

const SESSION_SECONDS = 7 * 24 * 60 * 60;

const createSessionToken = async (req, user) => {
  const issuedAt = Math.floor(Date.now() / 1000);
  const expiresAt = issuedAt + SESSION_SECONDS;

  const session = new Session({
    user: user._id,
    tokenVersion: user.tokenVersion ?? 0,
    userAgent: String(req.get("user-agent") || "").slice(0, 1000),
    ip: req.ip || "",
    createdAt: new Date(issuedAt * 1000),
    lastSeenAt: new Date(),
    expiresAt: new Date(expiresAt * 1000),
  });

  const token = jwt.sign(
    {
      id: String(user._id),
      role: user.role,
      tv: user.tokenVersion ?? 0,
      sid: String(session._id),
      iat: issuedAt,
      exp: expiresAt,
    },
    process.env.JWT_SECRET,
    { algorithm: "HS256" }
  );

  await session.save();

  return token;
};

module.exports = { createSessionToken };