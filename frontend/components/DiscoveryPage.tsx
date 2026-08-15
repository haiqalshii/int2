"use client";

import dynamic from "next/dynamic";
import Link from "next/link";
import { useEffect, useState } from "react";
import { getCompanies, getMapCompanies } from "@/lib/api";
import type { CompanyFilters, CompanyMapRecord, PagedResponse, Company } from "@/types";
import { CompanyList } from "./CompanyList";
import { EmptyState } from "./EmptyState";
import { ErrorState } from "./ErrorState";
import { FilterPanel } from "./FilterPanel";
import { LoadingState } from "./LoadingState";
import { SearchBar } from "./SearchBar";

const CompanyMap = dynamic(() => import("./CompanyMap").then((module) => module.CompanyMap), { ssr: false, loading: () => <div className="h-full min-h-[420px] animate-pulse rounded-2xl bg-slate-900" /> });
const EMPTY_FILTERS: CompanyFilters = { search: "", state: "", city: "", ranking: "", contacted: "" };

export function DiscoveryPage() {
  const [filters, setFilters] = useState<CompanyFilters>(EMPTY_FILTERS);
  const [debouncedFilters, setDebouncedFilters] = useState(filters);
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<PagedResponse<Company> | null>(null);
  const [mapCompanies, setMapCompanies] = useState<CompanyMapRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [requestKey, setRequestKey] = useState(0);

  useEffect(() => { const timer = setTimeout(() => setDebouncedFilters(filters), 350); return () => clearTimeout(timer); }, [filters]);
  useEffect(() => {
    const controller = new AbortController(); setLoading(true); setError(null);
    Promise.all([getCompanies(debouncedFilters, page, 20, controller.signal), getMapCompanies(debouncedFilters, controller.signal)])
      .then(([companies, mapped]) => { setResult(companies); setMapCompanies(mapped); })
      .catch((reason: unknown) => { if (!(reason instanceof DOMException && reason.name === "AbortError")) setError(reason instanceof Error ? reason.message : "Unexpected error"); })
      .finally(() => { if (!controller.signal.aborted) setLoading(false); });
    return () => controller.abort();
  }, [debouncedFilters, page, requestKey]);

  const changeFilter = (field: keyof CompanyFilters, value: string) => { setFilters((current) => ({ ...current, [field]: value })); setPage(0); };
  const clearFilters = () => { setFilters(EMPTY_FILTERS); setPage(0); };

  return <main className="min-h-screen bg-[radial-gradient(circle_at_top_right,rgba(34,211,238,0.08),transparent_28rem)] px-4 py-6 sm:px-6 lg:px-8">
    <header className="mx-auto mb-6 flex max-w-[1600px] items-center justify-between"><div><p className="text-xl font-black tracking-tight text-white">INT<span className="text-cyan-400">²</span></p><p className="text-xs text-slate-500">Intern Intelligence</p></div><div className="flex items-center gap-2"><div className="hidden rounded-full border border-slate-800 bg-slate-900/70 px-3 py-1.5 text-xs text-slate-400 sm:block">Malaysia · Cybersecurity</div><Link href="/settings" className="rounded-full border border-slate-700 bg-slate-900 px-3 py-1.5 text-xs font-semibold text-slate-200 hover:border-cyan-400/50">CV settings</Link></div></header>
    <div className="mx-auto grid max-w-[1600px] gap-5 lg:grid-cols-[380px_minmax(0,1fr)]">
      <aside className="space-y-4"><div><h1 className="text-2xl font-bold tracking-tight text-white">Hey love, let me help you find the right opportunity 💌</h1><p className="mt-2 text-sm leading-6 text-slate-400">I made this little space to make your internship search easier. Pick a company you like, and I’ll help you make the first move. You’ve got this, baby ♡</p></div><SearchBar value={filters.search} onChange={(value) => changeFilter("search", value)} /><FilterPanel filters={filters} onChange={changeFilter} onClear={clearFilters} />
        <section><div className="mb-3 flex items-end justify-between"><div><p className="text-xs uppercase tracking-wider text-slate-500">Companies</p><p className="mt-0.5 text-sm font-semibold text-slate-200">{result?.totalElements ?? 0} results</p></div>{result && result.totalPages > 1 && <p className="text-xs text-slate-500">Page {result.page + 1} of {result.totalPages}</p>}</div>
          {loading ? <LoadingState /> : error ? <ErrorState message={error} onRetry={() => setRequestKey((key) => key + 1)} /> : !result?.content.length ? <EmptyState onClear={clearFilters} /> : <CompanyList companies={result.content} />}
          {result && result.totalPages > 1 && <div className="mt-4 flex gap-2"><button type="button" disabled={result.first || loading} onClick={() => setPage((value) => value - 1)} className="flex-1 rounded-lg border border-slate-700 px-3 py-2 text-sm disabled:opacity-40">Previous</button><button type="button" disabled={result.last || loading} onClick={() => setPage((value) => value + 1)} className="flex-1 rounded-lg border border-slate-700 px-3 py-2 text-sm disabled:opacity-40">Next</button></div>}
        </section>
      </aside>
      <section className="min-h-[520px] lg:sticky lg:top-6 lg:h-[calc(100vh-3rem)]"><CompanyMap companies={mapCompanies} /></section>
    </div>
  </main>;
}
