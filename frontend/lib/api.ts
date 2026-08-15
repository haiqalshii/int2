const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function getHealth(): Promise<{ status: string }> {
  const response = await fetch(`${API_URL}/api/health`);
  if (!response.ok) throw new Error("Backend health check failed");
  return response.json() as Promise<{ status: string }>;
}

