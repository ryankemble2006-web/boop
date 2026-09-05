from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class WallPairingCompanionTest(unittest.TestCase):
    def test_companion_is_token_free_and_pinned(self):
        files = {p.name: p.read_text(encoding="utf-8") for p in (ROOT / "source/companion").glob("*.java")}
        self.assertEqual({"PairingLink.java", "HaLoopbackAuthServer.java",
                          "PinnedTlsPairingClient.java", "ShieldPairingActivity.java"}, set(files))
        combined = "\n".join(files.values()).lower()
        self.assertNotIn("refresh_token", combined)
        self.assertNotIn("access_token", combined)
        self.assertIn("messagedigest.isequal", combined)
        self.assertIn("127.0.0.1", combined)

    def test_main_activity_stays_pairing_free(self):
        main = (ROOT / "source/MainActivity.java").read_text(encoding="utf-8")
        self.assertNotIn("ShieldPairingActivity", main)
        self.assertNotIn("shield-pair", main)

    def test_manifest_and_materializer_include_companion(self):
        manifest = (ROOT / "source/AndroidManifest.xml").read_text(encoding="utf-8")
        materializer = (ROOT / "scripts/materialize-android.sh").read_text(encoding="utf-8")
        self.assertIn('android:name=".ShieldPairingActivity"', manifest)
        self.assertIn('android:host="shield-pair"', manifest)
        self.assertIn('android:host="shield-pair-return"', manifest)
        self.assertIn('cp source/companion/*.java "$MAIN"/', materializer)


if __name__ == "__main__":
    unittest.main()
