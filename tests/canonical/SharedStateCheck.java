import com.boop.shared.BoopState;
import com.boop.shared.MediaRequest;
import com.boop.shared.DeezerPlaybackSequence;
import com.boop.shared.DeezerScreen;
import java.util.ArrayList;
import java.util.List;

public final class SharedStateCheck {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        BoopState unknown = new BoopState();
        unknown.media("native-deezer",true,1);
        check(unknown.snapshot().owner == BoopState.Owner.NONE,"unknown/native playback must not create a corner");
        check(com.boop.shared.CastCornerPolicy.allowed("com.google.android.apps.mediashell","com.google.android.apps.mediashell","Deezer"),"Deezer Cast allowed");
        check(!com.boop.shared.CastCornerPolicy.allowed("deezer.android.app","com.google.android.apps.mediashell","Deezer"),"native UI always hides Cast corner");
        check(!com.boop.shared.CastCornerPolicy.allowed("com.google.android.apps.mediashell","deezer.android.app","Deezer"),"native playback is not Cast");
        check(!com.boop.shared.CastCornerPolicy.allowed("com.google.android.apps.mediashell","com.google.android.apps.mediashell","YouTube"),"other Cast apps excluded");
        check(!com.boop.shared.CastCornerPolicy.allowed(null,"com.google.android.apps.mediashell","Deezer"),"unknown foreground hides");
        check(com.boop.shared.CastCornerPolicy.foreground("com.google.android.apps.mediashell","android.app.Dialog").isEmpty(),"receiver dialogs cannot prove Cast screen");
        check(com.boop.shared.CastCornerPolicy.foreground("com.google.android.apps.mediashell","org.chromium.chromecast.shell.CastWebContentsActivity").equals("com.google.android.apps.mediashell"),"observed receiver activity confirms foreground");
        BoopState state = new BoopState();
        state.cornerAllowed(true);
        check(state.snapshot().owner == BoopState.Owner.NONE, "idle owns no media renderer");
        List<BoopState.Owner> owners = new ArrayList<>();
        Runnable unsubscribe = state.subscribe(s -> owners.add(s.owner));
        state.media("session-a", true, 10L);
        check(state.snapshot().owner == BoopState.Owner.MEDIA_CORNER, "playing away from home");
        state.homeVisible(true);
        check(owners.get(owners.size()-2) == BoopState.Owner.NONE, "release before acquire");
        check(state.snapshot().owner == BoopState.Owner.HOME_NOW_PLAYING, "home acquires");
        check(state.snapshot().mediaStartedMs == 10L, "handoff preserves phase epoch");
        state.speech(true, false);
        state.room("kitchen", "Kitchen");
        check(state.snapshot().listening && state.snapshot().roomId.equals("kitchen"), "shared context");
        state.homeVisible(false);
        check(state.snapshot().owner == BoopState.Owner.MEDIA_CORNER, "leave home");
        state.media("session-a", false, 20L);
        check(state.snapshot().owner == BoopState.Owner.NONE, "stop releases owner");
        int count = owners.size(); unsubscribe.run(); state.homeVisible(true);
        check(owners.size() == count, "unsubscribe");
        BoopState visibility = new BoopState();
        visibility.media("cast",true,30); visibility.cornerAllowed(true);
        check(visibility.snapshot().owner == BoopState.Owner.MEDIA_CORNER,"confirmed Cast acquires");
        visibility.cornerAllowed(false);
        check(visibility.snapshot().owner == BoopState.Owner.NONE,"native foreground immediately releases");
        visibility.homeVisible(true);
        check(visibility.snapshot().owner == BoopState.Owner.HOME_NOW_PLAYING,"Home remains independent of Cast gate");
        BoopState nested = new BoopState(); nested.cornerAllowed(true);
        nested.subscribe(s -> { if(s.owner == BoopState.Owner.MEDIA_CORNER) nested.media("",false,0); });
        List<BoopState.Owner> nestedSeen = new ArrayList<>();
        nested.subscribe(s -> nestedSeen.add(s.owner)); nested.media("b",true,12);
        check(!nestedSeen.contains(BoopState.Owner.MEDIA_CORNER), "no stale nested delivery");
        check(MediaRequest.parse("play Britney on Deezer").query.equals("Britney"), "Deezer query");
        check(MediaRequest.parse("play on Deezer") == null, "no empty query");
        check(MediaRequest.parse("pause music").kind == MediaRequest.Kind.PAUSE, "pause preserved");
        check(MediaRequest.parse("turn the fan on") == null, "not a media command");
        check(MediaRequest.parse("play Britney Spears").query.equals("Britney Spears"), "bare artist request");
        check(MediaRequest.parse("play music") != null && MediaRequest.parse("play music").kind.name().equals("DEEZER_FLOW"), "play music defaults to Deezer Flow");
        try {
            String card="<node package='deezer.android.app' enabled='true' clickable='true' bounds='[240,200][600,400]'><node package='deezer.android.app' text='Flow' /></node>";
            DeezerScreen screen=DeezerScreen.parse("<hierarchy>"+card+"</hierarchy>");
            check(screen.target("Flow").x==420 && screen.target("Flow").y==300, "semantic card bounds");
            check(screen.target("Queen")==null, "never select an unmatched label");
            check(DeezerScreen.parse("<hierarchy>"+card+card.replace("240,200","700,200").replace("600,400","900,400")+"</hierarchy>").target("Flow")==null, "ambiguous controls fail closed");
            check(!DeezerScreen.parse("<hierarchy><node package='com.other.app' text='Flow'/></hierarchy>").isDeezer(), "other app rejected");
            try { DeezerScreen.parse("<!DOCTYPE hierarchy [<!ENTITY x SYSTEM 'file:///never'>]><hierarchy/>"); throw new AssertionError("doctype accepted"); }
            catch(java.io.IOException expected) { }
        } catch(Exception e) { throw new AssertionError(e); }
        try {
            PlaybackProbe probe = new PlaybackProbe();
            check(DeezerPlaybackSequence.request(probe), "native artist playback requested");
            check(probe.events.equals("open,ready,pause,settle,select,"), "pause before select prevents repeat toggling");
            probe = new PlaybackProbe(); probe.active = false;
            check(!DeezerPlaybackSequence.request(probe) && !probe.events.contains("select"), "wrong app never gets select");
            probe = new PlaybackProbe(); probe.changeDuringPause = true;
            check(!DeezerPlaybackSequence.request(probe) && !probe.events.contains("select"), "room change cancels selection");
            probe = new PlaybackProbe(); probe.current = false;
            check(!DeezerPlaybackSequence.request(probe) && probe.events.isEmpty(), "stale request has no effects");
            probe = new PlaybackProbe(); probe.pauseFails = true;
            try { DeezerPlaybackSequence.request(probe); throw new AssertionError("pause failure ignored"); }
            catch (java.io.IOException expected) { check(!probe.events.contains("select"), "failed pause cannot toggle playback"); }
        } catch (Exception e) { throw new AssertionError(e); }
        System.out.println("Canonical shared state and media request checks passed");
    }
    private static final class PlaybackProbe implements DeezerPlaybackSequence.Gateway {
        String events=""; boolean active=true,current=true,changeDuringPause=false,pauseFails=false;
        public boolean current() { return current; }
        public boolean deezerActive() { return active; }
        public void openArtist() { events += "open,"; }
        public void awaitArtist() { events += "ready,"; }
        public void pause() throws Exception { events += "pause,"; if(pauseFails) throw new java.io.IOException(); if(changeDuringPause) current=false; }
        public void awaitPause() { events += "settle,"; }
        public void select() { events += "select,"; }
    }
}
