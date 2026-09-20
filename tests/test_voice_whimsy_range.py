"""Wider sliders must survive serialization, reception and playback clamping."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def test_expanded_profile_shares_without_clamping_or_changing_existing_values(tmp_path):
    (tmp_path / "BoopVoiceController.java").write_text('''package com.boop.alpha1;
class BoopVoiceController {
 static final String BACKEND_ANDROID="android",BACKEND_NATURAL="natural";
 static Object findNaturalVoice(String key){return java.util.Arrays.asList("bf_emma","bf_isabella","bm_george","bm_fable").contains(key)?key:null;}
}''', encoding="utf-8")
    (tmp_path / "WhimsyRangeHarness.java").write_text('''package com.boop.alpha1;
public class WhimsyRangeHarness {
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 static void near(float got,float want,String why){check(Math.abs(got-want)<.00001f,why+": "+got);}
 public static void main(String[] args){
  var old=SharedVoiceProfileProtocol.decode("BOOP_VOICE_V1|natural|bf_emma|1450|1250");
  check(old!=null && old.pitchMilli==1450 && old.rateMilli==1250,"Existing profile changed");
  var maximum=SharedVoiceProfileProtocol.decode("BOOP_VOICE_V1|natural|bf_emma|2900|2500");
  check(maximum!=null,"Doubled profile rejected during reception");
  check(SharedVoiceProfileProtocol.encode(maximum).equals("BOOP_VOICE_V1|natural|bf_emma|2900|2500"),"Maximum changed on wire");
  check(SharedVoiceProfileProtocol.decode("BOOP_VOICE_V1|natural|bf_emma|2901|2500")==null,"Pitch above maximum accepted");
  check(SharedVoiceProfileProtocol.decode("BOOP_VOICE_V1|natural|bf_emma|2900|2501")==null,"Rate above maximum accepted");
  near(BoopVoiceTuning.pitchFromProgress(1000),2.90f,"Pitch endpoint");
  near(BoopVoiceTuning.rateFromProgress(1000),2.50f,"Cadence endpoint");
  near(BoopVoiceTuning.pitchFromProgress(0),.75f,"Low pitch changed");
  near(BoopVoiceTuning.rateFromProgress(0),.70f,"Low cadence changed");
  near(BoopVoiceTuning.clampPitch(1.45f),1.45f,"Existing saved pitch changed");
  near(BoopVoiceTuning.clampRate(1.25f),1.25f,"Existing saved cadence changed");
  near(BoopVoiceTuning.clampPitch(20f),2.90f,"Pitch cap not enforced");
  near(BoopVoiceTuning.clampRate(20f),2.50f,"Rate cap not enforced");
  check(BoopVoiceTuning.progressFromPitch(2.90f)==1000 && BoopVoiceTuning.progressFromRate(2.50f)==1000,"Received max not represented on sliders");
  SharedVoiceProfileState sender=new SharedVoiceProfileState(old),receiver=new SharedVoiceProfileState(old);
  sender.connected(old);receiver.connected(old);sender.localChanged(maximum);
  var sent=sender.nextWrite();check(maximum.equals(sent),"Maximum failed to queue");
  var received=SharedVoiceProfileProtocol.decode(SharedVoiceProfileProtocol.encode(sent));
  receiver.remoteChanged(received);sender.writeConfirmed(received);
  check(maximum.equals(receiver.localProfile()) && maximum.equals(sender.localProfile()),"Devices did not converge at doubled values");
  receiver.disconnected();receiver.connected(maximum);check(maximum.equals(receiver.localProfile()),"Reconnection lost maximum");
  System.out.println("PASS expanded tuning, existing profiles, shared state and strict bounds");
 }
}''', encoding="utf-8")
    sources = [ROOT / "source" / (name + ".java") for name in
               ["BoopVoiceTuning", "SharedVoiceProfileProtocol", "SharedVoiceProfileState"]]
    subprocess.run(["javac", "-encoding", "UTF-8", "-d", str(tmp_path),
                    *map(str, sources), *map(str, tmp_path.glob("*.java"))], check=True)
    subprocess.run(["java", "-cp", str(tmp_path), "com.boop.alpha1.WhimsyRangeHarness"], check=True)
