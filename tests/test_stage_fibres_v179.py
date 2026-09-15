"""Scale/material contracts, not hosted visual acceptance."""
from pathlib import Path
import subprocess, unittest
ROOT=Path(__file__).resolve().parents[1]
BASE="45133f36559c3947531ff34af97df52ff5ae9dcd"
class StageFibres(unittest.TestCase):
 def test_soft_width_is_in_screen_pixels(self):
  vertex=(ROOT/"unified/animation/assets/felt-fringe.vert").read_text()
  fragment=(ROOT/"unified/animation/assets/felt-fringe.frag").read_text()
  self.assertIn("aOffset/uPixelScale",vertex)
  self.assertIn("smoothstep",fragment)
  self.assertIn("abs(vInk.z)",fragment)
 def test_visible_crown_fibres_cannot_paint_eye_whites(self):
  fragment=(ROOT/"unified/animation/assets/felt-fringe.frag").read_text()
  self.assertIn("uMaster",fragment)
  self.assertIn("uRig",fragment)
  self.assertIn("edge",fragment)
  renderer=(ROOT/"unified/animation/java/com/boop/eyes/CanonicalEyeRenderer.java").read_text()
  draw=renderer.split("public void onDrawFrame")[1]
  self.assertLess(draw.index("GL_TRIANGLE_STRIP"),draw.index("drawFringe()"))
  self.assertIn("width*x/1774f",renderer)
 def test_accepted_lighting_preview_motion_and_hue_are_unchanged(self):
  paths=["unified/animation/assets/eyes.frag","source/BoopAppearanceActivity.java",
   "unified/animation/java/com/boop/eyes/EyeColourBinding.java",
   "unified/animation/java/com/boop/eyes/EyeMotion.java",
   "unified/animation/java/com/boop/eyes/ProductionAnimationController.java",
   "source/BoopSharedEyeColourRuntime.java"]
  for path in paths:
   self.assertEqual((ROOT/path).read_bytes(),subprocess.check_output(["git","show",BASE+":"+path],cwd=ROOT),path)
if __name__=="__main__":unittest.main()
