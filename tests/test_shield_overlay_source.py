from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
APP = ROOT / "shield-overlay" / "app" / "src" / "main"


class ShieldOverlaySourceTest(unittest.TestCase):
    def read(self, relative):
        path = APP / relative
        self.assertTrue(path.exists(), f"missing required file: {path.relative_to(ROOT)}")
        return path.read_text(encoding="utf-8")

    def test_overlay_window_is_noninteractive_and_application_overlay(self):
        source = self.read("java/com/boop/shieldoverlay/OverlayWindowSpec.java")
        self.assertIn("TYPE_APPLICATION_OVERLAY", source)
        self.assertIn("FLAG_NOT_FOCUSABLE", source)
        self.assertIn("FLAG_NOT_TOUCHABLE", source)

    def test_eye_view_is_transparent_and_never_paints_black(self):
        source = self.read("java/com/boop/shieldoverlay/BoopOverlayView.java")
        self.assertIn("Color.TRANSPARENT", source)
        self.assertNotIn("Color.BLACK", source)
        self.assertNotIn("drawColor", source)

    def test_manifest_allows_local_network_but_still_blocks_voice_boot_and_accessibility(self):
        manifest = self.read("AndroidManifest.xml")
        self.assertIn("android.permission.SYSTEM_ALERT_WINDOW", manifest)
        self.assertIn("android.permission.FOREGROUND_SERVICE", manifest)
        self.assertIn("android.permission.FOREGROUND_SERVICE_SPECIAL_USE", manifest)
        self.assertIn("android.permission.INTERNET", manifest)
        self.assertIn("android.permission.ACCESS_NETWORK_STATE", manifest)
        self.assertIn("android:foregroundServiceType=\"specialUse\"", manifest)
        self.assertIn("persistent_noninteractive_visual_overlay_poc", manifest)
        for forbidden in (
            "RECORD_AUDIO",
            "RECEIVE_BOOT_COMPLETED",
            "BIND_ACCESSIBILITY_SERVICE",
            "SpeechRecognizer",
            "RecognizerIntent",
        ):
            self.assertNotIn(forbidden, manifest)

    def test_service_promotes_with_declared_special_use_type(self):
        source = self.read("java/com/boop/shieldoverlay/BoopOverlayService.java")
        self.assertIn("FOREGROUND_SERVICE_TYPE_SPECIAL_USE", source)

    def test_display_mode_change_reasserts_existing_eye_surface(self):
        source = self.read("java/com/boop/shieldoverlay/BoopOverlayService.java")
        self.assertIn("DisplayManager.DisplayListener", source)
        self.assertIn("onDisplayChanged", source)
        self.assertIn("registerDisplayListener", source)
        self.assertIn("unregisterDisplayListener", source)
        self.assertIn("postInvalidateOnAnimation", source)
        self.assertNotIn("postDelayed", source)

    def test_home_visibility_does_not_recreate_overlay(self):
        source = self.read("java/com/boop/shieldoverlay/BoopOverlayService.java")
        self.assertIn("ACTION_HIDE_EYES", source)
        self.assertIn("ACTION_SHOW_EYES", source)
        self.assertIn("setVisibility(View.GONE)", source)
        self.assertIn("setVisibility(View.VISIBLE)", source)
        self.assertIn("return START_STICKY", source)
        self.assertNotIn("removeOverlay(); // hide", source)

    def test_launcher_gateway_opens_fullscreen_home_and_home_restores_overlay(self):
        main = self.read("java/com/boop/shieldoverlay/MainActivity.java")
        home = self.read("java/com/boop/shieldoverlay/BoopHomeActivity.java")
        manifest = self.read("AndroidManifest.xml")
        themes = self.read("res/values/themes.xml")

        self.assertIn("startActivity(new Intent(this, BoopHomeActivity.class))", main)
        self.assertIn("startForegroundService(new Intent(this, BoopOverlayService.class))", main)
        self.assertIn("BoopOverlayController.hide(this)", home)
        self.assertIn("BoopOverlayController.show(this)", home)
        self.assertIn("protected void onStart()", home)
        self.assertIn("protected void onStop()", home)
        self.assertIn("finish()", home)
        self.assertIn('android:name=".BoopHomeActivity"', manifest)
        self.assertIn('android:exported="false"', manifest)
        self.assertIn("Theme.BoopHome", themes)
        self.assertIn("@android:color/black", themes)

    def test_home_immersive_mode_keeps_the_min_sdk_26_fallback(self):
        home = self.read("java/com/boop/shieldoverlay/BoopHomeActivity.java")
        self.assertIn("Build.VERSION.SDK_INT >= Build.VERSION_CODES.R", home)
        self.assertIn("SYSTEM_UI_FLAG_IMMERSIVE_STICKY", home)
        self.assertIn("SYSTEM_UI_FLAG_HIDE_NAVIGATION", home)
        self.assertIn("SYSTEM_UI_FLAG_FULLSCREEN", home)

    def test_home_creates_decor_before_requesting_android_11_insets_controller(self):
        home = self.read("java/com/boop/shieldoverlay/BoopHomeActivity.java")
        method_start = home.index("private void keepAwakeAndHideSystemUi()")
        method_end = home.index("\n    }", method_start)
        method = home[method_start:method_end]
        self.assertIn("window.getDecorView()", method)
        self.assertIn("window.getInsetsController()", method)
        self.assertLess(method.index("window.getDecorView()"), method.index("window.getInsetsController()"))

    def test_tv_focus_visual_does_not_reorder_linear_layout_children(self):
        source = self.read("java/com/boop/shieldoverlay/FocusCardView.java")
        self.assertNotIn("bringToFront()", source)
        self.assertIn("setTranslationZ(hasFocus ? dp(8) : 0f)", source)
        self.assertIn("scaleX(hasFocus ? 1.045f : 1f)", source)
        self.assertIn("scaleY(hasFocus ? 1.045f : 1f)", source)

    def test_routines_page_is_scrollable_remote_first_and_does_not_reorder_cards(self):
        source = self.read("java/com/boop/shieldoverlay/TvRoutinesView.java")
        self.assertIn("extends ScrollView", source)
        self.assertIn("setFillViewport(true)", source)
        self.assertIn("FocusCardView", source)
        self.assertIn("KEYCODE_DPAD_LEFT", source)
        self.assertIn("onRun", source)
        self.assertNotIn("bringToFront()", source)

    def test_home_activity_wires_routines_to_existing_ha_socket(self):
        home = self.read("java/com/boop/shieldoverlay/BoopHomeActivity.java")
        self.assertIn("RoutinesRepository", home)
        self.assertIn("RoutinesController", home)
        self.assertIn("subscribeStateChanges", home)
        self.assertIn("runRoutine", home)
        self.assertIn("routinesView.render", home)

    def test_home_session_failure_marks_routines_offline(self):
        home = self.read("java/com/boop/shieldoverlay/BoopHomeActivity.java")
        method_start = home.index("private void startHomeDashboard()")
        method_end = home.index("private void connectForHomeDashboard", method_start)
        method = home[method_start:method_end]
        catch_block = method[method.index("catch (Exception e)"):]
        self.assertIn("markRoutinesOffline()", catch_block)

    def test_java_only_module_disables_builtin_kotlin(self):
        gradle = (ROOT / "shield-overlay" / "app" / "build.gradle").read_text(encoding="utf-8")
        self.assertIn("enableKotlin = false", gradle)

    def test_protected_overlay_runtime_stays_free_of_voice_network_and_ha_code(self):
        protected_sources = (
            "java/com/boop/shieldoverlay/BoopOverlayService.java",
            "java/com/boop/shieldoverlay/BoopOverlayView.java",
            "java/com/boop/shieldoverlay/OverlayGeometry.java",
            "java/com/boop/shieldoverlay/OverlayWindowSpec.java",
        )
        combined = "\n".join(self.read(path) for path in protected_sources).lower()
        for forbidden in (
            "android.speech",
            "android.media.audiorecord",
            "java.net",
            "okhttp",
            "retrofit",
            "homeassistant",
        ):
            self.assertNotIn(forbidden, combined)


if __name__ == "__main__":
    unittest.main()
