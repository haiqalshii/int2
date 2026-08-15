"use client";

import { useState } from "react";
import type { GeneratedEmail } from "@/types";

interface EmailPreviewProps {
  email: GeneratedEmail;
  onChange: (email: GeneratedEmail) => void;
}

export function EmailPreview({ email, onChange }: EmailPreviewProps) {
  const [copied, setCopied] = useState<"subject" | "body" | "email" | null>(null);

  async function copy(value: string, target: "subject" | "body" | "email") {
    try {
      await navigator.clipboard.writeText(value);
      setCopied(target);
      window.setTimeout(() => setCopied(null), 1800);
    } catch {
      setCopied(null);
    }
  }

  return <section className="rounded-2xl border border-slate-800 bg-slate-900/70 p-5 shadow-2xl shadow-black/20 sm:p-7">
    <div className="flex flex-col gap-3 border-b border-slate-800 pb-5 sm:flex-row sm:items-center sm:justify-between">
      <div><p className="text-xs font-semibold uppercase tracking-[0.18em] text-cyan-400">Draft email</p><h2 className="mt-1 text-xl font-bold text-white">Review before sending</h2></div>
      <button type="button" onClick={() => copy(`Subject: ${email.subject}\n\n${email.body}`, "email")} className="min-h-11 rounded-xl bg-cyan-400 px-4 py-2 text-sm font-bold text-slate-950 transition hover:bg-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-300">
        {copied === "email" ? "Copied email" : "Copy full email"}
      </button>
    </div>

    <div className="mt-6 space-y-6">
      <label className="block"><span className="text-sm font-semibold text-slate-300">Subject</span><div className="mt-2 flex gap-2"><input value={email.subject} maxLength={500} onChange={(event) => onChange({ ...email, subject: event.target.value })} className="min-h-12 min-w-0 flex-1 rounded-xl border border-slate-700 bg-slate-950 px-4 text-sm text-white outline-none transition focus:border-cyan-400" /><button type="button" onClick={() => copy(email.subject, "subject")} className="rounded-xl border border-slate-700 px-4 text-sm font-semibold text-slate-200 hover:border-slate-500">{copied === "subject" ? "Copied" : "Copy"}</button></div></label>
      <label className="block"><span className="text-sm font-semibold text-slate-300">Body</span><textarea value={email.body} maxLength={5000} rows={14} onChange={(event) => onChange({ ...email, body: event.target.value })} className="mt-2 w-full resize-y rounded-xl border border-slate-700 bg-slate-950 p-4 text-sm leading-7 text-slate-200 outline-none transition focus:border-cyan-400" /><div className="mt-2 flex items-center justify-between"><span className="text-xs text-slate-500">{email.body.length.toLocaleString()} characters</span><button type="button" onClick={() => copy(email.body, "body")} className="rounded-lg border border-slate-700 px-3 py-2 text-xs font-semibold text-slate-200 hover:border-slate-500">{copied === "body" ? "Copied" : "Copy body"}</button></div></label>
    </div>
    <p className="mt-6 rounded-xl border border-amber-400/15 bg-amber-400/5 p-4 text-xs leading-5 text-amber-100/70">INTÂ² creates a draft only. Verify every detail, then copy and send it manually from your email account.</p>
  </section>;
}
