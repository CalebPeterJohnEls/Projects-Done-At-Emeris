// Client-side mirrors of the backend express-validator rules. They give instant,
// friendly feedback; the server still re-validates everything.
export type Errors = Record<string, string>;

//user validation
export function validateUsername(v: string): string | undefined {
  const value = v.trim();
  if (value.length < 3 || value.length > 30)
    return "Username must be between 3 and 30 characters";
  if (!/^[a-zA-Z0-9_]+$/.test(value))
    return "Username may only contain letters, numbers and underscores";
}

//user email validation
export function validateEmail(v: string): string | undefined {
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim()))
    return "A valid email address is required";
}

//user password validation
export function validatePassword(v: string): string | undefined {
  if (v.length < 8) return "Password must be at least 8 characters long";
  if (!/\d/.test(v)) return "Password must contain at least one number";
  if (!/[A-Z]/.test(v)) return "Password must contain at least one uppercase letter";
}

//login validation
export function validateLogin(email: string, password: string): Errors {
  const errors: Errors = {};
  const e = validateEmail(email);
  if (e) errors.email = e;
  if (!password) errors.password = "Password is required";
  return errors;
}

//reg validation
export function validateRegister(
  username: string,
  email: string,
  password: string,
): Errors {
  const errors: Errors = {};
  const u = validateUsername(username);
  const e = validateEmail(email);
  const p = validatePassword(password);
  if (u) errors.username = u;
  if (e) errors.email = e;
  if (p) errors.password = p;
  return errors;
}

// ---------- password strength (for the live meter) ----------
// The three checks are the same rules the server enforces. The extra point for a
// long password or a symbol only changes the label; the server decides what is accepted.
export interface PasswordStrength {
  score: 0 | 1 | 2 | 3 | 4;
  label: "Too weak" | "Weak" | "Good" | "Strong";
  meetsRules: boolean;
  checks: { label: string; ok: boolean }[];
}

export function passwordStrength(v: string): PasswordStrength {
  const checks = [
    { label: "At least 8 characters", ok: v.length >= 8 },
    { label: "A number", ok: /\d/.test(v) },
    { label: "An uppercase letter", ok: /[A-Z]/.test(v) },
  ];
  const meetsRules = checks.every((c) => c.ok);
  const bonus = v.length >= 8 && (v.length >= 12 || /[^A-Za-z0-9]/.test(v));

  let points = checks.filter((c) => c.ok).length + (bonus ? 1 : 0);
  if (!meetsRules) points = Math.min(points, 2); // never "Good" while a rule is unmet
  if (v.length < 8) points = Math.min(points, 1);
  if (v.length === 0) points = 0;

  const score = points as 0 | 1 | 2 | 3 | 4;
  const labels = ["Too weak", "Too weak", "Weak", "Good", "Strong"] as const;
  return { score, label: labels[score], meetsRules, checks };
}

// change-password form: field names match what the server sends back in `errors`
export function validateChangePassword(
  currentPassword: string,
  newPassword: string,
  confirmPassword: string,
  needsCode: boolean,
  code: string,
): Errors {
  const errors: Errors = {};
  if (!currentPassword) errors.currentPassword = "Current password is required";
  const p = validatePassword(newPassword);
  if (p) errors.newPassword = p;
  else if (newPassword === currentPassword)
    errors.newPassword = "Choose a new password that is different from your current one";
  if (confirmPassword !== newPassword) errors.confirmPassword = "Passwords do not match";
  if (needsCode && !code.trim()) errors.code = "Enter a 6-digit or recovery code";
  return errors;
}

// limits match gigModel.js
//gig validation
export function validateGig(
  title: string,
  description: string,
  category: string,
  price: string,
): Errors {
  const errors: Errors = {};
  if (!title.trim()) errors.title = "Title is required";
  else if (title.trim().length > 100) errors.title = "Title must be 100 characters or fewer";
  if (!description.trim()) errors.description = "Description is required";
  else if (description.trim().length > 2000)
    errors.description = "Description must be 2000 characters or fewer";
  if (!category) errors.category = "Choose a category";
  const n = Number(price);
  if (price.trim() === "" || !Number.isFinite(n) || n < 0)
    errors.price = "Enter a price of 0 or more";
  return errors;
}