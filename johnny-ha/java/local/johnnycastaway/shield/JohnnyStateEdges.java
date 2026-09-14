package local.johnnycastaway.shield;
/** Session-local edges: missing state breaks continuity and never synthesizes events. */
final class JohnnyStateEdges {
 private String last="unknown", lastLights="unknown";
 boolean update(String state) {
  boolean edge="off".equals(last)&&"on".equals(state);
  last=known(state);
  return edge;
 }
 boolean lightsOff(String state) {
  boolean edge="on".equals(lastLights)&&"off".equals(state);
  lastLights=known(state);
  return edge;
 }
 private static String known(String state){return ("on".equals(state)||"off".equals(state))?state:"unknown";}
 void reset(){last="unknown";lastLights="unknown";}
 static int night(String lights){return "off".equals(lights)?1:"on".equals(lights)?0:-1;}
}
