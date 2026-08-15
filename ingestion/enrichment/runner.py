"""Enrich companies that already have a known official website."""

import argparse
import os
import uuid

import psycopg

from enrichment.crawler import WebsiteCrawler
from enrichment.extractor import combine_pages


def enrich_companies(database_url: str, limit: int | None = None, delay_seconds: float = 1.0) -> tuple[int, int]:
    enriched = failed = 0
    with psycopg.connect(database_url) as connection, connection.cursor() as cursor:
        query = "SELECT id, name, website FROM companies WHERE website IS NOT NULL AND trim(website) <> '' ORDER BY name"
        parameters: tuple[object, ...] = ()
        if limit is not None:
            query += " LIMIT %s"
            parameters = (limit,)
        cursor.execute(query, parameters)
        companies = cursor.fetchall()

        with WebsiteCrawler(delay_seconds=delay_seconds) as crawler:
            for company_id, name, website in companies:
                try:
                    crawl = crawler.crawl(website)
                    intelligence = combine_pages(crawl.pages)
                    cursor.execute(
                        "UPDATE companies SET description=COALESCE(description,%s), email=COALESCE(email,%s), phone=COALESCE(phone,%s), address=COALESCE(address,%s), last_verified_at=CURRENT_TIMESTAMP, updated_at=CURRENT_TIMESTAMP WHERE id=%s",
                        (intelligence.description, intelligence.email, intelligence.phone, intelligence.address, company_id),
                    )
                    for service, source_url in intelligence.services.items():
                        cursor.execute(
                            "INSERT INTO company_services (id,company_id,service,source_url,created_at) VALUES (%s,%s,%s,%s,CURRENT_TIMESTAMP) ON CONFLICT (company_id,service) DO NOTHING",
                            (uuid.uuid4(), company_id, service, source_url),
                        )
                    connection.commit()
                    enriched += 1
                    print(f"Enriched {name}: {intelligence.pages_crawled} pages, {len(crawl.errors)} page errors")
                except Exception as error:
                    connection.rollback()
                    failed += 1
                    print(f"Failed {name}: {type(error).__name__}: {error}")
    return enriched, failed


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Enrich INT² companies from their known official websites")
    parser.add_argument("--limit", type=int, default=None)
    parser.add_argument("--delay", type=float, default=1.0)
    args = parser.parse_args()
    database_url = os.environ.get("SUPABASE_DATABASE_URL")
    if not database_url:
        raise SystemExit("SUPABASE_DATABASE_URL is required")
    print(enrich_companies(database_url, args.limit, args.delay))

