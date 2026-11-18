const logger = require("../utils/logger.js"); // local files + (Better Stack, 2026) & (IIE, 2026)
const Transaction = require("../models/transactionModel.js");
const Booking = require("../models/bookingModel.js");
const { calculateTax, taxYearBounds, taxYearMonths, TAX_YEAR } = require("../utils/tax.js");

// R - Transactions for the logged-in user. Clients see what they paid,
// freelancers see what they were paid. The filter comes from req.user, never
// from the URL or body, so nobody can ask for someone else's transactions.
const getMyTransactions = async (req, res) => {
    try {
        const filter = req.user.role === "freelancer"
            ? { freelancer: req.user._id }
            : { client: req.user._id };

        const transactions = await Transaction.find(filter)
            .populate("gig", "title category")
            .populate("client", "username")
            .populate("freelancer", "username")
            .sort({ createdAt: -1 });

        logger.info(`Transactions viewed by ${req.user.role} ${req.user._id} (${transactions.length} returned) from ${req.ip}`, "TRANSACTION");
        return res.status(200).json(transactions);
    } catch (error) {
        logger.error(`getMyTransactions failed: ${error.message}`, "TRANSACTION", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// R - Freelancer dashboard income for the CURRENT TAX YEAR: every confirmed
// booking on this freelancer's gigs, combined, plus a SARS tax estimate and a
// month-by-month breakdown. Each booking stores the price agreed at booking
// time, so later gig price edits never change past income.
const getIncome = async (req, res) => {
    try {
        const { start, end } = taxYearBounds();

        // Aggregation pipelines do not auto-cast IDs the way find() does, so
        // req.user._id must already be an ObjectId (it is, being a Mongoose doc id).
        const match = {
            freelancer: req.user._id,
            status: "confirmed",
            createdAt: { $gte: start, $lt: end },
        };

        const [totals] = await Booking.aggregate([
            { $match: match },
            { $group: { _id: null, total: { $sum: "$price" }, count: { $sum: 1 } } },
        ]);

        const monthRows = await Booking.aggregate([
            { $match: match },
            {
                $group: {
                    // Group by South African calendar month, not UTC
                    _id: { $dateToString: { format: "%Y-%m", date: "$createdAt", timezone: "Africa/Johannesburg" } },
                    income: { $sum: "$price" },
                    bookings: { $sum: 1 },
                },
            },
        ]);

        const byGig = await Booking.aggregate([
            { $match: match },
            { $group: { _id: "$gig", earned: { $sum: "$price" }, bookings: { $sum: 1 } } },
            { $lookup: { from: "gigs", localField: "_id", foreignField: "_id", as: "gigDoc" } },
            {
                $project: {
                    _id: 0,
                    gig: "$_id",
                    title: { $arrayElemAt: ["$gigDoc.title", 0] },
                    earned: 1,
                    bookings: 1,
                },
            },
            { $sort: { earned: -1 } },
        ]);

        // All 12 months always appear, with zeros where nothing was earned
        const byMonth = new Map(monthRows.map((r) => [r._id, r]));
        const monthly = taxYearMonths().map((month) => ({
            month,
            income: byMonth.has(month) ? byMonth.get(month).income : 0,
            bookings: byMonth.has(month) ? byMonth.get(month).bookings : 0,
        }));

        const total = totals ? totals.total : 0;
        const tax = calculateTax(total);

        // amounts are logged for the audit trail; ids only, no names or emails
        logger.info(`Income and tax estimate for freelancer ${req.user._id}, tax year ${TAX_YEAR.label}: gross ${total}, estimated tax ${tax.estimatedTax}, net ${tax.netIncome}, ${totals ? totals.count : 0} confirmed bookings, from ${req.ip}`, "TRANSACTION");

        return res.status(200).json({
            taxYear: TAX_YEAR.label,
            total, // gross income this tax year (kept name: the dashboard already reads it)
            count: totals ? totals.count : 0,
            taxBeforeRebate: tax.taxBeforeRebate,
            rebate: tax.rebate,
            estimatedTax: tax.estimatedTax,
            netIncome: tax.netIncome,
            effectiveRate: tax.effectiveRate,
            marginalRate: tax.marginalRate,
            belowThreshold: tax.belowThreshold,
            brackets: tax.brackets,
            monthly,
            byGig,
        });
    } catch (error) {
        logger.error(`getIncome failed: ${error.message}`, "TRANSACTION", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

module.exports = { getMyTransactions, getIncome };