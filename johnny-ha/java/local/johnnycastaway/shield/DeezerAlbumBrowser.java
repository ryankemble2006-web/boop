package local.johnnycastaway.shield;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
/** Exact Unified-style Deezer album lookup. Lookup never closes Johnny on failure. */
public final class DeezerAlbumBrowser {
 public interface Callback { void openAlbum(long albumId); }
 private final Handler main=new Handler(Looper.getMainLooper());
 private long generation; private boolean busy;
 public synchronized void cancel(){generation++;busy=false;}
 public synchronized void open(Context context,NowPlayingView view,NowPlayingTrack requested,Callback callback){
  if(busy)return;
  if(requested==null||!requested.complete()){message(context,"Album details aren't available for this track.");return;}
  final long operation=++generation;busy=true;message(context,"Finding album…");
  new Thread(()->{long album=0;try{album=lookup(requested);}catch(Exception unavailable){ }
   final long albumId=album;main.post(()->finishLookup(context,view,requested,callback,operation,albumId));
  },"johnny-album-browse").start();
 } private void finishLookup(Context context,NowPlayingView view,NowPlayingTrack requested,Callback callback,long operation,long albumId){
  synchronized(this){
   if(operation!=generation)return;busy=false;
   NowPlayingTrack current=view.currentTrack();if(!requested.sameAs(current))return;
   if(albumId<=0){message(context,"I couldn't identify this exact album.");return;}
   callback.openAlbum(albumId);
  }
 }
 private static long lookup(NowPlayingTrack track)throws Exception{
  String query=track.title+" "+track.artist;if(query.length()>500||track.album.length()>500)return 0;
  HttpURLConnection connection=(HttpURLConnection)new URL("https://api.deezer.com/search/track?q="+URLEncoder.encode(query,"UTF-8")+"&limit=100").openConnection();
  connection.setInstanceFollowRedirects(false);connection.setConnectTimeout(5000);connection.setReadTimeout(5000);
  try{
   if(connection.getResponseCode()!=200)return 0;StringBuilder body=new StringBuilder();
   try(Reader reader=new InputStreamReader(connection.getInputStream(),"UTF-8")){char[] buffer=new char[4096];int count;
    while((count=reader.read(buffer))!=-1){if(body.length()+count>1048576)return 0;body.append(buffer,0,count);}}
   JSONArray data=new JSONObject(body.toString()).optJSONArray("data");List<DeezerAlbumMatch.Row> rows=new ArrayList<>();
   for(int i=0;data!=null&&i<data.length();i++){
    JSONObject row=data.optJSONObject(i);if(row==null)continue;JSONObject artist=row.optJSONObject("artist"),album=row.optJSONObject("album");
    if(artist==null||album==null)continue;rows.add(new DeezerAlbumMatch.Row(row.optString("title"),artist.optString("name"),album.optString("title"),album.optLong("id")));
   }
   return DeezerAlbumMatch.resolve(track.title,track.artist,track.album,rows);
  }finally{connection.disconnect();}
 }
 private static void message(Context context,String text){Toast.makeText(context,text,Toast.LENGTH_SHORT).show();}
}