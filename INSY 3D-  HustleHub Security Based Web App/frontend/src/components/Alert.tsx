import React from "react";

export const Alert: React.FC<{
  kind: "danger" | "success";
  children: React.ReactNode;
}> = ({ kind, children }) => (
  <div className={`alert alert-${kind}`} role={kind === "danger" ? "alert" : "status"}>
    {children}
  </div>
);
/* Reference List
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.*/