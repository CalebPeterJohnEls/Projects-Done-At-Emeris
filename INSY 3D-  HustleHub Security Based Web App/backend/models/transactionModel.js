const crypto = require("crypto");
const mongoose = require("mongoose");

const transactionSchema = new mongoose.Schema(
    {
        // unique: one booking can only ever have one transaction
        booking: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "Booking",
            required: true,
            unique: true,
        },

        client: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User",
            required: true,
            index: true,
        },

        freelancer: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User",
            required: true,
            index: true,
        },

        gig: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "Gig",
            required: true,
        },

        // Copied from booking.price, never from the request
        amount: {
            type: Number,
            required: true,
            min: 0,
        },

        // Payment is simulated, so a transaction is "completed" on creation
        status: {
            type: String,
            enum: ["pending", "completed", "failed", "refunded"],
            default: "completed",
        },

        // Simulated confirmation number shown to the client, e.g. HH-3F9A1C7B2D40
        reference: {
            type: String,
            required: true,
            unique: true,
            default: () => "HH-" + crypto.randomBytes(6).toString("hex").toUpperCase(),
        },
    },
    { timestamps: true }
); // (The IIE, 2026)

const Transaction = mongoose.model("Transaction", transactionSchema);

module.exports = Transaction;
/*Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
*/ 