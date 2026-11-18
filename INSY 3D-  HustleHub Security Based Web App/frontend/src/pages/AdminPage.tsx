import React, { useCallback, useEffect, useState } from "react";
import {
  adminDeleteGig,
  adminGetBookings,
  adminGetTransactions,
  adminGetUsers,
  getErrorMessage,
  getGigs,
} from "../services/api.ts";
import type { Gig } from "../models/gig.ts";
import type { Booking, Transaction } from "../models/booking.ts";
import type { User } from "../models/user.ts";
import { Alert } from "../components/Alert.tsx";
import { formatDate, formatMoney, refLabel } from "../lib/format.ts";

type Tab = "users" | "bookings" | "transactions" | "gigs";

const TABS: Tab[] = ["users", "bookings", "transactions", "gigs"];

// admin: read-only overview of the platform plus removing any gig
// (IIE), 2026

export const AdminPage: React.FC = () => {
  const [tab, setTab] = useState<Tab>("users");
  const [users, setUsers] = useState<User[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [gigs, setGigs] = useState<Gig[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  // Retrieve all administration data without directly updating React state.
  const fetchAdminData = useCallback(async () => {
    const [u, b, t, g] = await Promise.all([
      adminGetUsers(),
      adminGetBookings(),
      adminGetTransactions(),
      getGigs(),
    ]);

    return {
      users: u,
      bookings: b,
      transactions: t,
      gigs: g,
    };
  }, []);

  // Apply the retrieved data to the component state.
  const applyAdminData = useCallback(
    (data: {
      users: User[];
      bookings: Booking[];
      transactions: Transaction[];
      gigs: Gig[];
    }) => {
      setUsers(data.users);
      setBookings(data.bookings);
      setTransactions(data.transactions);
      setGigs(data.gigs);
      setError(null);
    },
    []
  );

  // Load administration data when the page is mounted.
  useEffect(() => {
    let cancelled = false;

    const loadData = async () => {
      try {
        const data = await fetchAdminData();

        if (!cancelled) {
          applyAdminData(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(getErrorMessage(err));
        }
      }
    };

    void loadData();

    return () => {
      cancelled = true;
    };
  }, [fetchAdminData, applyAdminData]);

  // Remove a gig and refresh the administration data.
  const handleDelete = async (id: string) => {
    setMessage(null);
    setError(null);

    try {
      const res = await adminDeleteGig(id);

      setMessage(res?.message ?? "Gig removed.");

      const data = await fetchAdminData();
      applyAdminData(data);
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="bento-card teal-header">
      <h2 className="card-title">Admin</h2>

      <div className="btn-group tab-row" role="tablist">
        {TABS.map((t) => (
          <button
            key={t}
            type="button"
            role="tab"
            aria-selected={tab === t}
            className={`btn btn-sm ${
              tab === t ? "btn-teal" : "btn-outline"
            }`}
            onClick={() => setTab(t)}
          >
            {t[0].toUpperCase() + t.slice(1)}
          </button>
        ))}
      </div>

      {error && <Alert kind="danger">{error}</Alert>}
      {message && <Alert kind="success">{message}</Alert>}

      {tab === "users" &&
        (users.length === 0 ? (
          <p className="muted">No users.</p>
        ) : (
          users.map((u) => (
            <div key={u._id} className="book-item">
              <div className="book-info">
                <h4>
                  {u.username}{" "}
                  <span className="badge badge-teal">{u.role}</span>
                </h4>
                <p>{u.email}</p>
              </div>
            </div>
          ))
        ))}

      {tab === "bookings" &&
        (bookings.length === 0 ? (
          <p className="muted">No bookings.</p>
        ) : (
          bookings.map((b) => (
            <div key={b._id} className="book-item">
              <div className="book-info">
                <h4>
                  {refLabel(b.gig)}{" "}
                  <span className="badge badge-blue">{b.status}</span>
                </h4>
                <p>
                  Client: {refLabel(b.client)} | Freelancer:{" "}
                  {refLabel(b.freelancer)} |{" "}
                  {formatMoney(b.price ?? b.amount)} |{" "}
                  {formatDate(b.createdAt)}
                </p>
              </div>
            </div>
          ))
        ))}

      {tab === "transactions" &&
        (transactions.length === 0 ? (
          <p className="muted">No transactions.</p>
        ) : (
          transactions.map((t) => (
            <div key={t._id} className="book-item">
              <div className="book-info">
                <h4>
                  {t.reference}{" "}
                  <span className="badge badge-purple">{t.status}</span>
                </h4>
                <p>
                  {refLabel(t.gig)} | {formatMoney(t.amount)} | Client:{" "}
                  {refLabel(t.client)} | Freelancer: {refLabel(t.freelancer)}
                </p>
              </div>
            </div>
          ))
        ))}

      {tab === "gigs" &&
        (gigs.length === 0 ? (
          <p className="muted">No gigs.</p>
        ) : (
          gigs.map((g) => (
            <div key={g._id} className="book-item">
              <div className="book-info">
                <h4>
                  {g.title}{" "}
                  <span className="badge badge-teal">{g.category}</span>
                </h4>
                <p>
                  By {refLabel(g.freelancer)} | {formatMoney(g.price)}
                </p>
              </div>

              <button
                type="button"
                className="btn btn-danger btn-sm"
                onClick={() => handleDelete(g._id)}
              >
                Remove
              </button>
            </div>
          ))
        ))}
    </div>
  );
};

/*
Reference List

The Independent Institute of Education (IIE), 2026.
Information Systems 3D [INSY7314/W Module Manual].
The Independent Institute of Education: Unpublished.
*/
