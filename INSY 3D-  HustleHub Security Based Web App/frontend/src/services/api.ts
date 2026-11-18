import type { Gig, GigInput } from "../models/gig.ts";
import { normalizeIncome, type IncomeResponse } from "../lib/income.ts";
import type {
  Booking,
  BookingConfirmation,
  Income,
  Transaction,
} from "../models/booking.ts";

import type { ActiveSession } from "../models/user.ts";

import type {
  AuthResponse,
  LoginCredentials,
  LoginResult,
  RegisterCredentials,
  TwoFactorSetup,
  User,
} from "../models/user.ts";

import type {
  ActivityEvent,
  SessionChangeResult,
} from "../models/user.ts";


//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
const API_URL =
  (import.meta.env.VITE_API_URL as string | undefined) ??
  "https://localhost:3000/api";
const TOKEN_KEY = "auth_token";

// ---------- token helpers (localStorage) ----------
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
export function getAuthToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY);
  } catch {
    return null;
  }
}
let onTokenChanged: ((token: string) => void) | null = null;

export function setTokenChangedHandler(
  handler: ((token: string) => void) | null,
): void {
  onTokenChanged = handler;
}
export function setAuthToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
  onTokenChanged?.(token);
}
export function removeAuthToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export const getAccountSessions = () =>
  request<ActiveSession[]>("/account/sessions");

export const logoutCurrentSession = () =>
  request<void>("/account/logout", {
    method: "POST",
  });

// The JWT only carries id + role, so the display fields (username, email) are
// cached next to it to restore the session after a page reload. Neither is secret.
const USER_KEY = "auth_user";
export function getStoredUser(): User | null {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? (JSON.parse(raw) as User) : null;
  } catch {
    return null;
  }
}
export function setStoredUser(user: User): void {
  localStorage.setItem(USER_KEY, JSON.stringify(user));
}
export function removeStoredUser(): void {
  localStorage.removeItem(USER_KEY);
}

// true if the token is missing, malformed or past its `exp` claim.
// This is only a convenience check - the server still verifies every request.
export function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(
      atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")),
    ) as { exp?: number };
    return typeof payload.exp === "number" && payload.exp * 1000 <= Date.now();
  } catch {
    return true;
  }
}

// ---------- errors ----------
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
export interface FieldError {
  field: string;
  message: string;
}

// The only error type the UI ever sees. It carries a safe, friendly message and
// per-field messages - never a raw response, stack trace or token.
export class ApiError extends Error {
  status: number;
  fieldErrors: FieldError[];
  constructor(message: string, status = 0, fieldErrors: FieldError[] = []) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldErrors = fieldErrors;
  }
}

// turns an unknown thrown value into text that is safe to show to the user
export function getErrorMessage(err: unknown): string {
  if (err instanceof ApiError) return err.message;
  return "Something went wrong. Please try again.";
}

// field -> message lookup for form display
export function fieldErrorMap(err: unknown): Record<string, string> {
  const map: Record<string, string> = {};
  if (err instanceof ApiError) {
    for (const e of err.fieldErrors) {
      if (!map[e.field]) map[e.field] = e.message;
    }
  }
  return map;
}

// Called when any protected request comes back 401, so the app can log the user
// out. AuthProvider registers its logout here.
let onUnauthorized: (() => void) | null = null;
export function setUnauthorizedHandler(handler: (() => void) | null): void {
  onUnauthorized = handler;
}

// ---------- request plumbing ----------
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
function getHeaders(): HeadersInit {
  const token = getAuthToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
  };
  if (token) {
    // FIX: the Library sent "Bearer: <token>" (with a colon) which the backend rejects
    headers["Authorization"] = `Bearer ${token}`;
  }
  return headers;
}

interface ErrorBody {
  message?: unknown;
  errors?: unknown;
}
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
async function handleResponse<T>(
  response: Response,
  { authRequest = false }: { authRequest?: boolean } = {},
): Promise<T> {
  if (response.ok) {
    if (response.status === 204) return undefined as T;
    return (await response.json()) as T;
  }

  let body: ErrorBody = {};
  try {
    body = (await response.json()) as ErrorBody;
  } catch {
    // body was not JSON - fall back to the generic messages below
  }

  let message =
    typeof body.message === "string" && response.status < 500
      ? body.message
      : "Something went wrong on our side. Please try again.";

  // field-level validation errors: [{ field, message }]
  //Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
  //OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
  //IIE, 2026
  const fieldErrors: FieldError[] = Array.isArray(body.errors)
    ? (body.errors as unknown[]).flatMap((e) => {
        const item = e as Partial<FieldError>;
        return typeof item?.field === "string" &&
          typeof item?.message === "string"
          ? [{ field: item.field, message: item.message }]
          : [];
      })
    : [];

  if (response.status === 429) {
    message = "Too many requests. Please wait a moment and try again.";
  }
  if (response.status === 401 && !authRequest) {
    // token expired / invalid: drop the session
    removeAuthToken();
    removeStoredUser();
    onUnauthorized?.();
    message = "Your session has expired. Please log in again.";
  }
  throw new ApiError(message, response.status, fieldErrors);
}

async function request<T>(
  path: string,
  options: { method?: string; body?: unknown; authRequest?: boolean } = {},
): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method: options.method ?? "GET",
      headers: getHeaders(),
      body: options.body === undefined ? undefined : JSON.stringify(options.body),
    });
  } catch {
    throw new ApiError("Cannot reach the server. Check your connection and try again.");
  }
  return handleResponse<T>(response, { authRequest: options.authRequest });
}

// ---------- auth ----------
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
export const loginUser = (credentials: LoginCredentials) =>
  request<LoginResult>("/auth/login", {
    method: "POST",
    body: credentials,
    authRequest: true,
  });

export const registerUser = (credentials: RegisterCredentials) =>
  request<AuthResponse>("/auth/register", {
    method: "POST",
    body: credentials,
    authRequest: true,
  });

// ---------- two-factor authentication ----------
// authRequest is true where a 401 only means a wrong code or password, so the
// user is not logged out by mistake (OWASP, 2026 e)
//Mozilla, 2026 a; OWASP, 2026 e
//IIE, 2026
export const verifyTwoFactorLogin = (tempToken: string, code: string) =>
  request<AuthResponse>("/auth/2fa/login", {
    method: "POST",
    body: { tempToken, code },
    authRequest: true,
  });

export const getTwoFactorStatus = () =>
  request<{ enabled: boolean }>("/auth/2fa/status");

export const setupTwoFactor = () =>
  request<TwoFactorSetup>("/auth/2fa/setup", { method: "POST", body: {} });

export const enableTwoFactor = (code: string) =>
  request<{ recoveryCodes: string[] }>("/auth/2fa/enable", {
    method: "POST",
    body: { code },
    authRequest: true,
  });

export const disableTwoFactor = (password: string, code: string) =>
  request<{ message?: string }>("/auth/2fa/disable", {
    method: "POST",
    body: { password, code },
    authRequest: true,
  });

  // ---------- account security ----------

export async function changePassword(
  currentPassword: string,
  newPassword: string,
  code?: string,
): Promise<SessionChangeResult> {
  const result = await request<SessionChangeResult>("/account/password", {
    method: "POST",
    body: {
      currentPassword,
      newPassword,
      ...(code ? { code } : {}),
    },
    // A 401 can mean the entered password/code was incorrect.
    authRequest: true,
  });

  

  // Save the replacement before the component refreshes account data.
  setAuthToken(result.token);

  return result;
}

export async function logoutAllDevices(
  currentPassword: string,
): Promise<SessionChangeResult> {
  const result = await request<SessionChangeResult>("/account/logout-all", {
    method: "POST",
    body: { currentPassword },
    authRequest: true,
  });

  setAuthToken(result.token);

  return result;
}

export const getAccountActivity = () =>
  request<ActivityEvent[]>("/account/activity");


// ---------- gigs ----------
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
export const getGigs = () => request<Gig[]>("/gigs");
export const getGig = (id: string) => request<Gig>(`/gigs/${encodeURIComponent(id)}`);
export const getMyGigs = () => request<Gig[]>("/gigs/mine");

export const createGig = (gig: GigInput) =>
  request<Gig>("/gigs", { method: "POST", body: gig });

export const updateGig = (id: string, gig: Partial<GigInput>) =>
  request<Gig>(`/gigs/${encodeURIComponent(id)}`, { method: "PATCH", body: gig });

export const replaceGig = (id: string, gig: GigInput) =>
  request<Gig>(`/gigs/${encodeURIComponent(id)}`, { method: "PUT", body: gig });

export const deleteGig = (id: string) =>
  request<{ message?: string }>(`/gigs/${encodeURIComponent(id)}`, {
    method: "DELETE",
  });


// ---------- bookings ----------
// Only the gig id is sent. The server takes the client from the token and the
// price from the gig, so neither can be tampered with from the browser.
// The server may reply with the booking alone or { booking, transaction }.
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
export async function createBooking(gigId: string): Promise<BookingConfirmation> {
  const data = await request<Booking | BookingConfirmation>("/bookings", {
    method: "POST",
    body: { gig: gigId },
  });
  if (data && typeof data === "object" && "booking" in data) {
    return data as BookingConfirmation;
  }
  return { booking: data as Booking };
}
export const getMyBookings = () => request<Booking[]>("/bookings/mine");

export const getReceivedBookings = () => request<Booking[]>("/bookings/received");

// ---------- transactions & income ----------
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
export const getMyTransactions = () => request<Transaction[]>("/transactions/mine");

export async function getIncome(): Promise<Income> {
  const data = await request<IncomeResponse>("/income");
  return normalizeIncome(data);
}

// ---------- admin ----------
//Mozilla, 2026 a; Mozilla, 2026 b; Mozilla, 2026 c; Mozilla, 2026 d
//OWASP, 2026 a; OWASP, 2026 b;OWASP, 2026 c; OWASP, 2026 d
//IIE, 2026
export const adminGetUsers = () => request<User[]>("/admin/users");

export const adminGetBookings = () => request<Booking[]>("/admin/bookings");

export const adminGetTransactions = () => request<Transaction[]>("/admin/transactions");

export const adminDeleteGig = (id: string) =>
  request<{ message?: string }>(`/admin/gigs/${encodeURIComponent(id)}`, {
    method: "DELETE",
  });
/* Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.

Mozilla, 2026 a. HTTP response status codes (401, 429). [online] Available at: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Status [Accessed 12 October 2026].

Mozilla, 2026 b. Window: localStorage property. [online] Available at: https://developer.mozilla.org/en-US/docs/Web/API/Window/localStorage [Accessed 12 October 2026].

Mozilla, 2026 c. Authorization header (Bearer scheme). [online] Available at: https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Authorization [Accessed 12 October 2026].

Mozilla, 2026 d. Content-Security-Policy (CSP). [online] Available at: https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/CSP [Accessed 12 October 2026].

OWASP, 2026 a. Input Validation Cheat Sheet. [online] Available at: https://cheatsheetseries.owasp.org/cheatsheets/Input_Validation_Cheat_Sheet.html [Accessed 12 October 2026].

OWASP, 2026 b. Cross Site Scripting Prevention Cheat Sheet. [online] Available at: https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html [Accessed 12 October 2026].

OWASP, 2026 c. HTML5 Security Cheat Sheet (storing tokens in localStorage). [online] Available at: https://cheatsheetseries.owasp.org/cheatsheets/HTML5_Security_Cheat_Sheet.html [Accessed 12 October 2026].

OWASP, 2026 d. JSON Web Token for Java Cheat Sheet. [online] Available at: https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html [Accessed 12 October 2026].

OWASP, 2026 e. Multifactor Authentication Cheat Sheet. [online] Available at: https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html [Accessed 23 October 2026].

React, 2026. Writing markup with JSX (React escapes output; dangerouslySetInnerHTML is avoided). [online] Available at: https://react.dev/learn/writing-markup-with-jsx [Accessed 12 October 2026].

React Router, 2026. Documentation. [online] Available at: https://reactrouter.com/ [Accessed 12 October 2026].

Vite, 2026. Features and Env Variables and Modes. [online] Available at: https://vite.dev/guide/env-and-mode [Accessed 12 October 2026].*/