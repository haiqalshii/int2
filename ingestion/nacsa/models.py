from datetime import datetime
from email.utils import parsedate_to_datetime

from pydantic import BaseModel, ConfigDict, Field, field_validator


class LicenceHolder(BaseModel):
    model_config = ConfigDict(extra="ignore")

    name: str
    role: str
    service_name: str
    letter_status: str | None = None
    letter_application_date: datetime | None = None
    letter_approve_date: datetime | None = None
    letter_expiry_date: datetime | None = None

    @field_validator("name", "role", "service_name")
    @classmethod
    def required_text(cls, value: str) -> str:
        value = " ".join(value.split())
        if not value:
            raise ValueError("must not be blank")
        return value

    @field_validator("letter_application_date", "letter_approve_date", "letter_expiry_date", mode="before")
    @classmethod
    def nacsa_datetime(cls, value: object) -> object:
        if isinstance(value, str) and value.strip():
            return parsedate_to_datetime(value)
        return value


class NormalizedCompany(BaseModel):
    name: str
    normalized_name: str = Field(exclude=True)
    nacsa_licences: list[str]
    nacsa_status: str | None
    services: list[str]
    source_url: str
    last_verified_at: datetime
