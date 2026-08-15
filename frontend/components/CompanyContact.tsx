import type { Company } from "@/types";

function safeHttpUrl(value: string | null): string | null {
  if (!value) return null;
  try { const url = new URL(value); return ["http:", "https:"].includes(url.protocol) ? url.toString() : null; } catch { return null; }
}

function Row({ label, children }: { label: string; children: React.ReactNode }) {
  return <div className="grid gap-1 border-b border-slate-800 py-4 last:border-0 sm:grid-cols-[120px_1fr]"><dt className="text-xs font-semibold uppercase tracking-wider text-slate-500">{label}</dt><dd className="break-words text-sm text-slate-200">{children}</dd></div>;
}

export function CompanyContact({ company }: { company: Company }) {
  const website = safeHttpUrl(company.website);
  return <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6">
    <h2 className="text-sm font-semibold uppercase tracking-[0.16em] text-slate-400">Contact and location</h2>
    <dl className="mt-2">
      <Row label="Address">{company.address || "No public address found."}</Row>
      <Row label="Location">{[company.city, company.state].filter(Boolean).join(", ") || "No public location found."}</Row>
      <Row label="Website">{website ? <a href={website} target="_blank" rel="noreferrer" className="text-cyan-400 hover:text-cyan-300">{company.website} ↗</a> : "No official website found."}</Row>
      <Row label="Email">{company.email ? <a href={`mailto:${company.email}`} className="text-cyan-400 hover:text-cyan-300">{company.email}</a> : "No public email found."}</Row>
      <Row label="Phone">{company.phone ? <a href={`tel:${company.phone}`} className="text-cyan-400 hover:text-cyan-300">{company.phone}</a> : "No public phone number found."}</Row>
    </dl>
  </section>;
}

