"""PNG rig logic and original-image provenance, without hosted visual tests."""
from pathlib import Path
import subprocess,tempfile,unittest,hashlib,re
ROOT=Path(__file__).resolve().parents[1]
class PngPuppet(unittest.TestCase):
 def test_actual_png_rig_and_bright_palette(self):
  rig=ROOT/"unified/animation/java/com/boop/eyes/PngPuppetRig.java"
  self.assertTrue(rig.is_file(),"PNG puppet rig has not replaced the procedural approximation")
  with tempfile.TemporaryDirectory() as out:
   baseline=Path(out)/"PngPuppetRigBaseline.java"
   original=subprocess.run(["git","show","ab2bd9759bd120b762c6651b148191754d4314d2:unified/animation/java/com/boop/eyes/PngPuppetRig.java"],cwd=ROOT,check=True,capture_output=True,text=True).stdout
   baseline.write_text(original.replace("PngPuppetRig","PngPuppetRigBaseline"))
   subprocess.run(["javac","-d",out,str(baseline),str(rig),str(ROOT/"unified/animation/java/com/boop/eyes/FeltPalette.java"),str(ROOT/"tests/java/PngPuppetHarness.java")],check=True)
   subprocess.run(["java","-Djava.awt.headless=true","-cp",out,"com.boop.eyes.PngPuppetHarness",str(ROOT/"unified/animation/assets/boop-png-study.png")],check=True)
 def test_approved_source_pixels_are_used(self):
  path=ROOT/"unified/animation/assets/boop-png-study.png"
  self.assertTrue(path.is_file(),"Approved PNG is not loaded as animation art")
  self.assertEqual(hashlib.sha256(path.read_bytes()).hexdigest(),"9f2ec16f3aac0e443130b34127a16dcde8489d5a23440399b76a5295800d6f03")
 def test_existing_lid_moves_as_one_surface(self):
  code=(ROOT/"unified/animation/assets/eyes.frag").read_text()
  self.assertNotIn("uCloth",code,"A second felt surface must not appear under the existing lid")
  match=re.search(r"float lidSampleY[^{}]+[{]\s*return ([^;]+);",code)
  self.assertIsNotNone(match,"Existing lid needs an explicit continuous material mapping")
  expression=match.group(1)
  self.assertRegex(expression,r"^[a-zA-Z0-9_ .,+*/()\-]+$")
  for top,edge in [(80.,250.),(140.,290.),(200.,320.)]:
   for closure in [0.,.1,.5,1.]:
    end=edge+(642.-edge)*closure
    sample=lambda y:eval(expression,{"__builtins__":{}},{"min":min,"max":max,"y":y,"top":top,"edge":edge,"end":end})
    self.assertAlmostEqual(sample(top),top)
    self.assertAlmostEqual(sample(end),edge-2.)
    values=[sample(top+(end-top)*i/100) for i in range(101)]
    self.assertEqual(values,sorted(values))
    self.assertTrue(all(top<=v<=edge-2 for v in values))
    if closure>.1:self.assertLess(sample(edge),edge-2,"Old lip must move away, not remain above another lid")
 def test_felt_blend_stops_before_eye_shadow(self):
  code=(ROOT/"unified/animation/assets/eyes.frag").read_text()
  match=re.search(r"float cover=1.0-smoothstep[(]([-0-9.]+),([-0-9.]+),p.y-end[)]",code)
  self.assertIsNotNone(match)
  self.assertLess(float(match.group(1)),0.)
  self.assertEqual(float(match.group(2)),0.,"Material feather must not spill into the eye shadow")
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
