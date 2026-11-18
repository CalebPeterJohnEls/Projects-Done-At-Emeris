const logger = require("../utils/logger.js");

// logs every request once it has finished. It is added first in app.js so that
// blocked and rate-limited requests are logged too (IIE, 2026)
const httpLogger = (req, res, next) => {
  const startTime = Date.now();

  res.on("finish", () => {
    const duration = Date.now() - startTime;
    const { statusCode } = res;
    const method = req.method;
    const url = req.originalUrl || req.url;

    // req.ip is used because the x-forwarded-for header can be faked by the
    // client (OWASP, 2026e)
    const ip = req.ip || req.socket.remoteAddress || "unknown";

    // only the username and role are logged, never the token or password
    // (OWASP, 2026e)
    const userTag = req.user ? ` [User: ${req.user.username} | ${req.user.role}]` : "";

    const logMessage = `${method} ${url} ${statusCode} (${duration}ms) - IP: ${ip}${userTag}`;

    // the log level depends on the status code (IIE, 2026)
    if (statusCode >= 500) {
      logger.error(logMessage, "HTTP");
    } else if (statusCode >= 400) {
      logger.warn(logMessage, "HTTP");
    } else {
      logger.info(logMessage, "HTTP");
    }
  });

  next();
};

module.exports = httpLogger;

/*
References:
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
*/