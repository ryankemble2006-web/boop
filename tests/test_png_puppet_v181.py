"""PNG rig logic and original-image provenance, without hosted visual tests."""
from pathlib import Path
import subprocess,tempfile,unittest,hashlib
ROOT=Path(__file__).resolve().parents[1]
class PngPuppet(unittest.TestCase):
 def test_actual_png_rig_and_bright_palette(self):
  rig=ROOT/"unified/animation/java/com/boop/eyes/PngPuppetRig.java"
  self.assertTrue(rig.is_file(),"PNG puppet rig has not replaced the procedural approximation")
  with tempfile.TemporaryDirectory() as out:
   subprocess.run(["javac","-d",out,str(rig),str(ROOT/"unified/animation/java/com/boop/eyes/FeltPalette.java"),str(ROOT/"tests/java/PngPuppetHarness.java")],check=True)
   subprocess.run(["java","-Djava.awt.headless=true","-cp",out,"com.boop.eyes.PngPuppetHarness",str(ROOT/"unified/animation/assets/boop-png-study.png")],check=True)
 def test_approved_source_pixels_are_used(self):
  path=ROOT/"unified/animation/assets/boop-png-study.png"
  self.assertTrue(path.is_file(),"Approved PNG is not loaded as animation art")
  self.assertEqual(hashlib.sha256(path.read_bytes()).hexdigest(),"9f2ec16f3aac0e443130b34127a16dcde8489d5a23440399b76a5295800d6f03")
 def test_shader_links(self):
  subprocess.run(["glslangValidator","-l",str(ROOT/"unified/animation/assets/eyes.vert"),str(ROOT/"unified/animation/assets/eyes.frag")],check=True)
if __name__=="__main__":unittest.main()
