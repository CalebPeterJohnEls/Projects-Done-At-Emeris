// South African individual income tax estimate. Pure functions only: no
// database, no Express, no dates, so every result can be unit tested.
//
// Figures are the SARS 2027 tax year (1 March 2026 - 28 February 2027),
// taken from "Rates of Tax for Individuals" on sars.gov.za (checked 9 Nov 2026).
// When Budget day changes the figures, update TAX_YEAR, BRACKETS and REBATES only.

const TAX_YEAR = {
    label: "2026/27",
    start: "2026-03-01",
    end: "2027-02-28",
};

// Each bracket applies to the slice of taxable income above `from`, up to `to`.
// `to: null` means no upper limit.
const BRACKETS = [
    { from: 0, to: 245100, rate: 0.18 },
    { from: 245100, to: 383100, rate: 0.26 },
    { from: 383100, to: 530200, rate: 0.31 },
    { from: 530200, to: 695800, rate: 0.36 },
    { from: 695800, to: 887000, rate: 0.39 },
    { from: 887000, to: 1878600, rate: 0.41 },
    { from: 1878600, to: null, rate: 0.45 },
];

// Rebates stack by age: a 66 year old gets primary + secondary.
const REBATES = {
    primary: 17820,
    secondary: 9765, // age 65 and older
    tertiary: 3249, // age 75 and older
};

const round2 = (n) => Math.round((n + Number.EPSILON) * 100) / 100;

// Rebate for a given age. Defaults to under 65 (primary only), because
// HustleHub does not store a user's age.
const rebateForAge = (age) => {
    let rebate = REBATES.primary;
    if (age >= 65) rebate += REBATES.secondary;
    if (age >= 75) rebate += REBATES.tertiary;
    return rebate;
};

// Income below which no tax is payable (rebate wipes out the bracket tax).
const thresholdForAge = (age) => round2(rebateForAge(age) / BRACKETS[0].rate);

// Estimate tax on `income` (rand) earned in one tax year.
// Returns a plain object, ready to send as JSON.
const calculateTax = (income, { age = 0 } = {}) => {
    if (typeof income !== "number" || !Number.isFinite(income) || income < 0) {
        throw new RangeError("income must be a non-negative number");
    }

    const brackets = BRACKETS.map((b) => {
        const upper = b.to === null ? Infinity : b.to;
        const amountInBracket = Math.max(0, Math.min(income, upper) - b.from);
        return {
            from: b.from,
            to: b.to,
            rate: round2(b.rate * 100), // shown as a percentage, e.g. 26
            amountInBracket: round2(amountInBracket),
            taxInBracket: round2(amountInBracket * b.rate),
        };
    });

    const taxBeforeRebate = round2(brackets.reduce((sum, b) => sum + b.taxInBracket, 0));
    const rebate = rebateForAge(age);
    const estimatedTax = round2(Math.max(0, taxBeforeRebate - rebate));
    const grossIncome = round2(income);

    const marginal = [...BRACKETS].reverse().find((b) => income > b.from);

    return {
        taxYear: TAX_YEAR.label,
        grossIncome,
        taxBeforeRebate,
        rebate,
        estimatedTax,
        netIncome: round2(grossIncome - estimatedTax),
        effectiveRate: grossIncome === 0 ? 0 : round2((estimatedTax / grossIncome) * 100),
        marginalRate: marginal && estimatedTax > 0 ? round2(marginal.rate * 100) : 0,
        belowThreshold: income <= thresholdForAge(age),
        brackets,
    };
};

// Tax-year helpers. South Africa is UTC+2 all year (no daylight saving), so a
// fixed +02:00 offset gives exact local midnight boundaries.
const DAY_MS = 24 * 60 * 60 * 1000;

// [start, end) of the tax year as Dates, ready for a Mongo { $gte, $lt } filter.
const taxYearBounds = () => ({
    start: new Date(`${TAX_YEAR.start}T00:00:00+02:00`),
    end: new Date(new Date(`${TAX_YEAR.end}T00:00:00+02:00`).getTime() + DAY_MS),
});

// The 12 months of the tax year in order, as "YYYY-MM" keys (March first).
const taxYearMonths = () => {
    const startYear = Number(TAX_YEAR.start.slice(0, 4));
    const startMonth = Number(TAX_YEAR.start.slice(5, 7)); // 3 = March
    return Array.from({ length: 12 }, (_, i) => {
        const zeroBased = startMonth - 1 + i;
        const year = startYear + Math.floor(zeroBased / 12);
        const month = (zeroBased % 12) + 1;
        return `${year}-${String(month).padStart(2, "0")}`;
    });
};

module.exports = {
    calculateTax,
    rebateForAge,
    thresholdForAge,
    taxYearBounds,
    taxYearMonths,
    TAX_YEAR,
    BRACKETS,
    REBATES,
};