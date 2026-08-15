import unittest

from geocoding.google_places import canonical_state, company_name_score


class GooglePlacesTest(unittest.TestCase):
    def test_normalizes_google_state_variants(self):
        self.assertEqual("Kuala Lumpur", canonical_state("Federal Territory of Kuala Lumpur"))
        self.assertEqual("Kuala Lumpur", canonical_state("Wilayah Persekutuan Kuala Lumpur"))
        self.assertEqual("Penang", canonical_state("Pulau Pinang"))
        self.assertEqual("Johor", canonical_state("Johor Darul Ta'zim"))

    def test_company_suffixes_do_not_reduce_name_match(self):
        self.assertEqual(1.0, company_name_score("Example Sdn. Bhd.", "Example"))


if __name__ == "__main__":
    unittest.main()
