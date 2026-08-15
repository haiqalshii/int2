import type { Company } from "@/types";
import { CompanyCard } from "./CompanyCard";
export function CompanyList({ companies }: { companies: Company[] }) { return <div className="grid gap-3">{companies.map((company) => <CompanyCard key={company.id} company={company} />)}</div>; }

