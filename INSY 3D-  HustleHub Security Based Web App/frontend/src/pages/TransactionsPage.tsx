import React, { useEffect, useState } from "react";
import { useAuth } from "../context/useAuth.ts";
import { getErrorMessage, getMyTransactions } from "../services/api.ts";
import type { Transaction } from "../models/booking.ts";
import { Alert } from "../components/Alert.tsx";
import { formatDate, formatMoney, refLabel } from "../lib/format.ts";

// client and freelancer: transactions they are part of
//(IIE), 2026
export const TransactionsPage: React.FC = () => {
  const { user } = useAuth();
  const [items, setItems] = useState<Transaction[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    getMyTransactions()
      .then((t) => !cancelled && setItems(t))
      .catch((err) => !cancelled && setError(getErrorMessage(err)))
      .finally(() => !cancelled && setLoading(false));
    return () => {
      cancelled = true;
    };
  }, []);
//(IIE), 2026
  return (
    <div className='bento-card teal-header'>
      <h2 className='card-title'>My transactions</h2>
      {error && <Alert kind='danger'>{error}</Alert>}
      {loading ? (
        <p>Loading…</p>
      ) : items.length === 0 ? (
        <p className='muted'>No transactions yet.</p>
      ) : (
        items.map((t) => (
          <div key={t._id} className='book-item'>
            <div className='book-info'>
              <h4>
                {refLabel(t.gig)} <span className='badge badge-purple'>{t.status}</span>
              </h4>
              <p>
                Ref: {t.reference} | {formatMoney(t.amount)} |{" "}
                {user?.role === "freelancer"
                  ? `Client: ${refLabel(t.client)}`
                  : `Freelancer: ${refLabel(t.freelancer)}`}{" "}
                | {formatDate(t.createdAt)}
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