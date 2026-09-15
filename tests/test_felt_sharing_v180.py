"""Run real felt protocol/state and link flow against controlled HA replies."""
from pathlib import Path
import subprocess,tempfile,unittest,os,hashlib
ROOT=Path(__file__).resolve().parents[1]
class FeltSharing(unittest.TestCase):
 def test_protocol_state_and_helper_isolation(self):
  jar=ROOT/"work/unified-native-lyrics/json.jar"
  self.assertEqual(hashlib.sha256(jar.read_bytes()).hexdigest(),"3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed")
  names=["SharedEyeColourProtocol","SharedEyeColourState","SharedFeltColourProtocol","SharedFeltColourHaProtocol","SharedFeltColourLink"]
  with tempfile.TemporaryDirectory() as out:
   subprocess.run(["javac","-cp",str(jar),"-d",out,*[str(ROOT/("source/"+n+".java")) for n in names],str(ROOT/"tests/java/SharedFeltColourHarness.java"),str(ROOT/"tests/java/SharedFeltColourLinkHarness.java")],check=True)
   for cls in ["SharedFeltColourHarness","SharedFeltColourLinkHarness"]:
    subprocess.run(["java","-cp",out+os.pathsep+str(jar),"com.boop.alpha1."+cls],check=True)
 def test_default_stage_material_is_preserved(self):
  current=(ROOT/"unified/animation/assets/eyes.frag").read_text()
  old=subprocess.check_output(["git","show","277ad7c6c857075e8d279a08a4aa06786e93e529:unified/animation/assets/eyes.frag"],cwd=ROOT,text=True)
  current=current.replace("uniform vec3 uFeltTint;\n","").replace("))*uFeltTint;","));").replace("// Tint changes cloth colour while preserving the original material luminance.","// Hue belongs to the iris; all cloth lighting remains neutral.")
  self.assertEqual(current,old,"Charcoal light/texture/gaze must remain exact apart from neutral tint")
if __name__=="__main__":unittest.main()
