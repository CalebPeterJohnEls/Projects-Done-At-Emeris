import React, { useState } from "react";
import { changePassword, fieldErrorMap, getErrorMessage } from "../services/api.ts";
import { Alert } from "./Alert.tsx";
import { FormField } from "./FormField.tsx";
import { PasswordStrengthMeter } from "./PasswordStrengthMeter.tsx";
import { validateChangePassword, type Errors } from "../lib/validation.ts";

// change password: needs the current password, and a 2FA code when 2FA is on.
// The server signs out every other device and sends back a fresh token for this one.
export const ChangePasswordCard: React.FC<{ twoFactorEnabled: boolean; onChanged?: () => void }> = ({
  twoFactorEnabled,
  onChanged,
}) => {
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [code, setCode] = useState("");
  const [errors, setErrors] = useState<Errors>({});
  const [formError, setFormError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);
    setFormError(null);
    const found = validateChangePassword(currentPassword, newPassword, confirmPassword, twoFactorEnabled, code);
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    setSubmitting(true);
    try {
      const result = await changePassword(
        currentPassword,
        newPassword,
        twoFactorEnabled ? code.trim() : undefined,
      );
      setMessage(result.message);
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
      setCode("");
      setErrors({});
      onChanged?.();
    } catch (err) {
      setFormError(getErrorMessage(err));
      setErrors(fieldErrorMap(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className='bento-card bento-col-6 teal-header auth-card'>
      <h2 className='card-title'>Change password</h2>
      {message && <Alert kind='success'>{message}</Alert>}
      {formError && <Alert kind='danger'>{formError}</Alert>}
      <form onSubmit={handleSubmit} noValidate>
        <FormField id='cp-current' label='Current password' error={errors.currentPassword}>
          <input
            id='cp-current'
            type='password'
            autoComplete='current-password'
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
          />
        </FormField>
        <FormField id='cp-new' label='New password' error={errors.newPassword}>
          <input
            id='cp-new'
            type='password'
            autoComplete='new-password'
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
          />
        </FormField>
        <PasswordStrengthMeter password={newPassword} />
        <FormField id='cp-confirm' label='Confirm new password' error={errors.confirmPassword}>
          <input
            id='cp-confirm'
            type='password'
            autoComplete='new-password'
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
          />
        </FormField>
        {twoFactorEnabled && (
          <FormField id='cp-code' label='6-digit code or recovery code' error={errors.code}>
            <input
              id='cp-code'
              type='text'
              inputMode='numeric'
              autoComplete='one-time-code'
              value={code}
              onChange={(e) => setCode(e.target.value)}
            />
          </FormField>
        )}
        <p className='muted'>Changing your password signs you out of all your other devices.</p>
        <button type='submit' className='btn btn-teal' disabled={submitting}>
          {submitting ? "Saving…" : "Change password"}
        </button>
      </form>
    </div>
  );
};