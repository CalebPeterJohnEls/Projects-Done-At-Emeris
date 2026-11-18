const mongoose = require("mongoose");

const bookingSchema = new mongoose.Schema(
    {
        // Always set from req.user on the server, never from the request body
        client: {
            type: mongoose.Schema.Types.ObjectId,
            ref: "User",
            required: true,
            index: true,
        },

        // Copied from gig.freelancer (the gig owner) when the booking is made
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
            index: true,
        },

        // Price snapshot taken from the gig on the server. Later edits to the
        // gig's price must not change what this client agreed to pay.
        price: {
            type: Number,
            required: true,
            min: 0,
        },

        status: {
            type: String,
            enum: ["pending", "confirmed", "completed", "cancelled"],
            default: "pending",
        },
    },
    { timestamps: true }
); // (The IIE, 2026)

const Booking = mongoose.model("Booking", bookingSchema);

module.exports = Booking;

/*Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
*/ 