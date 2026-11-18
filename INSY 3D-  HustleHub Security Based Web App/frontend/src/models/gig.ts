export const GIG_CATEGORIES = [
  "Web Development",
  "Graphic Design",
  "Writing",
  "Photography",
  "Video Editing",
  "Marketing",
  "Programming",
  "Music & Audio",
  "Tutoring",
  "Data Entry",
] as const;

export type GigCategory = (typeof GIG_CATEGORIES)[number];

// Public gig lists populate the freelancer with just a username; /mine returns the id.
//(IIE), 2026
export type FreelancerRef = string | { _id: string; username: string };

export interface Gig {
  _id: string;
  title: string;
  description: string;
  category: GigCategory;
  price: number;
  freelancer: FreelancerRef;
  isActive: boolean;
  createdAt?: string;
  updatedAt?: string;
}

// the only fields the API accepts when creating/updating a gig
//(IIE), 2026
export interface GigInput {
  title: string;
  description: string;
  category: GigCategory;
  price: number;
}
/* Reference List 
The Independent Institute of Education (IIE), 2026. Information Systems 3D [INSY7314/W Module Manual]. The Independent Institute of Education: Unpublished.*/