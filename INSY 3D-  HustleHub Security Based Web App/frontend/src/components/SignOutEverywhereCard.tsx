import React, { useState } from "react";
import { getErrorMessage, logoutAllDevices } from "../services/api.ts";
import { Alert } from "./Alert.tsx";
import { FormField } from "./FormField.tsx";

// ends every session except this one. Two steps (button, then password), so it
// cannot be triggered by a stray click.
export const SignOutEverywhereCard: React.FC<{ onDone?: () => void }> = ({ onDone }) => {
  const [confirming, setConfirming] = useState(false);
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const cancel = () => {
    setConfirming(false);
    setPassword("");
    setError(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setMessage(null);
    if (!password) {
      setError("Enter your current password to continue");
      return;
    }
    setSubmitting(true);
    try {
      const result = await logoutAllDevices(password);
      setMessage(result.message);
      setConfirming(false);
      setPassword("");
      onDone?.();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className='bento-card bento-col-6 teal-header auth-card'>
      <h2 className='card-title'>Sign out of other devices</h2>
      {message && <Alert kind='success'>{message}</Alert>}
      {error && <Alert kind='danger'>{error}</Alert>}
      <p>
        Lost a phone, or used a shared computer? This signs you out everywhere except the device you
        are using now.
      </p>
      {!confirming ? (
        <button type='button' className='btn btn-outline' onClick={() => { setConfirming(true); setMessage(null); }}>
          Sign out of other devices
        </button>
      ) : (
        <form onSubmit={handleSubmit} noValidate>
          <FormField id='so-password' label='Current password'>
            <input
              id='so-password'
              type='password'
              autoComplete='current-password'
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </FormField>
          <div className='btn-group'>
            <button type='submit' className='btn btn-teal' disabled={submitting}>
              {submitting ? "Please wait…" : "Confirm sign out"}
            </button>
            <button type='button' className='btn btn-outline' onClick={cancel} disabled={submitting}>
              Cancel
            </button>
          </div>
        </form>
      )}
    </div>
  );
};