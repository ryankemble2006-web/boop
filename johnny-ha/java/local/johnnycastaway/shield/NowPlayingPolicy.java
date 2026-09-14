package local.johnnycastaway.shield;
/** Pure read-only selection and progress calculations. */
public final class NowPlayingPolicy {
 private NowPlayingPolicy(){ }
 public static int rank(int state){
  if(state==3||state==4||state==5||state==9||state==10||state==11)return 3;
  if(state==6||state==8)return 2;
  return state==2?1:0;
 }
 public static long position(long position,long updated,long now,float speed,int state,long duration){
  if(duration<=0||position<0)return 0;double result=position;
  if((state==3||state==4||state==5)&&!Float.isNaN(speed)&&!Float.isInfinite(speed))result+=(double)Math.max(0L,now-updated)*speed;
  return (long)Math.max(0d,Math.min((double)duration,result));
 }
 /** Compatibility for the earlier artist-only prototype; UI now uses NowPlayingMarqueePolicy. */
 public static float artistScrollOffset(float overflow,long elapsedMs){
  if(overflow<=0f||elapsedMs<1000L)return 0f;
  if(elapsedMs<2800L)return Math.min(overflow,overflow*((elapsedMs-1000L)/1800f));
  if(elapsedMs<3300L)return overflow;
  return 0f;
 }
}
