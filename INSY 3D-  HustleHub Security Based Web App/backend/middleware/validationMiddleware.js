const { body, param } = require("express-validator");
const Gig = require("../models/gigModel.js");
const { handleValidationErrors } = require("../controllers/authController.js");

// the allowed categories are read from the Gig model so the two never go out of sync (IIE, 2026)
const CATEGORIES = Gig.schema.path("category").enumValues;

// checks that the :id in the URL is a real MongoDB id, so a bad id gives a
// 400 instead of crashing with a 500 (Express-validator, 2026; Validator.js, 2026)
const idParamRule = [
    param("id").isMongoId().withMessage("Invalid ID"),
];

// removes any HTML tags from text. React already escapes output, so tags are
// stripped instead of escaped to avoid showing things like &amp; on screen
// (OWASP, 2026b)
const stripTags = (value) => String(value).replace(/<[^>]*>/g, "");

// builds the rules for the four gig fields. For PATCH every field is
// optional, for POST and PUT every field is required (Express-validator, 2026)
const gigFieldRules = (isOptional) => {
    const start = (chain) => (isOptional ? chain.optional() : chain.exists({ values: "falsy" }).withMessage("This field is required").bail());

    return [
        start(body("title"))
            .isString().withMessage("Title must be text").bail()
            .customSanitizer(stripTags)
            .trim() // removes spaces at the start and end (Validator.js, 2026)
            .isLength({ min: 3, max: 100 }).withMessage("Title must be between 3 and 100 characters"),

        start(body("description"))
            .isString().withMessage("Description must be text").bail()
            .customSanitizer(stripTags)
            .trim()
            .isLength({ min: 10, max: 2000 }).withMessage("Description must be between 10 and 2000 characters"),

        start(body("category"))
            .isString().withMessage("Category must be text").bail()
            .trim()
            .isIn(CATEGORIES).withMessage("Category must be one of the allowed categories"),

        // price must be a number above 0, and it is converted to a real number (Validator.js, 2026)
        start(body("price"))
            .isFloat({ gt: 0, max: 1000000 }).withMessage("Price must be a number above 0 and no more than 1,000,000")
            .toFloat(),
    ];
};

// rules for POST /api/gigs and PUT /api/gigs/:id (every field needed)
const createGigRules = [...gigFieldRules(false), handleValidationErrors];
const replaceGigRules = [...idParamRule, ...gigFieldRules(false), handleValidationErrors];

// rules for PATCH /api/gigs/:id (any field, but whatever is sent must be valid)
const updateGigRules = [...idParamRule, ...gigFieldRules(true), handleValidationErrors];

// rules for routes that only have an :id, such as GET one and DELETE
const gigIdRules = [...idParamRule, handleValidationErrors];

// the gig being booked must be a valid id. The client and the price are
// never taken from the body (OWASP, 2026e)
const createBookingRules = [
    body("gig")
        .isString().withMessage("Gig must be an id").bail()
        .isMongoId().withMessage("Invalid gig ID"),
    handleValidationErrors,
];

const changePasswordRules = [
    body("currentPassword")
        .isString().withMessage("Current password is required").bail()
        .notEmpty().withMessage("Current password is required"),
    body("newPassword")
        .isString().withMessage("New password is required").bail()
        .isLength({ min: 8 }).withMessage("Password must be at least 8 characters long")
        .matches(/\d/).withMessage("Password must contain at least one number")
        .matches(/[A-Z]/).withMessage("Password must contain at least one uppercase letter"),
    // 2FA code or recovery code, only checked by the controller when 2FA is on
    body("code").optional({ values: "falsy" }).isString().withMessage("Code must be text").trim(),
    handleValidationErrors,
];

const logoutAllRules = [
    body("currentPassword")
        .isString().withMessage("Current password is required").bail()
        .notEmpty().withMessage("Current password is required"),
    handleValidationErrors,
];

module.exports = {
    createGigRules,
    replaceGigRules,
    updateGigRules,
    gigIdRules,
    createBookingRules,
    changePasswordRules,
    logoutAllRules
};

/*
References:
Express-validator, 2026. Guides. [online] Available at: <https://express-validator.github.io/docs/category/guides> [Accessed 4 September 2026].
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2026b. Cross Site Scripting Prevention Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
Validator.js, 2026. Validators. [online] Available at: <https://github.com/validatorjs/validator.js#validators> [Accessed 4 September 2026].
*/