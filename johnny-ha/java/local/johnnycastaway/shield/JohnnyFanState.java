package local.johnnycastaway.shield;
/** Confirmed fan level, independent of light edges. Unknown never implies off. */
final class JohnnyFanState {
 private Boolean current;
 boolean update(String state) {
  if(!"on".equals(state) && !"off".equals(state)) return false;
  boolean next="on".equals(state);
  if(current!=null && current.booleanValue()==next) return false;
  current=next;
  return true;
 }
 boolean on(){return Boolean.TRUE.equals(current);}
 void reset(){current=null;}
}
