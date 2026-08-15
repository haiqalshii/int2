"""Normalize previously imported Google administrative-area labels."""
from dotenv import load_dotenv
from geocoding.google_places import canonical_state
from geocoding.runner import ROOT, database_connection

if __name__ == "__main__":
    load_dotenv(ROOT / ".env")
    updated = 0
    with database_connection() as connection, connection.cursor() as cursor:
        cursor.execute("SELECT id,state FROM companies WHERE state IS NOT NULL")
        for company_id, state in cursor.fetchall():
            normalized = canonical_state(state)
            if normalized != state:
                cursor.execute("UPDATE companies SET state=%s,updated_at=CURRENT_TIMESTAMP WHERE id=%s", (normalized, company_id))
                updated += 1
        connection.commit()
    print(f"Normalized {updated} company state labels")
