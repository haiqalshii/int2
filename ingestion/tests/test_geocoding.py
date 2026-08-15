import unittest

import httpx

from geocoding.client import GeoapifyClient


class GeocodingTest(unittest.TestCase):
    def test_accepts_confident_malaysian_result(self) -> None:
        def handler(request: httpx.Request) -> httpx.Response:
            self.assertEqual(request.url.params["filter"], "countrycode:my")
            self.assertEqual(request.url.params["apiKey"], "test-key")
            return httpx.Response(200, json={"results": [{
                "lat": 3.139, "lon": 101.6869, "country_code": "my",
                "formatted": "Kuala Lumpur, Malaysia", "rank": {"confidence": 0.94},
            }]})

        with GeoapifyClient("test-key", delay_seconds=0, transport=httpx.MockTransport(handler)) as client:
            result = client.geocode("Example Security, Malaysia")
        self.assertIsNotNone(result)
        assert result is not None
        self.assertEqual(result.latitude, 3.139)

    def test_rejects_foreign_or_low_confidence_results(self) -> None:
        payload = {"results": [
            {"lat": 1, "lon": 2, "country_code": "sg", "rank": {"confidence": 0.99}},
            {"lat": 3, "lon": 4, "country_code": "my", "rank": {"confidence": 0.40}},
        ]}
        transport = httpx.MockTransport(lambda _: httpx.Response(200, json=payload))
        with GeoapifyClient("test-key", delay_seconds=0, transport=transport) as client:
            self.assertIsNone(client.geocode("Ambiguous Company"))


if __name__ == "__main__":
    unittest.main()
