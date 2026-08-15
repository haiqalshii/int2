"""Deterministic extraction of basic public company intelligence."""

import re

from bs4 import BeautifulSoup

from enrichment.models import CrawledPage, EnrichmentResult, ExtractedPage

SERVICE_KEYWORDS = {
    "SOC": ("security operations centre", "security operations center", " soc "),
    "MDR": ("managed detection and response", "mdr"),
    "Penetration Testing": ("penetration testing", "pentest", "pen testing"),
    "Red Team": ("red team", "red teaming"),
    "Incident Response": ("incident response",),
    "Digital Forensics": ("digital forensics", "computer forensics"),
    "GRC": ("governance risk and compliance", " grc "),
    "Security Consulting": ("security consulting", "cybersecurity consulting"),
    "Cloud Security": ("cloud security",),
    "Network Security": ("network security",),
    "IAM": ("identity and access management", " iam "),
    "Threat Intelligence": ("threat intelligence",),
    "Security Awareness": ("security awareness",),
    "Managed Security": ("managed security", "mssp"),
    "Vulnerability Assessment": ("vulnerability assessment",),
}
EMAIL_PATTERN = re.compile(r"(?<![\w.+-])([\w.+-]+@[\w.-]+\.[A-Za-z]{2,})(?![\w.-])")
PHONE_PATTERN = re.compile(r"(?:\+?60|0)[\s-]?(?:\d[\s-]?){8,10}")


def _clean(value: str) -> str:
    return " ".join(value.split())


def extract_page(page: CrawledPage) -> ExtractedPage:
    soup = BeautifulSoup(page.html, "html.parser")
    for node in soup(["script", "style", "noscript", "svg"]): node.decompose()
    text = " " + _clean(soup.get_text(" ", strip=True)).lower() + " "
    meta = soup.select_one('meta[name="description"]') or soup.select_one('meta[property="og:description"]')
    description = _clean(str(meta.get("content", ""))) if meta else None
    description = description or None

    emails = {match.lower() for match in EMAIL_PATTERN.findall(text)}
    emails.update(link.get("href", "")[7:].split("?")[0].lower() for link in soup.select('a[href^="mailto:"]'))
    phones = {_clean(match.group()) for match in PHONE_PATTERN.finditer(text)}
    phones.update(_clean(link.get("href", "")[4:].split("?")[0]) for link in soup.select('a[href^="tel:"]'))
    address_node = soup.find("address")
    address = _clean(address_node.get_text(" ", strip=True)) if address_node else None
    services = sorted(service for service, keywords in SERVICE_KEYWORDS.items() if any(keyword in text for keyword in keywords))
    return ExtractedPage(source_url=page.url, description=description, emails=sorted(filter(None, emails)), phones=sorted(filter(None, phones)), address=address, services=services)


def combine_pages(pages: list[CrawledPage]) -> EnrichmentResult:
    extracted = [extract_page(page) for page in pages]
    result = EnrichmentResult(pages_crawled=len(extracted))
    for page in extracted:
        if not result.description and page.description:
            result.description, result.description_source_url = page.description, page.source_url
        if not result.email and page.emails:
            preferred = next((email for email in page.emails if any(word in email for word in ("career", "hr", "intern", "recruit"))), page.emails[0])
            result.email, result.email_source_url = preferred, page.source_url
        if not result.phone and page.phones:
            result.phone, result.phone_source_url = page.phones[0], page.source_url
        if not result.address and page.address:
            result.address, result.address_source_url = page.address, page.source_url
        for service in page.services:
            result.services.setdefault(service, page.source_url)
    return result

