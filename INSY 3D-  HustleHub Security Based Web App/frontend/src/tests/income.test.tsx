import { describe, it, expect } from "vitest";
import { EMPTY_INCOME, normalizeIncome } from "../lib/income.ts";
import { formatMonth, formatPercent } from "../lib/format.ts";

describe("normalizeIncome", () => {
  it("keeps every field of the full tax-year response", () => {
    const result = normalizeIncome({
      taxYear: "2026/27",
      total: 300000,
      count: 4,
      taxBeforeRebate: 58392,
      rebate: 17820,
      estimatedTax: 40572,
      netIncome: 259428,
      effectiveRate: 13.52,
      marginalRate: 26,
      belowThreshold: false,
      brackets: [{ from: 0, to: 245100, rate: 18, amountInBracket: 245100, taxInBracket: 44118 }],
      monthly: [{ month: "2026-03", income: 300000, bookings: 4 }],
    });
    expect(result.total).toBe(300000);
    expect(result.estimatedTax).toBe(40572);
    expect(result.netIncome).toBe(259428);
    expect(result.brackets).toHaveLength(1);
    expect(result.monthly[0].month).toBe("2026-03");
  });

  it("still understands the older { totalIncome, transactionCount } shape", () => {
    const result = normalizeIncome({ totalIncome: 1500, transactionCount: 3 });
    expect(result.total).toBe(1500);
    expect(result.count).toBe(3);
    expect(result.netIncome).toBe(1500); // no tax data: net falls back to gross
  });

  it("turns a bare number into a complete income object", () => {
    const result = normalizeIncome(900);
    expect(result.total).toBe(900);
    expect(result.brackets).toEqual([]);
    expect(result.monthly).toEqual([]);
  });

  it("never returns missing arrays, even for null or junk input", () => {
    expect(normalizeIncome(null)).toEqual(EMPTY_INCOME);
    expect(normalizeIncome(undefined)).toEqual(EMPTY_INCOME);
    const junk = normalizeIncome({ brackets: "nope", monthly: 5 } as never);
    expect(junk.brackets).toEqual([]);
    expect(junk.monthly).toEqual([]);
  });
});

describe("format helpers", () => {
  it("formats months with fixed short names", () => {
    expect(formatMonth("2026-03")).toBe("Mar");
    expect(formatMonth("2026-09")).toBe("Sep");
    expect(formatMonth("2027-02")).toBe("Feb");
  });

  it("formats percentages to two decimals", () => {
    expect(formatPercent(13.5)).toBe("13.50%");
    expect(formatPercent(undefined)).toBe("0.00%");
  });
});