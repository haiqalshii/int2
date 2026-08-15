"use client";

import { APIProvider, InfoWindow, Map, Marker, useMap } from "@vis.gl/react-google-maps";
import { useEffect, useState } from "react";
import type { CompanyMapRecord } from "@/types";

type PhotoCredit = { name: string; uri: string | null };

function PlacePhoto({ company }: { company: CompanyMapRecord }) {
  const [photoUrl, setPhotoUrl] = useState<string | null | undefined>();
  const [credit, setCredit] = useState<PhotoCredit | null>(null);
  useEffect(() => {
    if (company.latitude === null || company.longitude === null) return;
    let cancelled = false;
    setPhotoUrl(undefined);
    setCredit(null);
    void (async () => {
      try {
        const { Place } = await google.maps.importLibrary("places") as google.maps.PlacesLibrary;
        const { places } = await Place.searchByText({
          textQuery: `${company.name}, Malaysia`,
          fields: ["displayName", "location", "photos"],
          locationBias: { lat: company.latitude!, lng: company.longitude! },
          language: "en",
          region: "my",
          maxResultCount: 1,
        });
        const photo = places[0]?.photos?.[0];
        if (cancelled) return;
        if (!photo) {
          setPhotoUrl(null);
          return;
        }
        setPhotoUrl(photo.getURI({ maxWidth: 480, maxHeight: 240 }));
        const attribution = photo.authorAttributions[0];
        if (attribution) setCredit({ name: attribution.displayName, uri: attribution.uri ?? null });
      } catch {
        if (!cancelled) setPhotoUrl(null);
      }
    })();
    return () => { cancelled = true; };
  }, [company.id, company.latitude, company.longitude]);

  return <div className="relative mt-3 h-32 w-64 overflow-hidden rounded-lg bg-slate-200">
    {photoUrl && <img src={photoUrl} alt={`${company.name} on Google Maps`} className="h-full w-full object-cover" />}
    {photoUrl === undefined && <div className="absolute inset-0 grid place-items-center text-xs text-slate-500">Loading Google Maps photo…</div>}
    {photoUrl === null && <div className="absolute inset-0 grid place-items-center px-4 text-center text-xs text-slate-500">No Google Maps photo available</div>}
    {photoUrl && credit && <div className="absolute bottom-0 left-0 right-0 bg-black/65 px-2 py-1 text-[10px] text-white">Photo by {credit.uri ? <a href={credit.uri} target="_blank" rel="noreferrer" className="underline">{credit.name}</a> : credit.name}</div>}
  </div>;
}

function FitMarkers({ companies }: { companies: CompanyMapRecord[] }) {
  const map = useMap();
  useEffect(() => {
    if (!map || companies.length === 0) return;
    const bounds = new google.maps.LatLngBounds();
    companies.forEach(({ latitude, longitude }) => bounds.extend({ lat: latitude!, lng: longitude! }));
    map.fitBounds(bounds, 48);
    const listener = google.maps.event.addListenerOnce(map, "idle", () => {
      if ((map.getZoom() ?? 0) > 14) map.setZoom(14);
    });
    return () => google.maps.event.removeListener(listener);
  }, [companies, map]);
  return null;
}

function GoogleCompanyMap({ companies }: { companies: CompanyMapRecord[] }) {
  const located = companies.filter((company) => company.latitude !== null && company.longitude !== null);
  const [selected, setSelected] = useState<CompanyMapRecord | null>(null);
  useEffect(() => {
    if (selected && !located.some((company) => company.id === selected.id)) setSelected(null);
  }, [located, selected]);
  return <div className="relative h-full min-h-[420px] overflow-hidden rounded-2xl border border-slate-800 bg-slate-900">
    <Map defaultCenter={{ lat: 4.2105, lng: 101.9758 }} defaultZoom={6} gestureHandling="greedy" mapTypeControl={false} streetViewControl={false} className="h-full min-h-[420px] w-full">
      <FitMarkers companies={located} />
      {located.map((company) => <Marker key={company.id} position={{ lat: company.latitude!, lng: company.longitude! }} title={company.name} onClick={() => setSelected((current) => current?.id === company.id ? null : company)} />)}
      {selected && <InfoWindow position={{ lat: selected.latitude!, lng: selected.longitude! }} onCloseClick={() => setSelected(null)}><div className="min-w-64 text-slate-900"><div className="flex items-start justify-between gap-3"><p className="font-semibold">{selected.name}</p><button type="button" onClick={() => setSelected(null)} aria-label="Close company popup" className="-mr-1 -mt-1 grid h-7 w-7 shrink-0 place-items-center rounded-full bg-slate-200 text-lg leading-none text-slate-700 hover:bg-slate-300">×</button></div><p className="mt-1 text-xs text-slate-600">{[selected.city, selected.state].filter(Boolean).join(", ") || "Malaysia"}</p>{selected.googleRating !== null && <p className="mt-1 text-xs font-semibold text-amber-600">★ {selected.googleRating.toFixed(1)} · {selected.googleReviewCount ?? 0} reviews</p>}<PlacePhoto company={selected} />{selected.contacted && <p className="mt-2 text-xs font-semibold text-emerald-700">Contacted</p>}<a href={`https://www.google.com/search?q=${encodeURIComponent(`${selected.name} Malaysia official website`)}`} target="_blank" rel="noreferrer" className="mt-3 inline-block text-sm font-semibold text-cyan-700">Search company on Google →</a></div></InfoWindow>}
    </Map>
    <div className="absolute bottom-5 left-5 z-10 rounded-lg bg-slate-950/90 px-3 py-2 text-xs text-slate-300 shadow-xl ring-1 ring-white/10">{located.length} mapped · {companies.length - located.length} without coordinates</div>
  </div>;
}

export function CompanyMap({ companies }: { companies: CompanyMapRecord[] }) {
  const [apiKey, setApiKey] = useState<string | null>();
  useEffect(() => { void fetch("/api/maps-config").then((response) => response.ok ? response.json() : Promise.reject()).then((data: { apiKey: string }) => setApiKey(data.apiKey)).catch(() => setApiKey(null)); }, []);
  if (apiKey === undefined) return <div className="min-h-[420px] animate-pulse rounded-2xl bg-slate-900" />;
  if (!apiKey) return <div className="flex min-h-[420px] items-center justify-center rounded-2xl border border-amber-800/60 bg-amber-950/20 p-6 text-center text-sm text-amber-200">Google Maps is not configured.</div>;
  return <APIProvider apiKey={apiKey}><GoogleCompanyMap companies={companies} /></APIProvider>;
}
