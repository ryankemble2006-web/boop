package com.boop.alpha1;

import com.boop.shared.MediaRequest;
import java.net.URLEncoder;
import java.util.Locale;
import org.json.*;

/** Public metadata only; no HA or Deezer account credentials leave the LAN. */
final class DeezerCatalogue {
    static final class Selection {
        final String name,url,pageTitle,label;
        final boolean flow,track;
        Selection(String name,String url,String pageTitle,String label,boolean flow,boolean track) {
            this.name=name; this.url=url; this.pageTitle=pageTitle; this.label=label; this.flow=flow; this.track=track;
        }
    }
    static Selection resolve(DeezerArtistClient.Http http,MediaRequest request) throws Exception {
        if(request.kind==MediaRequest.Kind.DEEZER_FLOW) return new Selection("Deezer Flow","","","Flow",true,false);
        String query=request.query;
        JSONObject artists=get(http,"search/artist?q="+URLEncoder.encode(query,"UTF-8")+"&limit=25");
        JSONObject artist=DeezerArtistClient.exactArtist(artists.optJSONArray("data"),query);
        if(artist!=null) return new Selection(artist.getString("name"),"https://www.deezer.com/artist/"+artist.getLong("id"),artist.getString("name"),"Play top tracks",false,false);
        String title=query, requestedArtist="";
        int by=query.toLowerCase(Locale.ROOT).lastIndexOf(" by ");
        if(by>0) { title=query.substring(0,by).trim(); requestedArtist=query.substring(by+4).trim(); }
        JSONArray tracks=get(http,"search/track?q="+URLEncoder.encode(query,"UTF-8")+"&limit=25").optJSONArray("data");
        if(tracks==null) return null;
        // Deezer's top exact title match is the default; "by <artist>" disambiguates covers.
        for(int i=0;i<tracks.length();i++) {
            JSONObject song=tracks.optJSONObject(i);
            if(song==null || song.optLong("id")<=0 || !song.optBoolean("readable",true) || !normal(title).equals(normal(song.optString("title")))) continue;
            JSONObject performer=song.optJSONObject("artist"), album=song.optJSONObject("album");
            if(performer==null || album==null || album.optLong("id")<=0) continue;
            String artistName=performer.optString("name");
            if(artistName.isEmpty() || (!requestedArtist.isEmpty() && !normal(requestedArtist).equals(normal(artistName)))) continue;
            JSONObject detail=get(http,"album/"+album.getLong("id"));
            JSONObject trackList=detail.optJSONObject("tracks");
            JSONArray rows=trackList==null?null:trackList.optJSONArray("data");
            if(rows==null || detail.optString("title").isEmpty()) continue;
            for(int j=0;j<rows.length();j++) {
                JSONObject row=rows.optJSONObject(j);
                if(row!=null && row.optLong("id")==song.getLong("id") && !row.optString("title").isEmpty())
                    return new Selection(song.getString("title")+" by "+artistName,"https://www.deezer.com/album/"+album.getLong("id"),detail.getString("title"),row.getString("title"),false,true);
            }
        }
        return null;
    }
    private static JSONObject get(DeezerArtistClient.Http http,String path) throws Exception {
        return new JSONObject(http.request("https://api.deezer.com/"+path,null,null));
    }
    private static String normal(String s) { return s.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT); }
}
