import Link from "next/link";
import { CVUploader } from "@/components/CVUploader";

export default function SettingsPage() {
  return <main className="min-h-screen bg-[radial-gradient(circle_at_top_right,rgba(34,211,238,0.08),transparent_28rem)] px-4 py-8 sm:px-6"><div className="mx-auto max-w-3xl"><Link href="/" className="text-sm font-semibold text-cyan-400">← Back to discovery</Link><header className="mb-8 mt-8"><p className="text-xs font-semibold uppercase tracking-[0.18em] text-cyan-400">Settings</p><h1 className="mt-2 text-3xl font-bold tracking-tight text-white">Candidate profile</h1><p className="mt-3 text-sm leading-6 text-slate-400">Manage the CV INT² uses when generating truthful internship outreach.</p></header><CVUploader /></div></main>;
}
