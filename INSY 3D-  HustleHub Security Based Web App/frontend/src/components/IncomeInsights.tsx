import React from "react";
import type { Income } from "../models/booking.ts";
import { formatMoney, formatMoneyWhole, formatMonth, formatPercent } from "../lib/format.ts";

const box: React.CSSProperties = {
  border: "1px solid rgba(128, 128, 128, 0.35)",
  borderRadius: 8,
  padding: "12px 14px",
};

const Stat: React.FC<{ id: string; label: string; value: string; note?: string }> = ({ id, label, value, note }) => (
  <div style={box} data-testid={id}>
    <div className='stats-label'>{label}</div>
    <div className='stats-num stats-small'>{value}</div>
    {note && <div className='muted'>{note}</div>}
  </div>
);

// gross, tax, net and effective rate for the current tax year
export const TaxSummaryCards: React.FC<{ income: Income }> = ({ income }) => (
  <div>
    <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(170px, 1fr))", gap: 12 }}>
      <Stat
        id="card-gross"
        label="Gross income"
        value={formatMoney(income.total)}
        note={`${income.count} confirmed booking${income.count === 1 ? "" : "s"}`}
      />
      <Stat id='card-tax' label='Estimated tax' value={formatMoney(income.estimatedTax)} />
      <Stat
        id="card-net"
        label="Net income"
        value={formatMoney(income.netIncome)}
      />
      <Stat id='card-rate' label='Effective tax rate' value={formatPercent(income.effectiveRate)} note={income.estimatedTax > 0 ? `Marginal rate ${formatPercent(income.marginalRate)}` : undefined} />
    </div>
    {income.belowThreshold && (
      <p className="muted" data-testid="below-threshold">
        No tax is estimated on the income recorded for this tax year because
        the primary rebate covers the calculated tax. This may change as
        more income is recorded.
      </p>
    )}
  </div>
);

// how the gross income falls across the seven SARS brackets
export const BracketBreakdown: React.FC<{ income: Income }> = ({ income }) => {
  if (income.brackets.length === 0) return <p className='muted'>No bracket data available yet.</p>;
  return (
    <div style={{ overflowX: "auto" }}>
      <table style={{ width: "100%", borderCollapse: "collapse" }}>
        <thead>
          <tr style={{ textAlign: "left" }}>
            <th scope='col'>Taxable income</th>
            <th scope='col'>Rate</th>
            <th scope='col'>Income in bracket</th>
            <th scope='col'>Tax</th>
          </tr>
        </thead>
        <tbody>
          {income.brackets.map((b) => (
            <tr key={b.from} data-testid='bracket-row' style={{ opacity: b.amountInBracket === 0 ? 0.5 : 1 }}>
              <td>{b.to === null ? `Above ${formatMoneyWhole(b.from)}` : `${formatMoneyWhole(b.from)} – ${formatMoneyWhole(b.to)}`}</td>
              <td>{b.rate}%</td>
              <td>{formatMoney(b.amountInBracket)}</td>
              <td>{formatMoney(b.taxInBracket)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr>
            <td colSpan={3}>Tax before rebate</td>
            <td data-testid='tax-before-rebate'>{formatMoney(income.taxBeforeRebate)}</td>
          </tr>
          <tr>
            <td colSpan={3}>Less primary rebate applied</td>
            <td data-testid="rebate">
              −{formatMoney(Math.min(income.rebate, income.taxBeforeRebate))}
            </td>
          </tr>
          <tr style={{ fontWeight: 700 }}>
            <td colSpan={3}>Estimated tax</td>
            <td data-testid='estimated-tax-total'>{formatMoney(income.estimatedTax)}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  );
};

// one bar per month of the tax year (March to February)
export const MonthlyIncomeChart: React.FC<{ income: Income }> = ({ income }) => {
  const max = Math.max(0, ...income.monthly.map((m) => m.income));
  if (max === 0) return <p className='muted' data-testid='chart-empty'>No income recorded yet this tax year.</p>;

  return (
    <div style={{ overflowX: "auto" }}>
      <div
        role='list'
        aria-label={`Monthly income, ${income.taxYear} tax year`}
        style={{ display: "flex", alignItems: "flex-end", gap: 6, height: 190, minWidth: 360 }}
      >
        {income.monthly.map((m) => {
          const pct = (m.income / max) * 100;
          return (
            <div
              key={m.month}
              role='listitem'
              data-testid='month-column'
              data-month={m.month}
              data-income={m.income}
              title={`${formatMonth(m.month)}: ${formatMoney(m.income)} (${m.bookings} booking${m.bookings === 1 ? "" : "s"})`}
              style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "flex-end", height: "100%", textAlign: "center" }}
            >
              <div
                data-testid='month-bar'
                style={{ height: `${m.income > 0 ? Math.max(pct, 2) : 0}%`, background: "#0d9448", borderRadius: "4px 4px 0 0" }}
              />
              <div className='muted' style={{ fontSize: 12, marginTop: 4 }}>{formatMonth(m.month)}</div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

// short plain-language explanation of the estimate
export const TaxExplainer: React.FC<{ income: Income }> = ({ income }) => (
  <details>
    <summary>How is this tax estimated?</summary>
    <ol>
      <li>
        We add up your confirmed bookings for the {income.taxYear || "current"} tax year, which runs from
        1 March to the end of February. That is your gross income.
      </li>
      <li>
        SARS taxes income in slices. Each slice is taxed at its own rate, from 18% up to 45%, and the
        slices are added together. The table above shows each one.
      </li>
      <li>
        The primary annual rebate is {formatMoney(income.rebate)}.
        It reduces the calculated tax, but cannot reduce this estimate below
        zero. For your recorded income, the rebate applied is{" "}
        {formatMoney(Math.min(income.rebate, income.taxBeforeRebate))}.
      </li>
      <li>Net income is your gross income minus the estimated tax.</li>
    </ol>
    <p className='muted'>
      This is an estimate only. It assumes you are under 65 and have no deductions or other income, so
      your actual SARS assessment may differ. It is not tax advice.
    </p>
  </details>
);