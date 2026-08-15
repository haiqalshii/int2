export function LoadingState() { return <div className="grid gap-3" aria-label="Loading companies">{[1, 2, 3].map((item) => <div key={item} className="h-36 animate-pulse rounded-2xl border border-slate-800 bg-slate-900/60" />)}</div>; }

