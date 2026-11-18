/**
 * build-collection.js
 * Generates the HustleHub+ Postman collection (requests + automated tests).
 *
 * Usage (from backend/postman):
 *   node build-collection.js HustleHub_Part2.postman_collection.json
 *
 * With no argument it writes hustlehub.postman_collection.json next to this file.
 * Edit the requests below, re-run the command, then re-import or run with Newman.
 */
const fs = require("fs");
const path = require("path");

const outFile = path.resolve(process.argv[2] || path.join(__dirname, "hustlehub.postman_collection.json"));
const PW = "Password1";

// ---------- helpers ----------
const status = (code) => [`pm.test('Status is ${code}', () => pm.response.to.have.status(${code}));`];

// token = name of a collection variable that holds a JWT
function req(name, method, urlPath, { token, body, rawBody, tests = [], pre } = {}) {
  const header = [];
  if (body !== undefined || rawBody !== undefined) header.push({ key: "Content-Type", value: "application/json" });
  if (token) header.push({ key: "Authorization", value: `Bearer {{${token}}}` });

  const request = {
    method,
    header,
    url: { raw: `{{baseUrl}}${urlPath}`, host: ["{{baseUrl}}"], path: urlPath.replace(/^\//, "").split("/") },
  };
  if (body !== undefined)
    request.body = { mode: "raw", raw: JSON.stringify(body, null, 2), options: { raw: { language: "json" } } };
  if (rawBody !== undefined)
    request.body = { mode: "raw", raw: rawBody, options: { raw: { language: "json" } } };

  const event = [];
  if (pre) event.push({ listen: "prerequest", script: { type: "text/javascript", exec: pre } });
  if (tests.length) event.push({ listen: "test", script: { type: "text/javascript", exec: tests } });
  return { name, request, event };
}

const folder = (name, item) => ({ name, item });
const regBody = (prefix, role, extra = {}) => ({
  username: `${prefix}_{{runId}}`,
  email: `${prefix}_{{runId}}@example.com`,
  password: PW,
  ...(role ? { role } : {}),
  ...extra,
});
const hasTx = "pm.response.json().some(t => t._id === pm.collectionVariables.get('transactionId'))";
const hasBooking = "pm.response.json().some(b => b._id === pm.collectionVariables.get('bookingId'))";
const GIG = {
  title: "Logo design {{runId}}",
  description: "A clean, modern logo for your business.",
  category: "Graphic Design",
  price: 500,
};

// ---------- 0. health + headers ----------
const health = folder("0. Health and security headers", [
  req("Health check and security headers", "GET", "/api/home/healthCheck", {
    // unique value per run so repeated runs never collide on email/username
    pre: ["pm.collectionVariables.set('runId', String(Date.now()));"],
    tests: [
      ...status(200),
      "const h = n => pm.response.headers.get(n);",
      "pm.test('Content-Security-Policy is set and restrictive', () => {",
      "  pm.expect(h('Content-Security-Policy')).to.include(\"default-src 'none'\");",
      "  pm.expect(h('Content-Security-Policy')).to.include(\"frame-ancestors 'none'\");",
      "});",
      "pm.test('HSTS is set', () => pm.expect(h('Strict-Transport-Security')).to.include('max-age'));",
      "pm.test('X-Content-Type-Options is nosniff', () => pm.expect(h('X-Content-Type-Options')).to.eql('nosniff'));",
      "pm.test('X-Frame-Options is set', () => pm.expect(h('X-Frame-Options')).to.exist);",
      "pm.test('Referrer-Policy is set', () => pm.expect(h('Referrer-Policy')).to.exist);",
      "pm.test('X-Powered-By is hidden', () => pm.expect(h('X-Powered-By')).to.be.undefined);",
    ],
  }),
]);

// ---------- 1. authentication ----------
const auth = folder("1. Authentication", [
  req("Register freelancer", "POST", "/api/auth/register", {
    body: regBody("free", "freelancer"),
    tests: [
      ...status(201),
      "const j = pm.response.json();",
      "pm.test('Returns token and freelancer role', () => { pm.expect(j.token).to.be.a('string'); pm.expect(j.role).to.eql('freelancer'); });",
      "pm.test('Password is never returned', () => pm.expect(j).to.not.have.property('password'));",
      "pm.collectionVariables.set('freelancerToken', j.token);",
      "pm.collectionVariables.set('freelancerId', j._id);",
    ],
  }),
  req("Register second freelancer", "POST", "/api/auth/register", {
    body: regBody("freeb", "freelancer"),
    tests: [
      ...status(201),
      "const j = pm.response.json();",
      "pm.collectionVariables.set('freelancer2Token', j.token);",
      "pm.collectionVariables.set('freelancer2Id', j._id);",
    ],
  }),
  req("Register client (default role)", "POST", "/api/auth/register", {
    body: regBody("client"),
    tests: [
      ...status(201),
      "const j = pm.response.json();",
      "pm.test('Defaults to client role', () => pm.expect(j.role).to.eql('client'));",
      "pm.collectionVariables.set('clientToken', j.token);",
      "pm.collectionVariables.set('clientId', j._id);",
    ],
  }),
  req("FAIL: self-register as admin (role escalation)", "POST", "/api/auth/register", {
    body: regBody("evil", "admin"),
    tests: [...status(400), "pm.test('Error message returned', () => pm.expect(pm.response.json().errors).to.be.an('array'));"],
  }),
  req("FAIL: duplicate email", "POST", "/api/auth/register", {
    body: regBody("free", "freelancer"),
    tests: [
      ...status(409),
      "pm.test('Generic message (no account enumeration)', () => pm.expect(pm.response.json().message).to.eql('Unable to register with the supplied details'));",
    ],
  }),
  req("FAIL: weak password", "POST", "/api/auth/register", {
    body: { username: "weak_{{runId}}", email: "weak_{{runId}}@example.com", password: "short" },
    tests: status(400),
  }),
  req("Login freelancer", "POST", "/api/auth/login", {
    body: { email: "free_{{runId}}@example.com", password: PW },
    tests: [...status(200), "pm.test('Token returned', () => pm.expect(pm.response.json().token).to.be.a('string'));"],
  }),
  req("FAIL: wrong password", "POST", "/api/auth/login", {
    body: { email: "free_{{runId}}@example.com", password: "WrongPass1" },
    tests: [...status(401), "pm.test('Generic message', () => pm.expect(pm.response.json().message).to.eql('Invalid email or password'));"],
  }),
  req("FAIL: NoSQL injection on login", "POST", "/api/auth/login", {
    rawBody: '{"email": {"$ne": null}, "password": {"$ne": null}}',
    tests: [
      "pm.test('Injection rejected (400 or 401)', () => pm.expect([400, 401]).to.include(pm.response.code));",
      "pm.test('No token issued', () => pm.expect(pm.response.json()).to.not.have.property('token'));",
    ],
  }),
  req("Login admin (seeded account)", "POST", "/api/auth/login", {
    body: { email: "{{adminEmail}}", password: "{{adminPassword}}" },
    tests: [
      ...status(200),
      "const j = pm.response.json();",
      "pm.test('Role is admin', () => pm.expect(j.role).to.eql('admin'));",
      "pm.collectionVariables.set('adminToken', j.token);",
    ],
  }),
]);

// ---------- 2. gigs ----------
const gigs = folder("2. Gigs and ownership", [
  req("Browse gigs (public)", "GET", "/api/gigs", {
    tests: [...status(200), "pm.test('Returns an array', () => pm.expect(pm.response.json()).to.be.an('array'));"],
  }),
  req("FAIL: create gig without token", "POST", "/api/gigs", { body: GIG, tests: status(401) }),
  req("FAIL: client cannot create gig", "POST", "/api/gigs", { token: "clientToken", body: GIG, tests: status(403) }),
  req("Create gig as freelancer", "POST", "/api/gigs", {
    token: "freelancerToken",
    body: GIG,
    tests: [
      ...status(201),
      "const j = pm.response.json();",
      "pm.test('Gig is owned by the logged-in freelancer', () => pm.expect(j.freelancer).to.eql(pm.collectionVariables.get('freelancerId')));",
      "pm.collectionVariables.set('gigId', j._id);",
      "pm.collectionVariables.set('gigPrice', j.price);",
    ],
  }),
  req("Create gig - script tags are sanitised", "POST", "/api/gigs", {
    token: "freelancerToken",
    body: { ...GIG, title: "<script>alert(1)</script>Safe title {{runId}}" },
    tests: [
      ...status(201),
      "const j = pm.response.json();",
      "pm.test('Stored title contains no HTML tags', () => pm.expect(j.title).to.not.match(/[<>]/));",
      "pm.collectionVariables.set('xssGigId', j._id);",
    ],
  }),
  req("FAIL: price is not a positive number", "POST", "/api/gigs", {
    token: "freelancerToken",
    body: { ...GIG, price: -5 },
    tests: status(400),
  }),
  req("View one gig (public)", "GET", "/api/gigs/{{gigId}}", {
    tests: [...status(200), "pm.test('Correct gig', () => pm.expect(pm.response.json()._id).to.eql(pm.collectionVariables.get('gigId')));"],
  }),
  req("Update own gig (PATCH)", "PATCH", "/api/gigs/{{gigId}}", {
    token: "freelancerToken",
    body: { price: 550 },
    tests: [
      ...status(200),
      "pm.test('Price updated', () => pm.expect(pm.response.json().price).to.eql(550));",
      "pm.collectionVariables.set('gigPrice', 550);",
    ],
  }),
  req("FAIL: another freelancer cannot PATCH my gig", "PATCH", "/api/gigs/{{gigId}}", {
    token: "freelancer2Token",
    body: { price: 1 },
    tests: status(403),
  }),
  req("FAIL: another freelancer cannot DELETE my gig", "DELETE", "/api/gigs/{{gigId}}", {
    token: "freelancer2Token",
    tests: status(403),
  }),
  req("FAIL: tampered token rejected", "POST", "/api/gigs", {
    token: "tamperedToken",
    body: GIG,
    pre: ["pm.collectionVariables.set('tamperedToken', pm.collectionVariables.get('freelancerToken') + 'x');"],
    tests: [...status(401), "pm.test('Generic message', () => pm.expect(pm.response.json().message).to.include('Not authorized'));"],
  }),
]);

// ---------- 3. bookings ----------
const bookings = folder("3. Bookings", [
  req("FAIL: booking without token", "POST", "/api/bookings", { body: { gig: "{{gigId}}" }, tests: status(401) }),
  req("FAIL: freelancer cannot book", "POST", "/api/bookings", {
    token: "freelancerToken",
    body: { gig: "{{gigId}}" },
    tests: status(403),
  }),
  req("Client books a gig (simulated payment)", "POST", "/api/bookings", {
    token: "clientToken",
    body: { gig: "{{gigId}}" },
    tests: [
      ...status(201),
      "const j = pm.response.json();",
      "pm.test('Booking is confirmed', () => pm.expect(j.booking.status).to.eql('confirmed'));",
      "pm.test('A transaction record was created', () => { pm.expect(j.transaction).to.be.an('object'); pm.expect(j.transaction.reference).to.be.a('string'); });",
      "pm.test('Transaction amount equals the gig price', () => pm.expect(j.transaction.amount).to.eql(pm.collectionVariables.get('gigPrice')));",
      "pm.test('Transaction is linked to the right client and freelancer', () => {",
      "  pm.expect(j.transaction.client).to.eql(pm.collectionVariables.get('clientId'));",
      "  pm.expect(j.transaction.freelancer).to.eql(pm.collectionVariables.get('freelancerId'));",
      "});",
      "pm.collectionVariables.set('bookingId', j.booking._id);",
      "pm.collectionVariables.set('transactionId', j.transaction._id);",
    ],
  }),
  req("Client cannot choose the price or another client", "POST", "/api/bookings", {
    token: "clientToken",
    body: { gig: "{{gigId}}", price: 1, client: "{{freelancer2Id}}", freelancer: "{{freelancer2Id}}" },
    tests: [
      ...status(201),
      "const j = pm.response.json();",
      "pm.test('Server price used, not the client price', () => pm.expect(j.transaction.amount).to.eql(pm.collectionVariables.get('gigPrice')));",
      "pm.test('Booking belongs to the caller', () => pm.expect(j.booking.client).to.eql(pm.collectionVariables.get('clientId')));",
    ],
  }),
  req("Client sees own bookings", "GET", "/api/bookings/mine", {
    token: "clientToken",
    tests: [...status(200), `pm.test('Contains the new booking', () => pm.expect(${hasBooking}).to.be.true);`],
  }),
  req("Freelancer sees received bookings", "GET", "/api/bookings/received", {
    token: "freelancerToken",
    tests: [...status(200), `pm.test('Contains the new booking', () => pm.expect(${hasBooking}).to.be.true);`],
  }),
  req("Other freelancer does not see my bookings", "GET", "/api/bookings/received", {
    token: "freelancer2Token",
    tests: [...status(200), `pm.test('Booking not visible to another freelancer', () => pm.expect(${hasBooking}).to.be.false);`],
  }),
]);

// ---------- 4. transactions and income ----------
const money = folder("4. Transactions and income", [
  req("Client sees own transactions", "GET", "/api/transactions/mine", {
    token: "clientToken",
    tests: [
      ...status(200),
      `pm.test('Contains the booking transaction', () => pm.expect(${hasTx}).to.be.true);`,
      "pm.test('Every record belongs to this client', () => pm.expect(pm.response.json().every(t => (t.client._id || t.client) === pm.collectionVariables.get('clientId'))).to.be.true);",
    ],
  }),
  req("Other freelancer cannot see my transactions", "GET", "/api/transactions/mine", {
    token: "freelancer2Token",
    tests: [...status(200), `pm.test('Transaction not visible', () => pm.expect(${hasTx}).to.be.false);`],
  }),
  req("FAIL: client cannot view income", "GET", "/api/income", { token: "clientToken", tests: status(403) }),
  req("Freelancer income reflects bookings", "GET", "/api/income", {
    token: "freelancerToken",
    tests: [
      ...status(200),
      "const j = pm.response.json();",
      "pm.test('Two bookings counted', () => pm.expect(j.count).to.eql(2));",
      "pm.test('Total equals 2 x gig price', () => pm.expect(j.total).to.eql(2 * pm.collectionVariables.get('gigPrice')));",
      "pm.test('Income is broken down by gig', () => pm.expect(j.byGig).to.be.an('array').that.is.not.empty);",
    ],
  }),
]);

// ---------- 5. admin and RBAC ----------
const admin = folder("5. Admin and RBAC", [
  req("FAIL: client cannot list users", "GET", "/api/admin/users", { token: "clientToken", tests: status(403) }),
  req("Admin lists users (no passwords)", "GET", "/api/admin/users", {
    token: "adminToken",
    tests: [...status(200), "pm.test('No password field on any user', () => pm.expect(pm.response.json().every(u => !('password' in u))).to.be.true);"],
  }),
  req("Admin removes any gig", "DELETE", "/api/admin/gigs/{{xssGigId}}", {
    token: "adminToken",
    tests: [...status(200), "pm.test('Gig deleted', () => pm.expect(pm.response.json().message).to.eql('Gig deleted'));"],
  }),
]);

// ---------- 6. clean-up ----------
const cleanup = folder("6. Clean-up", [
  req("Delete gig with bookings (deactivated instead)", "DELETE", "/api/gigs/{{gigId}}", {
    token: "freelancerToken",
    tests: [...status(200), "pm.test('Deactivated, history kept', () => pm.expect(pm.response.json().deactivated).to.be.true);"],
  }),
]);

// ---------- collection ----------
const variables = [
  "runId", "freelancerToken", "freelancerId", "freelancer2Token", "freelancer2Id", "clientToken", "clientId",
  "adminToken", "gigId", "gigPrice", "xssGigId", "bookingId", "transactionId", "tamperedToken",
];

const collection = {
  info: {
    name: "HustleHub+ API (Part 2)",
    _postman_id: "hustlehub-part2-0001",
    description: "Functional, security and RBAC tests for HustleHub+. Generated by build-collection.js.",
    schema: "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
  },
  item: [health, auth, gigs, bookings, money, admin, cleanup],
  variable: variables.map((key) => ({ key, value: "" })),
};

fs.mkdirSync(path.dirname(outFile), { recursive: true });
fs.writeFileSync(outFile, JSON.stringify(collection, null, 2));

const total = collection.item.reduce((n, f) => n + f.item.length, 0);
console.log(`Wrote ${total} requests to ${outFile}`);