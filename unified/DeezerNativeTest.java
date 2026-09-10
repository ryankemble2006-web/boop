package com.boop.alpha1;

import com.boop.shared.MediaRequest;
import org.junit.Test;
import static org.junit.Assert.*;
import org.json.*;
import java.util.*;
import java.io.IOException;

public class DeezerNativeTest {
    @Test public void sameButtonWrapperCanOwnFocus() throws Exception {
        com.boop.shared.DeezerScreen screen=com.boop.shared.DeezerScreen.parse("<hierarchy><node package='deezer.android.app' focused='true' bounds='[0,0][100,100]'><node clickable='true' enabled='true' focused='false' bounds='[0,0][100,100]' text='Play top tracks'/></node></hierarchy>");
        assertTrue(screen.target("Play top tracks").focused);
        screen=com.boop.shared.DeezerScreen.parse("<hierarchy><node package='deezer.android.app' focused='true' bounds='[0,0][200,200]'><node clickable='true' enabled='true' focused='false' bounds='[0,0][100,100]' text='Play top tracks'/></node></hierarchy>");
        assertFalse(screen.target("Play top tracks").focused);
    }
    static final BoopRoom ROOM=new BoopRoom("lounge","Lounge");
    static final String MAC="02:00:00:00:00:01";
    static final class Rig implements DeezerArtistClient.Http {
        List<String> commands=new ArrayList<>();
        String response="", identity=MAC, label="Flow", title="An infinite mix of favorites and new tracks";
        boolean stale, foreign, move, changedRoom, changeAfterPause, failPause, missingDump, scrollRequired, focusLost, focusAfterScroll, descriptionLabel;
        int scrolls;
        int screens;
        public String request(String url,String token,JSONObject body) throws Exception {
            assertEquals("test-token",token);
            if(body!=null) {
                String cmd=body.getString("command"); commands.add(cmd);
                String nonce=cmd.substring(5,cmd.indexOf(';'));
                String output;
                if(cmd.contains("ip link")) output=identity;
                else if(cmd.contains("am start")) output="Starting: Intent";
                else if(cmd.contains("uiautomator dump")) {
                    screens++;
                    output="<?xml version='1.0'?><hierarchy><node package='"+(foreign?"other.app":"deezer.android.app")+"' text='"+title+"'>"
                        +"<node clickable='true' enabled='true' focused='"+(!(focusLost&&screens>1)&&!(focusAfterScroll&&scrolls<2))+"' bounds='[0,0][100,"+(move&&screens>1?"200":"100")+"]' "+(descriptionLabel?"content-desc":"text")+"='"+(scrollRequired&&scrolls<2?"Other":label)+"'/></node></hierarchy>";
                    if(missingDump) output="ERROR: could not get idle state";
                } else {
                    if(cmd.contains("KEYCODE_DPAD_DOWN")) scrolls++;
                    if(cmd.contains("input keyevent 127")) {
                        if(failPause) throw new IOException("Pause failed");
                        if(changeAfterPause) changedRoom=true;
                    }
                    output="BOOP_ACTION_DONE";
                }
                response=(stale?"old":nonce)+"\n"+output;
                return "[]";
            }
            return new JSONObject().put("state","playing").put("attributes",new JSONObject().put("adb_response",response)).toString();
        }
        void play() throws Exception {
            new DeezerNativeController("http://ha.invalid","test-token","media_player.adb",this,ms->{},ROOM,
                ()->changedRoom?new BoopRoom("other","Other"):ROOM).play(new JSONArray().put(MAC),
                DeezerCatalogue.resolve(this,MediaRequest.parse("play music")));
        }
        void fails() throws Exception { try { play(); fail("Must refuse unsafe selection"); } catch(IOException expected) {} }
        long taps() { return commands.stream().filter(c->c.contains("KEYCODE_DPAD_CENTER")).count(); }
    }
    @Test public void flowSelectsOneVerifiedControl() throws Exception { Rig r=new Rig(); r.play(); assertEquals(1,r.taps()); }
    @Test public void wrongHardwareNeverLaunches() throws Exception { Rig r=new Rig(); r.identity="02:00:00:00:00:02"; r.fails(); assertEquals(1,r.commands.size()); }
    @Test public void staleResponseNeverLaunches() throws Exception { Rig r=new Rig(); r.stale=true; r.fails(); assertEquals(1,r.commands.size()); }
    @Test public void anotherAppNeverReceivesInput() throws Exception { Rig r=new Rig(); r.foreign=true; r.fails(); assertEquals(0,r.taps()); }
    @Test public void movingControlNeverReceivesTap() throws Exception { Rig r=new Rig(); r.move=true; r.fails(); assertEquals(0,r.taps()); }
    @Test public void changedRoomHasNoEffects() throws Exception { Rig r=new Rig(); r.changedRoom=true; r.fails(); assertTrue(r.commands.isEmpty()); }
    @Test public void roomChangedDuringPauseNeverSelects() throws Exception { Rig r=new Rig(); r.changeAfterPause=true; r.fails(); assertEquals(0,r.taps()); assertEquals(1,r.screens); }
    @Test public void failedPauseNeverSelects() throws Exception { Rig r=new Rig(); r.failPause=true; r.fails(); assertEquals(0,r.taps()); assertEquals(1,r.screens); }
    @Test public void failedDumpDoesNotReusePriorHierarchy() throws Exception { Rig r=new Rig(); r.missingDump=true; r.fails(); assertEquals(0,r.taps()); }
    @Test public void scrollingReadsAgainBeforeSelection() throws Exception { Rig r=new Rig(); r.scrollRequired=true; r.play(); assertEquals(2,r.scrolls); assertEquals(4,r.screens); assertEquals(1,r.taps()); }
    @Test public void matchingUnfocusedControlIsNotSelected() throws Exception { Rig r=new Rig(); r.focusAfterScroll=true; r.play(); assertEquals(2,r.scrolls); assertEquals(1,r.taps()); }
    @Test public void losingFocusDuringPauseNeverSelects() throws Exception { Rig r=new Rig(); r.focusLost=true; r.fails(); assertEquals(0,r.taps()); }
    @Test public void flowAccessibilityDescriptionSelects() throws Exception { Rig r=new Rig(); r.descriptionLabel=true; r.play(); assertEquals(1,r.taps()); }
    @Test public void exactTrackUsesVerifiedAlbumAndNoCredentials() throws Exception {
        DeezerCatalogue.Selection s=DeezerCatalogue.resolve((url,token,body)->{
            assertNull(token); assertNull(body);
            if(url.contains("search/artist")) return "{\"data\":[]}";
            if(url.contains("search/track")) return "{\"data\":[{\"id\":7,\"title\":\"Bohemian Rhapsody\",\"artist\":{\"name\":\"Queen\"},\"album\":{\"id\":9}}]}";
            return "{\"title\":\"A Night At The Opera\",\"tracks\":{\"data\":[{\"id\":7,\"title\":\"Bohemian Rhapsody\"}]}}";
        },MediaRequest.parse("play Bohemian Rhapsody by Queen"));
        assertNotNull(s); assertEquals("https://www.deezer.com/album/9",s.url); assertEquals("Bohemian Rhapsody by Queen",s.name);
    }
    @Test public void flowDoesNotNeedPublicCatalogue() throws Exception {
        assertTrue(DeezerCatalogue.resolve((u,t,b)->{throw new AssertionError("Flow is local");},MediaRequest.parse("play music")).flow);
    }
}
