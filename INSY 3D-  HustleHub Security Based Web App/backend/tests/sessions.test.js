// Run with: node --test tests/sessions.test.js
const test = require("node:test");
const assert = require("node:assert/strict");
const { isTokenCurrent, describeDevice } = require("../utils/sessions.js");

test("a token with the same version as the user is current", () => {
    assert.equal(isTokenCurrent({ tv: 2 }, { tokenVersion: 2 }), true);
});

test("a token with an older version is rejected after a sign-out-everywhere", () => {
    assert.equal(isTokenCurrent({ tv: 1 }, { tokenVersion: 2 }), false);
});

test("a token with a newer version than the user is rejected", () => {
    assert.equal(isTokenCurrent({ tv: 5 }, { tokenVersion: 2 }), false);
});

test("tokens issued before this feature (no tv) still work for users on version 0", () => {
    assert.equal(isTokenCurrent({ id: "x", role: "client" }, { tokenVersion: 0 }), true);
    assert.equal(isTokenCurrent({ id: "x" }, {}), true); // user document with no version at all
});

test("an old token without tv stops working once the version is bumped", () => {
    assert.equal(isTokenCurrent({ id: "x" }, { tokenVersion: 1 }), false);
});

test("junk values never throw and never match a real version", () => {
    assert.equal(isTokenCurrent(null, { tokenVersion: 1 }), false);
    assert.equal(isTokenCurrent({ tv: "abc" }, { tokenVersion: 1 }), false);
    assert.equal(isTokenCurrent({ tv: 1 }, null), false);
});

const UA = {
    chromeWin: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36",
    edgeWin: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 Edg/126.0.0.0",
    firefoxLinux: "Mozilla/5.0 (X11; Linux x86_64; rv:128.0) Gecko/20100101 Firefox/128.0",
    safariMac: "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15",
    safariIphone: "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1",
    chromeAndroid: "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Mobile Safari/537.36",
    postman: "PostmanRuntime/7.39.0",
};

test("describeDevice names the browser and system", () => {
    assert.equal(describeDevice(UA.chromeWin), "Chrome on Windows");
    assert.equal(describeDevice(UA.edgeWin), "Edge on Windows");
    assert.equal(describeDevice(UA.firefoxLinux), "Firefox on Linux");
    assert.equal(describeDevice(UA.safariMac), "Safari on macOS");
});

test("describeDevice does not mix up iPhone with Mac, or Android with Linux", () => {
    assert.equal(describeDevice(UA.safariIphone), "Safari on iOS");
    assert.equal(describeDevice(UA.chromeAndroid), "Chrome on Android");
});

test("describeDevice handles API clients and empty input", () => {
    assert.equal(describeDevice(UA.postman), "API client");
    assert.equal(describeDevice(""), "Unknown device");
    assert.equal(describeDevice(undefined), "Unknown device");
    assert.equal(describeDevice("something odd"), "Unknown browser");
});