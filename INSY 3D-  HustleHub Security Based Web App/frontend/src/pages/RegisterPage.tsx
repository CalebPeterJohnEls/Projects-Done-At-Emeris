// imports for React, routing, login details, the form components and validation (IIE, 2026)
import React, { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth.ts";
import { Alert } from "../components/Alert.tsx";
import { FormField } from "../components/FormField.tsx";
import type { RegisterRole } from "../models/user.ts";
import { validateRegister, type Errors } from "../lib/validation.ts";

// registration form that checks the input and creates a new account (IIE, 2026)
export const RegisterPage: React.FC = () => {
  const { register, isAuthenticated, error, fieldErrors, clearError } = useAuth();
  const navigate = useNavigate();

  // form values, role choice, validation errors and submit status (IIE, 2026)
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  // admin is deliberately not offered - admin accounts are seeded on the server (IIE, 2026)
  const [role, setRole] = useState<RegisterRole>("client");
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);

  // already logged in, so skip the register page (IIE, 2026)
  if (isAuthenticated) return <Navigate to='/' replace />;

  // checks the fields first, then registers and sends the user to the right page (IIE, 2026)
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    clearError();
    const found = validateRegister(username, email, password);
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    setSubmitting(true);
    try {
      await register({ username: username.trim(), email: email.trim(), password, role });
      navigate(role === "freelancer" ? "/dashboard" : "/", { replace: true });
    } catch {
      // friendly message + field errors are already in the auth context (IIE, 2026)
    } finally {
      setSubmitting(false);
    }
  };

  // combines server errors and form errors so both can be displayed (IIE, 2026)
  const shown = { ...fieldErrors, ...errors };

  // the form layout comes from (IIE, 2026) and the classNames link to the CSS styles (Bro Code, 2023)
  return (
    <div className='bento-grid'>
      <div className='bento-card bento-col-6 teal-header auth-card'>
        <h2 className='card-title'>Create an account</h2>
        {error && <Alert kind='danger'>{error}</Alert>}
        <form onSubmit={handleSubmit} noValidate>
          {/* username input controlled by React state (IIE, 2026) */}
          <FormField id='username' label='Username' error={shown.username}>
            <input
              id='username'
              type='text'
              autoComplete='username'
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              aria-invalid={!!shown.username}
            />
          </FormField>
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
          {/* password input with a hint about the password rules (IIE, 2026) */}
          <FormField id='password' label='Password' error={shown.password}>
            <input
              id='password'
              type='password'
              autoComplete='new-password'
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              aria-invalid={!!shown.password}
            />
            <p className='hint'>At least 8 characters, with a number and an uppercase letter.</p>
          </FormField>
          {/* dropdown to choose between a client or freelancer account (IIE, 2026) */}
          <FormField id='role' label='I want to' error={shown.role}>
            <select
              id='role'
              value={role}
              onChange={(e) => setRole(e.target.value as RegisterRole)}
            >
              <option value='client'>Hire freelancers (client)</option>
              <option value='freelancer'>Sell my services (freelancer)</option>
            </select>
          </FormField>
          <button type='submit' className='btn btn-teal' disabled={submitting}>
            {submitting ? "Creating account…" : "Register"}
          </button>
        </form>
        {/* link for users who already have an account (IIE, 2026) */}
        <p className='muted-top'>
          Already registered?{" "}
          <Link to='/login' className='btn btn-teal'>
            Log in
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
// PNGEgg, 2026. Walter White Say My Name YouTube Hermanos, walter white, television, hat png. [image online] Available at: <https://www.pngegg.com/en/png-tfhlm> [Accessed 10 October 2026].
// Web Tech Talk, 2026. React JS tutorial for beginners | Images and styles. [video online] Available at: <https://www.youtube.com/watch?v=xhAs3DY6xRU> [Accessed 10 October 2026].