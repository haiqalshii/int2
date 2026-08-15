import { NextRequest, NextResponse } from "next/server";
import { timingSafeEqual } from "node:crypto";

function matches(left: string, right: string): boolean {
  const a = Buffer.from(left); const b = Buffer.from(right);
  return a.length === b.length && timingSafeEqual(a, b);
}

export async function POST(request: NextRequest) {
  const configured = process.env.APP_PASSWORD;
  const token = process.env.APP_SESSION_TOKEN;
  const body = await request.json().catch(() => null) as { password?: string } | null;
  if (!configured || !token || !body?.password || !matches(body.password, configured)) {
    return NextResponse.json({ message: "That password isn’t right." }, { status: 401 });
  }
  const response = NextResponse.json({ ok: true });
  response.cookies.set("int2_session", token, { httpOnly: true, secure: true, sameSite: "lax", path: "/", maxAge: 60 * 60 * 24 * 30 });
  return response;
}
