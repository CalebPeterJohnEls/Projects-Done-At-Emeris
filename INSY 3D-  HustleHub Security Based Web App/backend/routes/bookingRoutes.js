const express = require("express");
const router = express.Router();

const { validateAuth, validateRole } = require("../middleware/authMiddleware.js");
const {
    createBooking,
    getMyBookings,
    getReceivedBookings,
} = require("../controllers/bookingController.js");

// checks the gig id before the controller runs, so a bad id gives a 400
// instead of a 500 (Express-validator, 2026; OWASP, 2026e)
const { createBookingRules } = require("../middleware/validationMiddleware.js");

// The booking rate limiter is applied to POST /api/bookings in
// securityMiddleware.js, so it runs before any of these (OWASP, 2026e; IIE, 2026).
// Only clients can book, and only freelancers can see bookings made on their gigs.
router.post("/", validateAuth, validateRole("client"), createBookingRules, createBooking);
router.get("/mine", validateAuth, validateRole("client"), getMyBookings);
router.get("/received", validateAuth, validateRole("freelancer"), getReceivedBookings);

module.exports = router;

/*
References:
Express-validator, 2026. Guides. [online] Available at: <https://express-validator.github.io/docs/category/guides> [Accessed 4 September 2026].
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
*/