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

    def test_shield_masks_legacy_baked_eyes_before_canonical_surface(self):
        source = (ROOT / 'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java').read_text(encoding='utf-8')
        mask = source.index('LegacyEyeMaskView legacyEyeMask')
        canonical = source.index('eyeSurface = new GLSurfaceView(context)')
        self.assertLess(mask, canonical)
        self.assertIn('canvas.drawOval(left, maskPaint);', source)
        self.assertIn('canvas.drawOval(right, maskPaint);', source)

if __name__ == '__main__':
    unittest.main()
