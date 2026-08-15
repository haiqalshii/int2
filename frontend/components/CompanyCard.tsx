import Link from "next/link";
import type { Company } from "@/types";
import { ContactedBadge } from "./ContactedBadge";

export function CompanyCard({ company }: { company: Company }) {
  const location = [company.city, company.state].filter(Boolean).join(", ") || "Location unavailable";
  return <article className="group rounded-2xl border border-slate-800 bg-slate-900/65 p-5 transition hover:-translate-y-0.5 hover:border-cyan-500/40 hover:bg-slate-900">
    <div className="flex items-start justify-between gap-3"><div><p className="mb-1 text-xs text-slate-500">{location}</p><h3 className="font-semibold leading-snug text-white">{company.name}</h3></div><ContactedBadge contacted={company.contacted} /></div>
    <div className="mt-4 flex flex-wrap gap-2">{company.services.slice(0, 3).map((service) => <span key={service} className="rounded-md bg-cyan-400/5 px-2 py-1 text-xs text-cyan-200 ring-1 ring-inset ring-cyan-400/15">{service}</span>)}{!company.services.length && <span className="text-xs text-slate-500">No normalized services</span>}</div>
    <Link href={`/company/${company.id}`} className="mt-5 inline-flex items-center gap-2 text-sm font-semibold text-cyan-400 transition group-hover:text-cyan-300">View intelligence <span aria-hidden="true">→</span></Link>
  </article>;
}

