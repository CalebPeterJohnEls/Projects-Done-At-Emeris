
import React, { useEffect, useState } from "react";
import {
  getAccountSessions,
  getErrorMessage,
} from "../services/api.ts";
import type { ActiveSession } from "../models/user.ts";
import { Alert } from "./Alert.tsx";
import { formatDate } from "../lib/format.ts";

export const ActiveDevicesCard: React.FC<{
  refreshKey?: number;
}> = ({ refreshKey = 0 }) => {
  const [sessions, setSessions] = useState<ActiveSession[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [reloadKey, setReloadKey] = useState(0);

  useEffect(() => {
    let cancelled = false;

    getAccountSessions()
      .then((list) => {
        if (!cancelled) {
          setSessions(list);
          setError(null);
        }
      })
      .catch((err) => {
        if (!cancelled) {
          setError(getErrorMessage(err));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [refreshKey, reloadKey]);

  // Refresh the device list when requested by the user.
  const handleRefresh = () => {
    setLoading(true);
    setError(null);
    setReloadKey((key) => key + 1);
  };

  return (
    <div className="bento-card bento-col-6 teal-header auth-card">
      <h2 className="card-title">Signed-in devices</h2>

      <p className="muted">
        Browser sessions that are still signed in. Device names are
        estimated from browser information.
      </p>

      {error && <Alert kind="danger">{error}</Alert>}

      {loading && <p>Loading…</p>}

      {!loading && !error && sessions.length === 0 && (
        <p className="muted">No active sessions found.</p>
      )}

      {!loading && !error && (
        <ul
          style={{
            listStyle: "none",
            padding: 0,
            margin: 0,
          }}
        >
          {sessions.map((session) => (
            <li
              key={session._id}
              style={{
                padding: "12px 0",
                borderBottom: "1px solid rgba(128, 128, 128, 0.25)",
              }}
            >
              <div>
                <strong>{session.device}</strong>

                {session.isCurrent && (
                  <span
                    className="badge badge-gray"
                    style={{ marginLeft: 8 }}
                  >
                    This session
                  </span>
                )}
              </div>

              <div className="muted">
                {session.ip || "Unknown address"}
              </div>

              <div className="muted">
                Signed in: {formatDate(session.createdAt)}
              </div>

              <div className="muted">
                Last active: {formatDate(session.lastSeenAt)}
              </div>
            </li>
          ))}
        </ul>
      )}

      <button
        type="button"
        className="btn btn-outline"
        disabled={loading}
        onClick={handleRefresh}
        style={{ marginTop: 12 }}
      >
        Refresh devices
      </button>
    </div>
  );
};
