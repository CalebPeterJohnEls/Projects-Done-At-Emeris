// Run with: node --test tests/account.test.js
// No real database: the model methods are replaced with fakes below.
process.env.JWT_SECRET = "test-secret-for-account-tests";

const test = require("node:test");
const assert = require("node:assert/strict");
const jwt = require("jsonwebtoken");

const User = require("../models/userModel.js");
const LoginEvent = require("../models/loginEventModel.js");
const twoFactor = require("../controllers/twoFactorController.js");
const { changePassword, logoutAll, getActivity } = require("../controllers/accountController.js");
const { recordEvent, MAX_USER_AGENT } = require("../utils/securityEvents.js");

// ----- helpers -----
const makeUser = ({ password = "OldPassw0rd", tokenVersion = 0, twoFactorEnabled = false } = {}) => {
    const user = {
        _id: "user-1",
        role: "client",
        password,
        tokenVersion,
        twoFactor: { enabled: twoFactorEnabled },
        saves: 0,
        matchPassword: async (p) => p === user.password,
        save: async () => { user.saves += 1; },
    };
    return user;
};

const fakeRes = () => {
    const res = { statusCode: 200, body: undefined };
    res.status = (n) => { res.statusCode = n; return res; };
    res.json = (b) => { res.body = b; return res; };
    return res;
};

const fakeReq = (body = {}) => ({ user: { _id: "user-1" }, body, ip: "10.0.0.5", headers: { "user-agent": "Mozilla/5.0 (Windows NT 10.0) Chrome/126.0 Safari/537.36" } });

let events;
const install = (user) => {
    events = [];
    User.findById = () => {
        const q = { select: async () => user, then: (ok) => ok(user) };
        return q;
    };
    LoginEvent.create = async (doc) => { events.push(doc); return doc; };
};

// ----- changePassword -----
test("changePassword: wrong current password is rejected and nothing changes", async () => {
    const user = makeUser();
    install(user);
    const res = fakeRes();
    await changePassword(fakeReq({ currentPassword: "nope", newPassword: "NewPassw0rd" }), res);
    assert.equal(res.statusCode, 401);
    assert.equal(user.password, "OldPassw0rd");
    assert.equal(user.tokenVersion, 0);
    assert.equal(user.saves, 0);
    assert.equal(events.length, 0);
});

test("changePassword: the new password must differ from the current one", async () => {
    const user = makeUser();
    install(user);
    const res = fakeRes();
    await changePassword(fakeReq({ currentPassword: "OldPassw0rd", newPassword: "OldPassw0rd" }), res);
    assert.equal(res.statusCode, 400);
    assert.equal(user.tokenVersion, 0);
});

test("changePassword: success sets the password, bumps the version and returns a working new token", async () => {
    const user = makeUser();
    install(user);
    const res = fakeRes();
    await changePassword(fakeReq({ currentPassword: "OldPassw0rd", newPassword: "BrandNew1" }), res);
    assert.equal(res.statusCode, 200);
    assert.equal(user.password, "BrandNew1"); // the model's pre-save hook hashes it in real use
    assert.equal(user.tokenVersion, 1);
    assert.equal(user.saves, 1);

    const decoded = jwt.verify(res.body.token, process.env.JWT_SECRET);
    assert.equal(decoded.id, "user-1");
    assert.equal(decoded.tv, 1); // the new token matches the new version, so this device stays signed in
    assert.equal(events.length, 1);
    assert.equal(events[0].type, "password_changed");
});

test("changePassword: with 2FA on, a bad code stops the change", async () => {
    const user = makeUser({ twoFactorEnabled: true });
    install(user);
    twoFactor.checkSecondFactor = async () => "bad";
    const res = fakeRes();
    await changePassword(fakeReq({ currentPassword: "OldPassw0rd", newPassword: "BrandNew1", code: "000000" }), res);
    assert.equal(res.statusCode, 401);
    assert.equal(user.password, "OldPassw0rd");
    assert.equal(user.tokenVersion, 0);
});

test("changePassword: with 2FA on, a locked account gets 429", async () => {
    const user = makeUser({ twoFactorEnabled: true });
    install(user);
    twoFactor.checkSecondFactor = async () => "locked";
    const res = fakeRes();
    await changePassword(fakeReq({ currentPassword: "OldPassw0rd", newPassword: "BrandNew1", code: "123456" }), res);
    assert.equal(res.statusCode, 429);
    assert.equal(user.tokenVersion, 0);
});

test("changePassword: with 2FA on, a good code lets it through", async () => {
    const user = makeUser({ twoFactorEnabled: true });
    install(user);
    twoFactor.checkSecondFactor = async () => "ok";
    const res = fakeRes();
    await changePassword(fakeReq({ currentPassword: "OldPassw0rd", newPassword: "BrandNew1", code: "123456" }), res);
    assert.equal(res.statusCode, 200);
    assert.equal(user.tokenVersion, 1);
});

test("changePassword: without 2FA the code is never asked for", async () => {
    const user = makeUser({ twoFactorEnabled: false });
    install(user);
    let asked = false;
    twoFactor.checkSecondFactor = async () => { asked = true; return "bad"; };
    const res = fakeRes();
    await changePassword(fakeReq({ currentPassword: "OldPassw0rd", newPassword: "BrandNew1" }), res);
    assert.equal(res.statusCode, 200);
    assert.equal(asked, false);
});

test("changePassword: a database error gives a generic 500", async () => {
    install(makeUser());
    User.findById = () => ({ select: async () => { throw new Error("db exploded: secret detail"); } });
    const res = fakeRes();
    await changePassword(fakeReq({ currentPassword: "x", newPassword: "y" }), res);
    assert.equal(res.statusCode, 500);
    assert.ok(!JSON.stringify(res.body).includes("secret detail"));
});

// ----- logoutAll -----
test("logoutAll: wrong password is rejected and sessions stay valid", async () => {
    const user = makeUser({ tokenVersion: 3 });
    install(user);
    const res = fakeRes();
    await logoutAll(fakeReq({ currentPassword: "nope" }), res);
    assert.equal(res.statusCode, 401);
    assert.equal(user.tokenVersion, 3);
    assert.equal(events.length, 0);
});

test("logoutAll: bumps the version, keeps this device signed in, and records the event", async () => {
    const user = makeUser({ tokenVersion: 3 });
    install(user);
    const res = fakeRes();
    await logoutAll(fakeReq({ currentPassword: "OldPassw0rd" }), res);
    assert.equal(res.statusCode, 200);
    assert.equal(user.tokenVersion, 4);
    assert.equal(user.password, "OldPassw0rd"); // password itself is untouched
    assert.equal(jwt.verify(res.body.token, process.env.JWT_SECRET).tv, 4);
    assert.equal(events[0].type, "sessions_revoked");
});

test("logoutAll: a user document with no version yet goes from 0 to 1", async () => {
    const user = makeUser();
    delete user.tokenVersion;
    install(user);
    const res = fakeRes();
    await logoutAll(fakeReq({ currentPassword: "OldPassw0rd" }), res);
    assert.equal(user.tokenVersion, 1);
});

// ----- getActivity -----
test("getActivity: asks only for the caller's events, newest first, limited to 10", async () => {
    install(makeUser());
    const calls = {};
    LoginEvent.find = (filter) => {
        calls.filter = filter;
        const chain = {
            sort: (s) => { calls.sort = s; return chain; },
            limit: (n) => { calls.limit = n; return chain; },
            lean: async () => [
                { _id: "e1", type: "login_success", ip: "10.0.0.5", userAgent: "Mozilla/5.0 (Windows NT 10.0) Chrome/126.0 Safari/537.36", createdAt: new Date("2026-11-09T10:00:00Z") },
            ],
        };
        return chain;
    };
    const res = fakeRes();
    await getActivity(fakeReq(), res);
    assert.deepEqual(calls.filter, { user: "user-1" });
    assert.deepEqual(calls.sort, { createdAt: -1 });
    assert.equal(calls.limit, 10);
    assert.equal(res.statusCode, 200);
    assert.equal(res.body[0].device, "Chrome on Windows");
    assert.equal(res.body[0].type, "login_success");
    assert.equal("userAgent" in res.body[0], false); // the raw user-agent is not sent to the browser
});

// ----- recordEvent -----
test("recordEvent: stores ip and a truncated user-agent", async () => {
    install(makeUser());
    const req = { ip: "10.0.0.9", headers: { "user-agent": "x".repeat(500) } };
    await recordEvent(req, "user-1", "login_failed");
    assert.equal(events[0].user, "user-1");
    assert.equal(events[0].type, "login_failed");
    assert.equal(events[0].ip, "10.0.0.9");
    assert.equal(events[0].userAgent.length, MAX_USER_AGENT);
});

test("recordEvent: never throws, even when the database is down", async () => {
    install(makeUser());
    LoginEvent.create = async () => { throw new Error("db down"); };
    await assert.doesNotReject(() => recordEvent(fakeReq(), "user-1", "login_success"));
});

test("login event types are the five the Security page expects", () => {
    assert.deepEqual(LoginEvent.EVENT_TYPES, [
        "login_success", "login_failed", "two_factor_failed", "password_changed", "sessions_revoked",
    ]);
});