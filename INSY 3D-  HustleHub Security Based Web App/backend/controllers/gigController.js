const logger = require("../utils/logger.js"); // local files + (Better Stack, 2026) & (IIE, 2026)
const Gig = require("../models/gigModel.js");
const Booking = require("../models/bookingModel.js");

// The only fields a freelancer is allowed to change on a gig. Anything else in
// the body (freelancer, isActive, _id, createdAt...) is ignored, which stops
// mass assignment such as handing a gig to another user.
const UPDATABLE_FIELDS = ["title", "description", "category", "price"];

// C - Create (IIE, 2026)
const createGig = async (req, res) => {
    try {
        const { title, description, category, price } = req.body;

        if (!title || !description || !category || price === undefined) {
            return res.status(400).json({ message: "Please ensure all required fields are present" });
        }

        // req.user comes from validateAuth - the gig is always owned by
        // whoever is authenticated, never by an ID the client supplies.
        const gig = await Gig.create({ 
            title,
            description,
            category,
            price,
            freelancer: req.user._id,
        });

        logger.info(`Gig ${gig._id} created by freelancer ${req.user._id} from ${req.ip}`, "GIG");
        return res.status(201).json(gig);
    } catch (error) {
        logger.error(`createGig failed: ${error.message}`, "GIG", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// R - Read all (public browsing - clients need this too, no auth required) (IIE, 2026)
const getAllGigs = async (req, res) => {
    try {
        const gigs = await Gig.find({ isActive: true }).populate("freelancer", "username");
        return res.status(200).json(gigs);
    } catch (error) {
        logger.error(`getAllGigs failed: ${error.message}`, "GIG", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// R - Read the logged-in freelancer's own gigs, including paused ones.
// Must be registered BEFORE "/:id" in the routes file, otherwise Express
// treats the word "mine" as an ID.
const getMyGigs = async (req, res) => {
    try {
        const gigs = await Gig.find({ freelancer: req.user._id }).sort({ createdAt: -1 });
        return res.status(200).json(gigs);
    } catch (error) {
        logger.error(`getMyGigs failed: ${error.message}`, "GIG", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// R - Read one (IIE, 2026)
const getGig = async (req, res) => {
    try {
        const gig = await Gig.findById(req.params.id).populate("freelancer", "username");

        // Inactive gigs are treated as not found, matching getAllGigs, which
        // hides them. Owners still see their paused gigs through getMyGigs.
        if (!gig || !gig.isActive) {
            return res.status(404).json({ message: "Gig not found" });
        }
        return res.status(200).json(gig);
    } catch (error) {
        logger.error(`getGig failed: ${error.message}`, "GIG", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// U - Partial update (PATCH) (IIE, 2026)
const updateGig = async (req, res) => {
    try {
        const gig = await Gig.findById(req.params.id);
        if (!gig) {
            return res.status(404).json({ message: "Gig not found" });
        }

        // Ownership check: being *a* freelancer isn't enough - it has to
        // be *your* gig. Without this, any freelancer could edit anyone's gig.
        if (gig.freelancer.toString() !== req.user._id.toString()) {
            logger.warn(`Forbidden gig modify: user ${req.user._id} on gig ${gig._id} from ${req.ip}`, "SECURITY");
            return res.status(403).json({ message: "You do not have permission to modify this gig" });
        }

        // Whitelist: copy across only the allowed fields that were actually sent.
        const updates = {};
        for (const field of UPDATABLE_FIELDS) {
            if (req.body[field] !== undefined) {
                updates[field] = req.body[field];
            }
        }

        if (Object.keys(updates).length === 0) {
            return res.status(400).json({ message: "No valid fields to update" });
        }

        const updated = await Gig.findByIdAndUpdate(req.params.id, updates, {
            new: true,
            runValidators: true,
        });

        logger.info(`Gig ${gig._id} updated by ${req.user._id}`, "GIG");
        return res.status(200).json(updated);
    } catch (error) {
        logger.error(`updateGig failed: ${error.message}`, "GIG", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// U - Full replace (PUT) (IIE, 2026)
const replaceGig = async (req, res) => {
    try {
        const gig = await Gig.findById(req.params.id);
        if (!gig) {
            return res.status(404).json({ message: "Gig not found" });
        }

        if (gig.freelancer.toString() !== req.user._id.toString()) {
            logger.warn(`Forbidden gig modify: user ${req.user._id} on gig ${gig._id} from ${req.ip}`, "SECURITY");
            return res.status(403).json({ message: "You do not have permission to modify this gig" });
        }

        const { title, description, category, price } = req.body;
        if (!title || !description || !category || price === undefined) {
            return res.status(400).json({ message: "A full replacement requires all fields" });
        }

        // Unlike updateGig (PATCH), this overwrites title/description/ (IIE, 2026)
        // category/price wholesale - anything not sent is not preserved.
        // Only these four fields are written, so freelancer/isActive are safe.
        const replaced = await Gig.findByIdAndUpdate(
            req.params.id,
            { title, description, category, price },
            { new: true, runValidators: true }
        );

        logger.info(`Gig ${gig._id} replaced by ${req.user._id}`, "GIG");
        return res.status(200).json(replaced);
    } catch (error) {
        logger.error(`replaceGig failed: ${error.message}`, "GIG", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

// D - Delete (IIE, 2026)
const deleteGig = async (req, res) => {
    try {
        const gig = await Gig.findById(req.params.id);
        if (!gig) {
            return res.status(404).json({ message: "Gig not found" });
        }

        if (gig.freelancer.toString() !== req.user._id.toString()) {
            logger.warn(`Forbidden gig delete: user ${req.user._id} on gig ${gig._id} from ${req.ip}`, "SECURITY");
            return res.status(403).json({ message: "You do not have permission to delete this gig" });
        }

        // A gig with booking history must survive, because bookings and
        // transactions reference it. Deactivate it instead of deleting it.
        const hasBookings = await Booking.exists({ gig: gig._id });
        if (hasBookings) {
            gig.isActive = false;
            await gig.save();
            return res.status(200).json({
                message: "Gig has existing bookings, so it was deactivated instead of deleted",
                deactivated: true,
            });
        }

        await Gig.findByIdAndDelete(req.params.id);
        return res.status(200).json({ message: "Gig deleted", deactivated: false });
    } catch (error) {
        logger.error(`deleteGig failed: ${error.message}`, "GIG", error);
        return res.status(500).json({ message: "An unexpected error occurred." });
    }
};

module.exports = { createGig, getAllGigs, getMyGigs, getGig, updateGig, replaceGig, deleteGig };
/*
References:
Better Stack, 2026. Ingesting logs. [online] Available at: <https://betterstack.com/docs/logs/ingesting-data/http/logs/> [Accessed 9 November 2026].
Express-validator, 2026. Guides. [online] Available at: <https://express-validator.github.io/docs/category/guides> [Accessed 4 September 2026].
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
Mozilla, 2026. HTTP response status codes. [online] Available at: <https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status> [Accessed 4 September 2026].
Mozilla, 2026. AbortSignal. [online] Available at: <https://developer.mozilla.org/en-US/docs/Web/API/AbortSignal> [Accessed 9 November 2026].
OWASP, 2026a. Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026b. Cross Site Scripting Prevention Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026c. JSON Web Token Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2021. OWASP Top 10:2021 - A07 Identification and Authentication Failures. [online] Available at: <https://owasp.org/Top10/2021/A07_2021-Identification_and_Authentication_Failures/> [Accessed 4 September 2026].
OWASP, 2026d. Password Storage Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
Stack Overflow, 2010. Regular expression for password validation. [online] Available at: <https://stackoverflow.com/questions/2370015/regular-expression-for-password-validation> [Accessed 4 September 2026].
Stack Overflow, 2018. "Reference" - Password validation. [online] Available at: <https://stackoverflow.com/questions/48345922/reference-password-validation> [Accessed 4 September 2026].
Validator.js, 2026. Validators. [online] Available at: <https://github.com/validatorjs/validator.js#validators> [Accessed 4 September 2026].
*/