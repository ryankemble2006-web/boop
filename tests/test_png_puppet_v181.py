"""PNG rig logic and original-image provenance, without hosted visual tests."""
from pathlib import Path
import subprocess,tempfile,unittest,hashlib,re
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
 def test_cloth_sampling_stays_inside_photo_material(self):
  # Conservative body ellipses measured from this exact hidden material photo.
  photo=ROOT/"unified/animation/assets/boop-hidden-felt.png"
  self.assertEqual(hashlib.sha256(photo.read_bytes()).hexdigest(),"9f4bede5c59f55a067888779fa2d121484d902b1388b110a25cae0dbc48fb728")
  code=(ROOT/"unified/animation/assets/eyes.frag").read_text()
  xs=re.search(r"float clothX=left[?]mix[(]([^,]+),([^,]+),localX[)]:mix[(]([^,]+),([^,]+),localX[)]",code)
  ys=re.search(r"float clothY=mix[(]([^,]+),([^,]+),",code)
  self.assertIsNotNone(xs);self.assertIsNotNone(ys)
  vals=list(map(float,xs.groups())); yvals=list(map(float,ys.groups()))
  for bounds,cx in [(vals[:2],420),(vals[2:],1100)]:
   for x in bounds:
    for y in yvals:
     self.assertLess(((x*1536-cx)/280)**2+((y*1024-625)/210)**2,1,"Cloth sample rectangle reaches backdrop")
 def test_photo_iris_colour_coverage(self):
  code=(ROOT/"unified/animation/assets/eyes.frag").read_text()
  centres=re.search(r"vec2 centre=left[?]vec2[(]([0-9.]+),([0-9.]+)[)]:vec2[(]([0-9.]+),([0-9.]+)[)]",code)
  shape=re.search(r"float iris=1.0-smoothstep[(]([0-9.]+),([0-9.]+),length[(][(]source-centre[)]/vec2[(]([0-9.]+)[)]",code)
  self.assertIsNotNone(centres);self.assertIsNotNone(shape)
  with tempfile.TemporaryDirectory() as out:
   subprocess.run(["javac","-d",out,str(ROOT/"tests/java/PngIrisHarness.java")],check=True)
   subprocess.run(["java","-Djava.awt.headless=true","-cp",out,"com.boop.eyes.PngIrisHarness",str(ROOT/"unified/animation/assets/boop-png-study.png"),*centres.groups(),shape.group(3),shape.group(1)],check=True)
 def test_shader_links(self):
  subprocess.run(["glslangValidator","-l",str(ROOT/"unified/animation/assets/eyes.vert"),str(ROOT/"unified/animation/assets/eyes.frag")],check=True)
if __name__=="__main__":unittest.main()
