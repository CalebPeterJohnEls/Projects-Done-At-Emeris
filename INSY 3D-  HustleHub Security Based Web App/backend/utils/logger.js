// fs lets us write to files, path helps us build file locations (IIE, 2026)
const fs = require("fs");
const path = require("path");

// logs are saved in a "logs" folder, which is created if it is missing (IIE, 2026)
const LOGS_DIR = path.join(__dirname, "..", "logs");
if (!fs.existsSync(LOGS_DIR)) {
  fs.mkdirSync(LOGS_DIR, { recursive: true });
}

// colours for the terminal output (IIE, 2026)
const colours = {
  reset: "\x1b[0m",
  green: "\x1b[32m",
  yellow: "\x1b[33m",
  red: "\x1b[31m",
  cyan: "\x1b[36m",
  gray: "\x1b[90m",
};

// date used for the log file names, e.g. 2026-10-09 (IIE, 2026)
const getDateString = () => {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0"); // months start at 0
  const day = String(now.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
};

// date and time written inside each log line (IIE, 2026)
const getTimestamp = () => {
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, "0");
  const mins = String(now.getMinutes()).padStart(2, "0");
  const seconds = String(now.getSeconds()).padStart(2, "0");
  return `${getDateString()} ${hours}:${mins}:${seconds}`;
};


// ---------------------------------------------------------------------------
// Cloud transport (Better Stack / Logtail)
// ---------------------------------------------------------------------------
// Logs are queued in memory and sent to Better Stack in batches over HTTPS.
// Configure with environment variables (the token is never hard coded):
//   BETTERSTACK_SOURCE_TOKEN   source token from Better Stack (required to enable)
//   BETTERSTACK_INGEST_HOST    ingesting host, e.g. s123456.eu-nbg-2.betterstackdata.com
//                              (a full https:// URL is also accepted)
// Optional tuning: BETTERSTACK_BATCH_SIZE, BETTERSTACK_FLUSH_MS,
//   BETTERSTACK_MAX_QUEUE, BETTERSTACK_TIMEOUT_MS
// If the token or host is missing the transport is switched off and the app
// carries on logging to the terminal and files only.
// Nothing in this section is allowed to throw, so logging can never crash the app.
const toInt = (value, fallback) => {
  const n = parseInt(value, 10);
  return Number.isFinite(n) && n > 0 ? n : fallback;
};

const cloud = {
  token: (process.env.BETTERSTACK_SOURCE_TOKEN || "").trim(),
  host: (process.env.BETTERSTACK_INGEST_HOST || "").trim(),
  batchSize: toInt(process.env.BETTERSTACK_BATCH_SIZE, 50),
  flushMs: toInt(process.env.BETTERSTACK_FLUSH_MS, 5000),
  maxQueue: toInt(process.env.BETTERSTACK_MAX_QUEUE, 1000), // oldest logs are dropped past this
  timeoutMs: toInt(process.env.BETTERSTACK_TIMEOUT_MS, 5000),
  queue: [],
  sending: false,
  timer: null,
  failures: 0, // consecutive failed sends, used for back-off
  pausedUntil: 0, // no sends before this time after a failure
  dropped: 0,
  lastWarn: 0,
};
cloud.enabled = Boolean(cloud.token && cloud.host && typeof fetch === "function");
cloud.url = cloud.host.startsWith("http") ? cloud.host : `https://${cloud.host}`;

// reports a transport problem on the console only (never through log(), which
// would loop), at most once a minute so a long outage does not flood the output
const cloudWarn = (text) => {
  try {
    const now = Date.now();
    if (now - cloud.lastWarn < 60000) return;
    cloud.lastWarn = now;
    console.error(`[LOGGER] Cloud logging: ${text}`);
  } catch (e) {
    /* nothing more we can do */
  }
};

// sends up to one batch; always resolves, never rejects
const flushCloud = async () => {
  try {
    if (!cloud.enabled || cloud.sending || cloud.queue.length === 0) return;
    if (Date.now() < cloud.pausedUntil) return; // backing off after a failure

    cloud.sending = true;
    const batch = cloud.queue.splice(0, cloud.batchSize);

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), cloud.timeoutMs);
    try {
      const response = await fetch(cloud.url, {
        method: "POST",
        headers: {
          Authorization: `Bearer ${cloud.token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(batch),
        signal: controller.signal,
      });
      if (!response.ok) {
        throw new Error(`Better Stack responded with HTTP ${response.status}`);
      }
      cloud.failures = 0;
      cloud.pausedUntil = 0;
    } catch (err) {
      // put the batch back at the front so it is retried, then trim the queue
      // so memory stays bounded during a long outage
      cloud.queue.unshift(...batch);
      if (cloud.queue.length > cloud.maxQueue) {
        const extra = cloud.queue.length - cloud.maxQueue;
        cloud.queue.splice(0, extra);
        cloud.dropped += extra;
      }
      cloud.failures += 1;
      // exponential back-off: 2s, 4s, 8s ... capped at 60s
      cloud.pausedUntil = Date.now() + Math.min(60000, 1000 * 2 ** cloud.failures);
      cloudWarn(`send failed (${err.name === "AbortError" ? "timeout" : err.message}); ${cloud.queue.length} queued, ${cloud.dropped} dropped`);
    } finally {
      clearTimeout(timeout);
      cloud.sending = false;
    }

    // more waiting than one batch? send the next one straight away
    if (cloud.failures === 0 && cloud.queue.length >= cloud.batchSize) {
      setImmediate(flushCloud);
    }
  } catch (e) {
    cloud.sending = false; // belt and braces: any unexpected error is swallowed
  }
};

// adds one entry to the queue; flushes now if a full batch is waiting
const sendToCloud = (entry) => {
  try {
    if (!cloud.enabled) return;
    cloud.queue.push(entry);
    if (cloud.queue.length > cloud.maxQueue) {
      cloud.queue.shift();
      cloud.dropped += 1;
    }
    if (!cloud.timer) {
      cloud.timer = setInterval(flushCloud, cloud.flushMs);
      cloud.timer.unref(); // the timer alone must not keep the process alive
    }
    if (cloud.queue.length >= cloud.batchSize) flushCloud();
  } catch (e) {
    /* never let logging break the caller */
  }
};

// last attempt to send what is left when the process is stopping (e.g. docker stop)
const flushOnExit = async () => {
  try {
    let failedAttempts = 0;
    // each pass sends one batch; stop after 2 failed sends or 50 passes so a
    // dead server can never keep the process waiting forever
    for (let pass = 0; cloud.enabled && cloud.queue.length > 0 && failedAttempts < 2 && pass < 50; pass++) {
      cloud.pausedUntil = 0; // ignore the back-off, this is the last chance
      const failuresBefore = cloud.failures;
      await flushCloud();
      if (cloud.failures > failuresBefore) failedAttempts += 1;
    }
  } catch (e) {
    /* ignore */
  }
};
if (cloud.enabled) {
  // handlers only flush; they do not call process.exit, so the app's own
  // shutdown behaviour is unchanged apart from a short wait
  ["SIGTERM", "SIGINT"].forEach((signal) =>
    process.once(signal, () => {
      flushOnExit().finally(() => process.kill(process.pid, signal));
    }),
  );
  // "once": beforeExit fires again whenever async work is scheduled, so a
  // repeating handler would never let the process finish while Better Stack is down
  process.once("beforeExit", () => {
    flushOnExit();
  });
}

// removes line breaks so a request cannot fake extra log lines (log injection)
// (OWASP, 2026e)
const cleanForLog = (value) => String(value).replace(/[\r\n]+/g, " ");

// adds a line to the end of a log file instead of overwriting it (IIE, 2026)
const appendToFile = (fileName, logEntry) => {
  const filePath = path.join(LOGS_DIR, fileName);
  fs.appendFile(filePath, logEntry, (err) => {
    if (err) {
      console.error(`[LOGGER ERROR] Failed to write to ${fileName}`, err.message);
    }
  });
};

// level = INFO, WARN or ERROR, context = where the log came from,
// meta = extra details such as an error object (IIE, 2026)
// passwords, tokens and request bodies must never be logged (OWASP, 2026e)
const log = (level, message, context = "APP", meta = null) => {
  const timestamp = getTimestamp();
  const dateStr = getDateString();

  let metaStr = "";
  if (meta) {
    if (meta instanceof Error) {
      // stack traces go in the log file only, never in an API response (OWASP, 2026e)
      metaStr = `\n${meta.stack || meta.message}`;
    } else if (typeof meta === "object") {
      try {
        metaStr = ` ${cleanForLog(JSON.stringify(meta))}`;
      } catch (e) {
        metaStr = " [Unextractable Error]";
      }
    } else {
      metaStr = ` ${cleanForLog(meta)}`;
    }
  }

  const safeMessage = cleanForLog(message);
  const fileEntry = `[${timestamp}] [${level}] [${context}]: ${safeMessage}${metaStr}\n`;

  // every log goes in the combined file (IIE, 2026)
  appendToFile(`${dateStr}-combined.log`, fileEntry);

  // errors also go in their own file (IIE, 2026)
  if (level === "ERROR") {
    appendToFile(`${dateStr}-error.log`, fileEntry);
  }

  // and a structured copy goes to Better Stack (queued, never blocks or throws)
  const cloudEntry = {
    dt: new Date().toISOString(),
    level: level.toLowerCase(),
    message: safeMessage,
    context,
  };
  if (meta instanceof Error) {
    cloudEntry.error = { name: meta.name, message: meta.message, stack: meta.stack };
  } else if (meta && typeof meta === "object") {
    try {
      cloudEntry.meta = JSON.parse(JSON.stringify(meta)); // plain copy, fails safely on circular objects
    } catch (e) {
      cloudEntry.meta = "[Unextractable Error]";
    }
  } else if (meta) {
    cloudEntry.meta = cleanForLog(meta);
  }
  sendToCloud(cloudEntry);

  // print to the terminal with a colour for each level (IIE, 2026)
  let colour = colours.reset;
  if (level === "INFO") colour = colours.green;
  if (level === "WARN") colour = colours.yellow;
  if (level === "ERROR") colour = colours.red;

  console.log(
    `${colours.gray}[${timestamp}]${colours.reset} ${colour}[${level}]${colours.reset} ${colours.cyan}[${context}]${colours.reset}: ${safeMessage}${metaStr}`,
  );
};

const logger = {
  // sends anything still queued to Better Stack; resolves even on failure
  flush: flushOnExit,
  info: (message, context = "APP", meta = null) =>
    log("INFO", message, context, meta),
  warn: (message, context = "APP", meta = null) =>
    log("WARN", message, context, meta),
  error: (message, context = "APP", meta = null) =>
    log("ERROR", message, context, meta),
};

module.exports = logger;

/*
References:
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
OWASP, 2026e. REST Security Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
Better Stack, 2026. Ingesting logs. [online] Available at: <https://betterstack.com/docs/logs/ingesting-data/http/logs/> [Accessed 9 November 2026].
Mozilla, 2026. AbortSignal. [online] Available at: <https://developer.mozilla.org/en-US/docs/Web/API/AbortSignal> [Accessed 9 November 2026].
*/