"""Upsert normalized NACSA companies into PostgreSQL."""

import os
from pathlib import Path
import sys
import uuid

import psycopg

from nacsa.normalizer import normalize
from nacsa.parser import parse_file


def import_snapshot(path: Path, database_url: str) -> tuple[int, int]:
    records, errors = parse_file(path)
    if errors:
        raise ValueError(f"Snapshot contains {len(errors)} invalid records; first error: {errors[0]}")
    companies = normalize(records)
    inserted = updated = 0

    with psycopg.connect(database_url) as connection:
        with connection.cursor() as cursor:
            for company in companies:
                cursor.execute("SELECT id FROM companies WHERE lower(name) = lower(%s) ORDER BY created_at LIMIT 1", (company.name,))
                existing = cursor.fetchone()
                licence = "; ".join(company.nacsa_licences)
                if existing:
                    company_id = existing[0]
                    cursor.execute(
                        "UPDATE companies SET nacsa_licence=%s, nacsa_status=%s, source_url=%s, last_verified_at=%s, updated_at=CURRENT_TIMESTAMP WHERE id=%s",
                        (licence, company.nacsa_status, company.source_url, company.last_verified_at, company_id),
                    )
                    updated += 1
                else:
                    company_id = uuid.uuid4()
                    cursor.execute(
                        "INSERT INTO companies (id,name,nacsa_licence,nacsa_status,source_url,contacted,last_verified_at,created_at,updated_at) VALUES (%s,%s,%s,%s,%s,FALSE,%s,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                        (company_id, company.name, licence, company.nacsa_status, company.source_url, company.last_verified_at),
                    )
                    inserted += 1
                for service in company.services:
                    cursor.execute(
                        "INSERT INTO company_services (id,company_id,service,source_url,created_at) VALUES (%s,%s,%s,%s,CURRENT_TIMESTAMP) ON CONFLICT (company_id,service) DO UPDATE SET source_url=EXCLUDED.source_url",
                        (uuid.uuid4(), company_id, service, company.source_url),
                    )
    return inserted, updated


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("Usage: python -m nacsa.importer data/raw/nacsa/YYYY-MM-DD.json")
    url = os.environ.get("SUPABASE_DATABASE_URL")
    if not url:
        raise SystemExit("SUPABASE_DATABASE_URL is required")
    print(import_snapshot(Path(sys.argv[1]), url))

