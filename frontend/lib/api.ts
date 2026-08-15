import type { Candidate, Company, CompanyFilters, CompanyMapRecord, GeneratedEmail, PagedResponse } from "@/types";

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "";

function queryString(filters: CompanyFilters, page?: number, size?: number): string {
  const params = new URLSearchParams();
  Object.entries(filters).forEach(([key, value]) => { if (value) params.set(key, value); });
  if (page !== undefined) params.set("page", String(page));
  if (size !== undefined) params.set("size", String(size));
  return params.size ? `?${params.toString()}` : "";
}

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_URL}${path}`, options);
  if (!response.ok) {
    const error = await response.json().catch(() => null) as { message?: string } | null;
    throw new Error(error?.message ?? "Unable to reach the INT² API.");
  }
  return response.json() as Promise<T>;
}

export const getCompanies = (filters: CompanyFilters, page = 0, size = 20, signal?: AbortSignal) =>
  request<PagedResponse<Company>>(`/api/companies${queryString(filters, page, size)}`, { signal });

export const getMapCompanies = (filters: CompanyFilters, signal?: AbortSignal) =>
  request<CompanyMapRecord[]>(`/api/companies/map${queryString(filters)}`, { signal });

export const getCompany = (id: string, signal?: AbortSignal) => request<Company>(`/api/companies/${id}`, { signal });

export const updateContacted = (id: string, contacted: boolean) => request<Company>(`/api/companies/${id}/contacted`, {
  method: "PATCH", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ contacted }),
});

export const getHealth = () => request<{ status: string }>("/api/health");

export const getCandidate = (signal?: AbortSignal) => request<Candidate>("/api/candidate", { signal });

export const uploadCv = (file: File) => {
  const body = new FormData();
  body.append("file", file);
  return request<Candidate>("/api/candidate/cv", { method: "POST", body });
};

export const generateEmail = (companyId: string) => request<GeneratedEmail>("/api/email/generate", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ companyId }),
});
