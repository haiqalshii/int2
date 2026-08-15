import socket
import unittest

from enrichment.crawler import is_public_http_url, normalize_base_url
from enrichment.extractor import combine_pages, extract_page
from enrichment.models import CrawledPage


class EnrichmentTest(unittest.TestCase):
    def test_extracts_public_intelligence_with_source(self) -> None:
        page = CrawledPage(url="https://security.example/services", html="""
            <html><head><meta name="description" content="Malaysia cyber defence specialists."></head>
            <body><p>We provide managed detection and response, penetration testing and incident response.</p>
            <a href="mailto:careers@security.example">Careers</a><a href="tel:+60312345678">Call</a>
            <address>Level 1, Kuala Lumpur</address><script>fake@invalid.example</script></body></html>
        """)
        extracted = extract_page(page)
        combined = combine_pages([page])

        self.assertEqual("Malaysia cyber defence specialists.", extracted.description)
        self.assertEqual(["Incident Response", "MDR", "Penetration Testing"], extracted.services)
        self.assertEqual("careers@security.example", combined.email)
        self.assertEqual("https://security.example/services", combined.email_source_url)
        self.assertEqual("Level 1, Kuala Lumpur", combined.address)

    def test_rejects_private_and_credentialed_urls(self) -> None:
        def resolver(host: str, port: int, **_):
            address = "127.0.0.1" if host == "private.example" else "93.184.216.34"
            return [(socket.AF_INET, socket.SOCK_STREAM, 6, "", (address, port))]

        self.assertFalse(is_public_http_url("https://private.example", resolver))
        self.assertFalse(is_public_http_url("https://user:pass@public.example", resolver))
        self.assertTrue(is_public_http_url("https://public.example", resolver))
        self.assertEqual("https://example.com/", normalize_base_url("example.com/about"))


if __name__ == "__main__":
    unittest.main()

