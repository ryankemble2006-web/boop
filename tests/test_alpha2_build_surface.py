import re
import unittest
from pathlib import Path


class Alpha2BuildSurfaceTest(unittest.TestCase):
    def test_materializer_overlays_all_editable_sources(self):
        text = Path('scripts/materialize-android.sh').read_text(encoding='utf-8') if Path('scripts/materialize-android.sh').exists() else ''
        self.assertIn('cp source/*.java', text)
        self.assertIn('cp source/AndroidManifest.xml', text)
        self.assertIn('cp source/app-build.gradle', text)
        self.assertIn('cp source-test/*.java', text)

    def test_manifest_has_network_and_auth_callback(self):
        text = Path('source/AndroidManifest.xml').read_text(encoding='utf-8') if Path('source/AndroidManifest.xml').exists() else ''
        self.assertIn('android.permission.INTERNET', text)
        self.assertIn('android.permission.NEARBY_WIFI_DEVICES', text)
        self.assertIn('android:scheme="boop"', text)
        self.assertIn('android:host="auth-callback"', text)

    def test_build_is_alpha662_android36_java17(self):
        text = Path('source/app-build.gradle').read_text(encoding='utf-8') if Path('source/app-build.gradle').exists() else ''
        self.assertIn('compileSdk 36', text)
        self.assertIn('targetSdk 36', text)
        self.assertIn('minSdk 29', text)
        self.assertIn('versionCode 22', text)
        self.assertIn('versionName "0.4.8-alpha6.6.2"', text)
        self.assertIn('JavaVersion.VERSION_17', text)
        self.assertIn("implementation 'com.squareup.okhttp3:okhttp:4.12.0'", text)
        self.assertIn("testImplementation 'junit:junit:4.13.2'", text)

    def test_workflow_runs_jvm_tests_and_publishes_apk(self):
        text = Path('.github/workflows/build-apk.yml').read_text(encoding='utf-8')
        self.assertIn('bash scripts/materialize-android.sh', text)
        self.assertIn(':app:testDebugUnitTest', text)
        self.assertIn('actions/upload-artifact@v4', text)

    def test_build_requires_persistent_secret_backed_signing(self):
        workflow = Path('.github/workflows/build-apk.yml').read_text(encoding='utf-8')
        gradle = Path('source/app-build.gradle').read_text(encoding='utf-8')
        for secret in (
            'BOOP_KEYSTORE_BASE64',
            'BOOP_KEYSTORE_PASSWORD',
            'BOOP_KEY_ALIAS',
            'BOOP_KEY_PASSWORD',
        ):
            self.assertIn('secrets.' + secret, workflow)
        self.assertIn('base64 --decode', workflow)
        self.assertIn('BOOP_KEYSTORE_PATH', gradle)
        self.assertIn('signingConfigs', gradle)
        self.assertIn('signingConfig signingConfigs.boop', gradle)

    def test_no_literal_credentials_are_committed(self):
        text = '\n'.join(
            p.read_text(encoding='utf-8')
            for root in ('source', 'web')
            if Path(root).exists()
            for p in Path(root).rglob('*')
            if p.is_file() and p.suffix in {'.java', '.xml', '.gradle', '.html', '.js'}
        )
        self.assertIsNone(re.search(r'Bearer\s+[A-Za-z0-9_-]{20,}', text))
        self.assertNotIn('sk-', text)

    def test_discovery_is_local_android_nsd(self):
        p = Path('source/HomeAssistantDiscovery.java')
        text = p.read_text(encoding='utf-8') if p.exists() else ''
        self.assertIn('NsdManager', text)
        self.assertIn('_home-assistant._tcp.', text)
        self.assertNotIn('ui.nabu.casa', text)

    def test_refresh_token_is_keystore_encrypted(self):
        p = Path('source/SecureTokenStore.java')
        text = p.read_text(encoding='utf-8') if p.exists() else ''
        self.assertIn('AndroidKeyStore', text)
        self.assertIn('AES/GCM/NoPadding', text)
        self.assertNotIn('putString("refresh_token", refreshToken)', text)

    def test_main_routes_raw_speech_to_ha_off_ui_thread(self):
        text = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('ExecutorService', text)
