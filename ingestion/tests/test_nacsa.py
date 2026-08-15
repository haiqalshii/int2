from datetime import UTC, datetime
from pathlib import Path
import unittest

from nacsa.normalizer import normalize, normalize_name
from nacsa.parser import parse_file


FIXTURE = Path(__file__).parent / "fixtures" / "nacsa_sample.json"


class NacsaPipelineTest(unittest.TestCase):
    def test_parser_and_normalizer_deduplicate_businesses(self) -> None:
        records, errors = parse_file(FIXTURE)
        companies = normalize(records, datetime(2026, 8, 15, tzinfo=UTC))

        self.assertEqual([], errors)
        self.assertEqual(3, len(records))
        self.assertEqual(1, len(companies))
        self.assertEqual("Example Security Sdn Bhd", companies[0].name)
        self.assertEqual(["Penetration Testing", "SOC"], companies[0].services)
        self.assertEqual("approved", companies[0].nacsa_status)

    def test_name_normalization_is_unicode_and_whitespace_stable(self) -> None:
        self.assertEqual("abc security", normalize_name("  ABC\u00a0 Security "))


if __name__ == "__main__":
    unittest.main()

