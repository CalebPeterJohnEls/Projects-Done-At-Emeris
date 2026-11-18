// here we import express, which allows us to create an instance of the API and web server
require("dotenv").config();

const express = require("express");
const https = require("https");
const fs = require("fs");
const path = require("path");

// here, we call in the database middleware, which allows us to access the methods we've created to interact with the database
const database = require("./middleware/dbMiddleware.js");
const setupSecurity = require("./middleware/securityMiddleware.js");
const httpLogger = require("./middleware/loggerMiddleware.js"); // logs every request (IIE, 2026)
const logger = require("./utils/logger.js"); // writes to the log files (IIE, 2026)
const accountRoutes = require("./routes/accountRoutes.js");

// here, we call in all of our routing files, allowing us to map them later on in the file
const homeRoutes = require("./routes/homeRoutes.js");
const gigRoutes = require("./routes/gigRoutes.js");
const authRoutes = require("./routes/authRoutes.js");
const bookingRoutes = require("./routes/bookingRoutes.js");

const transactionRoutes = require("./routes/transactionRoutes.js");
const incomeRoutes = require("./routes/incomeRoutes.js");
const adminRoutes = require("./routes/adminRoutes.js");

// Fail fast if critical secrets are missing, rather than starting with an
// undefined JWT_SECRET or no database connection string (OWASP, 2026e).
const requiredEnvVars = ["JWT_SECRET", "CONN_STRING"];
requiredEnvVars.forEach((key) => {
    if (!process.env[key]) {
        console.error(`Missing required environment variable: ${key}`);
        process.exit(1);
    }
});

// the first step in creating the API, is to create a singleton (single instance) of the express library to call in throughout the whole app
const app = express();

const sslOptions = {
    key: fs.readFileSync(path.join(__dirname, "..", "cert.key")),    // key => identifies WHO owns the certificate
    cert: fs.readFileSync(path.join(__dirname, "..", "cert.crt")),   // cert => used to encrypt traffic
    ca: fs.readFileSync(path.join(__dirname, "..", "ca.crt")),        // ca => who to ask if the cert is real
};

// the logger goes first so that blocked, rate-limited and badly formed
// requests are logged too (IIE, 2026)
app.use(httpLogger);

// then, you need to tell the app to make use of any required middleware you need to complete and understand the requests
// express.json allows us to use json in requests and responses. The size
// limit reduces the risk of large-payload denial-of-service attempts (OWASP, 2026e).
app.use(express.json({ limit: "10kb" }));

// CORS, helmet, rate limiting, sanitising and hpp are all set up inside
// setupSecurity/securityMiddleware.js (OWASP, 2026e; IIE, 2026)
setupSecurity(app);

// then, you need to map the routes to specific endpoints, so that the user/client is able to access them
app.use("/api/home", homeRoutes);
app.use("/api/gigs", gigRoutes);
app.use("/api/auth", authRoutes);
app.use("/api/bookings", bookingRoutes);
app.use("/api/transactions", transactionRoutes);
app.use("/api/income", incomeRoutes);
app.use("/api/admin", adminRoutes);
app.use("/api/account", accountRoutes);

// Unknown routes get a controlled 404 rather than Express's default page.
app.use((req, res) => {
    res.status(404).json({ message: "Resource not found" });
});

// Centralised error handler - the full error goes to the log file only, and
// the client only gets a generic message, so no stack trace, file path or
// config value is ever returned (OWASP, 2026e; IIE, 2026).
app.use((err, req, res, next) => {
    logger.error(`Unhandled error on ${req.method} ${req.originalUrl}: ${err.message}`, "APP", err);

    // bad JSON or a body that is too large is the client's mistake, so it keeps its 4xx status
    const status = err.status >= 400 && err.status < 500 ? err.status : 500;
    const message = status === 500 ? "An unexpected error occurred." : "The request could not be processed.";
    res.status(status).json({ message });
});

// lastly, we tell the application to start listening. we need to specify a port for the app to listen on, in this case, 3000
// we do this using a then function. it does something first (in this case, connects to the database), THEN starts the listening for connections
database.start().then(() => {
    const port = process.env.PORT || 3000;
    https.createServer(sslOptions, app).listen(port, () => {
        // once the app starts, we print out to the developer terminal
        logger.info(`API started on port ${port}`, "APP");
    });
}).catch((error) => {
    // if the database cannot connect, the server stops instead of running half-broken (OWASP, 2026e)
    logger.error(`Startup failed: ${error.message}`, "APP", error);
    process.exit(1);
});

/*
References:
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
*/