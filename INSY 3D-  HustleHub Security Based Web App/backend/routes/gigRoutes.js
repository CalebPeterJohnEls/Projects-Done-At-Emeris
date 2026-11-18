// import express (the API package)
const express = require("express");

// in your routes file, you need to call in the controller it is related to, as well as all of its functions
const {
    createGig,
    getAllGigs,
    getMyGigs,
    getGig,
    updateGig,
    replaceGig,
    deleteGig,
} = require("../controllers/gigController.js");

// call in the methods to protect Gig routes
const { validateAuth, validateRole } = require("../middleware/authMiddleware.js");

// validation rules run before the controller, so bad input never reaches the
// database logic (Express-validator, 2026; OWASP, 2026e; IIE, 2026)
const {
    createGigRules,
    replaceGigRules,
    updateGigRules,
    gigIdRules,
} = require("../middleware/validationMiddleware.js");

const router = express.Router();

// Anyone (including unauthenticated clients) can browse gigs.
router.get("/", getAllGigs);

// "/mine" must come BEFORE "/:id", otherwise Express treats the word "mine" as an ID
router.get("/mine", validateAuth, validateRole("freelancer"), getMyGigs);

router.get("/:id", gigIdRules, getGig);

// Only authenticated freelancers can create/modify/delete - and the
// controller further restricts modify/delete to the *owning* freelancer.
// The order is: log in, check the role, check the input, then the controller.
router.post("/", validateAuth, validateRole("freelancer"), createGigRules, createGig);
router.put("/:id", validateAuth, validateRole("freelancer"), replaceGigRules, replaceGig);
router.patch("/:id", validateAuth, validateRole("freelancer"), updateGigRules, updateGig);
router.delete("/:id", validateAuth, validateRole("freelancer"), gigIdRules, deleteGig);

module.exports = router;

/*
References:
Express-validator, 2026. Guides. [online] Available at: <https://express-validator.github.io/docs/category/guides> [Accessed 4 September 2026].
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
*/