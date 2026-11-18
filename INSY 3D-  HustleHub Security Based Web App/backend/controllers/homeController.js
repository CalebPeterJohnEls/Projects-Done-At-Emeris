const logger = require("../utils/logger.js"); // (Better Stack, 2026) & (IIE, 2026)
// here we create a new request handler for a GET request
const healthCheck = async (req, res) => {
    // when the user accesses this endpoint/path, it returns a response with code
    return res.status(200).json({ message: "App running!" });
};

// here we create a new request handler for a POST request
const greet = async (req, res) => {
    // we assign the data in the request body to a variable called "name"
    const { name } = req.body;

    // and we return a greeting to the user, using the variable, when they
    // navigate to this endpoint.
    return res.status(200).json({ message: `Hello, ${name}` });
};

const message = async (req, res) => {
   //TIP: PUT YOUR WORK IN A TRY CATCH (IIE, 2026)
   try{
    //this will check to see if the inputted data matches the requirements needed
    const { recipient, message } = req.body;

    if (!recipient || !message) {
        return res.status(400).json({ message: "Please enter all required fields" }); //checks input validation
    }

    if (recipient.length > 20) {
        return res.status(400).json({ message: "Please enter shorter name" }); //checks username inputs
    }

    if (message.length < 20 || message.length > 200) {
        return res.status(400).json({ message: "Enter a message longer than 20 characters, but less than 200" });
    }

    return res.status(200).json({ message: `${message} sent to ${recipient}` });
   }
   catch (error){
        logger.error(`message handler failed: ${error.message}`, "HOME", error);
        res.status(500).json({message: "An unexpected error occurred."});
   }
};
//this does a check to see if the system runs 
module.exports = {
    healthCheck,
    greet,
    message,
};
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