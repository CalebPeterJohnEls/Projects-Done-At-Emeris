const express = require("express");
const router = express.Router();

const { validateAuth, validateRole } = require("../middleware/authMiddleware.js");
const {
    getAllUsers,
    getAllBookings,
    getAllTransactions,
    deleteAnyGig,
} = require("../controllers/adminController.js");

// checks the gig id before the controller runs, so a bad id gives a 400
// instead of a 500 (Express-validator, 2026; OWASP, 2026e)
const { gigIdRules } = require("../middleware/validationMiddleware.js");

// Every route below is admin-only, so apply both checks once for the whole router (OWASP, 2021; IIE, 2026).
// The admin rate limiter is applied to /api/admin in securityMiddleware.js.
router.use(validateAuth, validateRole("admin"));

router.get("/users", getAllUsers);
router.get("/bookings", getAllBookings);
router.get("/transactions", getAllTransactions);
router.delete("/gigs/:id", gigIdRules, deleteAnyGig);

module.exports = router;

/*
References:
Express-validator, 2026. Guides. [online] Available at: <https://express-validator.github.io/docs/category/guides> [Accessed 4 September 2026].
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2021. OWASP Top 10:2021 - A07 Identification and Authentication Failures. [online] Available at: <https://owasp.org/Top10/2021/A07_2021-Identification_and_Authentication_Failures/> [Accessed 4 September 2026].
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
*/