"""Google Places Text Search with conservative company-name matching."""
from dataclasses import dataclass
from difflib import SequenceMatcher
import re, time, unicodedata
import httpx

CORPORATE_WORDS = {"BERHAD", "BHD", "LIMITED", "LTD", "M", "MALAYSIA", "SDN"}
STATE_ALIASES = {
    "federal territory of kuala lumpur": "Kuala Lumpur",
    "wilayah persekutuan": "Kuala Lumpur",
    "wilayah persekutuan kuala lumpur": "Kuala Lumpur",
    "wp kuala lumpur": "Kuala Lumpur",
    "johor darul ta'zim": "Johor",
    "pulau pinang": "Penang",
    "wilayah persekutuan putrajaya": "Putrajaya",
    "federal territory of putrajaya": "Putrajaya",
    "wilayah persekutuan labuan": "Labuan",
    "federal territory of labuan": "Labuan",
}

def canonical_state(value: str | None) -> str | None:
    if not value:
        return None
    return STATE_ALIASES.get(value.strip().casefold(), value.strip())

def normalized_company_name(value: str) -> str:
    words = re.findall(r"[A-Z0-9]+", unicodedata.normalize("NFKD", value).upper().replace("&", " AND "))
    return " ".join(word for word in words if word not in CORPORATE_WORDS)

def company_name_score(expected: str, candidate: str) -> float:
    left, right = normalized_company_name(expected), normalized_company_name(candidate)
    return SequenceMatcher(None, left, right).ratio() if left and right else 0.0

@dataclass(frozen=True)
class PlaceResult:
    latitude: float
    longitude: float
    formatted_address: str
    city: str | None
    state: str | None
    place_id: str
    display_name: str
    name_score: float
    rating: float | None
    review_count: int | None
    website: str | None

class GooglePlacesClient:
    def __init__(self, api_key: str, delay_seconds: float = 0.15, transport: httpx.BaseTransport | None = None):
        self.delay_seconds, self._last_request_at = max(delay_seconds, 0), 0.0
        self.client = httpx.Client(base_url="https://places.googleapis.com", timeout=25, transport=transport, headers={"Content-Type": "application/json", "X-Goog-Api-Key": api_key, "X-Goog-FieldMask": "places.id,places.displayName,places.formattedAddress,places.addressComponents,places.location,places.rating,places.userRatingCount,places.websiteUri"})

    def close(self) -> None: self.client.close()

    def search_company(self, company_name: str, minimum_name_score: float = 0.80) -> PlaceResult | None:
        elapsed = time.monotonic() - self._last_request_at
        if elapsed < self.delay_seconds: time.sleep(self.delay_seconds - elapsed)
        response = self.client.post("/v1/places:searchText", json={"textQuery": f"{company_name}, Malaysia", "regionCode": "MY", "languageCode": "en", "maxResultCount": 5})
        self._last_request_at = time.monotonic(); response.raise_for_status(); matches = []
        for place in response.json().get("places", []):
            components = place.get("addressComponents", [])
            country = next((item.get("shortText") for item in components if "country" in item.get("types", [])), None)
            display_name = place.get("displayName", {}).get("text", "")
            score, location = company_name_score(company_name, display_name), place.get("location", {})
            if country != "MY" or score < minimum_name_score or "latitude" not in location or "longitude" not in location: continue
            def component(*types: str) -> str | None:
                return next((item.get("longText") for kind in types for item in components if kind in item.get("types", [])), None)
            matches.append(PlaceResult(float(location["latitude"]), float(location["longitude"]), place.get("formattedAddress", ""), component("locality", "postal_town", "administrative_area_level_2"), canonical_state(component("administrative_area_level_1")), place.get("id", ""), display_name, score, place.get("rating"), place.get("userRatingCount"), place.get("websiteUri")))
        return max(matches, key=lambda match: match.name_score, default=None)

    def __enter__(self): return self
    def __exit__(self, *_): self.close()
