package local.johnnycastaway.shield;
import java.util.Objects;
/** Stable media edges only; metadata, transient states and new sessions never perform a gag. */
final class JohnnyMusicPolicy {
 static final long SETTLE_MS=900, SAME_KIND_MS=8000, MIN_GAP_MS=1500;
 private Object source;
 private int stable,observed,pending;
 private long changed,lastAny=-1,lastPlay=-1,lastPause=-1;
 boolean observe(Object key,int state,long now) {
  boolean known=state==2 || state==3;
  if(key==null) {
   boolean changed=source!=null || observed!=0;
   source=null;stable=observed=pending=0;
   return changed;
  }
  if(!Objects.equals(source,key)) {
   source=key;stable=observed=known?state:0;pending=0;changed=now;
   return true;
  }
  if(!known) {
   boolean changed=observed!=0 || pending!=0;
   observed=pending=0;
   return changed; // Buffering preserves the last confirmed state, never invents pause.
  }
  if(stable==0){stable=observed=state;pending=0;return false;}
  if(observed==state)return false;
  observed=state;changed=now;pending=state==stable?0:state;
  return true;
 }
 long delay(long now){return pending==0?-1:Math.max(0,SETTLE_MS-(now-changed));}
 int poll(long now) {
  if(pending==0 || now-changed<SETTLE_MS)return 0;
  stable=pending;pending=0;
  int kind=stable==3?1:2;
  long previous=kind==1?lastPlay:lastPause;
  if((lastAny>=0 && now-lastAny<MIN_GAP_MS) || (previous>=0 && now-previous<SAME_KIND_MS))return 0;
  lastAny=now;if(kind==1)lastPlay=now;else lastPause=now;
  return kind;
 }
 void reset(){source=null;stable=observed=pending=0;changed=0;lastAny=lastPlay=lastPause=-1;}
}
