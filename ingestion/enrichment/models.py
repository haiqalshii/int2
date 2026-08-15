from pydantic import BaseModel, Field


class CrawledPage(BaseModel):
    url: str
    html: str


class CrawlResult(BaseModel):
    base_url: str
    pages: list[CrawledPage] = Field(default_factory=list)
    errors: list[str] = Field(default_factory=list)


class ExtractedPage(BaseModel):
    source_url: str
    description: str | None = None
    emails: list[str] = Field(default_factory=list)
    phones: list[str] = Field(default_factory=list)
    address: str | None = None
    services: list[str] = Field(default_factory=list)


class EnrichmentResult(BaseModel):
    description: str | None = None
    description_source_url: str | None = None
    email: str | None = None
    email_source_url: str | None = None
    phone: str | None = None
    phone_source_url: str | None = None
    address: str | None = None
    address_source_url: str | None = None
    services: dict[str, str] = Field(default_factory=dict)
    pages_crawled: int = 0

