"""Small, bounded crawler for official company websites."""

from collections.abc import Callable
import ipaddress
import socket
import ssl
import time
from urllib.parse import urljoin, urlparse
from urllib.robotparser import RobotFileParser

import httpx
import truststore

from enrichment.models import CrawledPage, CrawlResult

USER_AGENT = "INT2/1.0 (private internship research tool)"
PRIORITY_PATHS = ("/", "/about", "/about-us", "/services", "/solutions", "/products", "/careers", "/internship", "/contact")
MAX_RESPONSE_BYTES = 2_000_000


def normalize_base_url(value: str) -> str:
    value = value.strip()
    if not value:
        raise ValueError("Website URL is empty")
    if "://" not in value:
        value = "https://" + value
    parsed = urlparse(value)
    if parsed.scheme not in {"http", "https"} or not parsed.hostname or parsed.username or parsed.password:
        raise ValueError("Only public HTTP(S) website URLs are allowed")
    port = f":{parsed.port}" if parsed.port else ""
    return f"{parsed.scheme}://{parsed.hostname.lower()}{port}/"


def is_public_http_url(url: str, resolver: Callable[..., list] = socket.getaddrinfo) -> bool:
    try:
        parsed = urlparse(url)
        if parsed.scheme not in {"http", "https"} or not parsed.hostname or parsed.username or parsed.password:
            return False
        addresses = resolver(parsed.hostname, parsed.port or (443 if parsed.scheme == "https" else 80), type=socket.SOCK_STREAM)
        if not addresses:
            return False
        return all(ipaddress.ip_address(item[4][0]).is_global for item in addresses)
    except (OSError, ValueError):
        return False


def _same_company_host(base_url: str, candidate_url: str) -> bool:
    base = (urlparse(base_url).hostname or "").removeprefix("www.")
    candidate = (urlparse(candidate_url).hostname or "").removeprefix("www.")
    return bool(base and base == candidate)


class WebsiteCrawler:
    def __init__(self, delay_seconds: float = 1.0, max_pages: int = 9):
        self.delay_seconds = max(delay_seconds, 0)
        self.max_pages = min(max(max_pages, 1), len(PRIORITY_PATHS))
        context = truststore.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
        self.client = httpx.Client(verify=context, timeout=12, follow_redirects=False, headers={"User-Agent": USER_AGENT, "Accept": "text/html,application/xhtml+xml"})

    def close(self) -> None:
        self.client.close()

    def __enter__(self): return self
    def __exit__(self, *_): self.close()

    def crawl(self, website: str) -> CrawlResult:
        base_url = normalize_base_url(website)
        if not is_public_http_url(base_url):
            raise ValueError("Website does not resolve exclusively to public IP addresses")
        result = CrawlResult(base_url=base_url)
        robots = self._robots(base_url, result)

        for path in PRIORITY_PATHS[:self.max_pages]:
            url = urljoin(base_url, path)
            if robots is not None and not robots.can_fetch(USER_AGENT, url):
                result.errors.append(f"Blocked by robots.txt: {url}")
                continue
            try:
                response = self._get(url, base_url)
                response.raise_for_status()
                if "text/html" not in response.headers.get("content-type", "").lower():
                    raise ValueError("Response is not HTML")
                if len(response.content) > MAX_RESPONSE_BYTES:
                    raise ValueError("Response exceeds the 2 MB limit")
                result.pages.append(CrawledPage(url=str(response.url), html=response.text))
            except (httpx.HTTPError, ValueError) as error:
                result.errors.append(f"{url}: {error}")
            if self.delay_seconds:
                time.sleep(self.delay_seconds)
        return result

    def _robots(self, base_url: str, result: CrawlResult) -> RobotFileParser | None:
        robots_url = urljoin(base_url, "/robots.txt")
        parser = RobotFileParser(robots_url)
        try:
            response = self._get(robots_url, base_url)
            if response.status_code in {401, 403}:
                parser.parse(["User-agent: *", "Disallow: /"])
                return parser
            if response.status_code >= 400:
                return None
            parser.parse(response.text.splitlines())
            return parser
        except (httpx.HTTPError, ValueError) as error:
            result.errors.append(f"Unable to read robots.txt: {error}")
            return None

    def _get(self, url: str, base_url: str) -> httpx.Response:
        current = url
        for _ in range(6):
            if not _same_company_host(base_url, current) or not is_public_http_url(current):
                raise ValueError("Request or redirect left the validated official company host")
            response = self.client.get(current)
            if not response.is_redirect:
                return response
            location = response.headers.get("location")
            if not location:
                return response
            current = urljoin(current, location)
        raise ValueError("Too many redirects")
