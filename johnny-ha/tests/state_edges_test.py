import pathlib,subprocess,tempfile
root=pathlib.Path(__file__).resolve().parents[1]
client=(root/"java/local/johnnycastaway/shield/JohnnyStateClient.java").read_text()
assert "main.postDelayed" in client,"lights-off OI must wait for a night render window"
assert "JohnnyLightPolicy.oiDelayMs()" in client,"lights-off OI delay must use the tested policy"
story_patch=(root/"native/patch_story_urgency.py").read_text()
assert "int urgentStoryPlan=0;" in story_patch,"urgent HA must be able to abandon the remaining mini-story"
assert "if(ha_should_preempt()) { urgentStoryPlan=1; break; }" in story_patch,"intermediate scene must break the plan on HA urgency"
assert "if(urgentStoryPlan)" in story_patch and "adsReleaseIsland();" in story_patch,"aborted plan must release the island before redispatch"
test=r'''package local.johnnycastaway.shield;
public class StateEdgeHarness {
 static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
 public static void main(String[] args){
  JohnnyStateEdges e=new JohnnyStateEdges();
  check(!e.update("on"),"initial on is not an edge");
  check(!e.update("on"),"repeat on");
  check(!e.update("off"),"off no gag");
  check(e.update("on"),"off-on exactly once");
  check(!e.update("on"),"deduplicate");
  e.update("unknown");
  check(!e.update("on"),"reconnect does not replay");
  e.update("off");e.reset();
  check(!e.update("on"),"new view snapshot not edge");
  check(JohnnyStateEdges.night("off")==1,"off night");
  check(JohnnyStateEdges.night("on")==0,"on day");
  check(JohnnyStateEdges.night("unknown")==-1,"unknown preserves background");
  check(!e.lightsOff("off"),"initial off only synchronizes");
  check(!e.lightsOff("off"),"poll off no replay");
  check(!e.lightsOff("on"),"day arms edge");
  check(e.lightsOff("off"),"on to all-off reacts");
  check(!e.lightsOff("off"),"same off no duplicate");
  e.lightsOff("on");e.lightsOff("unknown");
  check(!e.lightsOff("off"),"unknown breaks light continuity");
  e.lightsOff("on");e.lightsOff("unavailable");
  check(!e.lightsOff("off"),"unavailable not an off edge");
  e.lightsOff("on");e.reset();
  check(!e.lightsOff("off"),"reconnect off no anger");
  e.lightsOff("on");
  check(e.lightsOff("off"),"new real edge after reconnect");
  e.update("off");
  check(e.update("on"),"light tracking does not overwrite fan tracking");
  check(!e.lightsOff("off"),"fan tracking does not overwrite light tracking");
  check(JohnnyStateEdges.night("unavailable")==-1,"unavailable preserves scenery");
  check(JohnnyStateEdges.night("")==-1,"missing preserves scenery");
  JohnnyFanState f=new JohnnyFanState();
  check(!f.update("unknown"),"unknown startup must not choose off");
  check(f.update("on") && f.on(),"initial on synchronizes sustained wind");
  check(!f.update("on") && f.on(),"repeated on does not restart");
  check(!f.update("unavailable") && f.on(),"unavailable holds last confirmed on");
  check(!f.update("unknown") && f.on(),"unknown holds wind");
  check(!f.update("on"),"same state on reconnect does not restart");
  check(f.update("off") && !f.on(),"confirmed off lowers wind");
  check(!f.update("off"),"repeated off does not lower twice");
  check(!f.update("") && !f.on(),"missing state preserves off");
  check(f.update("on") && f.on(),"rapid re-on changes target");
  check(f.update("off") && !f.on(),"rapid re-off changes target");
  f.reset();
  check(!f.update("unknown"),"new session unknown no invented off");
  check(f.update("on") && f.on(),"new session resynchronizes on");
  f.reset();
  check(f.update("off") && !f.on(),"new session resynchronizes off");
  check(JohnnyPollPolicy.healthyPollMs()==250L,"healthy HA poll is 250ms");
  check(JohnnyPollPolicy.unavailableSkipPolls()==39,"outage keeps a slow retry");
  check(JohnnyPollPolicy.unavailableRetryMs()==10000L,"outage retry remains about 10 seconds");
  check(JohnnyLightPolicy.oiDelayMs()==150L,"night gets a render window before OI");
  System.out.println("Johnny reconnect, fan edges, night-before-OI policy and polling cadence passed");
 }
}'''
with tempfile.TemporaryDirectory() as d:
 p=pathlib.Path(d)/"StateEdgeHarness.java";p.write_text(test)
 subprocess.run(["javac","-d",d,str(root/"java/local/johnnycastaway/shield/JohnnyStateEdges.java"),str(root/"java/local/johnnycastaway/shield/JohnnyFanState.java"),str(root/"java/local/johnnycastaway/shield/JohnnyPollPolicy.java"),str(root/"java/local/johnnycastaway/shield/JohnnyLightPolicy.java"),str(p)],check=True)
 subprocess.run(["java","-cp",d,"local.johnnycastaway.shield.StateEdgeHarness"],check=True)
