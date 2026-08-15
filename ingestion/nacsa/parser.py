"""Parse a saved NACSA JSON response with per-record validation."""

from pathlib import Path
import json

from pydantic import ValidationError

from nacsa.models import LicenceHolder


def parse_file(path: Path) -> tuple[list[LicenceHolder], list[str]]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(payload, list):
        raise ValueError("NACSA snapshot must contain a JSON array")

    records: list[LicenceHolder] = []
    errors: list[str] = []
    for index, item in enumerate(payload):
        try:
            records.append(LicenceHolder.model_validate(item))
        except ValidationError as error:
            errors.append(f"record {index}: {error}")
    return records, errors

