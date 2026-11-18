const express = require("express");
const {
    registerUser,
    loginUser,
    registerValidationRules,
    loginValidationRules,
    handleValidationErrors,
} = require("../controllers/authController.js");
const { validateAuth } = require("../middleware/authMiddleware.js");
const {
    setupTwoFactor,
    enableTwoFactor,
    loginTwoFactor,
    disableTwoFactor,
    getTwoFactorStatus,
} = require("../controllers/twoFactorController.js");
const router = express.Router();

// validation rules and the error handler run as middleware before the
// controller ever runs, so bad input never reaches the database logic
// (Express-validator, 2026; OWASP, 2026e; IIE, 2026)
router.post("/register", registerValidationRules, handleValidationErrors, registerUser);
router.post("/login", loginValidationRules, handleValidationErrors, loginUser);

router.post("/2fa/setup", validateAuth, setupTwoFactor);
router.post("/2fa/enable", validateAuth, enableTwoFactor);
router.post("/2fa/login", loginTwoFactor);
router.post("/2fa/disable", validateAuth, disableTwoFactor);
router.get("/2fa/status", validateAuth, getTwoFactorStatus);

module.exports = router;

/*
References:
Express-validator, 2026. Guides. [online] Available at: <https://express-validator.github.io/docs/category/guides> [Accessed 4 September 2026].
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
*/