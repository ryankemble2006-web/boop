"""Nonvisual hand material and concurrent colour update regressions."""
from pathlib import Path
import subprocess,tempfile,unittest
ROOT=Path(__file__).resolve().parents[1]
class HandColour(unittest.TestCase):
 def test_pixels_and_lifecycle(self):
  with tempfile.TemporaryDirectory() as out:
   files=[ROOT/"unified/animation/java/com/boop/eyes"/(n+".java") for n in ("HandColourPixels","HandColourWork")]
   subprocess.run(["javac","-d",out,*map(str,files),str(ROOT/"tests/java/HandColourHarness.java")],check=True)
   subprocess.run(["java","-cp",out,"com.boop.eyes.HandColourHarness",
     str(ROOT/"unified/animation/assets/boop-felt-sign-blank.png"),
     str(ROOT/"unified/assets/boop-notifications/boop-yellow-hands-approved.png")],check=True)
 def test_default_and_channel_wiring(self):
  subprocess.run(["python","scripts/verify-accepted-felt.py"],cwd=ROOT,check=True)
  ui=(ROOT/"source/BoopAppearanceActivity.java").read_text()
  for required in ("HandColourPreferences.save","HandColourPreferences.load","Original yellow","Share hand colour: Off","new NotificationSignView(this)","EyeColourBinding.install"):
   self.assertIn(required,ui)
  self.assertLess(ui.index('"Original charcoal"'),ui.index('"Original yellow"'))
  view=(ROOT/"unified/animation/java/com/boop/eyes/NotificationSignView.java").read_text()
  self.assertIn("new HandColourBinding",view)
  self.assertIn("feltSign.setArtwork",view)
  self.assertIn("fingers[side][digit]=colours",view)
  self.assertIn("thumbs[side]=colours",view)
  runtime=(ROOT/"source/BoopSharedHandColourRuntime.java").read_text()
  self.assertIn('getBoolean("shared_hand_enabled", false)',runtime)
  self.assertIn('HandColourPreferences.save',runtime)
  self.assertNotIn('FeltColourPreferences',runtime)
  self.assertNotIn('shared_felt_enabled',runtime)
  prefs=(ROOT/"unified/animation/java/com/boop/eyes/HandColourPreferences.java").read_text()
  self.assertIn('"hand_colour"',prefs)
  app=(ROOT/"unified/UnifiedApplication.java").read_text()
  self.assertIn("BoopSharedHandColourRuntime.initialize(this)",app)
if __name__=="__main__":unittest.main()
