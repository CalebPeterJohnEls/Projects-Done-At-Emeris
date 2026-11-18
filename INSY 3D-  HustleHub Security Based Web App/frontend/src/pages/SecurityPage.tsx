// imports for React, the 2FA API calls and the form components (IIE, 2026)
import React, { useEffect, useState } from "react";
import {
  disableTwoFactor,
  enableTwoFactor,
  getErrorMessage,
  getTwoFactorStatus,
  setupTwoFactor,
} from "../services/api.ts";
import type { TwoFactorSetup } from "../models/user.ts";
import { Alert } from "../components/Alert.tsx";
import { FormField } from "../components/FormField.tsx";
import { ChangePasswordCard } from "../components/ChangePasswordCard.tsx"
import { SignOutEverywhereCard } from "../components/SignOutEverywhereCard.tsx";
import { ActivityCard } from "../components/ActivityCard.tsx";
import { ActiveDevicesCard } from "../components/ActiveDevicesCard.tsx";

// lets any logged-in user turn two-factor authentication on or off, change their
// password, sign out of other devices and review recent activity (OWASP, 2026 e; IIE, 2026)
export const SecurityPage: React.FC = () => {
  const [enabled, setEnabled] = useState<boolean | null>(null);
  const [setup, setSetup] = useState<TwoFactorSetup | null>(null);
  const [recoveryCodes, setRecoveryCodes] = useState<string[] | null>(null);
  const [code, setCode] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  // goes up after a password change or sign-out, so the activity list reloads
  const [activityKey, setActivityKey] = useState(0);
  const refreshActivity = () => setActivityKey((k) => k + 1);

  // finds out whether 2FA is already on for this account (IIE, 2026)
  useEffect(() => {
    getTwoFactorStatus()
      .then((s) => setEnabled(s.enabled))
      .catch((err) => setError(getErrorMessage(err)));
  }, []);

  // asks the server for a new secret and QR code (OWASP, 2026 e)
  const handleSetup = async () => {
    setError(null);
    setMessage(null);
    setSubmitting(true);
    try {
      setSetup(await setupTwoFactor());
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  // sends the first code from the authenticator app to switch 2FA on, the recovery codes are shown once (OWASP, 2026 e)
  const handleEnable = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (!/^\d{6}$/.test(code.trim())) {
      setError("Enter the 6-digit code from your authenticator app");
      return;
    }
    setSubmitting(true);
    try {
      const result = await enableTwoFactor(code.trim());
      setRecoveryCodes(result.recoveryCodes);
      setEnabled(true);
      setSetup(null);
      setCode("");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  // switching 2FA off needs the password and a current code (OWASP, 2026 e)
  const handleDisable = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (!password || !code.trim()) {
      setError("Enter your password and a 6-digit or recovery code");
      return;
    }
    setSubmitting(true);
    try {
      await disableTwoFactor(password, code.trim());
      setEnabled(false);
      setRecoveryCodes(null);
      setPassword("");
      setCode("");
      setMessage("Two-factor authentication has been turned off.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  // the layout and classNames follow the other pages (IIE, 2026; Bro Code, 2023)
  return (
    <div className='bento-grid'>
      <div className='bento-card bento-col-6 teal-header auth-card'>
        <h2 className='card-title'>Two-factor authentication</h2>
        {error && <Alert kind='danger'>{error}</Alert>}
        {message && <Alert kind='success'>{message}</Alert>}

        {enabled === null && !error && <p>Loading…</p>}

        {/* recovery codes are only ever shown once, right after 2FA is switched on (OWASP, 2026 e) */}
        {recoveryCodes && (
          <>
            <Alert kind='success'>
              Two-factor authentication is on. Save these recovery codes now. Each one works once and they will not be shown again.
            </Alert>
            <ul>
              {recoveryCodes.map((c) => (
                <li key={c}>
                  <code>{c}</code>
                </li>
              ))}
            </ul>
          </>
        )}

        {/* not enabled and not started yet (IIE, 2026) */}
        {enabled === false && !setup && (
          <>
            <p>Add a second step to your login using an authenticator app.</p>
            <button type='button' className='btn btn-teal' onClick={handleSetup} disabled={submitting}>
              {submitting ? "Please wait…" : "Enable 2FA"}
            </button>
          </>
        )}

        {/* QR code is a data URL, which the page CSP already allows (OWASP, 2026 e) */}
        {enabled === false && setup && (
          <form onSubmit={handleEnable} noValidate>
            <p>Scan this QR code with your authenticator app, then enter the 6-digit code it shows.</p>
            <img src={setup.qrDataUrl} alt='QR code for your authenticator app' width={200} height={200} />
            <p>
              Cannot scan it? Enter this key manually: <code>{setup.secret}</code>
            </p>
            <FormField id='code' label='6-digit code'>
              <input
                id='code'
                type='text'
                inputMode='numeric'
                autoComplete='one-time-code'
                value={code}
                onChange={(e) => setCode(e.target.value)}
              />
            </FormField>
            <button type='submit' className='btn btn-teal' disabled={submitting}>
              {submitting ? "Verifying…" : "Confirm and enable"}
            </button>
          </form>
        )}

        {/* already enabled, so it can be switched off with the password and a code (OWASP, 2026 e) */}
        {enabled === true && (
          <form onSubmit={handleDisable} noValidate>
            <p>Two-factor authentication is on. To turn it off, confirm your password and a code.</p>
            <FormField id='password' label='Password'>
              <input
                id='password'
                type='password'
                autoComplete='current-password'
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </FormField>
            <FormField id='code' label='6-digit code or recovery code'>
              <input
                id='code'
                type='text'
                inputMode='numeric'
                autoComplete='one-time-code'
                value={code}
                onChange={(e) => setCode(e.target.value)}
              />
            </FormField>
            <button type='submit' className='btn btn-outline' disabled={submitting}>
              {submitting ? "Please wait…" : "Disable 2FA"}
            </button>
          </form>
        )}
      </div>

      <ChangePasswordCard twoFactorEnabled={enabled === true} onChanged={refreshActivity} />
      <SignOutEverywhereCard onDone={refreshActivity} />
      <ActiveDevicesCard refreshKey={activityKey} />
      <ActivityCard refreshKey={activityKey} />
    </div>
  );
};
/* Reference List
Bro Code, 2023. React js how to add CSS styles. [video online] Available at: <https://www.youtube.com/watch?v=r3SMd0YTSZs> [Accessed 10 October 2026].

The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.

OWASP, 2026 e. Multifactor Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html> [Accessed 23 October 2026].*/