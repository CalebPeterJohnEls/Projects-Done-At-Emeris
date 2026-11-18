const logger = require("../utils/logger.js"); // local files + (Better Stack, 2026) & (IIE, 2026)
const Booking = require("../models/bookingModel.js");
const Transaction = require("../models/transactionModel.js");
const Gig = require("../models/gigModel.js");

// C - Create a booking (client only - enforced by validateRole in the route)
const createBooking = async (req, res) => {
    let booking = null;
    let transaction = null;

    try {
        const { gig: gigId } = req.body;

        logger.info(`Booking attempt by user ${req.user._id} for gig ${gigId} from ${req.ip}`, "BOOKING");

        if (!gigId) {
            logger.warn(`Booking rejected (missing gig id) for user ${req.user._id} from ${req.ip}`, "BOOKING");
            return res.status(400).json({ message: "Please ensure all required fields are present" });
        }

        const gig = await Gig.findById(gigId);

        // Missing and inactive gigs get the same response, so we don't leak
        // which gigs exist but are paused.
        if (!gig || !gig.isActive) {
            logger.warn(`Booking rejected (gig not found or inactive: ${gigId}) for user ${req.user._id} from ${req.ip}`, "BOOKING");
            return res.status(404).json({ message: "Gig not found" });
        }

        // The client comes from req.user and the price comes from the gig.
        // Anything the request body says about either is ignored.
        booking = await Booking.create({
            client: req.user._id,
            freelancer: gig.freelancer,
            gig: gig._id,
            price: gig.price,
        });

        logger.info(`Booking ${booking._id} created (pending) by client ${req.user._id} for gig ${gig._id}`, "BOOKING");

        transaction = await Transaction.create({
            booking: booking._id,
            client: req.user._id,
            freelancer: gig.freelancer,
            gig: gig._id,
            amount: booking.price,
        });

        // Simulated payment succeeded, so the booking is now confirmed
        logger.info(`Transaction ${transaction._id} recorded: amount ${transaction.amount}, booking ${booking._id}, client ${req.user._id}, freelancer ${gig.freelancer}`, "TRANSACTION");

        booking.status = "confirmed";
        await booking.save();
        logger.info(`Booking ${booking._id} confirmed (price ${booking.price})`, "BOOKING");

        return res.status(201).json({ booking, transaction });
    } catch (error) {
        logger.error(`createBooking failed for user ${req.user && req.user._id}: ${error.message}`, "BOOKING", error);

        // Manual rollback: if any step failed, remove whatever was created so
        // we never leave a booking without a transaction (or vice versa).
        try {
            if (transaction) await Transaction.findByIdAndDelete(transaction._id);
            if (booking) await Booking.findByIdAndDelete(booking._id);
            if (booking || transaction) {
                logger.warn(`Rolled back booking ${booking && booking._id} and transaction ${transaction && transaction._id}`, "BOOKING");
            }
        } catch (cleanupError) {
            logger.error(`createBooking rollback failed: ${cleanupError.message}`, "BOOKING", cleanupError);
        }

        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// R - Client: their own bookings only (filter comes from req.user, not the URL)
const getMyBookings = async (req, res) => {
    try {
        const bookings = await Booking.find({ client: req.user._id })
            .populate("gig", "title category")
            .populate("freelancer", "username")
            .sort({ createdAt: -1 });

        return res.status(200).json(bookings);
    } catch (error) {
        logger.error(`getMyBookings failed: ${error.message}`, "BOOKING", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// R - Freelancer: bookings made on their own gigs only
const getReceivedBookings = async (req, res) => {
    try {
        const bookings = await Booking.find({ freelancer: req.user._id })
            .populate("gig", "title category")
            .populate("client", "username")
            .sort({ createdAt: -1 });

        return res.status(200).json(bookings);
    } catch (error) {
        logger.error(`getReceivedBookings failed: ${error.message}`, "BOOKING", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

module.exports = { createBooking, getMyBookings, getReceivedBookings };