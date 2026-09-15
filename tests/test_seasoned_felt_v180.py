"""Numeric material/colour contracts. No rendered visual judgement."""
from pathlib import Path
import subprocess, tempfile, unittest
ROOT=Path(__file__).resolve().parents[1]
class Seasoned(unittest.TestCase):
 def test_sparse_fibres_have_bounded_brightness(self):
  with tempfile.TemporaryDirectory() as out:
   subprocess.run(["javac","-d",out,str(ROOT/"unified/animation/java/com/boop/eyes/FeltFringeMesh.java"),str(ROOT/"tests/java/SeasonedFeltHarness.java")],check=True)
   subprocess.run(["java","-Djava.awt.headless=true","-cp",out,"com.boop.eyes.SeasonedFeltHarness",str(ROOT/"unified/assets/boop-eyes/boopApprovedEyes.png"),str(ROOT/"unified/animation/assets/lid-rig.png")],check=True)
 def test_colour_palette_keeps_charcoal_and_luminance(self):
  palette=ROOT/"unified/animation/java/com/boop/eyes/FeltPalette.java"
  self.assertTrue(palette.is_file(),"Independent felt palette is missing")
  with tempfile.TemporaryDirectory() as out:
   subprocess.run(["javac","-d",out,str(palette),str(ROOT/"tests/java/FeltPaletteHarness.java")],check=True)
   subprocess.run(["java","-cp",out,"com.boop.eyes.FeltPaletteHarness"],check=True)
if __name__=="__main__": unittest.main()
