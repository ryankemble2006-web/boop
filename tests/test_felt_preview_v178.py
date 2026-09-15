"""Non-visual fringe geometry and live-preview integration checks."""
from pathlib import Path
import subprocess, tempfile, unittest
ROOT=Path(__file__).resolve().parents[1]
BASE="af9ad31ff32869ee02a30a64680d1f217d84c03e"
ENGINE=ROOT/"unified/animation/java/com/boop/eyes"
class FeltPreview(unittest.TestCase):
 def test_real_master_has_deterministic_outer_fibres(self):
  with tempfile.TemporaryDirectory() as out:
   subprocess.run(["javac","-d",out,str(ENGINE/"FeltFringeMesh.java"),str(ROOT/"tests/java/FeltFringeHarness.java")],check=True)
   subprocess.run(["java","-Djava.awt.headless=true","-cp",out,"com.boop.eyes.FeltFringeHarness",str(ROOT/"unified/assets/boop-eyes/boopApprovedEyes.png"),str(ROOT/"unified/animation/assets/lid-rig.png")],check=True)
 def test_fringe_glsl_link_and_masked_surface_compositing(self):
  subprocess.run(["glslangValidator","-l",str(ROOT/"unified/animation/assets/felt-fringe.vert"),str(ROOT/"unified/animation/assets/felt-fringe.frag")],check=True)
  renderer=(ENGINE/"CanonicalEyeRenderer.java").read_text()
  draw=renderer.split("public void onDrawFrame")[1]
  self.assertGreater(draw.index("drawFringe(tint)"),draw.index("GL_TRIANGLE_STRIP"))
  self.assertIn("FeltFringeMesh.create(",renderer)
 def test_accepted_stage_lighting_and_colour_transport_unchanged(self):
  for path in ["unified/animation/assets/eyes.vert","source/BoopSharedEyeColourRuntime.java","source/BoopEyeHue.java"]:
   self.assertEqual((ROOT/path).read_bytes(),subprocess.check_output(["git","show",BASE+":"+path],cwd=ROOT),path)
 def test_preview_is_live_nonfocusable_and_pauses_with_activity(self):
  text=(ROOT/"source/BoopAppearanceActivity.java").read_text()
  self.assertIn("new CanonicalEyeRenderer(",text)
  self.assertIn("EyeColourBinding.install(preview, previewRenderer)",text)
  self.assertLess(text.index("preview.setRenderer("),text.index("EyeColourBinding.install("))
  self.assertIn("preview.setFocusable(false)",text)
  self.assertIn("preview.onPause()",text)
  self.assertIn("preview.onResume()",text)
  self.assertIn("GLSurfaceView.RENDERMODE_WHEN_DIRTY",text)
  self.assertIn("root.addView(preview",text)
  self.assertIn("root.addView(scroll",text)
  self.assertIn("BoopEyeHue.saveHue",text)
 def test_production_scope(self):
  changed=subprocess.check_output(["git","diff","--name-only",BASE,"HEAD","--","source","unified","scripts","launcher","shield-overlay"],cwd=ROOT,text=True).splitlines()
  allowed={"source/BoopAppearanceActivity.java","unified/app-build.gradle","unified/animation/java/com/boop/eyes/CanonicalEyeRenderer.java","unified/animation/java/com/boop/eyes/FeltFringeMesh.java","unified/animation/assets/felt-fringe.vert","unified/animation/assets/felt-fringe.frag","unified/animation/assets/eyes.frag","source/SharedFeltColourProtocol.java","source/SharedFeltColourHaProtocol.java","source/SharedFeltColourLink.java","source/BoopSharedFeltColourRuntime.java","unified/UnifiedApplication.java","unified/animation/java/com/boop/eyes/EyeColourBinding.java","unified/animation/java/com/boop/eyes/FeltPalette.java","unified/animation/java/com/boop/eyes/FeltColourPreferences.java"}
  self.assertLessEqual(set(changed),allowed)
if __name__=="__main__": unittest.main()
