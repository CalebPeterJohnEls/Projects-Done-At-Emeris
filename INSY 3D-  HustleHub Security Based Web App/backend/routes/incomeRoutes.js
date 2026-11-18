const express = require("express");
const router = express.Router();


const { validateAuth, validateRole } = require("../middleware/authMiddleware.js");
const { getIncome } = require("../controllers/transactionController.js");

router.get("/", validateAuth, validateRole("freelancer"), getIncome); // (The IIE, 2026)
module.exports = router;

/*Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
*/ 