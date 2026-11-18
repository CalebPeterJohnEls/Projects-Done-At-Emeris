// Roles match the backend userModel. Admin accounts are seeded on the server,
// so the register form only ever offers client and freelancer.
//(IIE), 2026
export type UserRole = "client" | "freelancer" | "admin";
export type RegisterRole = Exclude<UserRole, "admin">;

export interface User {
  _id: string;
  username: string;
  email: string;
  role: UserRole;
}

// login/register responses carry the JWT alongside the user
//(IIE), 2026
export interface AuthResponse extends User {
  token: string;
}

// when 2FA is on, login returns this instead of a session token (OWASP, 2026 e)
//(IIE), 2026
export interface TwoFactorChallenge {
  twoFactorRequired: true;
  tempToken: string;
}

export type LoginResult = AuthResponse | TwoFactorChallenge;

export interface TwoFactorSetup {
  qrDataUrl: string;
  secret: string;
}

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterCredentials {
  username: string;
  email: string;
  password: string;
  role?: RegisterRole;
}

// events the server records for an account, shown on the Security page
//(IIE), 2026
export type ActivityType =
  | "login_success"
  | "login_failed"
  | "two_factor_failed"
  | "password_changed"
  | "sessions_revoked";

export interface ActivityEvent {
  _id: string;
  type: ActivityType;
  ip: string;
  device: string; // already readable, e.g. "Chrome on Windows"
  createdAt: string;
}

// changing the password or signing out everywhere returns a fresh token for
// the device making the request; every other device's token stops working
export interface SessionChangeResult {
  message: string;
  token: string;
}

export interface ActiveSession {
  _id: string;
  device: string;
  ip: string;
  createdAt: string;
  lastSeenAt: string;
  expiresAt: string;
  isCurrent: boolean;
}
/* Reference List 
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.

OWASP, 2026 e. Multifactor Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html> [Accessed 23 October 2026].*/