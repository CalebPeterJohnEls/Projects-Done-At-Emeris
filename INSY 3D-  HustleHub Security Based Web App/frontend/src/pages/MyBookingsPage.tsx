import React, { useEffect, useState } from "react";
import { getErrorMessage, getMyBookings } from "../services/api.ts";
import type { Booking } from "../models/booking.ts";
import { Alert } from "../components/Alert.tsx";
import { formatDate, formatMoney, refLabel } from "../lib/format.ts";

// client: their own bookings
//(IIE), 2026
export const MyBookingsPage: React.FC = () => {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    getMyBookings()
      .then((b) => !cancelled && setBookings(b))
      .catch((err) => !cancelled && setError(getErrorMessage(err)))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, []);
//(IIE), 2026
  return (
    <div className='bento-card teal-header'>
      <h2 className='card-title'>My bookings</h2>
      {error && <Alert kind='danger'>{error}</Alert>}
      {loading ? (
        <p>Loading…</p>
      ) : bookings.length === 0 ? (
        <p className='muted'>You have not booked any gigs yet.</p>
      ) : (
        bookings.map((b) => (
          <div key={b._id} className='book-item'>
            <div className='book-info'>
              <h4>
                {refLabel(b.gig)} <span className='badge badge-blue'>{b.status}</span>
              </h4>
              <p>
                Freelancer: {refLabel(b.freelancer)} | {formatMoney(b.price ?? b.amount)} |{" "}
                {formatDate(b.createdAt)}
              </p>
            </div>
          </div>
        ))
      )}
    </div>
  );
};
/* Reference List 
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.*/