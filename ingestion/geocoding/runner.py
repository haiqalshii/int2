"""Geocode companies once and persist only confident Malaysian coordinates."""

import argparse
import os
from pathlib import Path

import psycopg
from dotenv import load_dotenv

from geocoding.client import GeoapifyClient


ROOT = Path(__file__).resolve().parents[2]


def database_connection() -> psycopg.Connection:
    database_url = os.environ.get("SUPABASE_DATABASE_URL")
    if database_url:
        return psycopg.connect(database_url)
    jdbc_url = os.environ.get("SPRING_DATASOURCE_URL", "").removeprefix("jdbc:")
    if not jdbc_url:
        raise RuntimeError("Set SUPABASE_DATABASE_URL or the SPRING_DATASOURCE_* variables.")
    return psycopg.connect(jdbc_url, user=os.environ.get("SPRING_DATASOURCE_USERNAME"),
                           password=os.environ.get("SPRING_DATASOURCE_PASSWORD"))


def geocode_companies(api_key: str, limit: int | None = None, delay_seconds: float = 0.4,
                      minimum_confidence: float = 0.8) -> tuple[int, int, int]:
    matched = unmatched = failed = 0
    with database_connection() as connection, connection.cursor() as cursor:
        query = "SELECT id,name,address,city,state FROM companies WHERE latitude IS NULL AND longitude IS NULL ORDER BY name"
        parameters: tuple[object, ...] = ()
        if limit is not None:
            query += " LIMIT %s"
            parameters = (limit,)
        cursor.execute(query, parameters)
        companies = cursor.fetchall()
        with GeoapifyClient(api_key, delay_seconds) as client:
            for company_id, name, address, city, state in companies:
                search = ", ".join(str(value).strip() for value in (address, city, state, "Malaysia") if value and str(value).strip())
                if not address:
                    search = f"{name}, Malaysia"
                try:
                    result = client.geocode(search, minimum_confidence)
                    if result is None:
                        unmatched += 1
                        print(f"No confident match: {name}")
                        continue
                    cursor.execute(
                        "UPDATE companies SET latitude=%s,longitude=%s,updated_at=CURRENT_TIMESTAMP WHERE id=%s AND latitude IS NULL AND longitude IS NULL",
                        (result.latitude, result.longitude, company_id),
                    )
                    connection.commit()
                    matched += 1
                    print(f"Geocoded {name}: {result.formatted_address} ({result.confidence:.2f})")
                except Exception as error:
                    connection.rollback()
                    failed += 1
                    print(f"Failed {name}: {type(error).__name__}: {error}")
    return matched, unmatched, failed


if __name__ == "__main__":
    load_dotenv(ROOT / ".env")
    parser = argparse.ArgumentParser(description="Geocode INT² companies missing coordinates")
    parser.add_argument("--limit", type=int, default=None)
    parser.add_argument("--delay", type=float, default=0.4)
    parser.add_argument("--minimum-confidence", type=float, default=0.8)
    args = parser.parse_args()
    key = os.environ.get("GEOAPIFY_API_KEY")
    if not key:
        raise SystemExit("GEOAPIFY_API_KEY is required in the environment or root .env")
    print(geocode_companies(key, args.limit, args.delay, args.minimum_confidence))
