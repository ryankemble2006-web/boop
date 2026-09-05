from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class WallStableSigningTest(unittest.TestCase):
    def test_gradle_uses_current_stable_signer_contract(self):
        gradle = (ROOT / "source/app-build.gradle").read_text(encoding="utf-8")
        for required in ("BOOP_SIGNING_STORE_FILE", "BOOP_DEV_STORE_PASSWORD",
                         "BOOP_DEV_KEY_PASSWORD", "keyAlias 'boop-dev'",
                         "signingConfig signingConfigs.boopDev"):
            self.assertIn(required, gradle)
        self.assertNotIn("BOOP_KEY_ALIAS", gradle)

    def test_private_signing_files_are_ignored(self):
        ignore = (ROOT / ".gitignore").read_text(encoding="utf-8")
        self.assertIn("*.jks", ignore)
        self.assertIn("*.keystore", ignore)

    def test_public_fingerprint_is_present(self):
        digest = (ROOT / "shield-overlay/signing/boop-dev-cert-sha256.txt").read_text(encoding="utf-8").strip()
        self.assertEqual(64, len(digest))
        int(digest, 16)


if __name__ == "__main__":
    unittest.main()
