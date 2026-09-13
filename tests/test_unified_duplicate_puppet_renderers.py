from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]

class DuplicatePuppetRendererTest(unittest.TestCase):
    def test_phone_portrait_renders_one_eye_pair_not_full_atlas(self):
        layout = (ROOT / 'source/BoopEyeLayout.java').read_text(encoding='utf-8')
        face = (ROOT / 'source/BoopFaceView.java').read_text(encoding='utf-8')
        self.assertNotIn('return new Layout(false, null, null);', layout)
        self.assertNotIn('drawPortraitFace(canvas);', face)
        self.assertIn('drawEye(canvas, LEFT_SOURCE, layout.left());', face)
        self.assertIn('drawEye(canvas, RIGHT_SOURCE, layout.right());', face)

    def test_shield_now_playing_has_no_legacy_mascot_under_canonical_surface(self):
        source = (ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java').read_text(encoding='utf-8')
        self.assertNotIn('R.drawable.boop_headphones', source)
        self.assertNotIn('LegacyEyeMaskView', source)
        self.assertNotIn('NowPlayingPuppetEyePlacement', source)
        self.assertIn('CanonicalEyeRenderer', source)
        self.assertIn('ProductionAnimationController', source)


if __name__ == '__main__':
    unittest.main()
