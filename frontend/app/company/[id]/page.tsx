import { CompanyDetails } from "@/components/CompanyDetails";

export default async function CompanyPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <CompanyDetails companyId={id} />;
}

