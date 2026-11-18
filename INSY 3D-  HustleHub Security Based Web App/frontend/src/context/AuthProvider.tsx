import React, { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext, type AuthContextType } from "./AuthContext.ts";
import type {
  AuthResponse,
  LoginCredentials,
  LoginResult,
  RegisterCredentials,
  User,
  UserRole,
} from "../models/user.ts";
import {
  fieldErrorMap,
  getAuthToken,
  getErrorMessage,
  getStoredUser,
  isTokenExpired,
  loginUser,
  registerUser,
  removeAuthToken,
  removeStoredUser,
  setAuthToken,
  setStoredUser,
  setUnauthorizedHandler,
  verifyTwoFactorLogin,
  logoutCurrentSession,
  setTokenChangedHandler,
  ApiError,
} from "../services/api.ts";

// IIE, 2026

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({
  children,
}) => {

  // Restore the existing authentication session during initialisation.
  // This avoids synchronous state updates inside a useEffect.
  // (IIE), 2026
  const [initialSession] = useState(() => {
    const savedToken = getAuthToken();
    const savedUser = getStoredUser();

    if (
      savedToken &&
      savedUser &&
      !isTokenExpired(savedToken)
    ) {
      return {
        token: savedToken,
        user: savedUser,
      };
    }

    // Remove expired or incomplete sessions.
    removeAuthToken();
    removeStoredUser();

    return {
      token: null,
      user: null,
    };
  });

  const [user, setUser] = useState<User | null>(
    initialSession.user
  );

  const [token, setToken] = useState<string | null>(
    initialSession.token
  );

  // Session restoration is synchronous, so no loading state is required.
  const isLoading = false;

  const [error, setError] = useState<string | null>(null);

  const [fieldErrors, setFieldErrors] = useState<
    Record<string, string>
  >({});

  // The short-lived 2FA token is kept in memory only,
  // never in localStorage (OWASP, 2026 e).
  const [tempToken, setTempToken] = useState<string | null>(null);

  // Clear the locally stored authentication session.
  const clearSession = useCallback(() => {
    removeAuthToken();
    removeStoredUser();
    setUser(null);
    setToken(null);
  }, []);

  // Register the token-change handler.
  useEffect(() => {
    setTokenChangedHandler(setToken);

    return () => {
      setTokenChangedHandler(null);
    };
  }, []);

  // Start a session after successful authentication.
  const startSession = useCallback((data: AuthResponse) => {
    const { token: newToken, ...profile } = data;

    const safeUser: User = {
      _id: profile._id,
      username: profile.username,
      email: profile.email,
      role: profile.role,
    };

    setAuthToken(newToken);
    setStoredUser(safeUser);

    setToken(newToken);
    setUser(safeUser);
  }, []);

  // Log out the current server-side session and clear local credentials.
  const logout = useCallback(async () => {
    setError(null);
    setFieldErrors({});

    try {
      await logoutCurrentSession();
    } catch (err) {
      // A rejected or expired session is already unusable.
      // Other failures must remain visible so logout can be retried.
      if (!(err instanceof ApiError && err.status === 401)) {
        setError(getErrorMessage(err));
        throw err;
      }
    }

    clearSession();
    setTempToken(null);
  }, [clearSession]);

  // Any 401 from the API (expired/invalid token) logs the user out.
  // (IIE), 2026
  useEffect(() => {
    setUnauthorizedHandler(() => {
      setUser(null);
      setToken(null);
    });

    return () => {
      setUnauthorizedHandler(null);
    };
  }, []);

  // If the server asks for a 2FA code, no session starts yet.
  // The temporary token is kept in memory only (OWASP, 2026 e).
  const run = useCallback(
    async (action: () => Promise<LoginResult>) => {
      setError(null);
      setFieldErrors({});

      try {
        const data = await action();

        if ("twoFactorRequired" in data) {
          setTempToken(data.tempToken);
          return;
        }

        startSession(data);
      } catch (err) {
        setError(getErrorMessage(err));
        setFieldErrors(fieldErrorMap(err));

        throw err; // Let the form know it failed.
      }
    },
    [startSession]
  );

  // (IIE), 2026
  // Checks the credentials, errors and role.
  const login = useCallback(
    (credentials: LoginCredentials) =>
      run(() => loginUser(credentials)),
    [run]
  );

  const register = useCallback(
    (credentials: RegisterCredentials) =>
      run(() => registerUser(credentials)),
    [run]
  );

  // Second step of login: the code or recovery code is checked by the server.
  // (OWASP, 2026 e)
  const completeTwoFactor = useCallback(
    async (code: string) => {
      if (!tempToken) return;

      setError(null);
      setFieldErrors({});

      try {
        startSession(
          await verifyTwoFactorLogin(tempToken, code)
        );

        setTempToken(null);
      } catch (err) {
        setError(getErrorMessage(err));
        setFieldErrors(fieldErrorMap(err));

        throw err; // Let the form know it failed.
      }
    },
    [tempToken, startSession]
  );

  // Cancel the pending two-factor authentication process.
  const cancelTwoFactor = useCallback(() => {
    setTempToken(null);
    setError(null);
    setFieldErrors({});
  }, []);

  // Clear authentication-related error messages.
  const clearError = useCallback(() => {
    setError(null);
    setFieldErrors({});
  }, []);

  // Check whether the authenticated user has one of the specified roles.
  const hasRole = useCallback(
    (...roles: UserRole[]) =>
      !!user && roles.includes(user.role),
    [user]
  );

  // Share authentication information and functions through AuthContext.
  // (IIE), 2026
  const value = useMemo<AuthContextType>(
    () => ({
      user,
      token,
      isAuthenticated: !!user && !!token,
      isLoading,
      error,
      fieldErrors,
      twoFactorPending: !!tempToken,
      login,
      register,
      completeTwoFactor,
      cancelTwoFactor,
      logout,
      clearError,
      hasRole,
    }),
    [
      user,
      token,
      isLoading,
      error,
      fieldErrors,
      tempToken,
      login,
      register,
      completeTwoFactor,
      cancelTwoFactor,
      logout,
      clearError,
      hasRole,
    ]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

/*
Reference List

The Independent Institute of Education (IIE), 2026.
Information Systems 3D [INSY7314/W Module Manual].
The Independent Institute of Education: Unpublished.

OWASP, 2026 e. Multifactor Authentication Cheat Sheet.
[online] Available at:
https://cheatsheetseries.owasp.org/cheatsheets/Multifactor_Authentication_Cheat_Sheet.html
[Accessed 23 October 2026].
*/
