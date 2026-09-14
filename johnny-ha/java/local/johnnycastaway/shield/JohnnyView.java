package local.johnnycastaway.shield;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.FrameLayout;
import java.io.*;
public final class JohnnyView extends FrameLayout {
 private final NativeJohnnyView nativePlayer;
 private final NowPlayingObserver media;
 private final NowPlayingView card;
 private final JohnnyStateClient states;
 private final Handler main=new Handler(Looper.getMainLooper());
 private static final Object RESOURCE_LOCK=new Object();
 private boolean running;private long generation;
 public JohnnyView(Context c){
  super(c);setBackgroundColor(Color.BLACK);
  nativePlayer=new NativeJohnnyView(c);addView(nativePlayer,new FrameLayout.LayoutParams(-1,-1));
  card=new NowPlayingView(c);addView(card,new FrameLayout.LayoutParams(-1,-1));
  media=new NowPlayingObserver(c,card);states=new JohnnyStateClient(c,nativePlayer);
 }
 public void start(){
  if(running)return;running=true;final long session=++generation;media.start();
  Thread preparation=new Thread(()->{
   try{
    synchronized(RESOURCE_LOCK){
    File dir=new File(getContext().getFilesDir(),"johnny");
    if(!dir.isDirectory()&&!dir.mkdirs())throw new IOException("Resource directory");
    for(String name:new String[]{"RESOURCE.MAP","RESOURCE.001"}){
     File target=new File(dir,name);
     if(target.isFile()&&target.length()>0)continue;
     File temporary=new File(dir,name+".tmp");
     try(InputStream in=getContext().getAssets().open("original/"+name);OutputStream out=new FileOutputStream(temporary)){
      byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);
     }
     if(!temporary.renameTo(target))throw new IOException("Resource install");
    }
    }
    main.post(()->{if(running&&generation==session){nativePlayer.start();states.start();}});
   }catch(IOException e){main.post(()->{if(running&&generation==session)Log.e("JohnnyHA","Original resources unavailable");});}
  },"JohnnyResources");preparation.setDaemon(true);preparation.start();
 }
 public boolean focusAlbumArt(){return card.focusAlbumArt();}
 public boolean albumArtFocused(){return card.albumArtFocused();}
 public void clearAlbumArtFocus(){card.clearAlbumArtFocus();}
 public NowPlayingTrack currentTrack(){return card.currentTrack();}
 public NowPlayingView nowPlayingView(){return card;}
 public void stop(){if(!running)return;running=false;generation++;states.stop();media.stop();nativePlayer.stop();main.removeCallbacksAndMessages(null);}
 @Override protected void onDetachedFromWindow(){stop();super.onDetachedFromWindow();}
}
