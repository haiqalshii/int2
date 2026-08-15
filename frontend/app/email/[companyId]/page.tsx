import { EmailGenerator } from "@/components/EmailGenerator";

export default async function EmailPage({ params }: { params: Promise<{ companyId: string }> }) {
  const { companyId } = await params;
  return <EmailGenerator companyId={companyId} />;
}
