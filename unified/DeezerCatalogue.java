package com.boop.alpha1;

import com.boop.shared.MediaRequest;
import java.net.URLEncoder;
import java.util.*;
import org.json.*;

/** Public metadata only; catalogue rank is never permission to guess an identity. */
final class DeezerCatalogue {
    static final class Selection {
        final String name,url,pageTitle,label;
        final boolean flow,track;
        final List<Selection> choices;
        final String question;
        Selection(String name,String url,String pageTitle,String label,boolean flow,boolean track) {
            this(name,url,pageTitle,label,flow,track,Collections.emptyList(),"");
        }
        private Selection(String name,String url,String pageTitle,String label,boolean flow,boolean track,
                List<Selection> choices,String question) {
            this.name=name;this.url=url;this.pageTitle=pageTitle;this.label=label;this.flow=flow;this.track=track;
            this.choices=Collections.unmodifiableList(new ArrayList<>(choices));this.question=question;
        }
        boolean ambiguous() {return !question.isEmpty();}
    }
    static Selection resolve(DeezerArtistClient.Http http,MediaRequest request)throws Exception {
        if(request.kind==MediaRequest.Kind.DEEZER_FLOW)
            return new Selection("Deezer Flow","","","Flow",true,false);
        String query=request.query.trim(), intent="";
        String lower=normal(query);
        for(String prefix:new String[]{"the artist ","artist ","the band ","band ","the song ","song ","the track ","track "}) {
            if(lower.startsWith(prefix)) {intent=prefix.contains("artist")||prefix.contains("band")?"artist":"track";
                query=query.substring(prefix.length()).trim();break;}
        }
        if(query.isEmpty())return null;
        JSONArray artists=get(http,"search/artist",query);
        JSONObject artist=DeezerArtistClient.exactArtist(artists,query);
        JSONArray tracks=get(http,"search/track",query);
        if(artist==null)artist=corroboratedArtist(artists,tracks,query);
        String title=query, requestedArtist="";
        int by=normal(query).lastIndexOf(" by ");
        if(by>0) {title=query.substring(0,by).trim();requestedArtist=query.substring(by+4).trim();}
        Map<String,JSONArray> identities=new HashMap<>();
        Map<String,Selection> literal=new LinkedHashMap<>(), combined=new LinkedHashMap<>();
        if(tracks!=null && !intent.equals("artist"))for(int i=0;i<tracks.length();i++) {
            JSONObject song=tracks.optJSONObject(i);
            if(song==null || song.optLong("id")<=0 || !song.optBoolean("readable",true))continue;
            JSONObject performer=song.optJSONObject("artist");
            if(performer==null || performer.optLong("id")<=0)continue;
            String name=performer.optString("name"), songTitle=song.optString("title");
            if(name.trim().isEmpty() || songTitle.trim().isEmpty())continue;
            String base=recordingTitle(songTitle);
            boolean explicit=!requestedArtist.isEmpty() && normal(requestedArtist).equals(normal(name))
                    && (normal(title).equals(normal(base)) || normal(title).equals(normal(songTitle)));
            boolean composite=requestedArtist.isEmpty() && (normal(query).equals(normal(name+" "+base))
                    || normal(query).equals(normal(base+" "+name))
                    || normal(query).equals(normal(name+"'s "+base))
                    || normal(query).equals(normal(name+" "+songTitle))
                    || normal(query).equals(normal(songTitle+" "+name))
                    || normal(query).equals(normal(name+"'s "+songTitle)));
            boolean exact=requestedArtist.isEmpty() && (normal(title).equals(normal(base)) || normal(title).equals(normal(songTitle)));
            if(!explicit && !composite && !exact)continue;
            if(!requestedArtist.isEmpty() || composite) {
                String key=normal(name);
                if(!identities.containsKey(key)) {
                    JSONArray identity=normal(query).equals(normal(name))?artists:get(http,"search/artist",name);
                    identities.put(key,identity);
                }
                if(!containsArtist(identities.get(key),performer))continue;
            }
            Selection selected=new Selection(songTitle+" by "+name,"https://www.deezer.com/track/"+song.getLong("id"),"",songTitle,false,true);
            Map<String,Selection> candidates=(explicit||composite)?combined:literal;
            String identity=performer.optLong("id")+":"+normal(base);
            // Prefer the unqualified recording when both it and a remaster exist.
            Selection prior=candidates.get(identity);
            if(prior==null || (normal(songTitle).equals(normal(base)) && !normal(prior.label).equals(normal(base))))candidates.put(identity,selected);
        }
        // Duplicate exact artists are ambiguous even when track ranking favours one.
        if(combined.isEmpty() && artist==null && !intent.equals("track") && requestedArtist.isEmpty() && artists!=null) {
            Set<Long> ids=new HashSet<>();
            for(int i=0;i<artists.length();i++) {JSONObject row=artists.optJSONObject(i);
                if(row!=null && row.optLong("id")>0 && normal(query).equals(normal(row.optString("name"))))ids.add(row.optLong("id"));}
            if(ids.size()>1)return new Selection("","","","",false,false,Collections.emptyList(),
                    "More than one artist is called "+query+". Please name a song and its artist.");
        }
        List<Selection> choices=new ArrayList<>();
        if(!combined.isEmpty())choices.addAll(combined.values());
        else {
            if(artist!=null && requestedArtist.isEmpty() && !intent.equals("track"))
                choices.add(new Selection(artist.getString("name"),"https://www.deezer.com/artist/"+artist.getLong("id"),artist.getString("name"),"Play top tracks",false,false));
            if(!intent.equals("artist"))choices.addAll(literal.values());
        }
        if(choices.size()==1)return choices.get(0);
        if(choices.size()>1)return ambiguity(choices);
        return null;
    }
    private static Selection ambiguity(List<Selection> choices) {
        boolean hasArtist=false,hasSong=false;
        for(Selection choice:choices){hasArtist|=!choice.track;hasSong|=choice.track;}
        String question;
        if(hasArtist && hasSong)question="Do you mean the artist "+choices.get(0).name+", or the song?";
        else question="Which song do you mean: "+choices.get(0).name+", or "+choices.get(1).name+"?";
        if(choices.size()>2)question+=" Please say play, the song title, and by the artist.";
        return new Selection("","","","",false,false,choices,question);
    }
    private static boolean containsArtist(JSONArray artists,JSONObject performer) {
        if(artists==null)return false;
        for(int i=0;i<artists.length();i++) {JSONObject row=artists.optJSONObject(i);
            if(row!=null && row.optLong("id")==performer.optLong("id")
                    && normal(row.optString("name")).equals(normal(performer.optString("name"))))return true;}
        return false;
    }
    private static JSONObject corroboratedArtist(JSONArray artists,JSONArray tracks,String query) {
        // Multiple different songs must agree on one catalogue-verified performer ID.
        // Any competing exact-name performer keeps the request ambiguous.
        JSONObject candidate=null;Set<String> titles=new HashSet<>();
        if(tracks==null)return null;
        for(int i=0;i<tracks.length();i++) {JSONObject row=tracks.optJSONObject(i);
            if(row==null || row.optLong("id")<=0 || !row.optBoolean("readable",true))continue;
            JSONObject performer=row.optJSONObject("artist");
            if(performer==null || !normal(query).equals(normal(performer.optString("name"))) || !containsArtist(artists,performer))continue;
            if(candidate!=null && candidate.optLong("id")!=performer.optLong("id"))return null;
            candidate=performer;titles.add(normal(recordingTitle(row.optString("title"))));
        }
        return titles.size()>=2?candidate:null;
    }
    static Selection answer(Selection pending,String text) {
        String value=normal(text).replaceAll("[.!?]+$","");
        if(value.startsWith("play "))value=value.substring(5);
        boolean artist=value.matches("(the )?(artist|band)( please)?");
        boolean song=value.matches("(the )?(song|track)( please)?");
        Selection match=null;
        for(Selection choice:pending.choices) {
            if((artist&&!choice.track) || (song&&choice.track) || value.equals(normal(choice.name))) {
                if(match!=null)return null;
                match=choice;
            }
        }
        return match;
    }
    private static JSONArray get(DeezerArtistClient.Http http,String path,String query)throws Exception {
        return new JSONObject(http.request("https://api.deezer.com/"+path+"?q="+URLEncoder.encode(query,"UTF-8")+"&limit=25",null,null)).optJSONArray("data");
    }
    private static String recordingTitle(String title) {
        // Studio mastering labels only. Live, karaoke, tribute and arbitrary remixes stay distinct.
        return title.replaceFirst("(?i)\\s*(?:\\((?:(?:\\d{4}\\s+)?remaster(?:ed)?(?:\\s+\\d{4})?|ultimate mix)\\)| - (?:(?:\\d{4}\\s+)?remaster(?:ed)?(?:\\s+\\d{4})?|ultimate mix))$","").trim();
    }
    private static String normal(String value) {return value.replace('\u2019','\'').trim().replaceAll("\\s+"," ").toLowerCase(Locale.ROOT);}
}
