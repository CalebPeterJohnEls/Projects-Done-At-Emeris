// imports for React, routing, login details, API calls and shared helpers (IIE, 2026)
import React, { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useAuth } from "../context/useAuth.ts";
import { createBooking, getErrorMessage, getGig } from "../services/api.ts";
import type { Gig } from "../models/gig.ts";
import type { BookingConfirmation } from "../models/booking.ts";
import { Alert } from "../components/Alert.tsx";
import { formatDate, formatMoney, refLabel } from "../lib/format.ts";

// shows one gig and lets a logged-in client book it (IIE, 2026)
export const GigDetailPage: React.FC = () => {
  // gig id taken from the page URL (IIE, 2026)
  const { id = "" } = useParams();
  const { user } = useAuth();
  // state for the gig, loading, errors, booking progress and the confirmation (IIE, 2026)
  const [gig, setGig] = useState<Gig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [booking, setBooking] = useState(false);
  const [bookError, setBookError] = useState<string | null>(null);
  const [confirmation, setConfirmation] = useState<BookingConfirmation | null>(null);

  // loads the gig when the page opens; "cancelled" stops updates if the page closes first (IIE, 2026)
  useEffect(() => {
    let cancelled = false;
    getGig(id)
      .then((g) => !cancelled && setGig(g))
      .catch((err) => !cancelled && setError(getErrorMessage(err)))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, [id]);

  // sends the booking request, then shows the confirmation or an error (IIE, 2026)
  const handleBook = async () => {
    if (!gig) return;
    setBooking(true);
    setBookError(null);
    try {
      setConfirmation(await createBooking(gig._id));
    } catch (err) {
      setBookError(getErrorMessage(err));
    } finally {
      setBooking(false);
    }
  };

  // simple messages while loading or if the gig cannot be found (IIE, 2026)
  if (loading) return <p>Loading gig…</p>;
  if (error || !gig) {
    return (
      <div className='bento-card teal-header'>
        <Alert kind='danger'>{error ?? "Gig not found."}</Alert>
        <Link to='/'>Back to gigs</Link>
      </div>
    );
  }

  // simulated confirmation screen shown after a successful booking (IIE, 2026)
  if (confirmation) {
    const { booking: b, transaction: t } = confirmation;
    return (
      <div className='bento-card teal-header'>
        <h2 className='card-title'>Booking confirmed</h2>
        <Alert kind='success'>
          Your booking was successful. This is a simulated payment - no real money moved.
        </Alert>
        <p><strong>Gig:</strong> {gig.title}</p>
        <p><strong>Amount:</strong> {formatMoney(t?.amount ?? b.price ?? b.amount ?? gig.price)}</p>
        <p><strong>Status:</strong> {b.status}</p>
        {t?.reference && <p><strong>Reference:</strong> {t.reference}</p>}
        {b.createdAt && <p><strong>Date:</strong> {formatDate(b.createdAt)}</p>}
        <div className='btn-group'>
          <Link className='btn btn-teal' to='/bookings'>View my bookings</Link>
          <Link className='btn btn-outline' to='/'>Keep browsing</Link>
        </div>
      </div>
    );
  }

  // main gig page; the layout comes from (IIE, 2026) and the classNames link to the CSS styles (Bro Code, 2023)
  return (
    <div className='bento-card teal-header'>
      <h2 className='card-title'>
        <span>{gig.title}</span>
        <span className='badge badge-teal'>{gig.category}</span>
      </h2>
      <p className='muted'>By {refLabel(gig.freelancer)}</p>
      {/* React escapes this text, so no raw HTML is ever rendered (IIE, 2026) */}
      <p className='gig-description'>{gig.description}</p>
      <p className='price'>{formatMoney(gig.price)}</p>

      {bookError && <Alert kind='danger'>{bookError}</Alert>}

      {/* only client accounts see the book button (IIE, 2026) */}
      {user?.role === "client" && (
        <button className='btn btn-teal' onClick={handleBook} disabled={booking}>
          {booking ? "Booking…" : "Book this gig"}
        </button>
      )}
      {/* visitors who are not logged in are sent to the login page (IIE, 2026) */}
      {!user && (
        <p>
          <Link to='/login' state={{ from: `/gigs/${gig._id}` }}>Log in</Link> as a client to book this gig.
        </p>
      )}
      {/* freelancers and admins cannot book (IIE, 2026) */}
      {user && user.role !== "client" && (
        <p className='muted'>Only client accounts can book gigs.</p>
      )}
      <p className='muted-top'>
      <Link className='btn btn-red' to='/'>
        ← Back to gigs
      </Link>
    </p>
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