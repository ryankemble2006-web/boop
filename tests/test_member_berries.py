import unittest
from pathlib import Path


class MemberBerrySurfaceTest(unittest.TestCase):
    def test_member_berries_are_scheduled_only_on_real_wakes(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('BoopMemberBerryState memberBerryState', main)
        self.assertIn('memberBerryState = new BoopMemberBerryState()', main)
        self.assertIn('int memberBerry = memberBerryState.onWake()', main)
        self.assertIn('face.playMemberBerry(memberBerry)', main)
        self.assertLess(main.find('presenceState.wake()'), main.find('memberBerryState.onWake()'))

    def test_member_berries_stay_visual_and_out_of_home_assistant(self):
        face = Path('source/BoopFaceView.java').read_text(encoding='utf-8')
        berry = Path('source/BoopMemberBerryState.java').read_text(encoding='utf-8')
        self.assertIn('void playMemberBerry(int variant)', face)
        self.assertIn('ObjectAnimator', face)
        self.assertIn('MEMBER_BERRY_DURATION_MS', face)
        self.assertNotIn('HomeAssistant', face)
        self.assertNotIn('HomeAssistant', berry)
        self.assertNotIn('ToneGenerator', face)
        self.assertNotIn('MediaPlayer', face)
        self.assertNotIn('AudioTrack', face)


if __name__ == '__main__':
    unittest.main()
