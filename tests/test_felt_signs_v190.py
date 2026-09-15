"""Accepted default and connected photographic sign contracts; no visual rendering."""
from pathlib import Path
import subprocess,tempfile,unittest
ROOT=Path(__file__).resolve().parents[1]
class FeltSigns(unittest.TestCase):
 def test_default_is_enforced_before_materialization(self):
  script=ROOT/'scripts/verify-accepted-felt.py'
  self.assertTrue(script.is_file(),'Accepted v189 default has no build lock')
  subprocess.run(['python',str(script)],cwd=ROOT,check=True)
  materializer=(ROOT/'scripts/patch-unified-canonical-animations.py').read_text()
  self.assertIn('verify-accepted-felt.py',materializer)
 def test_connected_sign_replaces_cut_finger_notification_draw(self):
  view=(ROOT/'unified/animation/java/com/boop/eyes/NotificationSignView.java').read_text()
  self.assertIn('feltSign.draw(c,style);',view,'Notification still assembles disconnected finger strips')
  draw=view.split('@Override protected void onDraw')[1].split('private void micHand')[0]
  self.assertNotIn('frontFingers(c,',draw)
  self.assertNotIn('handBridge(c,',draw)
  self.assertIn('c.rotate(pose.angle);',draw)
  self.assertIn('drawFreddie(c)',draw,'Separate performance must remain available')
 def test_real_sprite_and_five_digit_rig(self):
  rig=ROOT/'unified/animation/java/com/boop/eyes/FeltSignRig.java'
  self.assertTrue(rig.is_file(),'Five-digit sign anatomy has no shared rig')
  with tempfile.TemporaryDirectory() as tmp:
   subprocess.run(['javac','-d',tmp,str(rig),str(ROOT/'tests/java/FeltSignRigHarness.java')],check=True)
   subprocess.run(['java','-Djava.awt.headless=true','-cp',tmp,'com.boop.eyes.FeltSignRigHarness',str(ROOT/'unified/animation/assets/boop-felt-sign-blank.png')],check=True)
 def test_accepted_core_lock_detects_a_changed_copy(self):
  script=ROOT/'scripts/verify-accepted-felt.py'
  self.assertTrue(script.is_file(),'Need executable default preservation check')
  # The verifier self-test uses temporary bytes and never edits the project.
  subprocess.run(['python',str(script),'--self-test'],cwd=ROOT,check=True)
if __name__=='__main__':unittest.main()
