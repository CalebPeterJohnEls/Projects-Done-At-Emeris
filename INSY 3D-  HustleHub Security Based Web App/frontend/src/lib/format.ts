import type { Ref } from "../models/booking.ts";

//currency formating for payment/transactions
export const formatMoney = (n: number | undefined): string =>
  new Intl.NumberFormat("en-ZA", { style: "currency", currency: "ZAR" }).format(
    n ?? 0,
  );

// whole rands, for bracket limits like R245 100
export const formatMoneyWhole = (n: number | undefined): string =>
  new Intl.NumberFormat("en-ZA", {
    style: "currency",
    currency: "ZAR",
    maximumFractionDigits: 0,
  }).format(n ?? 0);

// 13.52 -> "13.52%"
export const formatPercent = (n: number | undefined): string =>
  `${(n ?? 0).toFixed(2)}%`;

// fixed names, so the label does not depend on the browser's locale data
const MONTH_NAMES = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

// "2026-03" -> "Mar"
export const formatMonth = (yyyyMm: string): string => {
  const index = Number(yyyyMm.slice(5, 7)) - 1;
  return MONTH_NAMES[index] ?? yyyyMm;
};

  //date formating
export const formatDate = (iso?: string): string =>
  iso ? new Date(iso).toLocaleString("en-ZA") : "";

// related documents can be an id string or a populated object
export function refId<T extends { _id: string }>(ref: Ref<T> | undefined): string {
  if (!ref) return "";
  return typeof ref === "string" ? ref : ref._id;
}

export function refLabel(
  ref: Ref<{ _id: string; username?: string; title?: string }> | undefined,
): string {
  if (!ref) return "Unknown";
  if (typeof ref === "string") return `#${ref.slice(-6)}`;
  return ref.username ?? ref.title ?? `#${ref._id.slice(-6)}`;
}