import type { Income } from "../models/booking.ts";

// What the dashboard shows before the first response arrives (and for a
// freelancer with no confirmed bookings).
export const EMPTY_INCOME: Income = {
  taxYear: "",
  total: 0,
  count: 0,
  taxBeforeRebate: 0,
  rebate: 0,
  estimatedTax: 0,
  netIncome: 0,
  effectiveRate: 0,
  marginalRate: 0,
  belowThreshold: true,
  brackets: [],
  monthly: [],
};

// The server may send the full object, an older { totalIncome, transactionCount }
// shape, or a bare number. Everything is turned into one complete Income so
// components never have to check for missing fields.
export type IncomeResponse =
  | number
  | (Partial<Income> & { totalIncome?: number; transactionCount?: number });

export function normalizeIncome(data: IncomeResponse | null | undefined): Income {
  if (typeof data === "number") return { ...EMPTY_INCOME, total: data, netIncome: data };
  if (!data || typeof data !== "object") return { ...EMPTY_INCOME };

  const total = data.total ?? data.totalIncome ?? 0;
  return {
    taxYear: data.taxYear ?? "",
    total,
    count: data.count ?? data.transactionCount ?? 0,
    taxBeforeRebate: data.taxBeforeRebate ?? 0,
    rebate: data.rebate ?? 0,
    estimatedTax: data.estimatedTax ?? 0,
    netIncome: data.netIncome ?? total,
    effectiveRate: data.effectiveRate ?? 0,
    marginalRate: data.marginalRate ?? 0,
    belowThreshold: data.belowThreshold ?? true,
    brackets: Array.isArray(data.brackets) ? data.brackets : [],
    monthly: Array.isArray(data.monthly) ? data.monthly : [],
  };
}