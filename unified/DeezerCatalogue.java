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
        String title=query, requestedArtist="";
        int by=query.toLowerCase(Locale.ROOT).lastIndexOf(" by ");
        if(by>0) { title=query.substring(0,by).trim(); requestedArtist=query.substring(by+4).trim(); }
        JSONArray tracks=get(http,"search/track?q="+URLEncoder.encode(query,"UTF-8")+"&limit=25").optJSONArray("data");
        JSONObject first=tracks==null?null:tracks.optJSONObject(0);
        JSONObject rankedArtist=first==null?null:first.optJSONObject("artist");
        if(first!=null && !normal(title).equals(normal(first.optString("title")))
                && rankedArtist!=null && rankedArtist.optLong("id")>0
                && normal(query).equals(normal(rankedArtist.optString("name")))) artist=rankedArtist;
        // A real artist can share a famous song's title. Deezer's first ranked exact
        // song result wins that ambiguity; an artist-name search normally ranks its songs.
        if(artist!=null && (first==null || !normal(title).equals(normal(first.optString("title")))))
            return new Selection(artist.getString("name"),"https://www.deezer.com/artist/"+artist.getLong("id"),artist.getString("name"),"Play top tracks",false,false);
        if(tracks==null) return null;
        // Deezer's top exact title match is the default; "by <artist>" disambiguates covers.
        for(int i=0;i<tracks.length();i++) {
            JSONObject song=tracks.optJSONObject(i);
            if(song==null || song.optLong("id")<=0 || !song.optBoolean("readable",true) || !normal(title).equals(normal(song.optString("title")))) continue;
            JSONObject performer=song.optJSONObject("artist");
            if(performer==null) continue;
            String artistName=performer.optString("name");
            if(artistName.isEmpty() || (!requestedArtist.isEmpty() && !normal(requestedArtist).equals(normal(artistName)))) continue;
            return new Selection(song.getString("title")+" by "+artistName,"https://www.deezer.com/track/"+song.getLong("id"),"",song.getString("title"),false,true);
        }
        return null;
    }
    private static JSONObject get(DeezerArtistClient.Http http,String path) throws Exception {
        return new JSONObject(http.request("https://api.deezer.com/"+path,null,null));
    }
    private static String normal(String s) { return s.trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT); }
}
