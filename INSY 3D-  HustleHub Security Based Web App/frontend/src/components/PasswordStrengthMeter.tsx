import React from "react";
import { passwordStrength } from "../lib/validation.ts";

// index = score. The label always says the strength in words, so colour is never the only signal.
const COLOURS = ["#c0392b", "#c0392b", "#e67e22", "#d4a017", "#0d9448"];

// live strength bar and rule checklist under a "new password" field
export const PasswordStrengthMeter: React.FC<{ password: string }> = ({ password }) => {
  const s = passwordStrength(password);
  return (
    <div data-testid='strength-meter' style={{ margin: "6px 0 12px" }}>
      <div style={{ display: "flex", gap: 4 }} aria-hidden='true'>
        {[1, 2, 3, 4].map((i) => (
          <div
            key={i}
            data-testid='strength-segment'
            data-filled={i <= s.score}
            style={{
              flex: 1,
              height: 6,
              borderRadius: 3,
              background: i <= s.score ? COLOURS[s.score] : "rgba(128, 128, 128, 0.3)",
            }}
          />
        ))}
      </div>
      <div className='muted' aria-live='polite' data-testid='strength-label'>
        {password ? `Strength: ${s.label}` : "Use at least 8 characters, a number and a capital letter."}
      </div>
      <ul style={{ listStyle: "none", padding: 0, margin: "4px 0 0" }}>
        {s.checks.map((c) => (
          <li key={c.label} data-ok={c.ok}>
            {c.ok ? "✓" : "○"} {c.label}
          </li>
        ))}
      </ul>
    </div>
  );
};