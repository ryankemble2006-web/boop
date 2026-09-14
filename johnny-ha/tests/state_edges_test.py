import pathlib,subprocess,tempfile
root=pathlib.Path(__file__).resolve().parents[1]
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
  System.out.println("Johnny reconnect, fan edges and light policy passed");
 }
}'''
with tempfile.TemporaryDirectory() as d:
 p=pathlib.Path(d)/"StateEdgeHarness.java";p.write_text(test)
 subprocess.run(["javac","-d",d,str(root/"java/local/johnnycastaway/shield/JohnnyStateEdges.java"),str(p)],check=True)
 subprocess.run(["java","-cp",d,"local.johnnycastaway.shield.StateEdgeHarness"],check=True)
