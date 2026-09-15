"""Run real hand protocol/state and link flow against controlled HA replies."""
from pathlib import Path
import subprocess,tempfile,unittest,os,hashlib
ROOT=Path(__file__).resolve().parents[1]
class HandSharing(unittest.TestCase):
 def test_protocol_state_and_helper_isolation(self):
  jar=ROOT/"work/unified-native-lyrics/json.jar"
  self.assertEqual(hashlib.sha256(jar.read_bytes()).hexdigest(),"3cf6cd6892e32e2b4c1c39e0f52f5248a2f5b37646fdfbb79a66b46b618414ed")
  names=["SharedEyeColourProtocol","SharedEyeColourState","SharedFeltColourProtocol","SharedHandColourProtocol","SharedHandColourHaProtocol","SharedHandColourLink"]
  with tempfile.TemporaryDirectory() as out:
   subprocess.run(["javac","-cp",str(jar),"-d",out,*[str(ROOT/("source/"+n+".java")) for n in names],str(ROOT/"tests/java/SharedHandColourHarness.java"),str(ROOT/"tests/java/SharedHandColourLinkHarness.java")],check=True)
   for cls in ["SharedHandColourHarness","SharedHandColourLinkHarness"]:
    subprocess.run(["java","-cp",out+os.pathsep+str(jar),"com.boop.alpha1."+cls],check=True)
if __name__=="__main__":unittest.main()
