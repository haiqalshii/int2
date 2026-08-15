import { NextRequest, NextResponse } from "next/server";

async function forward(request: NextRequest, context: { params: Promise<{ path: string[] }> }) {
  const backend = process.env.BACKEND_URL?.replace(/\/$/, "");
  const apiKey = process.env.APP_API_KEY;
  if (!backend || !apiKey) return NextResponse.json({ message: "Backend proxy is not configured." }, { status: 503 });
  const { path } = await context.params;
  const url = `${backend}/api/${path.join("/")}${request.nextUrl.search}`;
  const headers = new Headers();
  const contentType = request.headers.get("content-type");
  if (contentType) headers.set("content-type", contentType);
  headers.set("X-INT2-App-Key", apiKey);
  const response = await fetch(url, { method: request.method, headers,
    body: request.method === "GET" || request.method === "HEAD" ? undefined : await request.arrayBuffer(), cache: "no-store" });
  return new NextResponse(response.body, { status: response.status, headers: { "content-type": response.headers.get("content-type") ?? "application/json" } });
}

export const GET = forward;
export const POST = forward;
export const PATCH = forward;
