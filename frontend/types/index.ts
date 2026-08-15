export interface Company {
  id: string; name: string; description: string | null; website: string | null;
  email: string | null; phone: string | null; address: string | null;
  city: string | null; state: string | null; latitude: number | null; longitude: number | null;
  nacsaLicence: string | null; nacsaStatus: string | null; sourceUrl: string | null;
  services: string[]; contacted: boolean; contactedAt: string | null; lastVerifiedAt: string | null;
}

export interface CompanyMapRecord {
  id: string; name: string; latitude: number | null; longitude: number | null;
  city: string | null; state: string | null; contacted: boolean;
}

export interface CompanyFilters {
  search: string; state: string; city: string; service: string;
  nacsaLicence: string; contacted: "" | "true" | "false";
}

export interface PagedResponse<T> {
  content: T[]; page: number; size: number; totalElements: number;
  totalPages: number; first: boolean; last: boolean;
}

export type HealthResponse = { status: string };

export interface Candidate {
  id: string;
  name: string | null;
  email: string | null;
  cvUploaded: boolean;
  updatedAt: string;
}

export interface GeneratedEmail {
  subject: string;
  body: string;
}
