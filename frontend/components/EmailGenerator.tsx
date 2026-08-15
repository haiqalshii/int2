"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { generateEmail, getCompany } from "@/lib/api";
import type { Company, GeneratedEmail } from "@/types";
import { EmailPreview } from "./EmailPreview";
import { ErrorState } from "./ErrorState";
import { ContactedControl } from "./ContactedControl";

export function EmailGenerator({ companyId }: { companyId: string }) {
  const [company, setCompany] = useState<Company | null>(null);
  const [email, setEmail] = useState<GeneratedEmail | null>(null);
  const [loadingCompany, setLoadingCompany] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    setLoadingCompany(true);
    getCompany(companyId, controller.signal).then(setCompany).catch((reason: unknown) => {
      if (!(reason instanceof DOMException && reason.name === "AbortError")) setError(reason instanceof Error ? reason.message : "Unable to load this company.");
    }).finally(() => setLoadingCompany(false));
    return () => controller.abort();
  }, [companyId]);

  async function generate() {
    setGenerating(true); setError(null);
    try { setEmail(await generateEmail(companyId)); }
    catch (reason: unknown) { setError(reason instanceof Error ? reason.message : "Unable to generate an email right now."); }
    finally { setGenerating(false); }
  }

  if (loadingCompany) return <main className="mx-auto min-h-screen max-w-5xl animate-pulse px-4 py-10"><div className="h-5 w-36 rounded bg-slate-800" /><div className="mt-12 h-10 max-w-lg rounded bg-slate-800" /><div className="mt-8 h-96 rounded-2xl bg-slate-900" /></main>;
  if (!company) return <main className="mx-auto min-h-screen max-w-3xl px-4 py-16"><Link href="/" className="mb-6 inline-block text-sm text-cyan-400">← Back to discovery</Link><ErrorState title="Unable to load company" message={error ?? "Company not found."} onRetry={() => window.location.reload()} /></main>;

  return <main className="min-h-screen bg-[radial-gradient(circle_at_top_right,rgba(34,211,238,0.08),transparent_28rem)] px-4 py-8 sm:px-6">
    <div className="mx-auto max-w-5xl">
      <Link href={`/company/${company.id}`} className="text-sm font-semibold text-cyan-400 hover:text-cyan-300">← Back to company</Link>
      <header className="mt-8 flex flex-col gap-6 border-b border-slate-800 pb-8 sm:flex-row sm:items-end sm:justify-between">
        <div><p className="text-xs font-semibold uppercase tracking-[0.2em] text-cyan-400">Cold outreach</p><h1 className="mt-2 text-3xl font-bold tracking-tight text-white sm:text-4xl">Email {company.name}</h1><p className="mt-3 max-w-2xl text-sm leading-6 text-slate-400">Generate a grounded internship inquiry using your uploaded CV and this company’s verified intelligence.</p></div>
        <button type="button" disabled={generating} onClick={generate} className="inline-flex min-h-12 shrink-0 items-center justify-center rounded-xl bg-cyan-400 px-5 py-3 text-sm font-bold text-slate-950 transition hover:bg-cyan-300 disabled:cursor-wait disabled:opacity-60">{generating ? "Generating…" : email ? "Regenerate draft" : "Generate email"}</button>
      </header>

      {error && <div className="mt-6"><ErrorState title="Unable to generate email" message={error} onRetry={generate} />{error.toLowerCase().includes("cv") && <Link href="/settings" className="mt-3 inline-flex text-sm font-semibold text-cyan-400 hover:text-cyan-300">Upload your CV in settings →</Link>}</div>}
      {generating && <section aria-live="polite" className="mt-6 rounded-2xl border border-cyan-400/15 bg-slate-900/70 p-8 text-center"><div className="mx-auto h-8 w-8 animate-spin rounded-full border-2 border-slate-700 border-t-cyan-400" /><p className="mt-4 font-semibold text-slate-200">Writing a personalized draft…</p><p className="mt-1 text-sm text-slate-500">This can take a few moments.</p></section>}
      {!generating && email && <div className="mt-6 space-y-5"><EmailPreview email={email} onChange={setEmail} /><ContactedControl company={company} onChange={setCompany} /></div>}
      {!generating && !email && !error && <section className="mt-6 rounded-2xl border border-dashed border-slate-700 bg-slate-900/40 px-6 py-16 text-center"><div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-cyan-400/10 text-xl text-cyan-300">✦</div><h2 className="mt-5 text-lg font-bold text-white">Ready to create your draft</h2><p className="mx-auto mt-2 max-w-md text-sm leading-6 text-slate-400">INTÂ² will use only facts from your CV and the company profile. You can edit and copy the result before sending.</p><button type="button" onClick={generate} className="mt-6 min-h-12 rounded-xl border border-cyan-400/40 px-5 text-sm font-bold text-cyan-300 hover:bg-cyan-400/10">Generate email</button></section>}
    </div>
  </main>;
}
