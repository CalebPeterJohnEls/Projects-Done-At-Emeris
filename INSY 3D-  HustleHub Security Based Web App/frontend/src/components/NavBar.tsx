// imports for React, routing links, login details and the logo image (IIE, 2026)
import React from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth.ts";
// logo image imported as a file so it is bundled with the app (Facebook Inc., 2020)
import wwLogo from "../images/wwLogo.png";

// picks the CSS class for the active or inactive link (IIE, 2026; Bro Code, 2023)
const linkClass = ({ isActive }: { isActive: boolean }) =>
  isActive ? "nav-link-active" : "nav-link";

// links change with the logged-in user's role (IIE, 2026)
export const NavBar: React.FC = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  // logs the user out and returns them to the login page (IIE, 2026)
  const handleLogout = async () => {
  try {
    await logout();
    navigate("/login"); // use your existing login route
  } catch {
    // AuthProvider exposes the error for your UI to display.
  }
};
//Logo:(PNGEgg, 2026) used (Facebook Inc., 2020; Web Tech Talk, 2026)
  return (
    <nav className='navbar' aria-label='Main'>
      <div className='nav-brand'>
        {/* logo size is controlled in CSS by the nav-logo class (Olawanle, 2022) */}
        <img className='nav-logo' src={wwLogo} alt='Logo' />
        <span className='nav-title'>HustleHub+</span>
      </div>

      <div className='nav-links'>
        {/* link everyone can see (IIE, 2026) */}
        <NavLink to='/' end className={linkClass}>
          Browse Gigs
        </NavLink>

        {/* links for client accounts (IIE, 2026) */}
        {user?.role === "client" && (
          <>
            <NavLink to='/bookings' className={linkClass}>
              My Bookings
            </NavLink>
            <NavLink to='/transactions' className={linkClass}>
              My Transactions
            </NavLink>
          </>
        )}

        {/* links for freelancer accounts (IIE, 2026) */}
        {user?.role === "freelancer" && (
          <>
            <NavLink to='/dashboard' className={linkClass}>
              Dashboard
            </NavLink>
            <NavLink to='/transactions' className={linkClass}>
              Transactions
            </NavLink>
          </>
        )}

        {/* link for admin accounts (IIE, 2026) */}
        {user?.role === "admin" && (
          <NavLink to='/admin' className={linkClass}>
            Admin
          </NavLink>
        )}

        {/* shows the username and log out button, or login and register links (IIE, 2026) */}
        {isAuthenticated && user ? (
          <>
            {/* link to the two-factor settings page for every logged-in role (OWASP, 2026 e) */}
            <NavLink to='/security' className={linkClass}>
              Security
            </NavLink>
            <span className='nav-user'>
              {user.username} <span className='badge badge-teal'>{user.role}</span>
            </span>
            <button type='button' className='btn btn-outline btn-sm nav-btn' onClick={handleLogout}>
              Log out
            </button>
          </>
        ) : (
          <>
            <NavLink to='/login' className={linkClass}>
              Log in
            </NavLink>
            <NavLink to='/register' className={linkClass}>
              Register
            </NavLink>
          </>
        )}
      </div>
    </nav>
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