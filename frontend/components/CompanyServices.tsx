export function CompanyServices({ services }: { services: string[] }) {
  return <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-6">
    <h2 className="text-sm font-semibold uppercase tracking-[0.16em] text-slate-400">Cybersecurity services</h2>
    {services.length ? <div className="mt-4 flex flex-wrap gap-2">{services.map((service) => <span key={service} className="rounded-lg bg-cyan-400/5 px-3 py-2 text-sm text-cyan-100 ring-1 ring-inset ring-cyan-400/15">{service}</span>)}</div> : <p className="mt-4 text-sm text-slate-500">No normalized service information is available.</p>}
  </section>;
}

