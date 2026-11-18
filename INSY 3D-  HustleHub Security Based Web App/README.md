# HustleHub+ — Freelance Marketplace

A full-stack (MERN) marketplace where **Clients** book gigs, **Freelancers** list and deliver them, and **Admins** oversee the platform.

- **Backend:** Node.js, Express 5, MongoDB (Mongoose), JWT, bcrypt, TOTP two-factor authentication, served over HTTPS
- **Frontend:** React 19 + TypeScript + Vite, served over HTTPS in development (nginx in Docker)
- **Deployment:** Docker Compose (separate frontend and backend containers)
- **CI/CD:** GitHub Actions (API tests, frontend tests, dependency audit, static analysis, Docker smoke test, Docker Hub publish)

**Authors:** Caleb Peter-John Els (ST10306162), Joshua Kyle Meyer (ST10289059), Olwethu Ruselo (ST10365670)

---

## Table of Contents

1. [What's New](#whats-new)
2. [Features](#features)
3. [Project Structure](#project-structure)
4. [API Routes](#api-routes)
5. [Security Measures](#security-measures)
6. [Logging](#logging)
7. [New Security Features](#new-security-features)
8. [Setup](#setup)
9. [Running the Apps](#running-the-apps)
10. [Docker Setup](#docker-setup)
11. [Testing](#testing)
12. [Static Code Analysis](#static-code-analysis)
13. [Final Security Review](#final-security-review)
14. [Known Limitations](#known-limitations)
15. [References](#references)

---

## What's New

The original submission was a backend-only API (register, login, gig CRUD). This version adds:

- **React frontend** with role-aware navigation and protected routes
- **Bookings and simulated payments**, with a transaction reference per booking
- **Freelancer income** totals with a per-gig breakdown and a tax estimate
- **Admin role** (seeded, never self-registered) with platform-wide views and gig moderation
- **Security hardening:** helmet, rate limiting, NoSQL sanitising, `hpp`, `express-validator`, centralised error handling
- **Two-factor authentication (TOTP)** with encrypted secrets and hashed recovery codes
- **Session management:** view active sessions, sign out of other devices, server-side revocation
- **Docker deployment** with a multi-stage frontend image served by nginx over HTTPS
- **CI/CD pipeline** with automated tests, a dependency audit, static code analysis and Docker Hub publishing
- **Final security review** mapping threats to protections, with residual risks listed
- **File logging** of requests and security events
- **Seed scripts** for the admin account and demo data
- **Testing:** Newman/Postman API suite, Node unit tests and Vitest frontend suite

---

## Features

| Role | What they can do |
|------|------------------|
| **Guest** | Browse and view gigs |
| **Client** | Register/log in, book gigs, view own bookings (`/bookings`) and transactions (`/transactions`) |
| **Freelancer** | Create, edit and delete own gigs, see bookings received, income and transactions (`/dashboard`, `/transactions`) |
| **Admin** | View all users, bookings and transactions; remove any gig (`/admin`) |
| **Any logged-in user** | Turn two-factor authentication on or off, change password, view active devices and sign out of other devices (`/security`) |

Notes:
- Booking takes the **client from the token** and the **price from the gig**, never from the request body.
- A gig with bookings is **deactivated, not deleted**, so history is preserved.
- A price snapshot is stored on each booking, so later price edits don't change past income.
- The frontend redirects guests to `/login`, shows "Not allowed" for the wrong role, logs out on any `401`, and shows friendly errors (no stack traces).

---

## Project Structure

```text
.
├── .github/
│   └── workflows/
│       └── main.yml                   # CI/CD pipeline (tests, audit, static analysis, Docker, publish)
│
├── backend/
│   ├── controllers/
│   │   ├── authController.js          # register / login + validation rules
│   │   ├── twoFactorController.js     # 2FA setup, enable, login, disable, status
│   │   ├── accountController.js       # password change, sessions, sign-out, activity
│   │   ├── gigController.js           # gig CRUD with ownership checks
│   │   ├── bookingController.js       # create booking + simulated payment, list bookings
│   │   ├── transactionController.js   # my transactions, freelancer income and tax estimate
│   │   ├── adminController.js         # admin-only views and gig moderation
│   │   └── homeController.js          # health check / sample routes
│   ├── middleware/
│   │   ├── authMiddleware.js          # JWT + session verification, role gating
│   │   ├── securityMiddleware.js      # helmet, CORS, rate limiters, sanitiser, hpp
│   │   ├── validationMiddleware.js    # gig / booking / id validation rules
│   │   ├── loggerMiddleware.js        # per-request logging
│   │   └── dbMiddleware.js            # MongoDB connection bootstrap
│   ├── models/                        # user, gig, booking, transaction, session, loginEvent schemas
│   ├── routes/                        # auth, account, gigs, bookings, transactions, income, admin, home
│   ├── utils/
│   │   ├── logger.js                  # file + console + Better Stack logger
│   │   ├── twoFactor.js               # secret encryption (AES-256-GCM), recovery codes
│   │   ├── sessions.js                # session creation and checks
│   │   ├── sessionTokens.js           # session-bound JWT helpers
│   │   ├── securityEvents.js          # security event recording
│   │   ├── tax.js                     # income tax estimate
│   │   ├── seedAdmin.js               # creates the first admin account
│   │   └── seedGigs.js                # demo users and sample gigs
│   ├── tests/                         # Node unit tests (account, logger, sessions, tax)
│   ├── postman/                       # Newman collections, environment, HTML reports
│   ├── logs/                          # daily log files (git-ignored)
│   ├── .env.example                   # template for backend/.env
│   ├── Dockerfile
│   ├── app.js                         # entry point: wires middleware/routes, starts HTTPS server
│   └── package.json
│
├── frontend/
│   ├── src/
│   │   ├── components/                # NavBar, ProtectedRoute, FormField, Alert, security cards,
│   │   │                              # IncomeInsights, PasswordStrengthMeter
│   │   ├── context/                   # AuthContext, AuthProvider, useAuth
│   │   ├── lib/                       # validation.ts, format.ts, income.ts
│   │   ├── models/                    # TypeScript types (user, gig, booking)
│   │   ├── pages/                     # Gigs, GigDetail, Login, Register, MyBookings,
│   │   │                              # Transactions, Dashboard, Admin, Security, NotFound
│   │   ├── services/api.ts            # all API calls, token handling, safe errors
│   │   ├── tests/                     # Vitest suites (api, app, income, incomeinsights)
│   │   └── App.tsx / main.tsx         # route table and app entry
│   ├── nginx.conf                     # HTTPS server and security headers (Docker)
│   ├── Dockerfile
│   ├── vite.config.ts                 # HTTPS dev server, CSP + security headers, Vitest config
│   └── package.json
│
├── docker-compose.yml                 # backend + frontend services on app-network
├── .dockerignore                      # keeps .env, logs and node_modules out of images
├── ca.crt / ca.key                    # local certificate authority (dev TLS only)
├── cert.crt / cert.key                # server certificate + key (dev TLS only)
├── postman/                           # copies of the Postman collections / environment
└── README.md
```

---

## API Routes

Base URL: `https://localhost:3000`

| Method | Route | Auth / Role | Purpose |
|--------|-------|-------------|---------|
| POST | `/api/auth/register` | Public | Register as client or freelancer |
| POST | `/api/auth/login` | Public | Log in, receive JWT (or a temporary token if 2FA is on) |
| POST | `/api/auth/2fa/setup` | Logged in | Generate a TOTP secret and QR code |
| POST | `/api/auth/2fa/enable` | Logged in | Confirm with a code, switch 2FA on, receive recovery codes |
| POST | `/api/auth/2fa/login` | Temporary token | Submit a TOTP or recovery code, receive JWT |
| POST | `/api/auth/2fa/disable` | Logged in | Switch 2FA off (needs password and a valid code) |
| GET | `/api/auth/2fa/status` | Logged in | Whether 2FA is on for the account |
| POST | `/api/account/password` | Logged in | Change password (ends other sessions) |
| GET | `/api/account/sessions` | Logged in | List active sessions |
| POST | `/api/account/logout` | Logged in | Revoke the current session |
| POST | `/api/account/logout-all` | Logged in | Sign out of other devices (needs password) |
| GET | `/api/account/activity` | Logged in | Recent account security activity |
| GET | `/api/gigs`, `/api/gigs/:id` | Public | List / view active gigs |
| GET | `/api/gigs/mine` | Freelancer | Own gigs (incl. paused) |
| POST | `/api/gigs` | Freelancer | Create gig |
| PUT / PATCH / DELETE | `/api/gigs/:id` | Freelancer (owner) | Replace / update / delete gig |
| POST | `/api/bookings` | Client | Book a gig (simulated payment) |
| GET | `/api/bookings/mine` | Client | Own bookings |
| GET | `/api/bookings/received` | Freelancer | Bookings on own gigs |
| GET | `/api/transactions/mine` | Client, Freelancer | Own transactions |
| GET | `/api/income` | Freelancer | Total income, per-gig breakdown and tax estimate |
| GET | `/api/admin/users`, `/bookings`, `/transactions` | Admin | Platform-wide data (no password hashes) |
| DELETE | `/api/admin/gigs/:id` | Admin | Remove any gig |
| GET | `/api/home/healthCheck` | Public | Liveness check |

---

## Security Measures

- **Password hashing:** bcrypt (cost 12) in a `pre("save")` hook; hashes are never returned by the API (GeeksforGeeks, 2026a).
- **Two-factor authentication:** optional TOTP with encrypted secrets, single-use codes, hashed recovery codes and lockout (see [New Security Features](#new-security-features)).
- **JWT:** signed with `JWT_SECRET` from `.env`, 7-day expiry, payload holds only id and role. The user is re-loaded from the database on every request, so deleted users lose access (OWASP, 2026c).
- **RBAC and ownership:** `validateRole` on every protected route, owner checks on gig changes, and "my data" endpoints filter by `req.user`.
- **Role escalation blocked:** registration only accepts `client` or `freelancer`; admin exists only via `seedAdmin.js`.
- **Validation:** `express-validator` on all inputs (password 8+ chars with a number and uppercase, gig field limits, category enum, positive price, valid Mongo ids). HTML tags are stripped from gig text. Mongoose schemas add a second layer. The frontend mirrors the rules, but the server never trusts it (DEV Community, 2024; OWASP, 2026b).
- **NoSQL injection and parameter pollution:** custom sanitiser strips `$`/`.` keys; `hpp` removes duplicate parameters (OWASP, 2026f).
- **Rate limiting (per IP, 15 min):** general 150, auth 10, booking 20, admin 60. Returns `429` with `Retry-After`. Overridable via env variables.
- **Headers, CORS and CSP:** helmet (CSP, HSTS, frameguard, noSniff, etc.); CORS only allows the origins in `CORS_ORIGINS`; the frontend injects its own CSP via `vite.config.ts` (OWASP, 2026e).
- **HTTPS:** both servers use the local certificate chain (`ca.crt`, `cert.crt`, `cert.key`) so passwords and tokens are encrypted in transit (GeeksforGeeks, 2025).
- **Safe errors and logging:** a central error handler returns generic messages; login failures are indistinguishable; daily log files record requests, auth failures, rate-limit hits and blocked injections, with no passwords or tokens and with log-injection protection.
- **Fail-fast config:** the server won't start without `JWT_SECRET` and `CONN_STRING`; the body size limit is 10 kb.
- **Static code analysis in CI:** Semgrep scans the source on every push and blocks the pipeline on any finding (see [Static Code Analysis](#static-code-analysis)).

A full threat-by-threat breakdown is in the [Final Security Review](#final-security-review).

---

## Logging

### Purpose

Logging gives the team a permanent record of what the API did, who triggered it and whether it worked. It is used to spot attacks (repeated failed logins, forbidden requests), investigate faults (every server error is recorded with its stack trace) and keep an audit trail of money-related actions (bookings, transactions and income estimates). Everything goes through one module, `backend/utils/logger.js`, in three places at once: the terminal, daily files in `backend/logs/` (`YYYY-MM-DD-combined.log`, plus `YYYY-MM-DD-error.log` for errors) and, when configured, Better Stack in the cloud (OWASP, 2026e; Better Stack, 2026).

Each entry has a time, a level (`INFO`, `WARN` or `ERROR`) and a context that says where it came from. The cloud copy is structured JSON (`dt`, `level`, `message`, `context`, plus `meta` or `error`), so it can be searched and filtered by field.

### What is logged and why

| Context | What is logged | Why |
|---|---|---|
| `HTTP` | Every request: method, URL, status, duration, IP and the user's username and role. Level depends on the status (2xx/3xx `INFO`, 4xx `WARN`, 5xx `ERROR`). | Shows overall traffic and highlights blocked, rate-limited and failing requests, because the logger is the first middleware. |
| `AUTH` | Registration, login success, failed login (unknown email or wrong password), password accepted with 2FA required, 2FA setup, enable, disable and rejected codes. | Repeated failures reveal brute-force and credential-stuffing attempts, and a trail of successful logins shows whose account was used (OWASP, 2026a). |
| `SECURITY` | 2FA lock-outs, rejected 2FA disable attempts, and forbidden attempts to modify or delete another user's gig. | These are the strongest signs of someone attempting to bypass access control (OWASP, 2021). |
| `VALIDATION` | A rejected request: method, URL and IP only. | Many validation failures from one IP can mean probing or injection attempts. |
| `BOOKING` | Booking attempts, rejections (missing or inactive gig), the booking created, confirmed, and any rollback. | Follows a booking from start to finish, so a failed or half-completed booking can be traced and checked. |
| `TRANSACTION` | The transaction recorded for a booking (amount, booking, client, freelancer), transaction views, and income views with the gross income, estimated tax and net income for the tax year. | Financial actions need an audit trail so amounts can be reconciled and disputes investigated. |
| `GIG` and `ADMIN` | Gigs created, updated, replaced and deleted or deactivated, including admin moderation. | Records who changed or removed marketplace content, especially privileged admin actions. |
| `ERROR` level (any context) | The message and the full stack trace for every caught or unhandled error. | Developers need the detail to fix faults, while the client only receives a generic message, so nothing sensitive leaks in a response (OWASP, 2026e). |

**Never logged:** passwords, password hashes, JWTs, 2FA secrets and codes, recovery codes, request bodies and email addresses. Users are identified by their database id and, where useful, role. Line breaks are stripped from every message, so a request cannot inject fake log lines (log injection) (OWASP, 2026e).

### Cloud logging and reliability

- **Better Stack transport:** entries are queued in memory and sent over HTTPS in batches (every 5 seconds or 50 entries) with the source token from the environment, never hard coded.
- **Failure path:** each send has a 5 second timeout. A failed batch is kept and retried with exponential back-off (2s up to 60s), the queue is capped at 1000 entries (oldest dropped first) and warnings are printed at most once a minute. Every step is wrapped so a logging problem can never crash the app or slow a request (Mozilla, 2026).
- **Shutdown:** on `SIGTERM` or `SIGINT` the logger makes a last bounded attempt to send what is queued, then lets the process exit.
- **Off by default:** without `BETTERSTACK_SOURCE_TOKEN` and `BETTERSTACK_INGEST_HOST`, only the terminal and the `logs/` files are used. See [Setup](#setup).
- **Tested:** `backend/tests/logger.test.js` checks the transport against a fake server (see [Testing](#testing)).

### Why logging matters

- **Detection and response:** without logs, a brute-force attack, a stolen token or an access-control bypass leaves no evidence, and OWASP lists insufficient logging and monitoring as a security failure in its own right (OWASP, 2021).
- **Accountability:** the audit trail ties bookings, payments and admin actions to an account, which supports investigation and non-repudiation.
- **Debugging:** stack traces and request context let faults be found and reproduced quickly instead of guessing.
- **Resilience:** the cloud copy survives the container being restarted or the disk being lost, and it can be searched and alerted on, which local files cannot do (Better Stack, 2026).
- **Safe by design:** because logs are a common place for leaks, keeping secrets and personal data out of them is part of the design (OWASP, 2026e).

### Logging Results
<img width="852" height="333" alt="Screenshot 2026-11-09 230216" src="https://github.com/user-attachments/assets/8cad99e8-1705-462d-95c4-dabc5acb77f0" /> 

---
<img width="1574" height="747" alt="WhatsApp Image 2026-11-09 at 23 20 23" src="https://github.com/user-attachments/assets/bb25e507-4679-4f72-ae6b-66ff5851b575" />


---

## New Security Features

### 1. Two-factor authentication (TOTP)

**What it is.** Users can switch on a second login step. After entering the correct password, the user must also enter a 6-digit code from an authenticator app on their phone (connected by scanning a QR code), or a one-time recovery code. The code is a time-based one-time password (TOTP) that changes every 30 seconds (otplib, 2026).

**Purpose.** A stolen, guessed or reused password alone no longer lets an attacker in. OWASP describes multi-factor authentication as "by far the best defense against the majority of password-related attacks" (OWASP, 2026n) and recommends authenticator-app TOTP with QR enrolment and single-use recovery codes (OWASP, 2026g).

**How it works.**

- **Enrol** (logged in, on the `/security` page): `POST /api/auth/2fa/setup` generates a secret and returns a QR code. The user scans it and confirms with a code via `POST /api/auth/2fa/enable`. Eight recovery codes are shown once.
- **Log in:** if 2FA is on, `POST /api/auth/login` returns `twoFactorRequired` and a 5-minute temporary token instead of a session token. The code is then sent to `POST /api/auth/2fa/login`, which returns the normal session token.
- **Turn off:** `POST /api/auth/2fa/disable` needs the password and a valid code.

**Security controls built into the feature.**

| Control | Implementation | Why it matters |
|---------|----------------|----------------|
| Secret encrypted at rest | AES-256-GCM with a random IV per encryption and an enforced full 16-byte authentication tag, key in `TWOFA_ENC_KEY` (`utils/twoFactor.js`); the field is `select: false`, so it is never loaded by default | A database leak alone does not reveal the second factor. NIST requires authenticator keys to be "strongly protected against unauthorized disclosure" (Temoshok et al., 2025), and OWASP recommends AES-256 in an authenticated mode such as GCM, with keys kept apart from the data (OWASP, 2026q) |
| Each code works once | The accepted time step is stored, and a code from the same or an earlier step is rejected (`checkTotp`) | NIST: verifiers must accept a given OTP "only once while it is valid" (Temoshok et al., 2025) |
| Narrow time window | otplib `epochTolerance`: 30 seconds | Allows for clock drift without a wide guessing window (otplib, 2026) |
| Attempt lockout | 5 wrong codes lock the second factor for 15 minutes, on top of the 10-per-15-minute `/api/auth` rate limit | Stops brute-forcing 6-digit codes. NIST caps consecutive failures at 100 and allows stricter limits (Temoshok et al., 2025) |
| Hashed one-time recovery codes | 8 random codes, bcrypt-hashed, deleted when used | A database leak does not reveal usable codes. NIST requires look-up secrets to be single-use and, below the 112-bit minimum, salted and hashed (Temoshok et al., 2025). Ours are 40 bits, so hashing is essential |
| Temporary login token | Signed with a different secret (`JWT_SECRET:2fa`), expires in 5 minutes, held in memory only in the frontend | Passing the password step never yields a usable session token |
| Safe error messages | Wrong code, bad password and expired attempt return generic messages | Nothing useful for an attacker |
| Logging | Enrolment, success, rejection, lockout and disable events are logged without codes or secrets | Supports monitoring (OWASP, 2026l) |

**Why it is a good security feature.** It addresses the most common real-world attack on accounts: stolen passwords. It is integrated end to end: the user model, the login flow, rate limiting, logging and a frontend settings page.

### 2. Session management: sign out of other devices and view active sessions

**What it is.** Users can view their active browser sessions and sign out of all other devices from the `/security` page. The browser making the request stays signed in.

**Purpose.** Users can identify unfamiliar sessions and revoke access after using a shared computer, losing a device or noticing suspicious activity.

**How it works.**

- **View signed-in devices:** `GET /api/account/sessions` returns the authenticated user's unexpired, unrevoked sessions under their current account token version. The list shows the estimated browser and operating system, IP address, sign-in time and last activity time. The current session is labelled **This session**.
- **Sign out of other devices:** `POST /api/account/logout-all` requires the current password. The server increments the user's `tokenVersion`, invalidating previously issued tokens, and creates a replacement session for the current browser.
- **Ordinary sign-out:** `POST /api/account/logout` revokes the current session on the server before the frontend clears its stored authentication data.
- **Password changes:** changing the password also increments `tokenVersion`, ending previous sessions and returning a replacement token for the current browser.
- **Refresh:** the device list refreshes after a password change or signing out of other devices. Users can also refresh it manually.

**Security controls built into the feature.**

| Control | Implementation | Why it matters |
|---------|----------------|----------------|
| Server-side session tracking | Each authenticated JWT contains a session ID (`sid`) linked to a record in `models/sessionModel.js` | The server can reject a revoked session even if someone still has its token |
| Account-wide token invalidation | Protected routes compare the JWT's `tv` claim with the user's current `tokenVersion` | Increasing the version makes older tokens unusable on subsequent protected requests |
| Session validity checks | Authentication checks session ownership, version, expiry and revocation status | A valid JWT signature alone is insufficient to grant access |
| Password confirmation | Signing out of other devices requires the current password | Possession of a session token alone cannot trigger this action |
| Rate limiting | The password-change and sign-out-other-devices endpoints use the authentication limiter | Reduces repeated password-guessing attempts |
| Account isolation | Session queries use the authenticated user's ID from `req.user` | Users cannot request another account's session list |
| Expiry enforcement | Sessions and their JWTs expire after seven days; middleware checks expiry directly | Expired sessions are rejected even before database cleanup runs |
| Automatic cleanup | A MongoDB TTL index removes expired session records | Limits the retention of expired session data |
| Safe session responses | The device-list endpoint returns display information without JWTs or refresh tokens | Viewing the list does not expose authentication credentials |

**Device information and privacy.** Browser and operating-system names are estimated from the User-Agent header. They are descriptive hints, not verified physical-device identities. Multiple tabs sharing a token appear as one session. IP addresses and browser details are stored to support account security and should be treated as personal information. Last activity is updated at most once per minute and does not indicate whether a browser is currently open.

**What happens after revocation.** Other browsers lose access on their next protected API request. They may continue displaying an already-loaded page until they contact the server, at which point authentication fails and the frontend requests a new login.

**Why it is a good security feature.** It gives users visibility into account access and a practical way to end unwanted sessions. Revocation is enforced by the backend, so it does not depend on another device voluntarily deleting its token.

---

## Setup

**Prerequisites:** Node.js 18+, npm, MongoDB (local or Atlas). To run with Docker instead, see [Docker Setup](#docker-setup).

```bash
git clone https://github.com/EMECPE/insy7314-2026-poe-calebpeterjohnels.git
cd insy7314-2026-poe-calebpeterjohnels
cd backend && npm install
cd ../frontend && npm install
```

**Certificates:** `ca.crt`, `cert.crt` and `cert.key` must be in the project root (one level above `backend/` and `frontend/`). Self-signed is fine. Trust `ca.crt` or accept the browser warning for both `https://localhost:3000` and `https://localhost:5173`.

**Backend `.env`** (in `backend/`; copy `backend/.env.example` to start):

```env
CONN_STRING=<mongodb_connection_string>
JWT_SECRET=<long_random_secret>
TWOFA_ENC_KEY=<64_hex_characters>
CORS_ORIGINS=https://localhost:5173

# only for seed scripts
ADMIN_USERNAME=<admin_username>
ADMIN_EMAIL=<admin_email>
ADMIN_PASSWORD=<admin_password>
DEMO_PASSWORD=<demo_account_password>
```

Generate `TWOFA_ENC_KEY` with:

```bash
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

`CORS_ORIGINS` is a comma-separated list of the frontend URLs the API will accept requests from.

Optional: `PORT`, `GENERAL_LIMIT`, `AUTH_LIMIT`, `BOOKING_LIMIT`, `ADMIN_LIMIT`, `RATE_WINDOW_MINUTES`, `BETTERSTACK_SOURCE_TOKEN`, `BETTERSTACK_INGEST_HOST`.
The frontend works with defaults; override with `VITE_API_URL` and `VITE_API_ORIGIN` in `frontend/.env` if needed.

**Seed data** (run from `backend/`):

```bash
node utils/seedAdmin.js   # creates the admin account
node utils/seedGigs.js    # demo freelancers, a demo client and sample gigs
```

---

## Running the Apps

**Backend** (https://localhost:3000):

```bash
cd backend
npm start        # or: npm run dev (nodemon)
```

**Frontend** (https://localhost:5173), in a second terminal:

```bash
cd frontend
npm run dev
```

Other frontend scripts: `npm run build`, `npm run preview`, `npm run lint`.

---

## Docker Setup

**Prerequisites:** Docker Desktop (or Docker Engine with the Compose plugin) and a MongoDB database. The simplest option is a MongoDB Atlas connection string.

### 1. Clone the repository

```bash
git clone https://github.com/EMECPE/insy7314-2026-poe-calebpeterjohnels.git
cd insy7314-2026-poe-calebpeterjohnels
```

### 2. Certificates

`ca.crt`, `cert.crt` and `cert.key` are in the project root and are mounted into the containers. They are throwaway localhost development certificates. **Never reuse them anywhere real.**

### 3. Create the backend environment file

```bash
cp backend/.env.example backend/.env
```

Then fill in `backend/.env`:

```env
CONN_STRING=<mongodb_connection_string>
JWT_SECRET=<long_random_secret>
TWOFA_ENC_KEY=<64_hex_characters>
CORS_ORIGINS=https://localhost:5173

# only for seed scripts
ADMIN_USERNAME=<admin_username>
ADMIN_EMAIL=<admin_email>
ADMIN_PASSWORD=<admin_password>
DEMO_PASSWORD=<demo_account_password>
```

Generate `TWOFA_ENC_KEY` with:

```bash
node -e "console.log(require('crypto').randomBytes(32).toString('hex'))"
```

`CORS_ORIGINS` is a comma-separated list of the frontend URLs the API will accept requests from.

### 4. Build and start both containers

```bash
docker compose up --build -d
docker compose ps
```

Both `backend` and `frontend` should show `healthy`. The frontend only starts once the backend health check passes.

### 5. Open the app

| Service | URL |
|---------|-----|
| Frontend | https://localhost:5173 |
| API | https://localhost:3000 |

Trust `ca.crt` in your OS or browser, or open `https://localhost:3000/api/home/healthCheck` once and accept the warning. Otherwise the browser blocks the frontend's calls to the API.

### 6. Seed data (optional)

```bash
docker compose exec backend node utils/seedAdmin.js
docker compose exec backend node utils/seedGigs.js
```

### 7. Logs and shutdown

```bash
docker compose logs -f backend    # live container output
docker compose down               # stop and remove containers
```

The backend's log files appear in `./logs` on your machine through a volume mapping. On Linux, run `mkdir -p logs && chmod 777 logs` first so the container user can write there.

### How the stack is put together

- The frontend and backend are separate images and separate services on a bridge network (`app-network`).
- The frontend image is a multi-stage build: Node builds the app, then nginx serves it over HTTPS on port 5173. The API address is baked in at build time from `VITE_API_URL` and `VITE_API_ORIGIN`, with defaults for localhost (Docker, 2026).
- Environment values and certificates are supplied at run time and are not stored in the images, because `.dockerignore` excludes `.env`. OWASP advises against keeping secrets in images or Docker `ENV`/`ARG` instructions (OWASP, 2026r).

---

## Testing

### CI pipeline

Every push and pull request to `main` runs `.github/workflows/main.yml`, which has these jobs:

| Job | What it does |
|-----|--------------|
| Backend (Newman API tests) | Starts the API against a real MongoDB with generated TLS certificates, then runs the Newman API suite and the rate-limit test |
| Frontend (lint, tests, build) | ESLint, the Vitest suite and a production build |
| Dependency audit | `npm audit` on production packages, failing on high or critical vulnerabilities |
| Static analysis (Semgrep) | Scans the source code for insecure patterns (see [Static Code Analysis](#static-code-analysis)) |
| Docker build and Compose smoke test | Builds both images, starts the stack and checks the health endpoint and nginx security headers |
| Publish images to Docker Hub | Only on a push to `main`, and only after every other job has passed |

### Backend API tests (Newman)

Covers auth, gig ownership, bookings, transactions/income and admin RBAC, including negative tests (`FAIL:` requests) for role escalation, weak passwords, NoSQL injection, tampered tokens, and price/client tampering.

1. Seed the admin and set `adminEmail` / `adminPassword` in `backend/postman/local.postman_environment.json` to match.
2. Terminal 1: `cd backend && npm run start:test` (raised rate limits)
3. Terminal 2: `cd backend && npm test`
4. Report: `backend/postman/results/report.html`

> **Note:** on Node 20 and newer, Newman can fail every request with `Invalid IP address: undefined` when `localhost` resolves to more than one address. If that happens, run `NODE_OPTIONS=--no-network-family-autoselection npm test`. The CI workflow sets this automatically.

**Postman and Newman test results:**

<img width="653" height="426" alt="image" src="https://github.com/user-attachments/assets/2f2b24fa-5cdd-4cfc-94ea-18860b039dc4" />
<img width="959" height="473" alt="image" src="https://github.com/user-attachments/assets/f42c7d51-6eaf-44cd-a558-8c08d9a45eb4" />

### Rate-limit test

1. Restart the API with `npm run start:limits` (auth limit of 3).
2. In another terminal: `npm run test:ratelimit` (expects `401, 401, 401, 429, 429`).
3. Report: `backend/postman/results/ratelimit-report.html`. Restart the server before re-running, as counters are in memory.

### Backend unit tests (Node test runner)

These need no database or extra packages. Run them from `backend/`:

```bash
node --test tests/tax.test.js
node --test tests/account.test.js
node --test tests/sessions.test.js
node --test tests/logger.test.js
```

They cover the tax estimate, the account and session logic (with the model methods replaced by fakes) and the Better Stack log transport.

### Frontend tests (Vitest + React Testing Library)

```bash
cd frontend
npm test
```

The API is mocked, so the backend doesn't need to be running. The tests cover:

- the auth header format
- booking sending only the gig id
- logout on `401`
- friendly `429` and hidden `500` errors
- login validation
- gig list rendering
- booking success and failure
- route guards
- role-based nav links
- income calculations and the income insights display

### Manual checks

- Weak password and duplicate email are rejected.
- A client visiting `/dashboard` or `/admin` sees "Not allowed".
- Client books a gig → confirmation with reference → freelancer income increases.
- Deleting a gig with bookings deactivates it.
- Admin page shows all data with no password hashes.
- `backend/logs/` records failed logins, rate-limit hits and 2FA events without secrets.

---

## Static Code Analysis

### Tool and purpose

Static analysis runs in the CI pipeline (`.github/workflows/main.yml`) as a dedicated `sast` job using **Semgrep**. It reads the backend and frontend source code without running it and flags risky patterns such as injection, unsafe cryptography, open redirects and hard-coded secrets. It complements the Newman API tests, which check behaviour at runtime, and the dependency audit, which checks third-party packages.

- **Rules:** community JavaScript, Node.js, OWASP Top 10 and secrets rule packs (130 rules run).
- **Scope:** 99 git-tracked files in `backend/` and `frontend/` (`node_modules` and `dist` excluded).
- **Gate:** the job runs with `--error`, so any finding fails the pipeline. The Docker build and the Docker Hub publish jobs depend on it, so images are only published when the scan passes.

### Results

**First scan: 2 blocking findings, pipeline failed.** The Docker build and publish jobs were skipped, which shows the gate working.

<!-- Screenshot 1: Semgrep step output with 2 findings + job list showing Docker and publish skipped -->
![First Semgrep scan with 2 blocking findings]<img width="983" height="860" alt="Screenshot 2026-11-09 232036" src="https://github.com/user-attachments/assets/25342b06-7688-40f8-9e4a-990158b8f0ef" />


| # | File | Finding | Verdict | Fix |
|---|------|---------|---------|-----|
| 1 | `backend/utils/twoFactor.js` | `gcm-no-tag-length`: AES-256-GCM decryption did not enforce the authentication tag length, so an attacker could supply a shortened tag and forge ciphertexts of the stored 2FA secrets | True positive | Set `authTagLength: 16` on both the cipher and decipher, and reject stored values with a wrongly sized IV or tag before decrypting (OWASP, 2026q) |
| 2 | `frontend/nginx.conf` | `request-host-used`: the HTTP-to-HTTPS redirect was built from the request's Host header, which the client controls, creating an open-redirect risk | True positive | Redirect to a fixed host (`https://localhost:$server_port$request_uri`) |

**Second scan: 1 finding, pipeline failed.** The rule matched the variable name inside a code comment added next to the fixed redirect. This was a false positive caused by text matching. It was resolved by rewording the comment, not by suppressing the rule.

<!-- Screenshot 2: Semgrep step output with 1 finding (the comment on line 12) -->
![Second Semgrep scan with 1 finding]<img width="1917" height="898" alt="Screenshot 2026-11-09 232142" src="https://github.com/user-attachments/assets/0be2a8a7-a18d-417e-a0cf-9a5f1f78c882" />


**Final scan: 0 findings, pipeline passed.** The same 130 rules ran on the same 99 files, and every job, including the Docker build and publish, completed successfully.

<!-- Screenshot 3: green sast job showing "Findings: 0 (0 blocking)" and all jobs green -->
![Final Semgrep scan with 0 findings]<img width="1911" height="806" alt="Screenshot 2026-11-09 232238" src="https://github.com/user-attachments/assets/c961f8cd-9d54-4125-9774-c018c480bd7a" />


The history of runs (failing scans, fixes, then a green run) is shown below.

<!-- Screenshot 4: Actions run list -->
![GitHub Actions run history]<img width="1202" height="376" alt="Screenshot 2026-11-09 231700" src="https://github.com/user-attachments/assets/b19f180c-0504-48ed-9237-bef0042fec6e" />


### What the results indicate

The scan found two genuine weaknesses, one in cryptographic handling and one in redirect handling, and both are now fixed. The clean final scan means Semgrep's rule sets detected no known-risky patterns in the scanned code. No rules were disabled and no findings were suppressed. This does not prove the application is free of vulnerabilities, because static analysis cannot find logic flaws or runtime issues. That is why it runs alongside the Newman API security tests and the dependency audit in the same pipeline.

---

## Final Security Review

**Scope and method.** This review covers the whole system: React frontend, nginx container, Express API, MongoDB data and the Docker deployment. Threats are grouped using the OWASP Top Ten 2021 categories and their related cheat sheets (OWASP, 2026p), and each was checked against what the code actually does.

### Threat-to-protection map

| Threat | How it could affect HustleHub+ | Protections implemented | Source |
|--------|-------------------------------|-------------------------|--------|
| Injection (A03) | Operator keys such as `{"$gt": ""}` in a login or query body bypass checks; duplicate parameters change behaviour | Custom sanitiser removes keys starting with `$` or containing `.` and logs each attempt (`securityMiddleware.js`); `hpp` removes duplicate parameters; `express-validator` rules check type, length and format, and `isMongoId` checks every id; Mongoose schemas with types, enums and length limits; 10 kb body limit; allow-listed inputs (category enum, role only client or freelancer); Semgrep static analysis in CI | OWASP, 2026m; OWASP, 2026f |
| Cross-site scripting | Malicious script in gig titles, descriptions or usernames runs in another user's browser and steals the token | React escapes rendered text and `dangerouslySetInnerHTML` is not used; HTML tags are stripped from gig text and usernames are escaped on the server; the production page CSP allows only `'self'` scripts with no inline script; helmet CSP on the API; `nosniff` header | OWASP, 2026b; OWASP, 2026k |
| Authentication weaknesses (A07) | Brute force, credential stuffing, weak or leaked passwords, user enumeration, stolen tokens | bcrypt cost 12 with strength rules (8+ characters, a number, an uppercase letter); `/api/auth` limited to 10 requests per 15 minutes per IP; identical error for unknown email and wrong password; two-factor authentication with single-use codes and lockout; JWT with 7-day expiry, signed with a secret from `.env`, and the user is re-loaded from the database on every request; server-side session revocation; the server refuses to start without `JWT_SECRET` | OWASP, 2026a; OWASP, 2026c; OWASP, 2026d; OWASP, 2026g; OWASP, 2026n; Temoshok et al., 2025 |
| Access control (A01) | A client calls freelancer or admin routes; a freelancer edits someone else's gig; a user changes the price or role | `validateAuth` plus `validateRole` on every protected route (deny by default: no token gives 401, wrong role gives 403); owner check on gig update, replace and delete; "my data" endpoints filter by `req.user`, never by a URL id; bookings take the client from the token and the price from the gig; registration cannot create an admin (admin only via `seedAdmin.js`); 401 and 403 events are logged | OWASP, 2026o; OWASP, 2026p |
| CSRF | A malicious site makes the user's browser send a state-changing request | CSRF depends on credentials the browser attaches automatically, such as cookies (OWASP, 2026h). This API uses no cookies. The JWT is sent by the app's own JavaScript in an `Authorization` header, which a cross-site form or image cannot add. CORS only allows the configured frontend origins (`CORS_ORIGINS`) and the listed methods and headers | OWASP, 2026h |
| Denial of service | Request floods, expensive endpoints or oversized bodies exhaust the API | Per-IP rate limits (general 150, auth 10, booking 20, admin 60 per 15 minutes) returning 429 with `Retry-After`; 10 kb body limit; validation rejects bad input before database work; Docker health checks and `restart: unless-stopped` recover a crashed container; log rotation caps disk use | OWASP, 2026j; OWASP, 2026e |
| Sensitive data exposure (A02) | Password hashes, 2FA secrets, tokens or stack traces leak through the API, logs or images | Passwords are hashed and the field is excluded from queries; the 2FA fields are `select: false` and the secret is AES-256-GCM encrypted with an enforced authentication tag length; admin lists contain no hashes; a central error handler returns generic messages; logs exclude passwords and tokens and use IPs and ids instead of emails; HTTPS everywhere with HSTS; the HTTP-to-HTTPS redirect uses a fixed host; secrets live in `.env` and are never baked into images | OWASP, 2026l; OWASP, 2026q; OWASP, 2026r |
| Security misconfiguration (A05) | Default headers, root containers, exposed secrets | helmet (CSP, HSTS, frameguard, noSniff and more) on the API; nginx adds `nosniff`, `X-Frame-Options: DENY`, `no-referrer` and HSTS; backend runs as the non-root `node` user and nginx as the unprivileged image; `no-new-privileges` on both containers; certificates mounted read-only; `server_tokens off`; `.dockerignore` keeps `.env`, logs and `node_modules` out of images | OWASP, 2026i; Docker, 2026 |
| Logging and monitoring (A09) | Attacks go unnoticed | Daily log files record requests, authentication successes and failures, authorization failures, validation failures, rate-limit hits, blocked injection keys and 2FA events | OWASP, 2026l |

### Residual risks and limitations

These are listed deliberately, because a review that finds no weaknesses is not credible.

- **Token in `localStorage`.** An XSS flaw could steal the JWT. The strict CSP and input handling reduce the chance, but an `HttpOnly` cookie (with CSRF protection) would be stronger (OWASP, 2026b).
- **Token lifetime.** A stolen JWT stays valid for up to 7 days unless the session is revoked, the user signs out of other devices or the password is changed. Deleting the user also invalidates it, because the user is re-loaded on each request.
- **Rate limiting is per IP and in memory.** It resets on restart and does not stop distributed attacks, and OWASP warns that IP limits alone are weak against credential stuffing (OWASP, 2026n). 2FA is the real defence for protected accounts.
- **2FA is optional.** It is not enforced for admin accounts.
- **The 2FA lockout can be abused.** An attacker who knows a password can fail five codes and lock the owner out for 15 minutes. That is the price of limiting brute force.
- **The temporary 2FA token is reusable until it expires after 5 minutes.** The lockout limits the damage.
- **`frame-ancestors` in the page CSP is ignored** when the policy is delivered in a `<meta>` tag (OWASP, 2026k). Clickjacking protection therefore comes from the `X-Frame-Options` header set by nginx.
- **Containers have no resource limits or read-only filesystem.** Docker memory, CPU and process limits would further reduce denial-of-service impact (OWASP, 2026i, rules 7 and 8).
- **Static analysis has limits.** Semgrep finds known risky code patterns but not logic flaws, so it supplements the runtime tests rather than replacing them.
- **Development certificates are in the repository** and are for local use only. Payments are simulated.

---

## Known Limitations

- Payments are simulated.
- The JWT is stored in `localStorage` (mitigated by CSP and input sanitising; an `HttpOnly` cookie would be stronger).
- Rate limits are in-memory and reset on restart.
- 2FA is optional and not enforced for admin accounts.
- The included certificates and keys are for local development only.

See [Residual risks and limitations](#residual-risks-and-limitations) for the full list.

[Watch YouTube Video](https://youtu.be/Hkm1f-jxFeI)

---

## References
- Better Stack, 2026. Ingesting logs [online]. <https://betterstack.com/docs/logs/ingesting-data/http/logs/> [Accessed 9 November 2026].
- DEV Community, 2024. *What is Input Validation in Web Development* [online]. <https://dev.to/lialiago/what-is-input-validation-in-web-development-5b8l> [Accessed 6 September 2026].
- Docker, 2026. *Building best practices* [online]. <https://docs.docker.com/build/building/best-practices/> [Accessed 9 November 2026].
- GeeksforGeeks, 2025. *HTTP Strict Transport Security* [online]. <https://www.geeksforgeeks.org/ethical-hacking/explain-http-strict-transport-security/> [Accessed 6 September 2026].
- GeeksforGeeks, 2026a. *Password Hashing with Bcrypt in Flask* [online]. <https://www.geeksforgeeks.org/python/password-hashing-with-bcrypt-in-flask/> [Accessed 6 September 2026].
- Mozilla, 2026. AbortSignal [online]. <https://developer.mozilla.org/en-US/docs/Web/API/AbortSignal> [Accessed 9 November 2026].
- OWASP, 2021. OWASP Top 10:2021 - A09 Security Logging and Monitoring Failures [online]. <https://owasp.org/Top10/2021/A09_2021-Security_Logging_and_Monitoring_Failures/> [Accessed 9 November 2026].
- The Independent Institute of Education (IIE), 2026. *Information Systems 3D* [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.
- otplib, 2026. *Getting started* [online]. <https://otplib.yeojz.dev/guide/getting-started> [Accessed 23 October 2026].
- OWASP, 2026a. *Authentication Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Authentication_Cheat_Sheet.html> [Accessed 4 September 2026].
- OWASP, 2026b. *Cross Site Scripting Prevention Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Cross_Site_Scripting_Prevention_Cheat_Sheet.html> [Accessed 4 September 2026].
- OWASP, 2026c. *JSON Web Token Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_Cheat_Sheet.html> [Accessed 4 September 2026].
- OWASP, 2026d. *Password Storage Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html> [Accessed 4 September 2026].
- OWASP, 2026e. *REST Security Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/REST_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
- OWASP, 2026f. *NoSQL Security Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/NoSQL_Security_Cheat_Sheet.html> [Accessed 4 September 2026].
- OWASP, 2026g. *Multifactor Authentication Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html> [Accessed 23 October 2026].
- OWASP, 2026h. *Cross-Site Request Forgery Prevention Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Cross-Site_Request_Forgery_Prevention_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026i. *Docker Security Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Docker_Security_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026j. *Denial of Service Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Denial_of_Service_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026k. *Content Security Policy Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Content_Security_Policy_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026l. *Logging Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Logging_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026m. *Injection Prevention Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Injection_Prevention_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026n. *Credential Stuffing Prevention Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Credential_Stuffing_Prevention_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026o. *Authorization Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Authorization_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026p. *OWASP Top Ten 2021: Related Cheat Sheets* [online]. <https://cheatsheetseries.owasp.org/IndexTopTen.html> [Accessed 9 November 2026].
- OWASP, 2026q. *Cryptographic Storage Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html> [Accessed 9 November 2026].
- OWASP, 2026r. *Secrets Management Cheat Sheet* [online]. <https://cheatsheetseries.owasp.org/cheatsheets/Secrets_Management_Cheat_Sheet.html> [Accessed 9 November 2026].
- Temoshok, D., Fenton, J., Choong, Y., Lefkovitz, N., Regenscheid, A., Galluzzo, R. and Richer, J., 2025. *Digital Identity Guidelines: Authentication and Authenticator Management*. NIST Special Publication 800-63B-4 [online]. <https://csrc.nist.gov/pubs/sp/800/63/b/4/final> [Accessed 9 November 2026].

---

## YouTube Video

https://youtu.be/St4gRVLXs5M

## MERN Diagram

<img width="831" height="1491" alt="MERN diagram" src="https://github.com/user-attachments/assets/6ea6c11f-e044-461f-a19a-207f8036cfac" />

## License

Created for the **INSY7314 POE Assignment** for educational purposes.
