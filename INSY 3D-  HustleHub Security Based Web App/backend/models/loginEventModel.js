const mongoose = require("mongoose");

// One row per security-relevant event on an account. Shown to the owner on the
// Security page so they can spot activity that was not them.
const EVENT_TYPES = [
    "login_success",
    "login_failed",
    "two_factor_failed",
    "password_changed",
    "sessions_revoked",
];

const loginEventSchema = new mongoose.Schema({
    user: {
        type: mongoose.Schema.Types.ObjectId,
        ref: "User",
        required: true,
    },
    type: {
        type: String,
        enum: EVENT_TYPES,
        required: true,
    },
    // IP addresses are personal information under POPIA, so rows expire (below)
    ip: { type: String, default: "" },
    userAgent: { type: String, default: "" },
    // TTL index: MongoDB deletes each event automatically 90 days after it was created
    createdAt: {
        type: Date,
        default: Date.now,
        expires: 60 * 60 * 24 * 90,
    },
});

// the activity list asks for "this user's newest events"
loginEventSchema.index({ user: 1, createdAt: -1 });

const LoginEvent = mongoose.model("LoginEvent", loginEventSchema);

module.exports = LoginEvent;
module.exports.EVENT_TYPES = EVENT_TYPES;