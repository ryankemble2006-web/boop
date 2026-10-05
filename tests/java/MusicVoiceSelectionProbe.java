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
        final Map<String,JSONArray> catalogue=new HashMap<>();
        boolean strictCatalogue;
        BoopRoom room=new BoopRoom("lounge","Lounge");
        int houseCalls,catalogueCalls; boolean offline;long now=1000;
        String status="",token="private-test-token",connection="test-registration";
        boolean accepted;
        DeezerArtistClient client=new DeezerArtistClient(this,(b,t)->Collections.singleton("media_player.tv"),ms->{},()->now);
        public String request(String url,String token,JSONObject body)throws Exception {
            if(url.startsWith("https://api.deezer.com/")) {
                catalogueCalls++;
                check(token==null && body==null,"catalogue receives no credentials or POST");
                if(offline)throw new java.io.IOException();
                JSONArray reply=url.contains("search/artist")?artists:tracks;
                if(nameArtists!=null && url.contains("search/artist?q=John+Lennon&"))reply=nameArtists;
                if(strictCatalogue) {
                    java.net.URI uri=java.net.URI.create(url);
                    String query=java.net.URLDecoder.decode(uri.getRawQuery().split("&")[0].substring(2),"UTF-8");
                    reply=catalogue.getOrDefault(uri.getPath()+"?q="+query,new JSONArray());
                }
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
            accepted=outcome!=null && outcome.hasAcceptedMusicPlayback();
            return outcome==null?"FALLTHROUGH":LocalReply.forOutcome(outcome);
        }
        Rig lennon() {artists.put(artist(226,"John Lennon"));tracks.put(track(1,"John Lennon Imagine",999,"Namesake"));
            tracks.put(track(2,"Imagine (Remastered 2010)",226,"John Lennon"));return this;}
        Rig collision() {artists.put(artist(412,"Queen"));tracks.put(track(3,"Queen",999,"Other Artist"));
            tracks.put(track(4,"Bohemian Rhapsody",412,"Queen"));return this;}
        Rig metadata(String path,String query,JSONArray rows) {
            strictCatalogue=true;catalogue.put("/"+path+"?q="+query,rows);return this;
        }
    }
    static Rig song(String query,String title,String performer) {
        return new Rig().metadata("search/track",query,new JSONArray()
                .put(track(99,title,999,"A Cover Singer"))
                .put(track(20,title,10,performer)))
                .metadata("search/artist",performer,new JSONArray().put(artist(10,performer)));
    }
    static void plays(Rig rig,String command,long id) throws Exception {
        int before=DeezerNativeController.plays;
        check("Done".equals(rig.say(command)) && rig.accepted && DeezerNativeController.plays==before+1
                && DeezerNativeController.played.equals("https://www.deezer.com/track/"+id),command+" selects the requested recording");
    }
    static void conversationalRequests() throws Exception {
        // The HTTP fixture is keyed by decoded query. Wrappers leaking into search cannot pass.
        String[][] requests={
            {"play Britney Spears Toxic","Britney Spears Toxic","Toxic","Britney Spears"},
            {"play Toxic Britney Spears","Toxic Britney Spears","Toxic","Britney Spears"},
            {"play Toxic by Britney Spears","Toxic by Britney Spears","Toxic","Britney Spears"},
            {"put Britney Spears Toxic on","Britney Spears Toxic","Toxic","Britney Spears"},
            {"put Britney Spears' Toxic on","Britney Spears' Toxic","Toxic","Britney Spears"},
            {"please play Adele's Hello","Adele's Hello","Hello","Adele"},
            {"can you put on Hello by Adele","Hello by Adele","Hello","Adele"},
            {"could you please play The Beatles' Hey Jude","The Beatles' Hey Jude","Hey Jude","The Beatles"},
            {"put Hey Jude by The Beatles on Deezer","Hey Jude by The Beatles","Hey Jude","The Beatles"},
            {"play ABBA Dancing Queen","ABBA Dancing Queen","Dancing Queen","ABBA"},
            {"play Dancing Queen ABBA","Dancing Queen ABBA","Dancing Queen","ABBA"},
            {"put Daft Punk's Get Lucky on","Daft Punk's Get Lucky","Get Lucky","Daft Punk"},
            {"play Get Lucky Daft Punk","Get Lucky Daft Punk","Get Lucky","Daft Punk"},
            {"play Ben E. King Stand by Me","Ben E. King Stand by Me","Stand by Me","Ben E. King"},
            {"play Stand by Me Ben E. King","Stand by Me Ben E. King","Stand by Me","Ben E. King"},
            {"play Stand by Me by Ben E. King","Stand by Me by Ben E. King","Stand by Me","Ben E. King"},
            {"play Rush Fly by Night","Rush Fly by Night","Fly by Night","Rush"},
            {"play Fly by Night Rush","Fly by Night Rush","Fly by Night","Rush"},
            {"play Red Hot Chili Peppers By the Way","Red Hot Chili Peppers By the Way","By the Way","Red Hot Chili Peppers"},
            {"play Death by Unga Bunga Egoless","Death by Unga Bunga Egoless","Egoless","Death by Unga Bunga"},
            {"play Egoless by Death by Unga Bunga","Egoless by Death by Unga Bunga","Egoless","Death by Unga Bunga"},
            {"would you put Mira Sol\u2019s Northbound on","Mira Sol\u2019s Northbound","Northbound","Mira Sol"},
            {"play Please by Mira Sol","Please by Mira Sol","Please","Mira Sol"}
        };
        for(String[] row:requests)plays(song(row[1],row[2],row[3]),row[0],20);
        String[][] listening={
            {"listen to Toxic by Britney Spears","Toxic by Britney Spears","Toxic","Britney Spears"},
            {"I'd like to hear Hello by Adele","Hello by Adele","Hello","Adele"},
            {"I\u2019d like to hear Adele Hello","Adele Hello","Hello","Adele"},
            {"I want to listen to Dancing Queen ABBA","Dancing Queen ABBA","Dancing Queen","ABBA"},
            {"can you listen to Dancing Queen ABBA please","Dancing Queen ABBA","Dancing Queen","ABBA"},
            {"I would like to hear Hey Jude by The Beatles on Deezer","Hey Jude by The Beatles","Hey Jude","The Beatles"},
            {"listen to Me by Mira Sol","Me by Mira Sol","Me","Mira Sol"},
            {"put The Fan by Mira Sol on","The Fan by Mira Sol","The Fan","Mira Sol"}
        };
        for(String[] row:listening)plays(song(row[1],row[2],row[3]),row[0],20);
        for(String command:new String[]{"play the fan","put the song the fan on","put the fan on Deezer"}) {
            Rig r=new Rig().metadata("search/track","the fan",new JSONArray().put(track(20,"the fan",10,"Mira Sol")));
            plays(r,command,20);
        }
        String[][] nonmusicCollisions={
            {"put the fan on","the fan"},
            {"can you put the heating on","the heating"},
            {"please put the bedroom light on","the bedroom light"},
            {"could you put on the living room fan please","the living room fan please"},
            {"put the ceiling lights in the kitchen on","the ceiling lights in the kitchen"},
            {"put the air conditioning on","the air conditioning"},
            {"put the coffee machine on","the coffee machine"},
            {"put my desk lamp on","my desk lamp"},
            {"listen to me","me"},
            {"I'd like to hear your opinion","your opinion"},
            {"I'd like to hear what you think","what you think"},
            {"I want to listen to you","you"},
            {"I'd like to hear about my notifications","about my notifications"}
        };
        for(String[] row:nonmusicCollisions) {
            Rig r=new Rig();r.tracks.put(track(20,row[1],10,"Mira Sol"));int before=DeezerNativeController.plays;
            check("FALLTHROUGH".equals(r.say(row[0])) && !r.accepted && r.catalogueCalls==0 && r.houseCalls==0
                    && DeezerNativeController.plays==before,"nonmusic intent wins over a matching catalogue title: "+row[0]);
        }
        for(String command:new String[]{"could you play Toxic by Britney Spears please",
                "play Toxic by Britney Spears thank you","put Toxic by Britney Spears on please",
                "put on Toxic by Britney Spears please","please play Toxic by Britney Spears on Deezer please"})
            plays(song("Toxic by Britney Spears","Toxic","Britney Spears"),command,20);
        plays(song("Thank You by Dido","Thank You","Dido"),"could you play Thank You by Dido please",20);
        plays(song("Northbound by Mira Please","Northbound","Mira Please"),"play Northbound by Mira Please",20);
        Rig polite=new Rig().metadata("search/track","Toxic",new JSONArray().put(track(20,"Toxic",10,"Britney Spears")));
        plays(polite,"play Toxic please",20);
        Rig ambiguousPolite=song("Toxic","Toxic","Britney Spears");int beforePolite=DeezerNativeController.plays;
        ambiguousPolite.say("play Toxic please");
        check("LOCAL_QUESTION".equals(ambiguousPolite.status) && !ambiguousPolite.accepted && ambiguousPolite.houseCalls==0
                && DeezerNativeController.plays==beforePolite,"courtesy fallback still asks when a title has multiple performers");
        Rig rejectedSong=song("Toxic by Britney Spears","Toxic","Britney Spears");
        DeezerNativeController.fail=true;
        try {
            check("Failed".equals(rejectedSong.say("could you play Toxic by Britney Spears please")) && !rejectedSong.accepted
                    && DeezerNativeController.plays==beforePolite,"natural wording cannot report playback accepted after native rejection");
        } finally {DeezerNativeController.fail=false;}
        // Titles retain precedence over politeness, including when a shorter title also exists.
        for(String title:new String[]{"Please","Thank You","Say Please","Please Please Please"}) {
            Rig r=new Rig().metadata("search/track",title,new JSONArray().put(track(20,title,10,"Mira Sol")))
                    .metadata("search/track","Say",new JSONArray().put(track(21,"Say",11,"Someone Else")))
                    .metadata("search/track","Please Please",new JSONArray().put(track(22,"Please Please",12,"Another Artist")));
            plays(r,"play "+title,20);
        }
        Rig exactPerformer=song("The Beatles' Hey Jude","Hey Jude","The Beatles");
        exactPerformer.catalogue.get("/search/track?q=The Beatles' Hey Jude")
                .put(track(21,"Hey Jude",11,"The Beatles Complete On Ukulele"));
        exactPerformer.metadata("search/artist","The Beatles Complete On Ukulele",new JSONArray()
                .put(artist(11,"The Beatles Complete On Ukulele")));
        plays(exactPerformer,"play The Beatles' Hey Jude",20);
        Rig literal=new Rig();literal.tracks.put(track(20,"Stand by Me",10,"Ben E. King"));
        plays(literal,"play Stand by Me",20);
        for(String command:new String[]{"put some music on","please play some music","can you put music on","put on my flow","put music on Deezer",
                "could you play some music please","put some music on thank you","I'd like to hear some music","I want to listen to my flow"}) {
            Rig r=new Rig();int before=DeezerNativeController.plays;
            check("Done".equals(r.say(command)) && r.accepted && DeezerNativeController.plays==before+1
                    && r.catalogueCalls==0,"conversational Flow bypasses catalogue: "+command);
        }
        for(String command:new String[]{"put the fan on","can you put the heating on","please play a game",
                "can you tell me about Adele","play something","put on","could you put the fan on please","please play a game thank you",
                "I want to listen to","listen carefully","can you hear me","I want to talk to you"}) {
            Rig r=new Rig();int before=DeezerNativeController.plays;
            check("FALLTHROUGH".equals(r.say(command)) && !r.accepted && r.houseCalls==0
                    && DeezerNativeController.plays==before,"nonmusic retains its route: "+command);
        }
        for(String command:new String[]{"can you play missing on Deezer","put Missing on Deezer",
                "play something on Deezer","put something on Deezer","could you play missing on Deezer please","I'd like to hear missing on Deezer"}) {
            Rig r=new Rig();int before=DeezerNativeController.plays;
            check(!"FALLTHROUGH".equals(r.say(command)) && "LOCAL_REPLY".equals(r.status) && !r.accepted
                    && r.houseCalls==0 && DeezerNativeController.plays==before,"explicit provider failure is handled: "+command);
        }
        Rig missingQuery=new Rig();missingQuery.artists.put(artist(10,"Deezer"));
        check("FALLTHROUGH".equals(missingQuery.say("put on Deezer")) && missingQuery.catalogueCalls==0,
                "provider without a music query cannot become an artist request");
        for(String[] row:new String[][]{{"Britney's Toxic","Toxic","Britney Spears"},{"Mira's Northbound","Northbound","Mira Sol"}}) {
            Rig r=song(row[0],row[1],row[2]);int before=DeezerNativeController.plays;
            String reply=r.say("put "+row[0]+" on");
            check("LOCAL_QUESTION".equals(r.status) && reply.contains(row[2]) && !r.accepted && r.houseCalls==0
                    && DeezerNativeController.plays==before,"short artist name asks with the verified full identity: "+row[0]);
            r.client.prepareTurn("yes");
            plays(r,"yes",20);
        }
        Rig r=new Rig().metadata("search/track","Sam's Stay",new JSONArray()
                .put(track(20,"Stay",10,"Sam Smith")).put(track(21,"Stay",11,"Sam Ryder")))
                .metadata("search/artist","Sam Smith",new JSONArray().put(artist(10,"Sam Smith")))
                .metadata("search/artist","Sam Ryder",new JSONArray().put(artist(11,"Sam Ryder")));
        int before=DeezerNativeController.plays;
        r.say("put Sam's Stay on");
        check("LOCAL_QUESTION".equals(r.status) && r.houseCalls==0 && DeezerNativeController.plays==before,
                "shared artist prefix never chooses by catalogue order");
        check(!"Done".equals(r.say("yes")) && DeezerNativeController.plays==before,"yes cannot choose between different artists");
        r=song("Mira's Northbound","Northbound","Mira Sol");
        r.metadata("search/artist","Mira Sol",new JSONArray().put(artist(999,"Mira Sol")));
        before=DeezerNativeController.plays;
        check("FALLTHROUGH".equals(r.say("put Mira's Northbound on")) && r.houseCalls==0
                && DeezerNativeController.plays==before,"short-name suggestion also requires the actual performer ID");
        for(String suffix:new String[]{" (Live)"," (Karaoke Version)"," (Tribute)"," (Club Remix)"," (Re-Recorded)"}) {
            r=song("Mira Sol Northbound","Northbound"+suffix,"Mira Sol");before=DeezerNativeController.plays;
            check("FALLTHROUGH".equals(r.say("put Mira Sol Northbound on")) && r.houseCalls==0
                    && DeezerNativeController.plays==before,"ordinary request never substitutes "+suffix);
        }
        r=song("Northbound by Mira Sol","Northbound (Remastered 2020)","Mira Sol");
        r.catalogue.get("/search/track?q=Northbound by Mira Sol").put(track(21,"Northbound",10,"Mira Sol"));
        plays(r,"put Northbound by Mira Sol on",21);
        r=song("Northbound (Remastered 2020) by Mira Sol","Northbound (Remastered 2020)","Mira Sol");
        r.catalogue.get("/search/track?q=Northbound (Remastered 2020) by Mira Sol").put(track(21,"Northbound",10,"Mira Sol"));
        plays(r,"put Northbound (Remastered 2020) by Mira Sol on",20);
    }
    public static void main(String[] args)throws Exception {
        conversationalRequests();
        for(String shortcut:new String[]{"music","play some music"," MUSIC! ","Play   some music.","play some music on Deezer"}) {
            Rig flow=new Rig();int plays=DeezerNativeController.plays;
            check("Done".equals(flow.say(shortcut)) && DeezerNativeController.plays==plays+1
                    && flow.catalogueCalls==0,"Flow shortcut bypasses catalogue: "+shortcut);
            check(flow.accepted,"accepted Flow can finish the mic-button activity: "+shortcut);
        }
        DeezerNativeController.fail=true;
        Rig rejected=new Rig();
        check("Failed".equals(rejected.say("music")) && !rejected.accepted,"rejected playback retains its error reply");
        DeezerNativeController.fail=false;
        for(String query:new String[]{"John Lennon Imagine","Imagine John Lennon","John Lennon's Imagine","Imagine by John Lennon"}) {
            Rig r=new Rig().lennon(); String result=r.say("play "+query);
            check("Done".equals(result) && DeezerNativeController.played.endsWith("/track/2"),"natural Lennon request: "+query);
        }
        Rig r=new Rig().collision(); int before=DeezerNativeController.plays;
        String reply=r.say("play Queen");
        check("LOCAL_QUESTION".equals(r.status),"clarification opens the existing spoken follow-up window");
        check(!r.accepted,"music question must not dismiss the assistant");
        check(reply.toLowerCase(Locale.ROOT).contains("artist") && reply.toLowerCase(Locale.ROOT).contains("song"),"collision asks artist or song");
        check(r.houseCalls==0 && before==DeezerNativeController.plays,"ambiguity has no house/device effects");
        check("Done".equals(r.say("the artist")) && DeezerNativeController.played.endsWith("/artist/412"),"artist clarification executes exact choice");
        check(r.accepted,"accepted clarification can finish the assistant");
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
