from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "shield-overlay" / "app" / "src" / "main" / "java" / "com" / "boop" / "shieldoverlay"


class ShieldPairingSourceTest(unittest.TestCase):
    def read(self, name):
        path = JAVA / name
        self.assertTrue(path.exists(), f"missing pairing source: {path.relative_to(ROOT)}")
        return path.read_text(encoding="utf-8")

    def test_pairing_session_is_short_lived_and_constant_time_checked(self):
        source = self.read("PairingSession.java")
        self.assertIn("120_000L", source)
        self.assertIn("MessageDigest.isEqual", source)
        self.assertIn("SecureRandom", source)

    def test_qr_payload_contains_only_ephemeral_pairing_material(self):
        source = self.read("PairingQrPayload.java").lower()
        self.assertIn("boop", source)
        self.assertIn("shield-pair", source)
        for forbidden in (
            "refresh_token",
            "access_token",
            "password",
            "api.openai.com",
            "firebase",
            "cloudflare",
        ):
            self.assertNotIn(forbidden, source)

    def test_qr_payload_avoids_newer_java_runtime_helpers_on_android_11(self):
        source = self.read("PairingQrPayload.java")
        self.assertNotIn(".isBlank()", source)
        self.assertNotIn("URLEncoder.encode(value, StandardCharsets.UTF_8)", source)
        self.assertNotIn("URLDecoder.decode(value, StandardCharsets.UTF_8)", source)
        self.assertIn("StandardCharsets.UTF_8.name()", source)


if __name__ == "__main__":
    unittest.main()
