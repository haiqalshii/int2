export function ContactedBadge({ contacted }: { contacted: boolean }) {
  return contacted ? <span className="rounded-full bg-emerald-400/10 px-2.5 py-1 text-xs font-medium text-emerald-300 ring-1 ring-inset ring-emerald-400/20">Contacted</span> : null;
}

