package local.johnnycastaway.shield;
import android.content.Context;
import android.graphics.*;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.*;
import android.text.*;
import android.view.View;
/** Unified-style Now Playing card drawn above Johnny's untouched renderer. */
public final class NowPlayingView extends View {
 private final TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final RectF box=new RectF();
 private final NowPlayingArtworkResolver artworkResolver;
 private MediaMetadata metadata; private PlaybackState playback; private Bitmap artwork,fallbackArtwork;
 private String packageName="",title="",subtitle="",album="";
 private boolean running,albumFocused; private long marqueeStartedAt=SystemClock.elapsedRealtime();
 private final Runnable frameTick=new Runnable(){public void run(){
  if(!running)return;if(getVisibility()==VISIBLE)postInvalidateOnAnimation();postOnAnimation(this);
 }};
 public NowPlayingView(Context context){
  super(context);artworkResolver=new NowPlayingArtworkResolver(context,handler,this::postInvalidateOnAnimation);
  setFocusable(false);setClickable(false);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);setVisibility(GONE);
 }
 public void begin(){if(running)return;running=true;postOnAnimation(frameTick);}
 public void end(){running=false;removeCallbacks(frameTick);bind(null,null,null,"");}
 private static String clean(CharSequence value){return value==null?"":value.toString().replace('\n',' ').replace('\r',' ').trim();}
 public void bind(MediaMetadata next,PlaybackState state,Bitmap nextFallbackArtwork){bind(next,state,nextFallbackArtwork,"");} public void bind(MediaMetadata next,PlaybackState state,Bitmap nextFallbackArtwork,String nextPackageName){
  String previousKey=title+"\n"+subtitle+"\n"+album+"\n"+packageName;
  metadata=next;playback=state;artwork=null;fallbackArtwork=nextFallbackArtwork;packageName=clean(nextPackageName);title="";subtitle="";album="";
  if(next!=null){
   title=clean(next.getText(MediaMetadata.METADATA_KEY_TITLE));
   if(title.isEmpty())title=clean(next.getText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE));
   if(title.isEmpty())title=clean(next.getDescription().getTitle());
   subtitle=clean(next.getText(MediaMetadata.METADATA_KEY_ARTIST));
   if(subtitle.isEmpty())subtitle=clean(next.getText(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE));
   if(subtitle.isEmpty())subtitle=clean(next.getText(MediaMetadata.METADATA_KEY_ALBUM_ARTIST));
   album=clean(next.getText(MediaMetadata.METADATA_KEY_ALBUM));
   artwork=next.getBitmap(MediaMetadata.METADATA_KEY_ART);
   if(artwork==null)artwork=next.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
   if(artwork==null)artwork=next.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
   if(artwork==null)artwork=next.getDescription().getIconBitmap();
   if(artwork==null)artwork=artworkResolver.resolve(next,fallbackArtwork);
  }
  String nextKey=title+"\n"+subtitle+"\n"+album+"\n"+packageName;
  if(!nextKey.equals(previousKey)){marqueeStartedAt=SystemClock.elapsedRealtime();albumFocused=false;}
  boolean eligible=next!=null&&state!=null&&!title.isEmpty()&&NowPlayingPolicy.rank(state.getState())>0;
  setVisibility(eligible?VISIBLE:GONE);postInvalidateOnAnimation();
 }
 public boolean focusAlbumArt(){if(getVisibility()!=VISIBLE||metadata==null)return false;albumFocused=true;postInvalidateOnAnimation();return true;}
 public boolean albumArtFocused(){return albumFocused;}
 public void clearAlbumArtFocus(){if(albumFocused){albumFocused=false;postInvalidateOnAnimation();}}
 public NowPlayingTrack currentTrack(){return metadata==null?null:new NowPlayingTrack(packageName,title,subtitle,album);} private void text(Canvas canvas,String value,float x,float y,float size,int colour,float width){
  paint.setStyle(Paint.Style.FILL);paint.setColor(colour);paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(size);
  canvas.drawText(TextUtils.ellipsize(value,paint,width,TextUtils.TruncateAt.END).toString(),x,y,paint);
 }
 private void scrollingText(Canvas canvas,String value,float x,float y,float size,int colour,float width,long now,float speed){
  paint.setStyle(Paint.Style.FILL);paint.setColor(colour);paint.setTypeface(Typeface.DEFAULT);paint.setTextSize(size);
  float measured=paint.measureText(value);if(measured<=width){canvas.drawText(value,x,y,paint);return;}
  long since=Math.max(0L,now-marqueeStartedAt);float offset=NowPlayingMarqueePolicy.offset(measured,width,since,speed);
  canvas.save();canvas.clipRect(x,y-size*1.25f,x+width,y+size*.35f);canvas.drawText(value,x-offset,y,paint);canvas.restore();
 }
 private static String stateText(int state){
  if(state==PlaybackState.STATE_PLAYING)return "Playing";
  if(state==PlaybackState.STATE_PAUSED)return "Paused";
  if(state==PlaybackState.STATE_BUFFERING)return "Buffering";
  if(state==PlaybackState.STATE_CONNECTING)return "Connecting";
  return "Now Playing";
 }
 @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(metadata==null||playback==null)return;
  int[] r=NowPlayingLayout.cardRect(getWidth(),getHeight());float x=r[0],y=r[1],w=r[2]-r[0],h=r[3]-r[1];if(w<=0||h<=0)return;
  float s=w/516f;box.set(x,y,x+w,y+h);paint.setStyle(Paint.Style.FILL);paint.setColor(Color.rgb(16,16,16));canvas.drawRoundRect(box,14*s,14*s,paint);
  paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(1f,s));paint.setColor(Color.rgb(48,48,48));canvas.drawRoundRect(box,14*s,14*s,paint);paint.setStyle(Paint.Style.FILL);
  if(artwork==null)artwork=artworkResolver.resolve(metadata,fallbackArtwork);
  RectF cover=new RectF(x+18*s,y+23*s,x+144*s,y+149*s);paint.setColor(Color.rgb(28,28,28));canvas.drawRoundRect(cover,10*s,10*s,paint);
  if(artwork!=null&&!artwork.isRecycled()){
   int side=Math.min(artwork.getWidth(),artwork.getHeight());Rect source=new Rect((artwork.getWidth()-side)/2,(artwork.getHeight()-side)/2,(artwork.getWidth()+side)/2,(artwork.getHeight()+side)/2);
   Path clip=new Path();clip.addRoundRect(cover,10*s,10*s,Path.Direction.CW);canvas.save();canvas.clipPath(clip);paint.setColor(Color.WHITE);canvas.drawBitmap(artwork,source,cover,paint);canvas.restore();
  }  if(albumFocused){
   paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(Math.max(3f,4*s));paint.setColor(Color.rgb(77,184,255));
   RectF focus=new RectF(cover.left-3*s,cover.top-3*s,cover.right+3*s,cover.bottom+3*s);canvas.drawRoundRect(focus,12*s,12*s,paint);paint.setStyle(Paint.Style.FILL);
  }
  float tx=x+164*s,tw=w-184*s;long now=SystemClock.elapsedRealtime();
  scrollingText(canvas,title,tx,y+53*s,31*s,Color.WHITE,tw,now,55*s);
  scrollingText(canvas,subtitle,tx,y+85*s,23*s,Color.LTGRAY,tw,now,55*s);
  text(canvas,stateText(playback.getState()),tx,y+108*s,16*s,Color.LTGRAY,tw);
  long duration=metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
  float top=y+132*s;paint.setColor(Color.rgb(58,58,58));box.set(tx,top,tx+tw,top+8*s);canvas.drawRoundRect(box,4*s,4*s,paint);
  if(duration>0){long position=NowPlayingPolicy.position(playback.getPosition(),playback.getLastPositionUpdateTime(),SystemClock.elapsedRealtime(),playback.getPlaybackSpeed(),playback.getState(),duration);
   paint.setColor(Color.rgb(77,184,255));box.right=tx+tw*(float)((double)position/duration);canvas.drawRoundRect(box,4*s,4*s,paint);}
 }
 @Override protected void onDetachedFromWindow(){artworkResolver.clear();end();super.onDetachedFromWindow();}
}