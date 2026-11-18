import React, { useEffect, useState } from "react";
import { getAccountActivity, getErrorMessage } from "../services/api.ts";
import type { ActivityEvent, ActivityType } from "../models/user.ts";
import { Alert } from "./Alert.tsx";
import { formatDate } from "../lib/format.ts";

const LABELS: Record<ActivityType, string> = {
  login_success: "Signed in",
  login_failed: "Failed sign-in attempt",
  two_factor_failed: "Wrong two-factor code",
  password_changed: "Password changed",
  sessions_revoked: "Signed out of other devices",
};

const FAILURES: ActivityType[] = ["login_failed", "two_factor_failed"];

// the last few security events on this account, so unfamiliar activity stands out.
// `refreshKey` changes after a password change or sign-out so the list reloads.
export const ActivityCard: React.FC<{ refreshKey?: number }> = ({ refreshKey = 0 }) => {
  const [events, setEvents] = useState<ActivityEvent[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    getAccountActivity()
      .then((list) => {
        if (!cancelled) {
          setEvents(list);
          setError(null);
        }
      })
      .catch((err) => {
        if (!cancelled) setError(getErrorMessage(err));
      });
    return () => {
      cancelled = true;
    };
  }, [refreshKey]);

  return (
    <div className='bento-card bento-col-6 teal-header auth-card'>
      <h2 className='card-title'>Recent activity</h2>
      {error && <Alert kind='danger'>{error}</Alert>}
      {events === null && !error && <p>Loading…</p>}
      {events !== null && events.length === 0 && <p className='muted'>No activity recorded yet.</p>}
      {events !== null && events.length > 0 && (
        <ul style={{ listStyle: "none", padding: 0, margin: 0 }} data-testid='activity-list'>
          {events.map((ev) => (
            <li key={ev._id} data-testid='activity-item' style={{ padding: "8px 0", borderBottom: "1px solid rgba(128, 128, 128, 0.25)" }}>
              <div>
                <strong>{LABELS[ev.type] ?? ev.type}</strong>
                {FAILURES.includes(ev.type) && <span className='badge badge-gray' style={{ marginLeft: 8 }}>Not you?</span>}
              </div>
              <div className='muted'>
                {ev.device} · {ev.ip || "unknown address"} · {formatDate(ev.createdAt)}
              </div>
            </li>
          ))}
        </ul>
      )}
      <p className='muted'>
        See something you do not recognise? Change your password and sign out of your other devices.
      </p>
    </div>
  );
};