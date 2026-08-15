import Link from "next/link";
import type { Company } from "@/types";
import { ContactedBadge } from "./ContactedBadge";

export function CompanyCard({ company }: { company: Company }) {
  const location = [company.city, company.state].filter(Boolean).join(", ") || "Location unavailable";
  const googleSearch = `https://www.google.com/search?q=${encodeURIComponent(`${company.name} Malaysia official website`)}`;
  return <article className="group rounded-2xl border border-slate-800 bg-slate-900/65 p-5 transition hover:-translate-y-0.5 hover:border-cyan-500/40 hover:bg-slate-900">
    <div className="flex items-start justify-between gap-3"><div><p className="mb-1 text-xs text-slate-500">{location}</p><h3 className="font-semibold leading-snug text-white">{company.name}</h3></div><ContactedBadge contacted={company.contacted} /></div>
    <div className="mt-4 text-sm text-amber-300">{company.googleRating !== null ? <>★ {company.googleRating.toFixed(1)} <span className="text-xs text-slate-500">({company.googleReviewCount ?? 0} Google reviews)</span></> : <span className="text-xs text-slate-500">No Google rating</span>}</div>
    <div className="mt-5 grid gap-2 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
      <Link href={`/email/${company.id}`} className="inline-flex min-h-10 items-center justify-center rounded-lg bg-cyan-400 px-3 py-2 text-center text-sm font-bold text-slate-950 transition hover:bg-cyan-300">Generate cold email</Link>
      <a href={googleSearch} target="_blank" rel="noreferrer" className="inline-flex min-h-10 items-center justify-center rounded-lg border border-slate-700 px-3 py-2 text-center text-sm font-semibold text-slate-300 transition hover:border-cyan-400/60 hover:text-cyan-300">Research on Google ↗</a>
    </div>
  </article>;
}
