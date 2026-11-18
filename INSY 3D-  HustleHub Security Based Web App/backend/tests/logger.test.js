// Run with: node --test tests/logger.test.js   (built into Node, no extra packages)
// A fake Better Stack server is started on localhost, so no real account or token is needed.
const test = require("node:test");
const assert = require("node:assert/strict");
const http = require("node:http");

const LOGGER_PATH = require.resolve("../utils/logger.js");

// the logger reads its settings when it is first loaded, so each test loads a fresh copy
const loadLogger = (env) => {
  delete require.cache[LOGGER_PATH];
  for (const key of ["BETTERSTACK_SOURCE_TOKEN", "BETTERSTACK_INGEST_HOST"]) delete process.env[key];
  Object.assign(process.env, { BETTERSTACK_FLUSH_MS: "100000", BETTERSTACK_BATCH_SIZE: "5", BETTERSTACK_TIMEOUT_MS: "1000" }, env);
  return require(LOGGER_PATH);
};

// fake ingest server: records requests, and can be told to fail
const startServer = async () => {
  const state = { requests: [], status: 202 };
  const server = http.createServer((req, res) => {
    let body = "";
    req.on("data", (chunk) => (body += chunk));
    req.on("end", () => {
      if (state.status < 300) state.requests.push({ auth: req.headers.authorization, entries: JSON.parse(body) });
      res.statusCode = state.status;
      res.end("{}");
    });
  });
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  state.url = `http://127.0.0.1:${server.address().port}`;
  state.close = () => new Promise((resolve) => { server.close(resolve); server.closeAllConnections(); }); // fetch keeps connections open
  return state;
};

const count = (state) => state.requests.reduce((n, r) => n + r.entries.length, 0);

// keep the test output readable and quiet the expected failure warnings
test.beforeEach((t) => {
  t.mock.method(console, "log", () => {});
  t.mock.method(console, "error", () => {});
});

test("sends logs to Better Stack with the token and the right fields", async () => {
  const server = await startServer();
  const logger = loadLogger({ BETTERSTACK_SOURCE_TOKEN: "tok123", BETTERSTACK_INGEST_HOST: server.url });

  logger.info("hello", "TEST", { a: 1 });
  logger.error("boom", "TEST", new Error("bad thing"));
  await logger.flush();

  assert.equal(count(server), 2);
  assert.equal(server.requests[0].auth, "Bearer tok123");
  const [info, err] = server.requests[0].entries;
  assert.equal(info.level, "info");
  assert.equal(info.message, "hello");
  assert.equal(info.context, "TEST");
  assert.deepEqual(info.meta, { a: 1 });
  assert.equal(err.level, "error");
  assert.match(err.error.stack, /bad thing/);
  await server.close();
});

test("a full batch is sent without waiting for the timer", async () => {
  const server = await startServer();
  const logger = loadLogger({ BETTERSTACK_SOURCE_TOKEN: "t", BETTERSTACK_INGEST_HOST: server.url });

  for (let i = 0; i < 5; i++) logger.info(`m${i}`, "TEST"); // batch size is 5
  await new Promise((resolve) => setTimeout(resolve, 300));

  assert.equal(count(server), 5);
  await server.close();
});

test("does nothing and does not crash when no token is set", async () => {
  const logger = loadLogger({});
  assert.doesNotThrow(() => logger.info("no cloud", "TEST"));
  await logger.flush();
});

test("a failing server never throws, and the logs are kept and sent after recovery", async () => {
  const server = await startServer();
  server.status = 500;
  const logger = loadLogger({ BETTERSTACK_SOURCE_TOKEN: "t", BETTERSTACK_INGEST_HOST: server.url });

  assert.doesNotThrow(() => logger.warn("during outage", "TEST"));
  await logger.flush(); // fails quietly
  assert.equal(count(server), 0);

  server.status = 202; // server is back
  await logger.flush();
  assert.equal(count(server), 1);
  assert.equal(server.requests[0].entries[0].message, "during outage");
  await server.close();
});

test("an unreachable server does not crash the app", async () => {
  const server = await startServer();
  const url = server.url;
  await server.close(); // nothing is listening any more
  const logger = loadLogger({ BETTERSTACK_SOURCE_TOKEN: "t", BETTERSTACK_INGEST_HOST: url });

  assert.doesNotThrow(() => logger.error("nobody home", "TEST"));
  await logger.flush();
});

test("an object with a circular reference does not break logging", async () => {
  const server = await startServer();
  const logger = loadLogger({ BETTERSTACK_SOURCE_TOKEN: "t", BETTERSTACK_INGEST_HOST: server.url });
  const circular = {};
  circular.self = circular;

  assert.doesNotThrow(() => logger.info("circular", "TEST", circular));
  await logger.flush();
  assert.equal(count(server), 1);
  await server.close();
});

test("line breaks are stripped so a request cannot fake extra log lines", async () => {
  const server = await startServer();
  const logger = loadLogger({ BETTERSTACK_SOURCE_TOKEN: "t", BETTERSTACK_INGEST_HOST: server.url });

  logger.info("line one\nFAKE [ERROR] line two", "TEST");
  await logger.flush();

  assert.ok(!server.requests[0].entries[0].message.includes("\n"));
  await server.close();
});