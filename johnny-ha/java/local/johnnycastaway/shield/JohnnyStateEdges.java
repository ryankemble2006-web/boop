package local.johnnycastaway.shield;
/** Session-local edges: missing state breaks continuity and never synthesizes on. */
final class JohnnyStateEdges {
 private String last="unknown";
 boolean update(String state) {
  boolean edge="off".equals(last)&&"on".equals(state);
  last=("on".equals(state)||"off".equals(state))?state:"unknown";
  return edge;
 }
 void reset(){last="unknown";}
 static int night(String lights){return "off".equals(lights)?1:"on".equals(lights)?0:-1;}
}
