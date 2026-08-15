import { NextResponse } from "next/server";

export async function GET() {
  const apiKey = process.env.GOOGLE_MAPS_BROWSER_KEY;
  if (!apiKey) return NextResponse.json({ message: "Google Maps is not configured." }, { status: 503 });
  return NextResponse.json({ apiKey });
}
