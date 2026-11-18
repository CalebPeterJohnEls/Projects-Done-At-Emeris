import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import {
  ApiError,
  createBooking,
  getMyGigs,
  loginUser,
  setAuthToken,
  setUnauthorizedHandler,
} from "../services/api.ts";
//jsdom, 2026
const jsonResponse = (status: number, body: unknown) =>
  new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
//Testing Library, 2026 a; Testing Library, 2026 b; Testing Library, 2026 c
describe("api service", () => {
  const fetchMock = vi.fn();
  beforeEach(() => {
    fetchMock.mockReset();
    vi.stubGlobal("fetch", fetchMock);
  });
  afterEach(() => {
    vi.unstubAllGlobals();
    setUnauthorizedHandler(null);
  });
//Testing Library, 2026 a; Testing Library, 2026 b; Testing Library, 2026 c
  it("sends 'Bearer <token>' with NO colon", async () => {
    setAuthToken("abc.def.ghi");
    fetchMock.mockResolvedValue(jsonResponse(200, []));
    await getMyGigs();
    const headers = fetchMock.mock.calls[0][1].headers as Record<string, string>;
    expect(headers.Authorization).toBe("Bearer abc.def.ghi");
  });
//Testing Library, 2026 a; Testing Library, 2026 b; Testing Library, 2026 c
  it("only sends the gig id when booking (no client or price)", async () => {
    fetchMock.mockResolvedValue(jsonResponse(201, { _id: "b1", status: "confirmed" }));
    const result = await createBooking("g1");
    expect(JSON.parse(fetchMock.mock.calls[0][1].body)).toEqual({ gig: "g1" });
    expect(result.booking._id).toBe("b1");
  });
//Testing Library, 2026 a; Testing Library, 2026 b; Testing Library, 2026 c
  it("passes field-level validation errors through", async () => {
    fetchMock.mockResolvedValue(
      jsonResponse(400, {
        message: "Invalid input supplied",
        errors: [{ field: "email", message: "A valid email address is required" }],
      }),
    );
    await expect(loginUser({ email: "x", password: "y" })).rejects.toMatchObject({
      status: 400,
      fieldErrors: [{ field: "email", message: "A valid email address is required" }],
    });
  });
//Testing Library, 2026 a; Testing Library, 2026 b; Testing Library, 2026 c
  it("logs out on 401 for protected requests", async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    setAuthToken("expired");
    fetchMock.mockResolvedValue(jsonResponse(401, { message: "Unauthorized" }));
    await expect(getMyGigs()).rejects.toBeInstanceOf(ApiError);
    expect(handler).toHaveBeenCalled();
    expect(localStorage.getItem("auth_token")).toBeNull();
  });
//Testing Library, 2026 a; Testing Library, 2026 b; Testing Library, 2026 c
  it("does not log out when a login attempt itself returns 401", async () => {
    const handler = vi.fn();
    setUnauthorizedHandler(handler);
    fetchMock.mockResolvedValue(jsonResponse(401, { message: "Invalid email or password" }));
    await expect(loginUser({ email: "a@b.co", password: "x" })).rejects.toThrow(
      "Invalid email or password",
    );
    expect(handler).not.toHaveBeenCalled();
  });
//Testing Library, 2026 a; Testing Library, 2026 b; Testing Library, 2026 c
  it("shows a friendly message on 429", async () => {
    fetchMock.mockResolvedValue(jsonResponse(429, { message: "raw limiter text" }));
    await expect(getMyGigs()).rejects.toThrow(/too many requests/i);
  });
//Testing Library, 2026 a; Testing Library, 2026 b; Testing Library, 2026 c
  it("hides server error details on 500", async () => {
    fetchMock.mockResolvedValue(jsonResponse(500, { message: "stack trace at /app/x.js" }));
    await expect(getMyGigs()).rejects.not.toThrow(/stack trace/);
  });
});

/* Reference List
jsdom, 2026. jsdom: a JavaScript implementation of the WHATWG DOM and HTML standards. [online] Available at: https://github.com/jsdom/jsdom [Accessed 12 October 2026].
Testing Library, 2026 a. React Testing Library. [online] Available at: https://testing-library.com/docs/react-testing-library/intro/ [Accessed 12 October 2026].
Testing Library, 2026 b. user-event. [online] Available at: https://testing-library.com/docs/user-event/intro [Accessed 12 October 2026].
Testing Library, 2026 c. jest-dom. [online] Available at: https://github.com/testing-library/jest-dom [Accessed 12 October 2026].
Vitest, 2026 a. Getting started. [online] Available at: https://vitest.dev/guide/ [Accessed 12 October 2026].
Vitest, 2026 b. Configuring Vitest. [online] Available at: https://vitest.dev/config/ [Accessed 12 October 2026].
Vitest, 2026 c. Mocking. [online] Available at: https://vitest.dev/guide/mocking [Accessed 12 October 2026].*/