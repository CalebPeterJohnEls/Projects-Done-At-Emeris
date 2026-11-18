import React from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/useAuth.ts";
import type { UserRole } from "../models/user.ts";

interface Props {
  // if omitted, any logged-in user may enter
  roles?: UserRole[];
}

// Route guard. Not logged in -> /login (remembering where they were going).
// Logged in with the wrong role -> a friendly "not allowed" panel.
// This is a UX guard only; the API enforces the real RBAC.
//(IIE), 2026
export const ProtectedRoute: React.FC<Props> = ({ roles }) => {
  const { isAuthenticated, isLoading, user } = useAuth();
  const location = useLocation();

  if (isLoading) return <p>Loading…</p>;

  if (!isAuthenticated) {
    return <Navigate to='/login' replace state={{ from: location.pathname }} />;
  }

  if (roles && user && !roles.includes(user.role)) {
    return (
      <div className='bento-card teal-header'>
        <h2 className='card-title'>Not allowed</h2>
        <p>Your account does not have access to this page.</p>
      </div>
    );
  }

  return <Outlet />;
};
/* Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.*/