package com.boop.alpha1;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
import java.util.*;
import java.io.IOException;

public class DeezerArtistClientTest {
    private static final BoopRoom ROOM=new BoopRoom("lounge","Lounge");
    private static final String BASE="http://ha.invalid";
    private static final String TOKEN="test-token";
    static final class Rig implements DeezerArtistClient.Http {
        Rig() throws Exception { }
        JSONArray targets=new JSONArray().put(new JSONObject().put("media","media_player.screen").put("remote","remote.screen"));
        JSONArray artists=new JSONArray().put(new JSONObject().put("name","Britney Spears").put("id",483));
        List<String> effects=new ArrayList<>();
        Set<String> allowed=new HashSet<>(Arrays.asList("media_player.screen"));
        String active="deezer.android.app", state="on";
        String failure="";
        int catalogueCalls=0;
        BoopRoom room=ROOM;
        boolean changeRoomAfterOpen;
        DeezerNativeTest.Rig nativeRig;
        public String request(String url,String token,JSONObject body) throws Exception {
            if(url.startsWith("https://api.deezer.com/")) {
                assertNull("HA token must never reach catalogue",token); catalogueCalls++;
                return new JSONObject().put("data",artists).toString();
            }
            assertEquals(TOKEN,token);
            if(nativeRig!=null && (url.endsWith("/api/states/media_player.adb") || url.endsWith("/api/services/androidtv/adb_command"))) return nativeRig.request(url,token,body);
            if(url.endsWith("/api/template")) return targets.toString();
            if(url.endsWith("/api/states/media_player.screen"))
                return new JSONObject().put("state",state).put("attributes",new JSONObject().put("friendly_name","Screen")).toString();
            if(url.endsWith("/api/states/remote.screen"))
                return new JSONObject().put("state","on").put("attributes",new JSONObject().put("current_activity",active)).toString();
            if(!url.contains("/api/services/")) throw new AssertionError("Unexpected request "+url);
            String action=url.substring(url.indexOf("/api/services/")+14);
            effects.add(action);
            if(action.equals(failure)) throw new IOException("test service failure");
            if(action.equals("remote/turn_on")) {
                assertEquals("remote.screen",body.getString("entity_id"));
                assertEquals("https://www.deezer.com/artist/483",body.getString("activity"));
                if(changeRoomAfterOpen) room=new BoopRoom("elsewhere","Elsewhere");
            } else if(action.equals("media_player/media_pause")) {
                assertEquals("media_player.screen",body.getString("entity_id"));
            } else if(action.equals("remote/send_command")) {
                assertEquals("DPAD_CENTER",body.getString("command"));
                assertEquals("remote.screen",body.getString("entity_id"));
            }
            return "[]";
        }
        CommandOutcome run(String command) {
            try { return new DeezerArtistClient(this,(b,t)->allowed,ms->{}).process(BASE,TOKEN,command,ROOM,()->room); }
            catch(Exception e) { throw new AssertionError(e); }
        }
    }
    @Test public void verifiedNativeRouteReceivesArtist() throws Exception {
        Rig r=new Rig(); r.nativeRig=new DeezerNativeTest.Rig();
        r.nativeRig.title="Britney Spears"; r.nativeRig.label="Play top tracks";
        r.targets.getJSONObject(0).put("adb",new JSONArray().put("media_player.adb")).put("macs",new JSONArray().put(DeezerNativeTest.MAC));
        assertEquals("Requested Britney Spears on Screen.",LocalReply.forOutcome(r.run("play Britney Spears")));
        assertEquals(1,r.nativeRig.taps()); assertTrue(r.effects.isEmpty());
    }
    @Test public void bareMusicUsesNativeFlowWithoutCatalogue() throws Exception {
        Rig r=new Rig(); r.nativeRig=new DeezerNativeTest.Rig();
        r.targets.getJSONObject(0).put("adb",new JSONArray().put("media_player.adb")).put("macs",new JSONArray().put(DeezerNativeTest.MAC));
        assertEquals("Requested Deezer Flow on Screen.",LocalReply.forOutcome(r.run("play music")));
        assertEquals(0,r.catalogueCalls); assertEquals(1,r.nativeRig.taps());
    }
    @Test public void bareArtistRequiresVerifiedNativeControl() throws Exception {
        Rig r=new Rig(); CommandOutcome outcome=r.run("play Britney Spears");
        assertTrue(r.effects.isEmpty());
        assertEquals(CommandOutcome.Status.LOCAL_REPLY,outcome.status());
        assertTrue(LocalReply.forOutcome(outcome).contains("Android Debug Bridge"));
    }
    @Test public void explicitDeezerUsesSameRoute() throws Exception {
        Rig r=new Rig(); r.run("play Britney Spears on Deezer"); assertTrue(r.effects.isEmpty());
    }
    @Test public void unknownArtistHasNoDeviceEffects() throws Exception {
        Rig r=new Rig(); r.run("play an unknown artist"); assertTrue(r.effects.isEmpty());
    }
    @Test public void duplicateExactNamesAreAmbiguous() throws Exception {
        Rig r=new Rig(); r.artists.put(new JSONObject().put("name","Britney Spears").put("id",999));
        r.run("play Britney Spears"); assertTrue(r.effects.isEmpty());
    }
    @Test public void hiddenTvHasNoDeviceEffects() throws Exception {
        Rig r=new Rig(); r.allowed.clear(); r.run("play Britney Spears");
        assertTrue(r.effects.isEmpty());
    }
    @Test public void twoTvsDoNotGuessOrFallBackToSpeaker() throws Exception {
        Rig r=new Rig(); r.targets.put(new JSONObject().put("media","media_player.other").put("remote","remote.other"));
        r.allowed.add("media_player.other"); r.run("play Britney Spears");
        assertTrue(r.effects.isEmpty());
    }
    @Test public void unavailableTvHasNoEffects() throws Exception {
        Rig r=new Rig(); r.state="unavailable"; r.run("play Britney Spears"); assertTrue(r.effects.isEmpty());
    }
    @Test public void existingTransportAndHouseCommandsRemainUntouched() throws Exception {
        Rig r=new Rig(); assertNull(r.run("pause music")); assertNull(r.run("turn the fan on"));
        assertTrue(r.effects.isEmpty()); assertEquals(0,r.catalogueCalls);
    }
    @Test public void unmatchedBarePlayKeepsConversationRoutingEvenWithoutTv() throws Exception {
        Rig r=new Rig(); r.targets=new JSONArray();
        assertNull(r.run("play a game")); assertTrue(r.effects.isEmpty());
    }
    @Test public void explicitUnmatchedDeezerDoesNotFallThroughToConversation() throws Exception {
        Rig r=new Rig(); assertEquals(CommandOutcome.Status.LOCAL_REPLY,
                r.run("play an unknown artist on Deezer").status());
        assertTrue(r.effects.isEmpty());
    }
    @Test public void unavailableCatalogueKeepsBarePlayConversationRouting() throws Exception {
        DeezerArtistClient client=new DeezerArtistClient((u,t,b)->{throw new IOException();},(b,t)->Collections.emptySet(),ms->{});
        try { assertNull(client.process(BASE,TOKEN,"play a game",ROOM,()->ROOM)); }
        catch(Exception e) { throw new AssertionError(e); }
    }
}
