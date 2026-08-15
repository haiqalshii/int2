"use client";

import { useEffect, useRef, useState } from "react";
import { getCandidate, uploadCv } from "@/lib/api";
import type { Candidate } from "@/types";

const MAX_BYTES = 10 * 1024 * 1024;

export function CVUploader() {
  const inputRef = useRef<HTMLInputElement>(null);
  const [candidate, setCandidate] = useState<Candidate | null>(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    getCandidate(controller.signal).then(setCandidate).catch((reason: unknown) => {
      if (reason instanceof Error && !reason.message.includes("No candidate profile")) setError(reason.message);
    }).finally(() => setLoading(false));
    return () => controller.abort();
  }, []);

  async function choose(file: File | undefined) {
    setError(null); setSuccess(null);
    if (!file) return;
    if (file.type !== "application/pdf" || !file.name.toLowerCase().endsWith(".pdf")) { setError("Please choose a PDF file."); return; }
    if (file.size > MAX_BYTES) { setError("CV files must not exceed 10 MB."); return; }
    setUploading(true);
    try { const updated = await uploadCv(file); setCandidate(updated); setSuccess("CV uploaded and text extracted successfully."); }
    catch (reason) { setError(reason instanceof Error ? reason.message : "Unable to upload the CV."); }
    finally { setUploading(false); if (inputRef.current) inputRef.current.value = ""; }
  }

  const updated = candidate?.updatedAt ? new Intl.DateTimeFormat("en-MY", { dateStyle: "medium", timeStyle: "short" }).format(new Date(candidate.updatedAt)) : null;
  return <section className="rounded-2xl border border-slate-800 bg-slate-900/65 p-6 sm:p-8">
    <div className="flex items-start gap-4"><div className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-cyan-400/10 text-xl text-cyan-300">↥</div><div><h2 className="text-lg font-semibold text-white">Candidate CV</h2><p className="mt-1 text-sm leading-6 text-slate-400">Upload one selectable-text PDF. Your CV content stays server-side and is used only to personalize outreach.</p></div></div>
    <div className="mt-6 rounded-xl border border-dashed border-slate-700 bg-slate-950/30 p-6 text-center">
      {loading ? <p className="text-sm text-slate-500">Checking CV status…</p> : <><p className="text-sm font-medium text-slate-200">{candidate?.cvUploaded ? "A CV is ready for email generation." : "No CV uploaded yet."}</p>{updated && <p className="mt-1 text-xs text-slate-500">Last updated {updated}</p>}</>}
      <input ref={inputRef} type="file" accept="application/pdf,.pdf" className="sr-only" id="cv-file" onChange={(event) => void choose(event.target.files?.[0])} disabled={uploading} />
      <label htmlFor="cv-file" className={`mt-5 inline-flex min-h-11 items-center justify-center rounded-lg px-4 py-2 text-sm font-bold ${uploading ? "cursor-wait bg-slate-700 text-slate-400" : "cursor-pointer bg-cyan-400 text-slate-950 hover:bg-cyan-300"}`}>{uploading ? "Uploading and extracting…" : candidate?.cvUploaded ? "Replace CV" : "Choose PDF CV"}</label>
      <p className="mt-3 text-xs text-slate-500">PDF only · maximum 10 MB · scanned image-only PDFs are not supported</p>
    </div>
    {error && <p role="alert" className="mt-4 rounded-lg border border-rose-400/20 bg-rose-400/5 p-3 text-sm text-rose-200">{error}</p>}
    {success && <p role="status" className="mt-4 rounded-lg border border-emerald-400/20 bg-emerald-400/5 p-3 text-sm text-emerald-200">{success}</p>}
  </section>;
}

