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

    def test_build_is_alpha63_android36_java17(self):
        text = Path('source/app-build.gradle').read_text(encoding='utf-8') if Path('source/app-build.gradle').exists() else ''
        self.assertIn('compileSdk 36', text)
        self.assertIn('targetSdk 36', text)
        self.assertIn('minSdk 29', text)
        self.assertIn('versionCode 16', text)
        self.assertIn('versionName "0.4.3-alpha6.3"', text)
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
        self.assertIn('HomeAssistantDeviceSetup', text)
        self.assertIn('HomeAssistantClient', text)
        self.assertIn('LocalReply.forOutcome', text)
        self.assertNotIn('RoomContext', text)
        self.assertNotIn('roomContext.qualify', text)
        self.assertIn('commandRouter.process(transcript)', text)
        self.assertNotIn('speak("You said, " + best)', text)

    def test_old_room_context_grammar_is_deleted(self):
        self.assertFalse(Path('source/RoomContext.java').exists())
        self.assertFalse(Path('source-test/RoomContextTest.java').exists())

    def test_conversation_request_does_not_rewrite_speech(self):
        text = Path('source/HomeAssistantConversationRequest.java').read_text(encoding='utf-8')
        self.assertIn('.put("text", text)', text)
        self.assertIn('.put("device_id", deviceId)', text)
        self.assertNotIn('replace(', text)
        self.assertNotIn('Pattern.compile', text)
        self.assertNotIn('Matcher', text)
        self.assertNotIn('Living Room', text)

    def test_bcp47_speech_fix_is_preserved(self):
        text = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('Locale.getDefault().toLanguageTag()', text)

    def test_auth_uses_public_client_id_and_direct_app_callback(self):
        text = Path('source/HomeAssistantAuthUrls.java').read_text(encoding='utf-8')
        self.assertIn('raw.githubusercontent.com/ryankemble2006-web/boop/alpha2-local-ha-control/web/ha-auth/index.html', text)
        self.assertIn('REDIRECT_URI = "boop://auth-callback"', text)
        self.assertNotIn('github.io', text)

    def test_boop_wall_setup_is_one_time_and_registry_scoped(self):
        p = Path('source/HomeAssistantDeviceSetup.java')
        text = p.read_text(encoding='utf-8') if p.exists() else ''
        guard = text.find('hasHaDeviceIdentity()')
        registration = text.find('/api/mobile_app/registrations')
        self.assertGreaterEqual(guard, 0)
        self.assertGreater(registration, guard)
        self.assertIn('config/area_registry/list', text)
        self.assertIn('config/device_registry/update', text)
        self.assertNotIn('config/entity_registry/list', text)
        self.assertNotIn('/api/states', text)

    def test_auto_rotate_uses_responsive_face_without_touching_ha_flow(self):
        manifest = Path('source/AndroidManifest.xml').read_text(encoding='utf-8')
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        face_path = Path('source/BoopFaceView.java')
        face = face_path.read_text(encoding='utf-8') if face_path.exists() else ''

        self.assertNotIn('android:screenOrientation="portrait"', manifest)
        self.assertIn('android:configChanges="keyboardHidden|orientation|screenSize"', manifest)
        self.assertIn('BoopFaceView', main)
        self.assertNotIn('new ImageView', main)
        self.assertNotIn('ImageView.ScaleType.FIT_CENTER', main)
        self.assertIn('commandRouter.process(transcript)', main)
        self.assertTrue(face_path.exists())
        self.assertIn('BoopEyeLayout.calculate', face)
        self.assertIn('canvas.drawBitmap', face)
        self.assertIn('LEFT_SOURCE', face)
        self.assertIn('RIGHT_SOURCE', face)

    def test_surprise_presence_behavior_stays_out_of_ha_path(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        face = Path('source/BoopFaceView.java').read_text(encoding='utf-8')
        state = Path('source/BoopPresenceState.java')
        self.assertTrue(state.exists())
        self.assertIn('BoopPresenceState', main)
        self.assertIn('IDLE_TIMEOUT_MS', main)
        self.assertIn('showIdleBlackImmediately', face)
        self.assertIn('wakeFromIdle', face)
        self.assertIn('goIdleBlack', face)
        self.assertIn('commandRouter.process(transcript)', main)
        self.assertNotIn('HomeAssistantClient', face)
        self.assertNotIn('HomeAssistantDeviceSetup', face)

    def test_sleeping_face_keeps_a_full_screen_touch_surface(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('FrameLayout interactionSurface', main)
        self.assertIn('interactionSurface.setOnTouchListener(this::onFaceTouch)', main)
        self.assertIn('interactionSurface.addView(face', main)
        self.assertNotIn('\n        face.setOnTouchListener(this::onFaceTouch)', main)


if __name__ == '__main__':
    unittest.main()
