"""Download NACSA's public structured licence-holder dataset."""

from datetime import UTC, datetime
from pathlib import Path
import json
import ssl

import httpx
import truststore

ENDPOINT = "https://licence.nacsa.gov.my/be/licence/licence_holder"
DEFAULT_RAW_ROOT = Path(__file__).resolve().parents[2] / "data" / "raw" / "nacsa"


def collect(output_root: Path = DEFAULT_RAW_ROOT) -> Path:
    tls_context = truststore.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
    with httpx.Client(verify=tls_context, follow_redirects=True, timeout=30) as client:
        response = client.get(
            ENDPOINT,
            headers={"Accept": "application/json", "User-Agent": "INT2/1.0 (private research tool)"},
        )
    response.raise_for_status()
    payload = response.json()
    if not isinstance(payload, list):
        raise ValueError("NACSA response must be a JSON array")

    output_root.mkdir(parents=True, exist_ok=True)
    destination = output_root / f"{datetime.now(UTC).date().isoformat()}.json"
    if destination.exists():
        raise FileExistsError(f"Raw snapshot already exists and will not be overwritten: {destination}")
    destination.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    return destination


if __name__ == "__main__":
    print(collect())
