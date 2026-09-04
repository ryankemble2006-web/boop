import unittest
from pathlib import Path


class MemberBerrySurfaceTest(unittest.TestCase):
    def test_member_berry_is_a_hidden_single_hold_gesture(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('ViewConfiguration.getLongPressTimeout()', main)
        self.assertIn('memberBerryRunnable', main)
        self.assertIn('scheduleMemberBerryHold()', main)
        self.assertIn('cancelMemberBerryHold()', main)
        self.assertIn('memberBerryConsumed', main)
        self.assertIn('HapticFeedbackConstants.CONFIRM', main)
        self.assertIn('face.playMemberBerry(memberBerryVariant++)', main)
        self.assertNotIn('BoopMemberBerryState', main)
        self.assertFalse(Path('source/BoopMemberBerryState.java').exists())
        self.assertFalse(Path('source-test/BoopMemberBerryStateTest.java').exists())

    def test_long_hold_does_not_also_start_speech(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        down = main.find('MotionEvent.ACTION_DOWN')
        up = main.find('MotionEvent.ACTION_UP')
        schedule = main.find('scheduleMemberBerryHold()', down)
        consumed = main.find('memberBerryConsumed', up)
        begin_speech = main.find('beginTapToSpeak()', up)
        self.assertGreaterEqual(down, 0)
        self.assertGreater(up, down)
        self.assertGreater(schedule, down)
        self.assertGreater(consumed, up)
        self.assertGreater(begin_speech, consumed)

    def test_member_berry_is_brief_visual_puppetry_only(self):
        face = Path('source/BoopFaceView.java').read_text(encoding='utf-8')
        self.assertIn('void playMemberBerry(int variant)', face)
        self.assertIn('ObjectAnimator', face)
        self.assertIn('MEMBER_BERRY_DURATION_MS', face)
        self.assertNotIn('HomeAssistant', face)
        self.assertNotIn('ToneGenerator', face)
        self.assertNotIn('MediaPlayer', face)
        self.assertNotIn('AudioTrack', face)


if __name__ == '__main__':
    unittest.main()
