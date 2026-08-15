"""Small Geoapify client with conservative Malaysian-result validation."""

from dataclasses import dataclass
import time

import httpx


@dataclass(frozen=True)
class GeocodeResult:
    latitude: float
    longitude: float
    formatted_address: str
    confidence: float


class GeoapifyClient:
    def __init__(self, api_key: str, delay_seconds: float = 0.4, transport: httpx.BaseTransport | None = None):
        self.api_key = api_key
        self.delay_seconds = max(delay_seconds, 0)
        self._last_request_at = 0.0
        self.client = httpx.Client(
            base_url="https://api.geoapify.com",
            timeout=20,
            transport=transport,
            headers={"User-Agent": "INT2/1.0 (private internship research tool)"},
        )

    def close(self) -> None:
        self.client.close()

    def geocode(self, query: str, minimum_confidence: float = 0.8) -> GeocodeResult | None:
        elapsed = time.monotonic() - self._last_request_at
        if elapsed < self.delay_seconds:
            time.sleep(self.delay_seconds - elapsed)
        response = self.client.get("/v1/geocode/search", params={
            "text": query,
            "filter": "countrycode:my",
            "bias": "countrycode:my",
            "format": "json",
            "limit": 3,
            "apiKey": self.api_key,
        })
        self._last_request_at = time.monotonic()
        response.raise_for_status()
        for item in response.json().get("results", []):
            confidence = float(item.get("rank", {}).get("confidence", 0))
            if item.get("country_code", "").casefold() != "my" or confidence < minimum_confidence:
                continue
            latitude, longitude = item.get("lat"), item.get("lon")
            if latitude is None or longitude is None:
                continue
            return GeocodeResult(float(latitude), float(longitude), item.get("formatted", query), confidence)
        return None

    def __enter__(self) -> "GeoapifyClient":
        return self

    def __exit__(self, *_: object) -> None:
        self.close()
