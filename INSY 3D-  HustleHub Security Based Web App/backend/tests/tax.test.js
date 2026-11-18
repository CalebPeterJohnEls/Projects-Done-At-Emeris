// Run with: node --test tests/tax.test.js   (built into Node, no extra packages)
const test = require("node:test");
const assert = require("node:assert/strict");
const {
    calculateTax,
    rebateForAge,
    thresholdForAge,
    taxYearBounds,
    taxYearMonths,
} = require("../utils/tax.js");

// Tax before rebate at each bracket ceiling, copied from the SARS table
// ("44 118 + 26% above 245 100" etc.). These are the boundary checks.
const SARS_TABLE = [
    [245100, 44118],
    [383100, 79998],
    [530200, 125599],
    [695800, 185215],
    [887000, 259783],
    [1878600, 666339],
];

test("zero income gives zero tax and a 0% rate", () => {
    const r = calculateTax(0);
    assert.equal(r.estimatedTax, 0);
    assert.equal(r.netIncome, 0);
    assert.equal(r.effectiveRate, 0);
    assert.equal(r.belowThreshold, true);
});

for (const [ceiling, expected] of SARS_TABLE) {
    test(`tax before rebate at R${ceiling} matches the SARS table (R${expected})`, () => {
        assert.equal(calculateTax(ceiling).taxBeforeRebate, expected);
    });
}

test("one rand into the next bracket is taxed at the next rate", () => {
    // R245,101: R44,118 + 26% of R1
    assert.equal(calculateTax(245101).taxBeforeRebate, 44118.26);
    // R1,878,601: R666,339 + 45% of R1
    assert.equal(calculateTax(1878601).taxBeforeRebate, 666339.45);
});

test("tax-free threshold for under 65s is R99,000", () => {
    assert.equal(thresholdForAge(30), 99000);
    const at = calculateTax(99000);
    assert.equal(at.estimatedTax, 0);
    assert.equal(at.belowThreshold, true);
    // one rand over the threshold pays 18 cents
    const over = calculateTax(99001);
    assert.equal(over.estimatedTax, 0.18);
    assert.equal(over.belowThreshold, false);
});

test("age rebates stack and move the threshold", () => {
    assert.equal(rebateForAge(30), 17820);
    assert.equal(rebateForAge(65), 17820 + 9765);
    assert.equal(rebateForAge(75), 17820 + 9765 + 3249);
    assert.equal(thresholdForAge(65), 153250);
    assert.equal(thresholdForAge(75), 171300);
    assert.equal(calculateTax(153250, { age: 65 }).estimatedTax, 0);
    assert.equal(calculateTax(171300, { age: 75 }).estimatedTax, 0);
});

test("estimated tax is bracket tax minus the primary rebate", () => {
    // R300,000: 44,118 + 26% of 54,900 = 58,392 ; minus 17,820 = 40,572
    const r = calculateTax(300000);
    assert.equal(r.taxBeforeRebate, 58392);
    assert.equal(r.estimatedTax, 40572);
    assert.equal(r.netIncome, 259428);
    assert.equal(r.effectiveRate, 13.52);
    assert.equal(r.marginalRate, 26);
});

test("bracket breakdown adds up to the tax before rebate", () => {
    for (const income of [0, 50000, 245100, 612345.67, 1500000, 3000000]) {
        const r = calculateTax(income);
        const sumTax = r.brackets.reduce((s, b) => s + b.taxInBracket, 0);
        const sumIncome = r.brackets.reduce((s, b) => s + b.amountInBracket, 0);
        assert.ok(Math.abs(sumTax - r.taxBeforeRebate) < 0.05, `tax mismatch at ${income}`);
        assert.ok(Math.abs(sumIncome - income) < 0.05, `income mismatch at ${income}`);
        assert.equal(r.brackets.length, 7);
    }
});

test("net income plus tax always equals gross income", () => {
    for (const income of [1, 99000, 250000, 999999.99, 2500000]) {
        const r = calculateTax(income);
        assert.ok(Math.abs(r.netIncome + r.estimatedTax - r.grossIncome) < 0.01);
    }
});

test("top bracket has no ceiling", () => {
    const r = calculateTax(2000000);
    const top = r.brackets[6];
    assert.equal(top.to, null);
    assert.equal(top.rate, 45);
    assert.equal(top.amountInBracket, 121400);
    assert.equal(r.marginalRate, 45);
});

test("invalid income is rejected", () => {
    for (const bad of [-1, NaN, Infinity, "100", null, undefined]) {
        assert.throws(() => calculateTax(bad), RangeError);
    }
});

test("tax year bounds are SA midnight, start inclusive and end exclusive", () => {
    const { start, end } = taxYearBounds();
    assert.equal(start.toISOString(), "2026-02-28T22:00:00.000Z"); // 1 Mar 2026 00:00 SAST
    assert.equal(end.toISOString(), "2027-02-28T22:00:00.000Z"); // 1 Mar 2027 00:00 SAST
});

test("tax year has 12 months, March to February", () => {
    const months = taxYearMonths();
    assert.equal(months.length, 12);
    assert.equal(months[0], "2026-03");
    assert.equal(months[9], "2026-12");
    assert.equal(months[10], "2027-01");
    assert.equal(months[11], "2027-02");
});