package com.boop.shieldhome;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import com.boop.shared.DeezerAlbumMatch;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** User-triggered public metadata lookup. Never sends a playback command. */
final class DeezerAlbumBrowser {
    private long generation;
    private boolean busy;
    synchronized void cancel() { generation++; busy=false; }

    synchronized void open(Activity activity, ShieldNowPlayingManager manager, NowPlayingSnapshot requested) {
        if (busy) return;
        if (requested.album().isEmpty() || requested.title().isEmpty() || requested.subtitle().isEmpty()) {
            message(activity,"Album details aren't available for this track."); return;
        }
        final long operation=++generation;
        busy=true;
        message(activity,"Finding album…");
        new Thread(() -> {
            long album=0;
            try { album=lookup(requested); } catch(Exception unavailable) { /* Plain local failure below. */ }
            final long albumId=album;
            activity.runOnUiThread(() -> {
                synchronized(DeezerAlbumBrowser.this) {
                    if(operation!=generation) return;
                    busy=false;
                    if(activity.isFinishing() || activity.isDestroyed() || !activity.hasWindowFocus()) return;
                    NowPlayingSnapshot current=manager.state().current();
                    if(current==null || current.sessionId()!=requested.sessionId()
                            || !current.trackKey().equals(requested.trackKey())
                            || !current.album().equals(requested.album())) return;
                    if(albumId<=0) { message(activity,"I couldn't identify this exact album."); return; }
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.deezer.com/album/"+albumId)).setPackage("deezer.android.app"));
                    } catch(RuntimeException unavailable) { message(activity,"Deezer couldn't open that album."); }
                }
            });
        },"boop-album-browse").start();
    }

    private static long lookup(NowPlayingSnapshot track) throws Exception {
        String query=track.title()+" "+track.subtitle();
        if(query.length()>500 || track.album().length()>500) return 0;
        HttpURLConnection connection=(HttpURLConnection)new URL("https://api.deezer.com/search/track?q="
                +URLEncoder.encode(query,"UTF-8")+"&limit=100").openConnection();
        connection.setInstanceFollowRedirects(false);
        connection.setConnectTimeout(5000); connection.setReadTimeout(5000);
        try {
            if(connection.getResponseCode()!=200) return 0;
            StringBuilder body=new StringBuilder();
            try(Reader reader=new InputStreamReader(connection.getInputStream(),"UTF-8")) {
                char[] buffer=new char[4096]; int count;
                while((count=reader.read(buffer))!=-1) {
                    if(body.length()+count>1048576) return 0;
                    body.append(buffer,0,count);
                }
            }
            JSONArray data=new JSONObject(body.toString()).optJSONArray("data");
            List<DeezerAlbumMatch.Row> rows=new ArrayList<>();
            for(int i=0;data!=null && i<data.length();i++) {
                JSONObject row=data.optJSONObject(i);
                if(row==null) continue;
                JSONObject artist=row.optJSONObject("artist"), album=row.optJSONObject("album");
                if(artist==null || album==null) continue;
                rows.add(new DeezerAlbumMatch.Row(row.optString("title"),artist.optString("name"),
                        album.optString("title"),album.optLong("id")));
            }
            return DeezerAlbumMatch.resolve(track.title(),track.subtitle(),track.album(),rows);
        } finally { connection.disconnect(); }
    }
    private static void message(Activity activity,String text) {
        Toast.makeText(activity,text,Toast.LENGTH_SHORT).show();
    }
}
