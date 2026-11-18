import React, { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { getErrorMessage, getGigs } from "../services/api.ts";
import { GIG_CATEGORIES, type Gig } from "../models/gig.ts";
import { Alert } from "../components/Alert.tsx";
import { formatMoney, refLabel } from "../lib/format.ts";

// public gig browsing
//(IIE), 2026
export const GigsPage: React.FC = () => {
  const [gigs, setGigs] = useState<Gig[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setGigs(await getGigs());
    } catch (err) {
      setGigs([]);
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);
//(IIE), 2026
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load();
  }, [load]);
//(IIE), 2026
  const visible = useMemo(() => {
    const q = search.trim().toLowerCase();
    return gigs.filter(
      (g) =>
        (!category || g.category === category) &&
        (!q || g.title.toLowerCase().includes(q) || g.description.toLowerCase().includes(q)),
    );
  }, [gigs, search, category]);

  return (
    <div className='bento-grid'>
      <div className='bento-card bento-col-12 teal-header'>
        <h2 className='card-title'>
          <span>Browse gigs</span>
          <button className='btn btn-outline btn-sm' onClick={load} disabled={loading}>
            Refresh
          </button>
        </h2>
        <div className='form-row'>
          <div className='form-group'>
            <label htmlFor='search'>Search</label>
            <input
              id='search'
              type='search'
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder='Search title or description'
            />
          </div>
          <div className='form-group'>
            <label htmlFor='category'>Category</label>
            <select id='category' value={category} onChange={(e) => setCategory(e.target.value)}>
              <option value=''>All categories</option>
              {GIG_CATEGORIES.map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
          </div>
        </div>
        {error && <Alert kind='danger'>{error}</Alert>}
        {loading ? (
          <p>Loading gigs…</p>
        ) : visible.length === 0 ? (
          <p className='muted'>No gigs found.</p>
        ) : (
          visible.map((gig) => (
            <div key={gig._id} className='book-item'>
              <div className='book-info'>
                <h4>
                  {gig.title} <span className='badge badge-teal'>{gig.category}</span>
                </h4>
                <p>
                  By {refLabel(gig.freelancer)}{" "}
                  | {formatMoney(gig.price)}
                </p>
              </div>
              <Link className='btn btn-teal btn-sm' to={`/gigs/${gig._id}`}>
                View
              </Link>
            </div>
          ))
        )}
      </div>
    </div>
  );
};
/* Reference List 
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.*/