import { NextRequest, NextResponse } from "next/server";

export function proxy(request: NextRequest) {
  const token = process.env.APP_SESSION_TOKEN;
  if (!token) return NextResponse.next();
  const valid = request.cookies.get("int2_session")?.value === token;
  if (valid || request.nextUrl.pathname === "/login" || request.nextUrl.pathname.startsWith("/api/auth/")) return NextResponse.next();
  if (request.nextUrl.pathname.startsWith("/api/")) return NextResponse.json({ message: "Authentication required." }, { status: 401 });
  return NextResponse.redirect(new URL("/login", request.url));
}

export const config = { matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"] };
