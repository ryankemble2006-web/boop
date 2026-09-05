from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class WallStableSigningTest(unittest.TestCase):
    def test_unified_workflow_verifies_stable_signer(self):
        workflow = (ROOT / ".github/workflows/build-boop-wall-resurrection.yml").read_text(encoding="utf-8")
        for required in ("BOOP_DEV_KEYSTORE_B64", "BOOP_SIGNING_STORE_FILE",
                         "boop-dev-cert-sha256.txt", "apksigner"):
            self.assertIn(required, workflow)

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

    def test_ci_uses_and_verifies_the_current_stable_signer(self):
        workflow = (ROOT / ".github/workflows/build-apk.yml").read_text(encoding="utf-8")
        for required in (
            "secrets.BOOP_DEV_KEYSTORE_B64",
            "secrets.BOOP_DEV_STORE_PASSWORD",
            "secrets.BOOP_DEV_KEY_PASSWORD",
            'BOOP_SIGNING_STORE_FILE="${RUNNER_TEMP}/boop-dev.jks"',
            "base64 --decode",
            "-alias boop-dev",
            "BOOP_SIGNING_STORE_FILE=$BOOP_SIGNING_STORE_FILE",
            "shield-overlay/signing/boop-dev-cert-sha256.txt",
            "${ANDROID_HOME}/build-tools/36.0.0/apksigner",
            "sha256",
        ):
            self.assertIn(required, workflow)
        for retired in (
            "BOOP_KEYSTORE_BASE64",
            "BOOP_KEYSTORE_PASSWORD",
            "BOOP_KEY_ALIAS",
            "BOOP_KEY_PASSWORD",
            "BOOP_KEYSTORE_PATH",
        ):
            self.assertNotIn(retired, workflow)

    def test_public_fingerprint_is_present(self):
        digest = (ROOT / "shield-overlay/signing/boop-dev-cert-sha256.txt").read_text(encoding="utf-8").strip()
        self.assertEqual(64, len(digest))
        int(digest, 16)


if __name__ == "__main__":
    unittest.main()
