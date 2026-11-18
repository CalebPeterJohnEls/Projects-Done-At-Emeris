const logger = require("../utils/logger.js"); // local files + (Better Stack, 2026) & (IIE, 2026)
const User = require("../models/userModel.js");
const Booking = require("../models/bookingModel.js");
const Transaction = require("../models/transactionModel.js");
const Gig = require("../models/gigModel.js");

// R - All users. The password hash is explicitly excluded: the schema does not
// hide it by default, so leaving .select("-password") off would leak every hash.
const getAllUsers = async (req, res) => {
    try {
        // Users have no timestamps, so sort on _id (roughly creation order)
        const users = await User.find().select("-password").sort({ _id: -1 });
        return res.status(200).json(users);
    } catch (error) {
        logger.error(`getAllUsers failed: ${error.message}`, "ADMIN", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// R - Every booking on the platform
const getAllBookings = async (req, res) => {
    try {
        const bookings = await Booking.find()
            .populate("gig", "title category")
            .populate("client", "username")
            .populate("freelancer", "username")
            .sort({ createdAt: -1 });
        return res.status(200).json(bookings);
    } catch (error) {
        logger.error(`getAllBookings failed: ${error.message}`, "ADMIN", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// R - Every transaction on the platform
const getAllTransactions = async (req, res) => {
    try {
        const transactions = await Transaction.find()
            .populate("gig", "title category")
            .populate("client", "username")
            .populate("freelancer", "username")
            .sort({ createdAt: -1 });
        return res.status(200).json(transactions);
    } catch (error) {
        logger.error(`getAllTransactions failed: ${error.message}`, "ADMIN", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// D - Remove any gig (moderation). No ownership check: that is the admin's power.
// Same rule as freelancer deletion: a gig with booking history is deactivated,
// not deleted, so bookings and transactions never point at a missing gig.
const deleteAnyGig = async (req, res) => {
    try {
        const gig = await Gig.findById(req.params.id);
        if (!gig) {
            logger.warn(`Admin ${req.user._id} tried to delete missing gig ${req.params.id}`, "ADMIN");
            return res.status(404).json({ message: "Gig not found" });
        }

        const hasBookings = await Booking.exists({ gig: gig._id });
        if (hasBookings) {
            gig.isActive = false;
            await gig.save();
            logger.warn(`Admin ${req.user._id} deactivated gig ${gig._id} (has bookings) from ${req.ip}`, "ADMIN");
            return res.status(200).json({
                message: "Gig has existing bookings, so it was deactivated instead of deleted",
                deactivated: true,
            });
        }

        await Gig.findByIdAndDelete(req.params.id);
        logger.warn(`Admin ${req.user._id} deleted gig ${req.params.id} from ${req.ip}`, "ADMIN");
        return res.status(200).json({ message: "Gig deleted", deactivated: false });
    } catch (error) {
        logger.error(`deleteAnyGig failed: ${error.message}`, "ADMIN", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

module.exports = { getAllUsers, getAllBookings, getAllTransactions, deleteAnyGig };