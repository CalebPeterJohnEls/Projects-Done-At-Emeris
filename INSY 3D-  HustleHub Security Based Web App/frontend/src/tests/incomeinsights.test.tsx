import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import {
  BracketBreakdown,
  MonthlyIncomeChart,
  TaxExplainer,
  TaxSummaryCards,
} from "../components/IncomeInsights.tsx";
import { EMPTY_INCOME } from "../lib/income.ts";
import { formatMoney } from "../lib/format.ts";
import type { Income } from "../models/booking.ts";

afterEach(cleanup);

const BRACKET_LIMITS: [number, number | null, number][] = [
  [0, 245100, 18],
  [245100, 383100, 26],
  [383100, 530200, 31],
  [530200, 695800, 36],
  [695800, 887000, 39],
  [887000, 1878600, 41],
  [1878600, null, 45],
];

// R300,000 gross: R245,100 at 18% and R54,900 at 26%
const sample: Income = {
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
  brackets: BRACKET_LIMITS.map(([from, to, rate], i) => ({
    from,
    to,
    rate,
    amountInBracket: i === 0 ? 245100 : i === 1 ? 54900 : 0,
    taxInBracket: i === 0 ? 44118 : i === 1 ? 14274 : 0,
  })),
  monthly: [
    "2026-03", "2026-04", "2026-05", "2026-06", "2026-07", "2026-08",
    "2026-09", "2026-10", "2026-11", "2026-12", "2027-01", "2027-02",
  ].map((month) => ({
    month,
    income: month === "2026-11" ? 200000 : month === "2026-10" ? 100000 : 0,
    bookings: month === "2026-11" ? 3 : month === "2026-10" ? 1 : 0,
  })),
};

describe("TaxSummaryCards", () => {
  it("shows gross, tax, net and effective rate", () => {
    render(<TaxSummaryCards income={sample} />);
    expect(screen.getByTestId("card-gross").textContent).toContain(formatMoney(300000));
    expect(screen.getByTestId("card-gross").textContent).toContain("4 confirmed bookings");
    expect(screen.getByTestId("card-tax").textContent).toContain(formatMoney(40572));
    expect(screen.getByTestId("card-net").textContent).toContain(formatMoney(259428));
    expect(screen.getByTestId("card-rate").textContent).toContain("13.52%");
    expect(screen.getByTestId("card-rate").textContent).toContain("26.00%");
  });

  it("explains when income is below the tax threshold", () => {
    render(<TaxSummaryCards income={{ ...EMPTY_INCOME, total: 50000, netIncome: 50000, count: 1 }} />);
    expect(screen.getByTestId("below-threshold")).toBeTruthy();
    expect(screen.getByTestId("card-gross").textContent).toContain("1 confirmed booking");
    expect(screen.getByTestId("card-gross").textContent).not.toContain("bookings");
  });

  it("hides the threshold note once tax is payable", () => {
    render(<TaxSummaryCards income={sample} />);
    expect(screen.queryByTestId("below-threshold")).toBeNull();
  });
});

describe("BracketBreakdown", () => {
  it("lists all seven brackets and the rebate steps", () => {
    render(<BracketBreakdown income={sample} />);
    expect(screen.getAllByTestId("bracket-row")).toHaveLength(7);
    expect(screen.getByTestId("tax-before-rebate").textContent).toContain(formatMoney(58392));
    expect(screen.getByTestId("rebate").textContent).toContain(formatMoney(17820));
    expect(screen.getByTestId("estimated-tax-total").textContent).toContain(formatMoney(40572));
  });

  it("labels the top bracket as open ended", () => {
    render(<BracketBreakdown income={sample} />);
    const rows = screen.getAllByTestId("bracket-row");
    expect(rows[6].textContent).toContain("Above");
    expect(rows[6].textContent).toContain("45%");
  });

  it("shows a message instead of an empty table when there is no data", () => {
    render(<BracketBreakdown income={EMPTY_INCOME} />);
    expect(screen.queryAllByTestId("bracket-row")).toHaveLength(0);
    expect(screen.getByText(/no bracket data/i)).toBeTruthy();
  });
});

describe("MonthlyIncomeChart", () => {
  it("draws one column per month of the tax year, March first", () => {
    render(<MonthlyIncomeChart income={sample} />);
    const columns = screen.getAllByTestId("month-column");
    expect(columns).toHaveLength(12);
    expect(columns[0].getAttribute("data-month")).toBe("2026-03");
    expect(columns[11].getAttribute("data-month")).toBe("2027-02");
  });

  it("scales bars against the biggest month", () => {
    render(<MonthlyIncomeChart income={sample} />);
    const bars = screen.getAllByTestId("month-bar") as HTMLElement[];
    const heights = bars.map((b) => b.style.height);
    expect(heights[8]).toBe("100%"); // Nov, the largest month
    expect(heights[7]).toBe("50%"); // Oct, half of it
    expect(heights[0]).toBe("0%"); // Mar, nothing earned
  });

  it("shows an empty state when nothing was earned", () => {
    render(<MonthlyIncomeChart income={{ ...sample, monthly: sample.monthly.map((m) => ({ ...m, income: 0 })) }} />);
    expect(screen.getByTestId("chart-empty")).toBeTruthy();
    expect(screen.queryAllByTestId("month-column")).toHaveLength(0);
  });
});

describe("TaxExplainer", () => {
  it("names the tax year and states that it is only an estimate", () => {
    render(<TaxExplainer income={sample} />);
    expect(screen.getByText(/how is this tax estimated/i)).toBeTruthy();
    expect(screen.getByText(/2026\/27 tax year/)).toBeTruthy();
    expect(screen.getByText(/estimate only/i)).toBeTruthy();
  });
});