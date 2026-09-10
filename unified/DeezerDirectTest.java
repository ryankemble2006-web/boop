package com.boop.alpha1;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
import java.io.IOException;
import java.util.*;
public class DeezerDirectTest {
    static final String MAC="02:00:00:00:00:01";
    static final BoopRoom ROOM=new BoopRoom("lounge","Lounge");
    static final class Rig implements DeezerArtistClient.Http {
        List<String> commands=new ArrayList<>();
        String response="",identity=MAC;
        boolean stale,fail,changedRoom,changeAfterIdentity,needsPrepare,prepared,changeAfterPrepare;
        public String request(String url,String token,JSONObject body)throws Exception {
            assertEquals("test-token",token);
            if(body==null)return new JSONObject().put("state","playing").put("attributes",new JSONObject().put("adb_response",response)).toString();
            assertEquals("media_player.adb",body.getString("entity_id"));
            String command=body.getString("command");commands.add(command);
            String nonce=command.substring(5,command.indexOf(';'));
            boolean identityRead=command.contains("ip link");
            String output=identityRead?identity:(fail?"BOOP_MEDIA_UNAVAILABLE":"BOOP_MEDIA_REQUESTED");
            if(!identityRead&&needsPrepare) {
                if(command.contains("DeezerMediaBridge prepare ")) {
                    prepared=true;output="BOOP_MEDIA_READY";
                    if(changeAfterPrepare)changedRoom=true;
                } else if(!prepared)output="BOOP_MEDIA_NEEDS_PREPARE";
            }
            response=(stale?"old":nonce)+"\n"+output;
            if(identityRead&&changeAfterIdentity)changedRoom=true;
            return "[]";
        }
        void play(String url)throws Exception {
            new DeezerNativeController("http://ha.invalid","test-token","media_player.adb",this,ms->{throw new AssertionError("No fixed startup delay");},ROOM,
                ()->changedRoom?new BoopRoom("other","Other"):ROOM).play(new JSONArray().put(MAC),
                new DeezerCatalogue.Selection("Test",url,"","",url.endsWith("/flow"),url.contains("/track/")));
        }
        void fails(String url)throws Exception {try{play(url);fail("Request should fail closed");}catch(IOException expected){}}
        long requests(){return commands.stream().filter(c->c.contains("app_process")).count();}
    }
    @Test public void artistTrackAndFlowUseDirectUrisWithoutNavigation()throws Exception {
        for(String url:Arrays.asList("https://www.deezer.com/artist/483","https://www.deezer.com/track/4091937401","https://www.deezer.com/flow")){
            Rig r=new Rig();r.play(url);assertEquals(2,r.commands.size());assertEquals(1,r.requests());
            String command=r.commands.get(1);assertTrue(command.contains(url));assertTrue(command.contains("sha256sum"));
            assertTrue(command.contains("trap"));assertTrue(command.contains("rm -f"));
            assertFalse(command.contains("force-stop"));assertFalse(command.contains("uiautomator"));assertFalse(command.contains("input keyevent"));
        }
    }
    @Test public void wrongHardwareNeverSendsPlayback()throws Exception {Rig r=new Rig();r.identity="02:00:00:00:00:02";r.fails("https://www.deezer.com/flow");assertEquals(1,r.commands.size());}
    @Test public void staleIdentityNeverSendsPlayback()throws Exception {Rig r=new Rig();r.stale=true;r.fails("https://www.deezer.com/flow");assertEquals(1,r.commands.size());}
    @Test public void roomChangeAfterIdentityNeverSendsPlayback()throws Exception {Rig r=new Rig();r.changeAfterIdentity=true;r.fails("https://www.deezer.com/flow");assertEquals(1,r.commands.size());}
    @Test public void invalidLinkNeverTouchesDevice()throws Exception {Rig r=new Rig();r.fails("https://evil.invalid/track/1;input keyevent 3");assertTrue(r.commands.isEmpty());}
    @Test public void failedNativeControlIsNotSuccess()throws Exception {Rig r=new Rig();r.fail=true;r.fails("https://www.deezer.com/flow");assertEquals(1,r.requests());}
    @Test public void coldSessionPreparesThenDispatches()throws Exception {
        Rig r=new Rig();r.needsPrepare=true;r.play("https://www.deezer.com/flow");
        assertEquals(4,r.commands.size());assertTrue(r.commands.get(2).contains("DeezerMediaBridge prepare "));
        assertTrue(r.commands.get(3).contains("DeezerMediaBridge play "));
    }
    @Test public void roomChangeDuringPreparationNeverDispatchesAfterward()throws Exception {
        Rig r=new Rig();r.needsPrepare=true;r.changeAfterPrepare=true;r.fails("https://www.deezer.com/flow");
        assertEquals(3,r.commands.size());assertTrue(r.prepared);
    }
}
