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
  System.out.println("Johnny reconnect, fan edges and light policy passed");
 }
}'''
with tempfile.TemporaryDirectory() as d:
 p=pathlib.Path(d)/"StateEdgeHarness.java";p.write_text(test)
 subprocess.run(["javac","-d",d,str(root/"java/local/johnnycastaway/shield/JohnnyStateEdges.java"),str(p)],check=True)
 subprocess.run(["java","-cp",d,"local.johnnycastaway.shield.StateEdgeHarness"],check=True)
