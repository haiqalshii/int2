"use client";

import { useState } from "react";
import { updateContacted } from "@/lib/api";
import type { Company } from "@/types";

export function ContactedControl({ company, onChange }: { company: Company; onChange: (company: Company) => void }) {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const contactedDate = company.contactedAt
    ? new Intl.DateTimeFormat("en-MY", { dateStyle: "medium", timeStyle: "short", timeZone: "Asia/Kuala_Lumpur" }).format(new Date(company.contactedAt))
    : null;

  async function toggle() {
    setSaving(true); setError(null);
    try { onChange(await updateContacted(company.id, !company.contacted)); }
    catch (reason: unknown) { setError(reason instanceof Error ? reason.message : "Unable to update contacted status."); }
    finally { setSaving(false); }
  }

  return <div className={`rounded-xl border p-4 ${company.contacted ? "border-emerald-400/20 bg-emerald-400/5" : "border-slate-800 bg-slate-900/50"}`}>
    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
      <div><p className={`text-sm font-semibold ${company.contacted ? "text-emerald-300" : "text-slate-200"}`}>{company.contacted ? "Company contacted" : "Not contacted yet"}</p><p className="mt-1 text-xs text-slate-500">{contactedDate ? `Marked ${contactedDate}` : "Mark this after you send your email manually."}</p></div>
      <button type="button" disabled={saving} onClick={toggle} className={`min-h-10 shrink-0 rounded-lg px-4 text-sm font-bold transition disabled:cursor-wait disabled:opacity-60 ${company.contacted ? "border border-slate-700 text-slate-300 hover:border-slate-500" : "bg-emerald-400 text-slate-950 hover:bg-emerald-300"}`}>{saving ? "Saving…" : company.contacted ? "Mark as not contacted" : "Mark as contacted"}</button>
    </div>
    {error && <p role="alert" className="mt-3 text-xs text-rose-300">{error}</p>}
  </div>;
}
