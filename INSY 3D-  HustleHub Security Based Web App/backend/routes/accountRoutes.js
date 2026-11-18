const express = require("express");
const router = express.Router();

const { validateAuth } = require("../middleware/authMiddleware.js");
const { changePasswordRules, logoutAllRules } = require("../middleware/validationMiddleware.js");
const {
  changePassword,
  logoutAll,
  getActivity,
  getSessions,
  logoutCurrent,
} = require("../controllers/accountController.js");


// Every route here is for the logged-in user's own account. Caleb: put the auth
// limiter in front of the two POST routes in app.js, because they check a password
// and can be guessed at like a login form.
router.post("/password", validateAuth,  changePassword);
router.post("/logout-all", validateAuth, logoutAll);
router.get("/activity", validateAuth, getActivity);
router.post(
  "/password",
  validateAuth,
  changePasswordRules,
  changePassword
);

router.post(
  "/logout-all",
  validateAuth,
  logoutAll
);

router.get("/activity", validateAuth, getActivity);
router.get("/sessions", validateAuth, getSessions);
router.post("/logout", validateAuth, logoutCurrent);

module.exports = router;