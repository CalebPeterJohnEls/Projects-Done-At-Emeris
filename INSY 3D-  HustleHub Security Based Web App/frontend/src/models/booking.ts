import type { Gig } from "./gig.ts";

export type BookingStatus = "pending" | "confirmed" | "completed" | "cancelled";

// Related documents may arrive as a plain id or populated, depending on the endpoint.
//(IIE), 2026
export interface UserRef {
  _id: string;
  username: string;
  email?: string;
}
export type Ref<T extends { _id: string }> = string | T;

export interface Booking {
  _id: string;
  client: Ref<UserRef>;
  freelancer: Ref<UserRef>;
  gig: Ref<Pick<Gig, "_id" | "title"> & Partial<Gig>>;
  // price captured from the gig on the server at booking time
  price?: number;
  amount?: number;
  status: BookingStatus | string;
  createdAt?: string;
}

// body for POST /api/bookings - the client and price are NEVER sent; the server decides them
//(IIE), 2026
export interface BookingInput {
  gig: string;
}

export interface Transaction {
  _id: string;
  booking: string | { _id: string };
  client: Ref<UserRef>;
  freelancer: Ref<UserRef>;
  gig: Ref<Pick<Gig, "_id" | "title"> & Partial<Gig>>;
  amount: number;
  status: string;
  reference: string;
  createdAt?: string;
}

// what the booking endpoint gives back - used for the simulated confirmation screen
//(IIE), 2026
export interface BookingConfirmation {
  booking: Booking;
  transaction?: Transaction;
}

// One row of the SARS bracket breakdown. `to` is null for the top bracket.
export interface TaxBracketRow {
  from: number;
  to: number | null;
  rate: number; // percentage, e.g. 26
  amountInBracket: number;
  taxInBracket: number;
}

// One month of the tax year, month is "YYYY-MM"
export interface MonthlyIncome {
  month: string;
  income: number;
  bookings: number;
}

// GET /api/income: confirmed-booking income for the current tax year plus a
// SARS tax estimate. total/count keep their original names.
export interface Income {
  taxYear: string;
  total: number;
  count: number;
  taxBeforeRebate: number;
  rebate: number;
  estimatedTax: number;
  netIncome: number;
  effectiveRate: number; // percentage
  marginalRate: number; // percentage
  belowThreshold: boolean;
  brackets: TaxBracketRow[];
  monthly: MonthlyIncome[];
}
/* Reference List 
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.*/