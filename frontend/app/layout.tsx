import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "INT² — Intern Intelligence",
  description: "Cybersecurity company discovery and cold outreach",
};

export default function RootLayout({ children }: Readonly<{ children: React.ReactNode }>) {
  return <html lang="en"><body>{children}</body></html>;
}

