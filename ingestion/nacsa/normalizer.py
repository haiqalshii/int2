"""Normalize and conservatively deduplicate NACSA business records."""

from collections import defaultdict
from datetime import UTC, datetime
import re
import unicodedata

from nacsa.collector import ENDPOINT
from nacsa.models import LicenceHolder, NormalizedCompany

SERVICE_MAP = {
    "managed security operations centre monitoring service licence": "SOC",
    "penetration testing service licence": "Penetration Testing",
}


def normalize_name(value: str) -> str:
    value = unicodedata.normalize("NFKC", value)
    return re.sub(r"\s+", " ", value).strip().casefold()


def normalize(records: list[LicenceHolder], verified_at: datetime | None = None) -> list[NormalizedCompany]:
    checked_at = verified_at or datetime.now(UTC)
    grouped: dict[str, list[LicenceHolder]] = defaultdict(list)
    for record in records:
        if record.role.casefold() == "business":
            grouped[normalize_name(record.name)].append(record)

    companies: list[NormalizedCompany] = []
    for key, company_records in grouped.items():
        licences = sorted({record.service_name for record in company_records})
        statuses = sorted({record.letter_status for record in company_records if record.letter_status})
        services = sorted({SERVICE_MAP[name.casefold()] for name in licences if name.casefold() in SERVICE_MAP})
        companies.append(NormalizedCompany(
            name=company_records[0].name,
            normalized_name=key,
            nacsa_licences=licences,
            nacsa_status=", ".join(statuses) or None,
            services=services,
            source_url=ENDPOINT,
            last_verified_at=checked_at,
        ))
    return sorted(companies, key=lambda company: company.normalized_name)

