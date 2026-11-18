const express = require("express");
const router = express.Router();

const { validateAuth, validateRole } = require("../middleware/authMiddleware.js");
const { getMyTransactions } = require("../controllers/transactionController.js");

// Both clients and freelancers may view their own transactions. If your
// validateRole takes one role only, use two calls or an array, whichever
// your middleware supports. (The IIE, 2026)
router.get("/mine", validateAuth, validateRole("client", "freelancer"), getMyTransactions);

module.exports = router;
/*Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
*/ 