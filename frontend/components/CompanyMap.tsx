"use client";

import Link from "next/link";
import L from "leaflet";
import { useEffect } from "react";
import { MapContainer, Marker, Popup, TileLayer, useMap } from "react-leaflet";
import type { CompanyMapRecord } from "@/types";

const markerIcon = (contacted: boolean) => L.divIcon({
  className: "int2-marker-wrap",
  html: `<span class="int2-marker${contacted ? " int2-marker-contacted" : ""}"></span>`,
  iconSize: [24, 24], iconAnchor: [12, 12], popupAnchor: [0, -12],
});

function FitMarkers({ companies }: { companies: CompanyMapRecord[] }) {
  const map = useMap();
  useEffect(() => {
    const positions = companies.filter((company) => company.latitude !== null && company.longitude !== null).map((company) => [company.latitude!, company.longitude!] as [number, number]);
    if (positions.length > 0) map.fitBounds(L.latLngBounds(positions), { padding: [36, 36], maxZoom: 11 });
  }, [companies, map]);
  return null;
}

export function CompanyMap({ companies }: { companies: CompanyMapRecord[] }) {
  const located = companies.filter((company) => company.latitude !== null && company.longitude !== null);
  return <div className="relative h-full min-h-[420px] overflow-hidden rounded-2xl border border-slate-800 bg-slate-900">
    <MapContainer center={[4.2105, 101.9758]} zoom={6} scrollWheelZoom className="h-full min-h-[420px] w-full" attributionControl>
      <TileLayer attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      <FitMarkers companies={located} />
      {located.map((company) => <Marker key={company.id} position={[company.latitude!, company.longitude!]} icon={markerIcon(company.contacted)}><Popup><div className="min-w-48"><p className="font-semibold">{company.name}</p><p className="mt-1 text-xs text-slate-600">{[company.city, company.state].filter(Boolean).join(", ") || "Location unavailable"}</p>{company.contacted && <p className="mt-2 text-xs font-semibold text-emerald-700">Contacted</p>}<Link href={`/company/${company.id}`} className="mt-3 inline-block text-sm font-semibold text-cyan-700">View details →</Link></div></Popup></Marker>)}
    </MapContainer>
    <div className="pointer-events-none absolute bottom-5 left-5 z-[500] rounded-lg bg-slate-950/90 px-3 py-2 text-xs text-slate-300 shadow-xl ring-1 ring-white/10">{located.length} mapped · {companies.length - located.length} without coordinates</div>
  </div>;
}
