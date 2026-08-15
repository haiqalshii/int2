"use client";
import { FormEvent, useState } from "react";

export default function LoginPage() {
  const [error, setError] = useState(""); const [loading, setLoading] = useState(false);
  async function login(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setLoading(true); setError("");
    const password = new FormData(event.currentTarget).get("password");
    const response = await fetch("/api/auth/login", { method: "POST", headers: { "Content-Type": "application/json" }, body: JSON.stringify({ password }) });
    if (response.ok) window.location.href = "/";
    else { const body = await response.json().catch(() => null); setError(body?.message ?? "Unable to sign in."); setLoading(false); }
  }
  return <main className="grid min-h-screen place-items-center bg-[radial-gradient(circle_at_top,rgba(34,211,238,0.12),transparent_32rem)] px-4"><form onSubmit={login} className="w-full max-w-sm rounded-2xl border border-slate-800 bg-slate-900/80 p-7 shadow-2xl"><p className="text-xs font-bold uppercase tracking-[0.2em] text-cyan-400">INT² · Private</p><h1 className="mt-3 text-2xl font-bold text-white">Hey love, this space is just for you 💌</h1><p className="mt-2 text-sm leading-6 text-slate-400">Enter our password and I’ll help you find your next opportunity.</p><label className="mt-6 block text-sm font-medium text-slate-300">Password<input name="password" type="password" required autoFocus className="mt-2 h-11 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 text-white outline-none focus:border-cyan-400" /></label>{error && <p className="mt-3 text-sm text-rose-300">{error}</p>}<button disabled={loading} className="mt-5 h-11 w-full rounded-lg bg-cyan-400 text-sm font-bold text-slate-950 hover:bg-cyan-300 disabled:opacity-60">{loading ? "Opening…" : "Let me in ♡"}</button></form></main>;
}
