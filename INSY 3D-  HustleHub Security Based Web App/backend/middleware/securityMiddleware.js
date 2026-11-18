const cors = require("cors"); // (LearnCodePro, 2025)
const helmet = require("helmet"); // (OWASP, 2026e; Dhananjaya, 2025; Shivanandhan, 2025)
const { rateLimit } = require("express-rate-limit"); // (Express-rate-limit, 2026; Mukherjee, 2026; Programming with Umair, 2023)
const hpp = require("hpp"); // (OWASP, 2026e; Dhananjaya, 2025)
const logger = require("../utils/logger.js"); // (IIE, 2026)

// reads a number from .env, or uses a default if it is missing or invalid.
// This lets the limits change for testing without turning limiters off
// (Express-rate-limit, 2026)
const num = (value, fallback) => {
    const n = parseInt(value, 10);
    return Number.isFinite(n) && n > 0 ? n : fallback;
};

const FRONTEND_ORIGIN = process.env.FRONTEND_ORIGIN || "https://localhost:5173";
const WINDOW_MS = num(process.env.RATE_WINDOW_MINUTES, 15) * 60 * 1000;

// only our frontend may call this API (OWASP, 2026e; LearnCodePro, 2025; IIE, 2026)
const corsOptions = {
    origin: FRONTEND_ORIGIN,
    credentials: true,
    methods: ["GET", "POST", "PUT", "PATCH", "DELETE"],
    allowedHeaders: ["Content-Type", "Authorization"],
    optionsSuccessStatus: 200,
};

// helmet sets the security headers. The API only returns JSON, so the CSP
// blocks everything except connections to itself. The React page needs its
// own CSP because the browser uses the policy of the page that loads it
// (OWASP, 2026e; Dhananjaya, 2025; Shivanandhan, 2025)
const helmetOptions = {
    contentSecurityPolicy: {
        useDefaults: false,
        directives: {
            defaultSrc: ["'none'"],       // block everything by default
            baseUri: ["'none'"],          // stops the <base> tag being abused
            formAction: ["'none'"],       // no form submissions
            frameAncestors: ["'none'"],   // stops clickjacking
            objectSrc: ["'none'"],        // no plugins
            scriptSrc: ["'none'"],        // no scripts
            styleSrc: ["'none'"],         // no styles
            imgSrc: ["'none'"],           // no images
            connectSrc: ["'self'"],
            upgradeInsecureRequests: [],  // use https
        },
    },
    // off because the React app runs on a different origin
    crossOriginEmbedderPolicy: false,
    // stops other windows from accessing ours
    crossOriginOpenerPolicy: { policy: "same-origin" },
    // lets the frontend read API responses (CORS still controls who can call)
    crossOriginResourcePolicy: { policy: "cross-origin" },
    // turns off DNS prefetching
    dnsPrefetchControl: { allow: false },
    // blocks the site being shown in an iframe
    frameguard: { action: "deny" },
    // hides that we use Express
    hidePoweredBy: true,
    // forces https for 180 days
    hsts: {
        maxAge: 15552000,
        includeSubDomains: true,
        preload: true,
    },
    ieNoOpen: true,
    // stops browsers guessing file types
    noSniff: true,
    originAgentCluster: true,
    // limits what is shared when leaving our site
    referrerPolicy: { policy: "strict-origin-when-cross-origin" },
    // the old XSS filter is unsafe, so the CSP is used instead
    xssFilter: false,
};

// one function builds every limiter so they all return the same style of
// 429 message. express-rate-limit also sends a Retry-After header
// (Express-rate-limit, 2026; Mukherjee, 2026; Programming with Umair, 2023)
const makeLimiter = ({ name, limit, message }) =>
    rateLimit({
        windowMs: WINDOW_MS,
        limit,
        standardHeaders: "draft-7",
        legacyHeaders: false,
        handler: (req, res) => {
            // seconds until the limit resets
            const resetTime = req.rateLimit && req.rateLimit.resetTime;
            const retryAfterSeconds = resetTime
                ? Math.max(1, Math.ceil((resetTime.getTime() - Date.now()) / 1000))
                : Math.ceil(WINDOW_MS / 1000);

            // the hit is written to the security log (IIE, 2026)
            logger.warn(`Rate limit (${name}) exceeded by ${req.ip} on ${req.method} ${req.originalUrl}`, "RATE_LIMIT");

            return res.status(429).json({
                message: `${message} Please try again in ${Math.ceil(retryAfterSeconds / 60)} minute(s).`,
                retryAfterSeconds,
            });
        },
    });

// limit for every API route (OWASP, 2026e; Express-rate-limit, 2026)
const generalLimiter = makeLimiter({
    name: "general",
    limit: num(process.env.GENERAL_LIMIT, 150),
    message: "Too many requests from this address.",
});

// strict limit on login and register to slow down brute-force attacks
// (OWASP, 2026a; OWASP, 2021)
const authLimiter = makeLimiter({
    name: "auth",
    limit: num(process.env.AUTH_LIMIT, 10),
    message: "Too many login or registration attempts.",
});

// bookings create transactions, so creating them has its own limit
// (OWASP, 2026e; Express-rate-limit, 2026)
const bookingLimiter = makeLimiter({
    name: "booking",
    limit: num(process.env.BOOKING_LIMIT, 20),
    message: "Too many booking attempts.",
});

// admin routes show platform-wide data, so they are limited too
// (OWASP, 2026e; Express-rate-limit, 2026)
const adminLimiter = makeLimiter({
    name: "admin",
    limit: num(process.env.ADMIN_LIMIT, 60),
    message: "Too many admin requests.",
});

// removes any key that starts with "$" or contains "." to stop NoSQL
// injection. It edits the object in place because Express 5 does not let
// packages like express-mongo-sanitize replace req.query
// (OWASP, 2026f; Shroff, 2025)
const sanitizeInPlace = (obj, req) => {
    if (!obj || typeof obj !== "object") return;
    for (const key of Object.keys(obj)) {
        if (key.startsWith("$") || key.includes(".")) {
            // the attempt is written to the security log (IIE, 2026)
            logger.warn(`NoSQL operator key "${key}" stripped from request by ${req.ip} on ${req.method} ${req.originalUrl}`, "SECURITY");
            delete obj[key];
        } else if (typeof obj[key] === "object") {
            sanitizeInPlace(obj[key], req);
        }
    }
};

const sanitizeRequest = (req, res, next) => {
    sanitizeInPlace(req.body, req);
    sanitizeInPlace(req.params, req);
    sanitizeInPlace(req.query, req);
    next();
};

// the order matters: headers, CORS, limiters, sanitiser, then hpp.
// Limiters come first so abusive requests are rejected early. express.json()
// already runs in app.js, which the sanitiser needs (OWASP, 2026e; IIE, 2026)
const setupSecurity = (app) => {
    app.use(helmet(helmetOptions));
    app.use(cors(corsOptions));

    // limit for everything under /api
    app.use("/api", generalLimiter);

    // stricter limits on sensitive routes
    app.use("/api/auth", authLimiter);
    // only creating a booking is limited, viewing bookings is not
    app.post("/api/bookings", bookingLimiter);
    app.use("/api/admin", adminLimiter);
    app.post("/api/account/password", authLimiter);
    app.post("/api/account/logout-all", authLimiter);

    // stops NoSQL injection (OWASP, 2026f; Shroff, 2025)
    app.use(sanitizeRequest);

    // stops parameter pollution, e.g. ?role=client&role=admin
    // (OWASP, 2026e; Dhananjaya, 2025)
    app.use(hpp());
};

module.exports = setupSecurity;

/*
References:
Dhananjaya, K., 2025. Implementing Security in a Node.js Express Server: An OWASP Guide. [online] Available at: <https://medium.com/@kumar62.shivu/implementing-security-in-a-node-js-express-server-an-owasp-guide-8d4c62733164> [Accessed 6 September 2026].
Express-rate-limit, 2026. express-rate-limit. [online] Available at: <https://www.npmjs.com/package/express-rate-limit> [Accessed 6 September 2026].
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
LearnCodePro, 2025. Express - Rate limiting, CORS, secure headers (helmet). [online] Available at: <https://www.learncodepro.com/tutorials/mern-stack-web-development/exprssjs/express-rate-limiting-cors-secure-headers-helmet> [Accessed 6 September 2026].
Mukherjee, S., 2026. Node.js Security in Production & Deployment Full Course 2026 | JWT, Cookies, CSRF, CORS. [video online] Available at: <https://www.youtube.com/watch?v=-6PPHXdn3Ts> [Accessed 6 September 2026].
OWASP, 2026a. Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2021. OWASP Top 10:2021 - A07 Identification and Authentication Failures. [online] Available at: <https://owasp.org/Top10/2021/A07_2021-Identification_and_Authentication_Failures/> [Accessed 4 September 2026].
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
OWASP, 2026f. NoSQL Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/NoSQL_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
Programming with Umair, 2023. Build REST API with Node Express MongoDB - Security and Protection #39. [video online] Available at: <https://www.youtube.com/watch?v=5NrK1XJAs7w> [Accessed 6 September 2026].
Shivanandhan, M., 2025. How to Harden Your Node.js APIs - Security Best Practices. [online] Available at: <https://www.freecodecamp.org/news/how-to-harden-your-nodejs-apis-security-best-practices/> [Accessed 6 September 2026].
Shroff, S., 2025. Node.js Security Basics: Rate Limiting, Sanitization & Helmet Setup. [online] Available at: <https://prateeksha.com/blog/nodejs-security-basics-rate-limiting-input-sanitization-helmet-setup> [Accessed 6 September 2026].
*/