import unittest
from pathlib import Path


class LauncherSwipeIntegrationTest(unittest.TestCase):
    def test_deliberate_swipe_consumes_release_before_tap_to_speak(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        swipe = main.find('BoopLauncherSwipeGesture.shouldOpenLauncher')
        launch = main.find('launchLauncher()', swipe)
        begin_speech = main.find('beginTapToSpeak()', swipe)

        self.assertGreaterEqual(swipe, 0)
        self.assertGreater(launch, swipe)
        self.assertGreater(begin_speech, launch)
        self.assertIn('dp(96)', main)

    def test_movement_and_multitouch_cancel_the_hidden_hold(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('event.getActionMasked()', main)
        self.assertIn('MotionEvent.ACTION_MOVE', main)
        self.assertIn('MotionEvent.ACTION_POINTER_DOWN', main)
        self.assertIn('cancelMemberBerryHold()', main)
        self.assertIn('swipeHadMultiplePointers', main)

    def test_manifest_allows_explicit_launcher_package_lookup(self):
        manifest = Path('source/AndroidManifest.xml').read_text(encoding='utf-8')
        self.assertIn('<package android:name="com.boop.launcher" />', manifest)

    def test_wall_owns_the_mirrored_cross_app_transition(self):
        materialize = Path('scripts/materialize-android.sh').read_text(encoding='utf-8')
        self.assertIn('ActivityOptions.makeCustomAnimation', materialize)
        self.assertIn('R.anim.boop_launcher_enter_from_right', materialize)
        self.assertIn('R.anim.boop_wall_exit_to_left', materialize)
        self.assertIn('android:fromXDelta="100%p"', materialize)
        self.assertIn('android:toXDelta="-100%p"', materialize)


if __name__ == '__main__':
    unittest.main()
