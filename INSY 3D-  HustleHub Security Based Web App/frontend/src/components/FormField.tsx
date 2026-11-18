import React from "react";

interface Props {
  id: string;
  label: string;
  error?: string;
  children: React.ReactNode;
}

// label + control + per-field error message, wired up for screen readers
export const FormField: React.FC<Props> = ({ id, label, error, children }) => (
  <div className='form-group'>
    <label htmlFor={id}>{label}</label>
    {children}
    {error && (
      <p className='field-error' id={`${id}-error`} role='alert'>
        {error}
      </p>
    )}
  </div>
);
/* Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.*/