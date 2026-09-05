from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class BoopSigningTest(unittest.TestCase):
    def test_both_workflows_use_stable_boop_signing(self):
        for rel in (
            ".github/workflows/build-shield-overlay-poc.yml",
            ".github/workflows/build-apk.yml",
        ):
            text = (ROOT / rel).read_text(encoding="utf-8")
            self.assertIn("BOOP_DEV_KEYSTORE_B64", text)
            self.assertIn("BOOP_DEV_STORE_PASSWORD", text)
            self.assertIn("BOOP_DEV_KEY_PASSWORD", text)
            self.assertIn("boop-dev-cert-sha256.txt", text)

    def test_private_key_files_are_ignored(self):
        text = (ROOT / ".gitignore").read_text(encoding="utf-8")
        self.assertIn("*.jks", text)
        self.assertIn("*.keystore", text)


if __name__ == "__main__":
    unittest.main()
