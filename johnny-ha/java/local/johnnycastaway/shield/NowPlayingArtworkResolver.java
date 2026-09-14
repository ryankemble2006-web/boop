package local.johnnycastaway.shield;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.net.Uri;
import android.os.Handler;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.HttpsURLConnection;
/** Unified-style safe artwork resolver; remote work never blocks the UI thread. */
public final class NowPlayingArtworkResolver {
 private static final int MAX_CACHE_ENTRIES=12;
 private static final int MAX_REMOTE_BYTES=8*1024*1024;
 private final Context context;
 private final Handler mainHandler;
 private final Runnable invalidate;
 private final ExecutorService executor=Executors.newSingleThreadExecutor();
 private final LinkedHashMap<String,Bitmap> cache=new LinkedHashMap<>();
 private final Set<String> pending=new HashSet<>(),failed=new HashSet<>();
 private int generation;
 public NowPlayingArtworkResolver(Context context,Handler handler,Runnable invalidate){
  this.context=context;this.mainHandler=handler;this.invalidate=invalidate;
 }
 public Bitmap resolve(MediaMetadata metadata,Bitmap notificationFallback){
  if(metadata==null)return notificationFallback;
  for(String key:new String[]{MediaMetadata.METADATA_KEY_ART,MediaMetadata.METADATA_KEY_ALBUM_ART,MediaMetadata.METADATA_KEY_DISPLAY_ICON}){
   Bitmap bitmap=metadata.getBitmap(key);if(bitmap!=null)return bitmap;
  }
  for(String key:new String[]{MediaMetadata.METADATA_KEY_ART_URI,MediaMetadata.METADATA_KEY_ALBUM_ART_URI,MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI}){
   String raw=metadata.getString(key);NowPlayingArtworkSourcePolicy.Kind kind=NowPlayingArtworkSourcePolicy.kind(raw);
   if(kind==NowPlayingArtworkSourcePolicy.Kind.LOCAL){Bitmap local=decodeLocal(raw);if(local!=null)return local;}
   else if(kind==NowPlayingArtworkSourcePolicy.Kind.REMOTE_HTTPS){
    String clean=raw.trim();Bitmap cached=cache.get(clean);if(cached!=null)return cached;schedule(clean);
   }
  }
  return notificationFallback;
 }
 public void clear(){generation++;cache.clear();pending.clear();failed.clear();}
 private Bitmap decodeLocal(String raw){
  try{Uri uri=Uri.parse(raw.trim());try(InputStream stream=context.getContentResolver().openInputStream(uri)){return stream==null?null:BitmapFactory.decodeStream(stream);}}
  catch(Exception unavailable){return null;}
 }
 private void schedule(String raw){
  if(cache.containsKey(raw)||pending.contains(raw)||failed.contains(raw))return;
  pending.add(raw);int requestGeneration=generation;
  executor.execute(()->{Bitmap bitmap=downloadHttps(raw);mainHandler.post(()->{
   if(requestGeneration!=generation)return;pending.remove(raw);
   if(bitmap==null){failed.add(raw);return;}failed.remove(raw);cache.put(raw,bitmap);
   while(cache.size()>MAX_CACHE_ENTRIES){String eldest=cache.keySet().iterator().next();cache.remove(eldest);}
   if(invalidate!=null)invalidate.run();
  });});
 }
 private Bitmap downloadHttps(String raw){
  HttpsURLConnection connection=null;
  try{
   URL url=new URL(raw);connection=(HttpsURLConnection)url.openConnection();
   connection.setConnectTimeout(4000);connection.setReadTimeout(6000);connection.setInstanceFollowRedirects(true);connection.setUseCaches(true);
   connection.setRequestProperty("Accept","image/*");connection.setRequestProperty("User-Agent","Johnny-Castaway-Shield");
   int response=connection.getResponseCode();if(response<200||response>=300)return null;
   if(!"https".equalsIgnoreCase(connection.getURL().getProtocol()))return null;
   int length=connection.getContentLength();if(length>MAX_REMOTE_BYTES)return null;
   String type=connection.getContentType();if(type!=null&&!type.toLowerCase().startsWith("image/"))return null;
   try(InputStream stream=connection.getInputStream()){return BitmapFactory.decodeStream(stream);}
  }catch(Exception unavailable){return null;}
  finally{if(connection!=null)connection.disconnect();}
 }
}
