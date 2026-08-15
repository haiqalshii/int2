export function SearchBar({ value, onChange }: { value: string; onChange: (value: string) => void }) {
  return <label className="relative block"><span className="sr-only">Search companies</span>
    <svg className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-500" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true"><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></svg>
    <input type="search" value={value} onChange={(event) => onChange(event.target.value)} placeholder="Search companies, cities, or states" className="h-12 w-full rounded-xl border border-slate-700 bg-slate-900/80 pl-12 pr-4 text-sm text-white outline-none transition placeholder:text-slate-500 focus:border-cyan-400 focus:ring-2 focus:ring-cyan-400/15" />
  </label>;
}

