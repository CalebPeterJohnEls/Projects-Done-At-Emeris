import React, { useCallback, useEffect, useState } from "react";
import {
  createGig,
  deleteGig,
  fieldErrorMap,
  getErrorMessage,
  getIncome,
  getMyGigs,
  getReceivedBookings,
  updateGig,
} from "../services/api.ts";
import { GIG_CATEGORIES, type Gig, type GigCategory } from "../models/gig.ts";
import type { Booking, Income } from "../models/booking.ts";
import { Alert } from "../components/Alert.tsx";
import { FormField } from "../components/FormField.tsx";
import {
  BracketBreakdown,
  MonthlyIncomeChart,
  TaxExplainer,
  TaxSummaryCards,
} from "../components/IncomeInsights.tsx";
import { EMPTY_INCOME } from "../lib/income.ts";
import { validateGig, type Errors } from "../lib/validation.ts";
import { formatDate, formatMoney, refLabel } from "../lib/format.ts";

// freelancer: manage gigs, see received bookings, income and the tax estimate
export const DashboardPage: React.FC = () => {
  const [gigs, setGigs] = useState<Gig[]>([]);
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [income, setIncome] = useState<Income>(EMPTY_INCOME);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);
  const [formError, setFormError] = useState<string | null>(null);

  const [editingId, setEditingId] = useState<string | null>(null);
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [category, setCategory] = useState<GigCategory | "">("");
  const [price, setPrice] = useState("");
  const [errors, setErrors] = useState<Errors>({});
  const [submitting, setSubmitting] = useState(false);

  const load = useCallback(async () => {
    try {
      const [g, b, i] = await Promise.all([getMyGigs(), getReceivedBookings(), getIncome()]);
      setGigs(g);
      setBookings(b);
      setIncome(i);
      setLoadError(null);
    } catch (err) {
      setLoadError(getErrorMessage(err));
    }
  }, []);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect
    load();
  }, [load]);

  const resetForm = () => {
    setEditingId(null);
    setTitle("");
    setDescription("");
    setCategory("");
    setPrice("");
    setErrors({});
    setFormError(null);
  };

  const startEdit = (gig: Gig) => {
    setEditingId(gig._id);
    setTitle(gig.title);
    setDescription(gig.description);
    setCategory(gig.category);
    setPrice(String(gig.price));
    setErrors({});
    setFormError(null);
    setMessage(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMessage(null);
    setFormError(null);
    const found = validateGig(title, description, category, price);
    setErrors(found);
    if (Object.keys(found).length > 0) return;

    const payload = {
      title: title.trim(),
      description: description.trim(),
      category: category as GigCategory,
      price: Number(price),
    };
    setSubmitting(true);
    try {
      if (editingId) await updateGig(editingId, payload);
      else await createGig(payload);
      setMessage(editingId ? "Gig updated." : "Gig created.");
      resetForm();
      await load();
    } catch (err) {
      setFormError(getErrorMessage(err));
      setErrors(fieldErrorMap(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id: string) => {
    setMessage(null);
    try {
      const res = await deleteGig(id);
      setMessage(res?.message ?? "Gig removed.");
      if (editingId === id) resetForm();
      await load();
    } catch (err) {
      setLoadError(getErrorMessage(err));
    }
  };

  return (
    <div className='bento-grid'>
      <div className='bento-card bento-col-4' style={{ borderLeft: "5px solid #0d9448" }}>
        <h3 className='card-title'>Income</h3>
        <div className='stats-num'>{formatMoney(income.total)}</div>
        <div className='stats-label'>
          Total earned{income.taxYear ? ` (${income.taxYear} tax year)` : ""}
        </div>
        <div className='stats-num stats-small'>{gigs.length}</div>
        <div className='stats-label'>My gigs</div>
      </div>

      <div className='bento-card bento-col-8 teal-header'>
        <h3 className='card-title'>
          <span>{editingId ? "Edit gig" : "Create a gig"}</span>
          {editingId && (
            <button type='button' className='btn btn-outline btn-sm' onClick={resetForm}>
              Cancel edit
            </button>
          )}
        </h3>
        {message && <Alert kind='success'>{message}</Alert>}
        {formError && <Alert kind='danger'>{formError}</Alert>}
        <form onSubmit={handleSubmit} noValidate>
          <FormField id='title' label='Title' error={errors.title}>
            <input id='title' type='text' value={title} maxLength={100} onChange={(e) => setTitle(e.target.value)} />
          </FormField>
          <FormField id='description' label='Description' error={errors.description}>
            <textarea id='description' rows={4} value={description} maxLength={2000} onChange={(e) => setDescription(e.target.value)} />
          </FormField>
          <div className='form-row'>
            <FormField id='gig-category' label='Category' error={errors.category}>
              <select id='gig-category' value={category} onChange={(e) => setCategory(e.target.value as GigCategory | "")}>
                <option value=''>Choose…</option>
                {GIG_CATEGORIES.map((c) => (
                  <option key={c} value={c}>{c}</option>
                ))}
              </select>
            </FormField>
            <FormField id='price' label='Price (ZAR)' error={errors.price}>
              <input id='price' type='number' min='0' step='0.01' value={price} onChange={(e) => setPrice(e.target.value)} />
            </FormField>
          </div>
          <button type='submit' className='btn btn-teal' disabled={submitting}>
            {submitting ? "Saving…" : editingId ? "Save changes" : "Create gig"}
          </button>
        </form>
      </div>

      {loadError && (
        <div className='bento-col-12'>
          <Alert kind='danger'>{loadError}</Alert>
        </div>
      )}

      <div className='bento-card bento-col-12 teal-header'>
        <h3 className='card-title'>Tax estimate{income.taxYear ? ` (${income.taxYear})` : ""}</h3>
        <TaxSummaryCards income={income} />
        <TaxExplainer income={income} />
      </div>

      <div className='bento-card bento-col-12 teal-header'>
        <h3 className='card-title'>Income by tax bracket</h3>
        <BracketBreakdown income={income} />
      </div>

      <div className='bento-card bento-col-12 teal-header'>
        <h3 className='card-title'>Monthly income</h3>
        <MonthlyIncomeChart income={income} />
      </div>

      <div className='bento-card bento-col-12 teal-header'>
        <h3 className='card-title'>My gigs</h3>
        {gigs.length === 0 ? (
          <p className='muted'>You have not created any gigs yet.</p>
        ) : (
          gigs.map((g) => (
            <div key={g._id} className='book-item'>
              <div className='book-info'>
                <h4>
                  {g.title} <span className='badge badge-teal'>{g.category}</span>{" "}
                  {!g.isActive && <span className='badge badge-gray'>inactive</span>}
                </h4>
                <p>{formatMoney(g.price)}</p>
              </div>
              <div className='btn-group'>
                <button className='btn btn-outline btn-sm' onClick={() => startEdit(g)}>Edit</button>
                <button className='btn btn-danger btn-sm' onClick={() => handleDelete(g._id)}>Delete</button>
              </div>
            </div>
          ))
        )}
      </div>

      <div className='bento-card bento-col-12 teal-header'>
        <h3 className='card-title'>Bookings received</h3>
        {bookings.length === 0 ? (
          <p className='muted'>No bookings yet.</p>
        ) : (
          bookings.map((b) => (
            <div key={b._id} className='book-item'>
              <div className='book-info'>
                <h4>
                  {refLabel(b.gig)} <span className='badge badge-blue'>{b.status}</span>
                </h4>
                <p>
                  Client: {refLabel(b.client)} | {formatMoney(b.price ?? b.amount)} | {formatDate(b.createdAt)}
                </p>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};