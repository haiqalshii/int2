"""Resolve NACSA company names through Google Places and persist locations."""
import argparse, os
from dotenv import load_dotenv
from geocoding.google_places import GooglePlacesClient
from geocoding.runner import ROOT, database_connection

def enrich_companies(api_key: str, limit: int | None = None, delay_seconds: float = 0.15, minimum_name_score: float = 0.80, refresh_located: bool = False) -> tuple[int, int, int]:
    matched = unmatched = failed = 0
    with database_connection() as connection, connection.cursor() as cursor:
        query, parameters = "SELECT id,name FROM companies" + (" WHERE latitude IS NULL AND longitude IS NULL" if not refresh_located else "") + " ORDER BY name", ()
        if limit is not None: query, parameters = query + " LIMIT %s", (limit,)
        cursor.execute(query, parameters)
        with GooglePlacesClient(api_key, delay_seconds) as client:
            for company_id, name in cursor.fetchall():
                try:
                    result = client.search_company(name, minimum_name_score)
                    if result is None: unmatched += 1; print(f"No confident Google Places match: {name}"); continue
                    cursor.execute("UPDATE companies SET address=%s,city=%s,state=%s,latitude=%s,longitude=%s,google_place_id=%s,google_rating=%s,google_review_count=%s,website=COALESCE(website,%s),last_verified_at=CURRENT_TIMESTAMP,updated_at=CURRENT_TIMESTAMP WHERE id=%s", (result.formatted_address, result.city, result.state, result.latitude, result.longitude, result.place_id, result.rating, result.review_count, result.website, company_id))
                    connection.commit(); matched += 1; print(f"Located {name} -> {result.display_name}: {result.formatted_address} ({result.name_score:.2f})")
                except Exception as error:
                    connection.rollback(); failed += 1; print(f"Failed {name}: {type(error).__name__}: {error}")
    return matched, unmatched, failed

if __name__ == "__main__":
    load_dotenv(ROOT / ".env")
    parser = argparse.ArgumentParser(description="Locate INT2 companies using Google Places")
    parser.add_argument("--limit", type=int); parser.add_argument("--delay", type=float, default=0.15); parser.add_argument("--minimum-name-score", type=float, default=0.80); parser.add_argument("--refresh-located", action="store_true")
    args = parser.parse_args(); key = os.environ.get("GOOGLE_PLACES_API_KEY") or os.environ.get("GOOGLE_MAPS_API_KEY")
    if not key: raise SystemExit("GOOGLE_MAPS_API_KEY (or GOOGLE_PLACES_API_KEY) is required in the root .env")
    print(enrich_companies(key, args.limit, args.delay, args.minimum_name_score, args.refresh_located))
