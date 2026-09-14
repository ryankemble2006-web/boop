package local.johnnycastaway.shield;
/** Pure one-shot marquee timing for long title/artist text. */
public final class NowPlayingMarqueePolicy {
 public static final long START_DELAY_MS=1200L;
 public static final long END_HOLD_MS=450L;
 private NowPlayingMarqueePolicy(){ }
 public static float offset(float textWidth,float slotWidth,long elapsedMs,float speedPxPerSecond){
  float overflow=Math.max(0f,textWidth-slotWidth);
  if(overflow<=0f||speedPxPerSecond<=0f||elapsedMs<START_DELAY_MS)return 0f;
  long scrollMs=(long)Math.ceil((overflow/speedPxPerSecond)*1000f);
  long moving=elapsedMs-START_DELAY_MS;
  if(moving<scrollMs)return Math.min(overflow,(moving/1000f)*speedPxPerSecond);
  if(moving<scrollMs+END_HOLD_MS)return overflow;
  return 0f;
 }
 public static boolean animating(float textWidth,float slotWidth,long elapsedMs,float speedPxPerSecond){
  float overflow=Math.max(0f,textWidth-slotWidth);
  if(overflow<=0f||speedPxPerSecond<=0f||elapsedMs<START_DELAY_MS)return false;
  long scrollMs=(long)Math.ceil((overflow/speedPxPerSecond)*1000f);
  return elapsedMs<START_DELAY_MS+scrollMs+END_HOLD_MS;
 }
}
