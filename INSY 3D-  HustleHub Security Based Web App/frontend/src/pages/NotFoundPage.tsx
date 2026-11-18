import React from "react";
import { Link } from "react-router-dom";
//(IIE), 2026
export const NotFoundPage: React.FC = () => (
  <div className='bento-card teal-header'>
    <h2 className='card-title'>Page not found</h2>
    <p>That page does not exist.</p>
    <Link to='/'>Back to gigs</Link>
  </div>
);
/* Reference List 
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.*/