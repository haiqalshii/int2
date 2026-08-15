"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { getCompany } from "@/lib/api";
import type { Company } from "@/types";
import { CompanyContact } from "./CompanyContact";
import { CompanyServices } from "./CompanyServices";
import { ContactedBadge } from "./ContactedBadge";
import { ErrorState } from "./ErrorState";

function safeSource(value: string | null): string | null {
  if (!value) return null;
  try { const url = new URL(value); return ["http:", "https:"].includes(url.protocol) ? url.toString() : null; } catch { return null; }
}

export function CompanyDetails({ companyId }: { companyId: string }) {
  const [company, setCompany] = useState<Company | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [requestKey, setRequestKey] = useState(0);

  useEffect(() => {
    const controller = new AbortController(); setError(null);
    getCompany(companyId, controller.signal).then(setCompany).catch((reason: unknown) => {
      if (!(reason instanceof DOMException && reason.name === "AbortError")) setError(reason instanceof Error ? reason.message : "Unexpected error");
    });
    return () => controller.abort();
  }, [companyId, requestKey]);

  if (error) return <main className="mx-auto min-h-screen max-w-3xl px-4 py-16"><Link href="/" className="mb-6 inline-block text-sm text-cyan-400">← Back to discovery</Link><ErrorState message={error} onRetry={() => setRequestKey((key) => key + 1)} /></main>;
  if (!company) return <main className="mx-auto min-h-screen max-w-5xl animate-pulse px-4 py-10"><div className="h-5 w-32 rounded bg-slate-800" /><div className="mt-12 h-12 max-w-xl rounded bg-slate-800" /><div className="mt-8 grid gap-5 md:grid-cols-2"><div className="h-72 rounded-2xl bg-slate-900" /><div className="h-72 rounded-2xl bg-slate-900" /></div></main>;

  const source = safeSource(company.sourceUrl);
  const verified = company.lastVerifiedAt ? new Intl.DateTimeFormat("en-MY", { dateStyle: "medium", timeZone: "Asia/Kuala_Lumpur" }).format(new Date(company.lastVerifiedAt)) : null;
  return <main className="min-h-screen bg-[radial-gradient(circle_at_top_right,rgba(34,211,238,0.08),transparent_28rem)] px-4 py-8 sm:px-6">
    <div className="mx-auto max-w-6xl">
      <Link href="/" className="text-sm font-semibold text-cyan-400 hover:text-cyan-300">← Back to discovery</Link>
      <header className="mt-8 border-b border-slate-800 pb-8">
        <div className="flex flex-col gap-6 md:flex-row md:items-end md:justify-between"><div className="max-w-3xl"><div className="mb-4 flex flex-wrap items-center gap-2"><span className="rounded-full bg-cyan-400/10 px-3 py-1 text-xs font-semibold text-cyan-300 ring-1 ring-inset ring-cyan-400/20">NACSA licence holder</span><ContactedBadge contacted={company.contacted} /></div><h1 className="text-3xl font-bold tracking-tight text-white sm:text-5xl">{company.name}</h1><p className="mt-3 text-sm text-slate-400">{[company.city, company.state].filter(Boolean).join(", ") || "Location unavailable"}</p></div>
          <Link href={`/email/${company.id}`} className="inline-flex min-h-12 items-center justify-center rounded-xl bg-cyan-400 px-5 py-3 text-sm font-bold text-slate-950 transition hover:bg-cyan-300">Generate cold email <span className="ml-2">→</span></Link>
        </div>
      </header>
      <div className="mt-6 grid gap-5 lg:grid-cols-[minmax(0,1.2fr)_minmax(320px,.8fr)]">
        <div className="space-y-5"><section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6"><h2 className="text-sm font-semibold uppercase tracking-[0.16em] text-slate-400">Company intelligence</h2><p className="mt-4 whitespace-pre-line text-[15px] leading-7 text-slate-300">{company.description || "No verified company description is available yet."}</p><div className="mt-6 rounded-xl border border-amber-400/15 bg-amber-400/5 p-4"><p className="text-xs font-semibold uppercase tracking-wider text-amber-300">Internship availability</p><p className="mt-1 text-sm text-amber-100/70">No public information found.</p></div></section><CompanyServices services={company.services} /></div>
        <div className="space-y-5"><CompanyContact company={company} /><section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6"><h2 className="text-sm font-semibold uppercase tracking-[0.16em] text-slate-400">NACSA record</h2><dl className="mt-4 space-y-4 text-sm"><div><dt className="text-slate-500">Licence</dt><dd className="mt-1 text-slate-200">{company.nacsaLicence || "Not provided"}</dd></div><div><dt className="text-slate-500">Status</dt><dd className="mt-1 text-slate-200">{company.nacsaStatus || "Not provided"}</dd></div><div><dt className="text-slate-500">Last verified</dt><dd className="mt-1 text-slate-200">{verified || "Not recorded"}</dd></div></dl>{source && <a href={source} target="_blank" rel="noreferrer" className="mt-5 inline-flex text-sm font-semibold text-cyan-400 hover:text-cyan-300">View source ↗</a>}</section></div>
      </div>
    </div>
  </main>;
}

