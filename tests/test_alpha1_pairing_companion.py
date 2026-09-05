from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "source"
COMPANION = SOURCE / "companion"
WORKFLOW = ROOT / ".github" / "workflows" / "build-apk.yml"
PATCHER = ROOT / "scripts" / "patch_alpha1_companion.py"


class Alpha1PairingCompanionSourceTest(unittest.TestCase):
    def read(self, path):
        self.assertTrue(path.exists(), f"missing required file: {path.relative_to(ROOT)}")
        return path.read_text(encoding="utf-8")

    def test_existing_main_activity_stays_pairing_free(self):
        main = self.read(SOURCE / "MainActivity.java")
        self.assertIn("SpeechRecognizer.ERROR_NO_MATCH", main)
        self.assertIn('speak("Speech error " + error', main)
        self.assertNotIn("shield-pair", main)
        self.assertNotIn("ShieldPairingActivity", main)

    def test_pairing_companion_is_strict_and_token_free(self):
        link = self.read(COMPANION / "PairingLink.java").lower()
        self.assertIn('"boop"', link)
        self.assertIn('"shield-pair"', link)
        for forbidden in ("refresh_token", "access_token", "password"):
            self.assertNotIn(forbidden, link)

        loopback = self.read(COMPANION / "HaLoopbackAuthServer.java")
        self.assertIn('InetAddress.getByName("127.0.0.1")', loopback)
        self.assertIn("auth_callback", loopback)
        self.assertIn("state", loopback.lower())

        client = self.read(COMPANION / "PinnedTlsPairingClient.java")
        self.assertIn("MessageDigest.isEqual", client)
        self.assertIn("SHA-256", client)
        self.assertNotIn("refresh_token", client.lower())
        self.assertNotIn("access_token", client.lower())

        activity = self.read(COMPANION / "ShieldPairingActivity.java")
        self.assertIn("Intent.ACTION_VIEW", activity)
        self.assertIn("/auth/authorize", activity)
        self.assertIn("shield-pair-return", activity)

    def test_ci_patches_companion_without_replacing_main_activity(self):
        patcher = self.read(PATCHER)
        for required in (
            "android.permission.INTERNET",
            "ShieldPairingActivity",
            "android.intent.category.BROWSABLE",
            "shield-pair",
            "shield-pair-return",
        ):
            self.assertIn(required, patcher)

        workflow = self.read(WORKFLOW)
        self.assertIn("cp source/MainActivity.java", workflow)
        self.assertIn("source/companion", workflow)
        self.assertIn("patch_alpha1_companion.py", workflow)

    def test_ci_smoke_tests_pairing_deep_link_routing(self):
        workflow = self.read(WORKFLOW)
        self.assertIn("Smoke test BOOP pairing deep link", workflow)
        self.assertIn("boop://shield-pair-return?sid=test", workflow)
        self.assertIn("android.intent.action.VIEW", workflow)
        self.assertIn("ShieldPairingActivity", workflow)


if __name__ == "__main__":
    unittest.main()
