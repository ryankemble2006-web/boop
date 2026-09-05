from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
JAVA = ROOT / "shield-overlay" / "app" / "src" / "main" / "java" / "com" / "boop" / "shieldoverlay"
MANIFEST = ROOT / "shield-overlay" / "app" / "src" / "main" / "AndroidManifest.xml"


class ShieldAuthSourceTest(unittest.TestCase):
    def read_java(self, name):
        path = JAVA / name
        self.assertTrue(path.exists(), f"missing auth source: {path.relative_to(ROOT)}")
        return path.read_text(encoding="utf-8")

    def test_refresh_credential_uses_android_keystore_aes_gcm(self):
        store = self.read_java("SecureCredentialStore.java")
        self.assertIn("AndroidKeyStore", store)
        self.assertIn("AES/GCM/NoPadding", store)
        self.assertIn("KeyGenParameterSpec", store)
        self.assertIn("setRandomizedEncryptionRequired(true)", store)

    def test_stored_credential_has_no_access_token_field(self):
        stored = self.read_java("StoredHomeAssistantCredential.java")
        self.assertIn("refreshToken", stored)
        self.assertNotIn("accessToken", stored)
        self.assertNotIn("access_token", stored.lower())

    def test_auth_client_does_not_log_credentials_or_response_bodies(self):
        auth = self.read_java("HomeAssistantAuthClient.java")
        for forbidden in (
            "HttpLoggingInterceptor",
            "System.out",
            "System.err",
            "Log.d(",
            "Log.i(",
            "Log.v(",
        ):
            self.assertNotIn(forbidden, auth)
        self.assertNotIn("response.body().string()", auth)

    def test_local_http_is_explicitly_allowed_for_home_assistant(self):
        manifest = MANIFEST.read_text(encoding="utf-8")
        self.assertIn('android:usesCleartextTraffic="true"', manifest)


if __name__ == "__main__":
    unittest.main()
