// Small pure helpers for session handling. No database or Express in here, so
// they are easy to unit test.

// A token is only valid while its version (tv) matches the user's current
// tokenVersion. Bumping the user's version kills every older token at once.
// Tokens issued before this feature have no tv, which counts as 0 - the same
// as the default on existing users - so nobody is logged out when it ships.
const isTokenCurrent = (decoded, user) =>
    (Number(decoded && decoded.tv) || 0) === (Number(user && user.tokenVersion) || 0);

// Turns a raw User-Agent string into something a person can read, e.g.
// "Chrome on Windows". Best effort only - it is a hint, not an identity.
const describeDevice = (userAgent) => {
    const ua = String(userAgent || "");
    if (!ua) return "Unknown device";

    let browser = "Unknown browser";
    if (/PostmanRuntime|newman|curl\//i.test(ua)) browser = "API client";
    else if (/Edg\//.test(ua)) browser = "Edge"; // Edge also says Chrome and Safari, so test it first
    else if (/OPR\/|Opera/.test(ua)) browser = "Opera";
    else if (/Firefox\//.test(ua)) browser = "Firefox";
    else if (/Chrome\/|CriOS\//.test(ua)) browser = "Chrome";
    else if (/Safari\//.test(ua)) browser = "Safari";

    // order matters: Android also says Linux, and iPhones say "like Mac OS X"
    let os = "";
    if (/Android/.test(ua)) os = "Android";
    else if (/iPhone|iPad|iPod/.test(ua)) os = "iOS";
    else if (/Windows/.test(ua)) os = "Windows";
    else if (/Mac OS X|Macintosh/.test(ua)) os = "macOS";
    else if (/CrOS/.test(ua)) os = "ChromeOS";
    else if (/Linux/.test(ua)) os = "Linux";

    return os ? `${browser} on ${os}` : browser;
};

module.exports = { isTokenCurrent, describeDevice };