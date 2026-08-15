import type { CompanyFilters } from "@/types";

const STATES = ["Johor", "Kedah", "Kelantan", "Kuala Lumpur", "Labuan", "Melaka", "Negeri Sembilan", "Pahang", "Penang", "Perak", "Perlis", "Putrajaya", "Sabah", "Sarawak", "Selangor", "Terengganu"];
const inputClass = "h-10 w-full rounded-lg border border-slate-700 bg-slate-900 px-3 text-sm text-slate-200 outline-none focus:border-cyan-400";

export function FilterPanel({ filters, onChange, onClear }: { filters: CompanyFilters; onChange: (field: keyof CompanyFilters, value: string) => void; onClear: () => void }) {
  const hasFilters = Object.values(filters).some(Boolean);
  return <section className="rounded-2xl border border-slate-800 bg-slate-900/60 p-4" aria-label="Company filters">
    <div className="mb-3 flex items-center justify-between"><h2 className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">Filters</h2><button type="button" onClick={onClear} disabled={!hasFilters} className="text-xs font-medium text-cyan-400 disabled:cursor-not-allowed disabled:text-slate-600">Clear all</button></div>
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-1">
      <select aria-label="State" value={filters.state} onChange={(e) => onChange("state", e.target.value)} className={inputClass}><option value="">All states</option>{STATES.map((state) => <option key={state}>{state}</option>)}</select>
      <input aria-label="City" value={filters.city} onChange={(e) => onChange("city", e.target.value)} placeholder="Any city" className={inputClass} />
      <select aria-label="Google reputation" value={filters.ranking} onChange={(e) => onChange("ranking", e.target.value)} className={inputClass}><option value="">Any Google reputation</option><option value="top">Top rated (4.5+, 20+ reviews)</option><option value="high">Highly rated (4.0+, 10+ reviews)</option><option value="popular">Well known (100+ reviews)</option></select>
      <select aria-label="Contacted status" value={filters.contacted} onChange={(e) => onChange("contacted", e.target.value)} className={inputClass}><option value="">Any contact status</option><option value="false">Not contacted</option><option value="true">Contacted</option></select>
    </div>
  </section>;
}
