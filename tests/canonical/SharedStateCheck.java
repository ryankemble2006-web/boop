import com.boop.shared.BoopState;
import com.boop.shared.MediaRequest;
import java.util.ArrayList;
import java.util.List;

public final class SharedStateCheck {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        BoopState state = new BoopState();
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
        BoopState nested = new BoopState();
        nested.subscribe(s -> { if(s.owner == BoopState.Owner.MEDIA_CORNER) nested.media("",false,0); });
        List<BoopState.Owner> nestedSeen = new ArrayList<>();
        nested.subscribe(s -> nestedSeen.add(s.owner)); nested.media("b",true,12);
        check(!nestedSeen.contains(BoopState.Owner.MEDIA_CORNER), "no stale nested delivery");
        check(MediaRequest.parse("play Britney on Deezer").query.equals("Britney"), "Deezer query");
        check(MediaRequest.parse("play on Deezer") == null, "no empty query");
        check(MediaRequest.parse("pause music").kind == MediaRequest.Kind.PAUSE, "pause preserved");
        check(MediaRequest.parse("turn the fan on") == null, "not a media command");
        System.out.println("Canonical shared state and media request checks passed");
    }
}
