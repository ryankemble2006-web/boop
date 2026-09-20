package com.boop.alpha1;
import org.json.*;
import java.util.*;

public final class MusicVoiceSelectionProbe {
    static int checks, failures;
    static void check(boolean value,String message) {
        checks++; if(!value) {failures++;System.err.println("FAIL "+message);}
    }
    static JSONObject artist(long id,String name) {return new JSONObject().put("id",id).put("name",name);}
    static JSONObject track(long id,String title,long performer,String name) {
        return new JSONObject().put("id",id).put("title",title).put("readable",true).put("artist",artist(performer,name));
    }
    static final class Rig implements DeezerArtistClient.Http {
        JSONArray artists=new JSONArray(), tracks=new JSONArray();
        JSONArray nameArtists;
        BoopRoom room=new BoopRoom("lounge","Lounge");
        int houseCalls,catalogueCalls; boolean offline;long now=1000;
        String status="",token="private-test-token",connection="test-registration";
        DeezerArtistClient client=new DeezerArtistClient(this,(b,t)->Collections.singleton("media_player.tv"),ms->{},()->now);
        public String request(String url,String token,JSONObject body)throws Exception {
            if(url.startsWith("https://api.deezer.com/")) {
                catalogueCalls++;
                check(token==null && body==null,"catalogue receives no credentials or POST");
                if(offline)throw new java.io.IOException();
                JSONArray reply=url.contains("search/artist")?artists:tracks;
                if(nameArtists!=null && url.contains("search/artist?q=John+Lennon&"))reply=nameArtists;
                return new JSONObject().put("data",reply).toString();
            }
            houseCalls++;
            if(url.endsWith("/api/template")) return new JSONArray().put(new JSONObject()
                .put("media","media_player.tv").put("remote","remote.tv")
                .put("adb",new JSONArray().put("media_player.adb"))).toString();
            return new JSONObject().put("state","on").toString();
        }
        String say(String text)throws Exception {
            CommandOutcome outcome=client.processForConnection("http://ha.invalid",token,connection,text,room,()->room);
            status=outcome==null?"":outcome.status().name();
            return outcome==null?"FALLTHROUGH":LocalReply.forOutcome(outcome);
        }
        Rig lennon() {artists.put(artist(226,"John Lennon"));tracks.put(track(1,"John Lennon Imagine",999,"Namesake"));
            tracks.put(track(2,"Imagine (Remastered 2010)",226,"John Lennon"));return this;}
        Rig collision() {artists.put(artist(412,"Queen"));tracks.put(track(3,"Queen",999,"Other Artist"));
            tracks.put(track(4,"Bohemian Rhapsody",412,"Queen"));return this;}
    }
    public static void main(String[] args)throws Exception {
        for(String shortcut:new String[]{"music","play some music"," MUSIC! ","Play   some music.","play some music on Deezer"}) {
            Rig flow=new Rig();int plays=DeezerNativeController.plays;
            check("Done".equals(flow.say(shortcut)) && DeezerNativeController.plays==plays+1
                    && flow.catalogueCalls==0,"Flow shortcut bypasses catalogue: "+shortcut);
        }
        for(String query:new String[]{"John Lennon Imagine","Imagine John Lennon","John Lennon's Imagine","Imagine by John Lennon"}) {
            Rig r=new Rig().lennon(); String result=r.say("play "+query);
            check("Done".equals(result) && DeezerNativeController.played.endsWith("/track/2"),"natural Lennon request: "+query);
        }
        Rig r=new Rig().collision(); int before=DeezerNativeController.plays;
        String reply=r.say("play Queen");
        check("LOCAL_QUESTION".equals(r.status),"clarification opens the existing spoken follow-up window");
        check(reply.toLowerCase(Locale.ROOT).contains("artist") && reply.toLowerCase(Locale.ROOT).contains("song"),"collision asks artist or song");
        check(r.houseCalls==0 && before==DeezerNativeController.plays,"ambiguity has no house/device effects");
        check("Done".equals(r.say("the artist")) && DeezerNativeController.played.endsWith("/artist/412"),"artist clarification executes exact choice");
        check("FALLTHROUGH".equals(r.say("the artist")),"clarification consumed once");
        r=new Rig().collision();r.say("play Queen");r.token="newly-refreshed-access-token";
        check("Done".equals(r.say("the artist")) && DeezerNativeController.played.endsWith("/artist/412"),"normal access-token refresh preserves clarification");
        r=new Rig().collision();r.say("play Queen");r.connection="different-registration";before=DeezerNativeController.plays;
        check("FALLTHROUGH".equals(r.say("the artist")) && before==DeezerNativeController.plays,"changed connection invalidates clarification");
        r=new Rig().collision();r.say("play Queen on Deezer");
        check(r.houseCalls==0,"explicit ambiguity also precedes target discovery");
        check("Done".equals(r.say("the song")) && DeezerNativeController.played.endsWith("/track/3"),"song clarification executes exact choice");
        r=new Rig().collision();r.say("play Queen");r.say("turn the fan on");before=DeezerNativeController.plays;
        check("FALLTHROUGH".equals(r.say("the artist")) && before==DeezerNativeController.plays,"unrelated command clears pending music");
        r=new Rig().collision();r.say("play Queen");r.room=new BoopRoom("bedroom","Bedroom");before=DeezerNativeController.plays;
        check("FALLTHROUGH".equals(r.say("the artist")) && before==DeezerNativeController.plays,"room change invalidates pending music");
        r=new Rig().collision();r.say("play Queen");r.now+=60001;before=DeezerNativeController.plays;
        check("FALLTHROUGH".equals(r.say("the artist")) && before==DeezerNativeController.plays,"expired clarification cannot play");
        r=new Rig().collision();r.say("play Queen");before=DeezerNativeController.plays;
        check("Cancelled.".equals(r.say("cancel")) && before==DeezerNativeController.plays,"cancel is safe");
        check("FALLTHROUGH".equals(r.say("the song")),"cancel removes choices");
        r=new Rig().collision();check("Done".equals(r.say("play artist Queen")) && DeezerNativeController.played.endsWith("/artist/412"),"explicit artist resolves collision");
        r=new Rig().collision();check("Done".equals(r.say("play song Queen")) && DeezerNativeController.played.endsWith("/track/3"),"explicit song resolves collision");
        r=new Rig();r.tracks.put(track(1,"Imagine",226,"John Lennon")).put(track(2,"Imagine",999,"Cover Singer"));before=DeezerNativeController.plays;
        check(!"Done".equals(r.say("play Imagine")) && before==DeezerNativeController.plays,"covers ask instead of taking first title");
        check(!"Done".equals(r.say("the song")) && before==DeezerNativeController.plays,"song answer cannot pick between covers");
        for(String name:new String[]{"Queen","John Lennon","Britney Spears"}) {
            r=new Rig();r.artists.put(artist(412,name));r.tracks.put(track(4,"A real song",412,name));
            check("Done".equals(r.say("play "+name)) && DeezerNativeController.played.endsWith("/artist/412"),"artist-only "+name);
        }
        r=new Rig();r.tracks.put(track(2,"Imagine (Live)",226,"John Lennon"));before=DeezerNativeController.plays;
        check(!"Done".equals(r.say("play Imagine by John Lennon")) && before==DeezerNativeController.plays,"never silently substitute live recording");
        r=new Rig();r.artists.put(artist(1,"Duplicate")).put(artist(2,"Duplicate"));r.tracks.put(track(7,"Song",1,"Duplicate"));before=DeezerNativeController.plays;
        check(!"Done".equals(r.say("play Duplicate")) && before==DeezerNativeController.plays,"duplicate artist IDs must not resolve by ranking");
        r=new Rig();r.offline=true;check("FALLTHROUGH".equals(r.say("play a game")),"unconfirmed nonmusic keeps existing routing");
        check("FALLTHROUGH".equals(r.say("pause music")),"transport keeps existing routing");
        r=new Rig().lennon();check("Done".equals(r.say("play Imagine (Remastered 2010) by John Lennon")) && DeezerNativeController.played.endsWith("/track/2"),"explicit remaster title remains playable");
        r=new Rig().lennon();r.artists.put(artist(99999,"John Lennon"));
        check("Done".equals(r.say("play John Lennon Imagine")) && DeezerNativeController.played.endsWith("/track/2"),"track identity corroborates a duplicate artist name");
        r=new Rig().lennon();r.nameArtists=new JSONArray().put(artist(226,"John Lennon")).put(artist(99999,"John Lennon"));
        r.tracks.put(track(10,"Imagine",99999,"John Lennon"));before=DeezerNativeController.plays;
        check(!"Done".equals(r.say("play John Lennon Imagine")) && before==DeezerNativeController.plays,"full-query artist cache cannot hide a competing verified performer");
        r=new Rig().collision();r.artists.put(artist(99999,"Queen"));r.tracks.put(track(8,"Under Pressure",412,"Queen"));before=DeezerNativeController.plays;
        check(r.say("play Queen").contains("or the song") && before==DeezerNativeController.plays,"corroborated artist never overrides title collision");
        r=new Rig();r.artists.put(artist(1,"Duplicate")).put(artist(2,"Duplicate"));r.tracks.put(track(7,"Duplicate",3,"Someone Else"));before=DeezerNativeController.plays;
        check(!"Done".equals(r.say("play Duplicate")) && before==DeezerNativeController.plays,"duplicate artists plus namesake song must ask");
        final Rig blocked=new Rig().collision();
        java.util.concurrent.CountDownLatch entered=new java.util.concurrent.CountDownLatch(1),release=new java.util.concurrent.CountDownLatch(1);
        blocked.client=new DeezerArtistClient((u,t,b)->{entered.countDown();release.await();return blocked.request(u,t,b);},(b,t)->Collections.emptySet(),ms->{});
        Thread worker=new Thread(()->{try{blocked.say("play Queen");}catch(Exception e){throw new RuntimeException(e);}});
        worker.start();check(entered.await(2,java.util.concurrent.TimeUnit.SECONDS),"slow catalogue entered");
        Thread lifecycle=new Thread(()->blocked.client.cancelClarification());lifecycle.start();lifecycle.join(300);
        check(!lifecycle.isAlive(),"UI cancellation never waits on network IO");
        release.countDown();worker.join(2000);lifecycle.join(2000);
        check("FALLTHROUGH".equals(blocked.say("the artist")),"cancelled lookup cannot restore pending question");
        System.out.println("Music selection: "+checks+" checks, "+failures+" failures");
        if(failures>0)throw new AssertionError("music selection regressions: "+failures);
    }
}
