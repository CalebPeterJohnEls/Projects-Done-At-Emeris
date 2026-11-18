// imports for React, routing, login details and the form components (IIE, 2026)
import React, { useState } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth.ts";
import { Alert } from "../components/Alert.tsx";
import { FormField } from "../components/FormField.tsx";
import { validateLogin, type Errors } from "../lib/validation.ts";

// login form that checks the input and signs the user in (IIE, 2026)
export const LoginPage: React.FC = () => {
  const {
    login,
    completeTwoFactor,
    cancelTwoFactor,
    twoFactorPending,
    isAuthenticated,
    error,
    fieldErrors,
    clearError,
  } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  // page the user was trying to reach before being sent to login, defaults to home (IIE, 2026)
  const from = (location.state as { from?: string } | null)?.from ?? "/";

  // form values, validation errors and submit status (IIE, 2026)
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [code, setCode] = useState("");
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);

  // already logged in, so skip the login page (IIE, 2026)
  if (isAuthenticated) return <Navigate to={from} replace />;

  // checks the fields first, then logs in and returns the user to their page (IIE, 2026)
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    const found = validateLogin(email, password);
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    setSubmitting(true);
    try {
      await login({ email: email.trim(), password });
      navigate(from, { replace: true });
    } catch {
      // the provider already stored a friendly message in `error` (IIE, 2026)
    } finally {
      setSubmitting(false);
    }
  };

  // second step: sends the 6-digit code or a recovery code to the server (OWASP, 2026 e)
  const handleCodeSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    if (!code.trim()) {
      setErrors({ code: "Enter your 6-digit code or a recovery code" });
      return;
    }
    setErrors({});

    setSubmitting(true);
    try {
      await completeTwoFactor(code.trim());
      navigate(from, { replace: true });
    } catch {
      // the provider already stored a friendly message in `error` (IIE, 2026)
    } finally {
      setSubmitting(false);
    }
  };

  // combines server errors and form errors so both can be displayed (IIE, 2026)
  const shown = { ...fieldErrors, ...errors };

  // the password was accepted and the account has 2FA on, so only the code is asked for (OWASP, 2026 e)
  if (twoFactorPending) {
    return (
      <div className='bento-grid'>
        <div className='bento-card bento-col-6 teal-header auth-card'>
          <h2 className='card-title'>Two-factor authentication</h2>
          {error && <Alert kind='danger'>{error}</Alert>}
          <form onSubmit={handleCodeSubmit} noValidate>
            <FormField id='code' label='6-digit code or recovery code' error={shown.code}>
              <input
                id='code'
                type='text'
                inputMode='numeric'
                autoComplete='one-time-code'
                value={code}
                onChange={(e) => setCode(e.target.value)}
                aria-invalid={!!shown.code}
              />
            </FormField>
            <button type='submit' className='btn btn-teal' disabled={submitting}>
              {submitting ? "Verifying…" : "Verify"}
            </button>{" "}
            <button
              type='button'
              className='btn btn-outline'
              onClick={() => {
                cancelTwoFactor();
                setCode("");
                setErrors({});
              }}
            >
              Back to login
            </button>
          </form>
        </div>
      </div>
    );
  }

  // the form layout comes from (IIE, 2026) and the classNames link to the CSS styles (Bro Code, 2023)
  return (
    <div className='bento-grid'>
      <div className='bento-card bento-col-6 teal-header auth-card'>
        <h2 className='card-title'>Log in</h2>
        {error && <Alert kind='danger'>{error}</Alert>}
        <form onSubmit={handleSubmit} noValidate>
          {/* email input controlled by React state (IIE, 2026) */}
          <FormField id='email' label='Email' error={shown.email}>
            <input
              id='email'
              type='email'
              autoComplete='email'
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              aria-invalid={!!shown.email}
            />
          </FormField>
          {/* password input controlled by React state (IIE, 2026) */}
          <FormField id='password' label='Password' error={shown.password}>
            <input
              id='password'
              type='password'
              autoComplete='current-password'
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              aria-invalid={!!shown.password}
            />
          </FormField>
          <button type='submit' className='btn btn-teal' disabled={submitting}>
            {submitting ? "Logging in…" : "Log in"}
          </button>
        </form>
       {/* link for users who do not have an account yet (IIE, 2026) */}
       <p className='muted-top'>
        No account?{" "}
        <Link to='/register' className='btn btn-teal'>
          Register
        </Link>
      </p>
      </div>
    </div>
  );
};
// Reference List
// Bro Code, 2023. React js how to add CSS styles. [video online] Available at: <https://www.youtube.com/watch?v=r3SMd0YTSZs> [Accessed 10 October 2026].
// Facebook Inc., 2020. Adding images, fonts, and files. [online] Create React App. Available at: <https://create-react-app.dev/docs/adding-images-fonts-and-files/> [Accessed 10 October 2026].
// Facebook Inc., 2021. Adding a CSS reset. [online] Create React App. Available at: <https://create-react-app.dev/docs/adding-css-reset> [Accessed 10 October 2026].
// The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual] The Independent Institute of Education: Unpublished.
// MUI, 2026. Material UI: overview. [online] Available at: <https://mui.com/material-ui/getting-started/> [Accessed 10 October 2026].
// Olawanle, J., 2022. Resizing images with React. [online] Stack Abuse. Available at: <https://stackabuse.com/resizing-images-with-react/> [Accessed 10 October 2026].
// OWASP, 2026 e. Multifactor Authentication Cheat Sheet. [online] Available at: <https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html> [Accessed 23 October 2026].
// PNGEgg, 2026. Walter White Say My Name YouTube Hermanos, walter white, television, hat png. [image online] Available at: <https://www.pngegg.com/en/png-tfhlm> [Accessed 10 October 2026].
// Web Tech Talk, 2026. React JS tutorial for beginners | Images and styles. [video online] Available at: <https://www.youtube.com/watch?v=xhAs3DY6xRU> [Accessed 10 October 2026].