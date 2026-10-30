package com.boop.shieldhome;
import org.junit.Test;
import static org.junit.Assert.*;
public class DeezerLyricsGateTest {
    @Test public void duplicatePressDoesNotQueueOrExtendTheRequest() {
        DeezerLyricsGate g=new DeezerLyricsGate();
        DeezerLyricsGate.Ticket t=g.begin("session:123",100L);
        assertNotNull(t); assertNull(g.begin("session:123",150L));
        assertTrue(g.consume(t,"session:123",200L));
        assertFalse(g.consume(t,"session:123",210L));
    }
    @Test public void cancellationAndTrackChangesDiscardOldResults() {
        DeezerLyricsGate g=new DeezerLyricsGate();
        DeezerLyricsGate.Ticket t=g.begin("session:123",100L);
        g.cancel(); assertFalse(g.consume(t,"session:123",200L));
        t=g.begin("session:123",300L);
        assertFalse(g.consume(t,"session:124",400L));
    }
    @Test public void newerRequestCannotBeCompletedByOlderWorker() {
        DeezerLyricsGate g=new DeezerLyricsGate();
        DeezerLyricsGate.Ticket old=g.begin("session:123",100L), next=g.begin("session:124",200L);
        assertFalse(g.consume(old,"session:123",300L));
        assertTrue(g.consume(next,"session:124",400L));
    }
    @Test public void deadlinePreventsLateScreenChanges() {
        DeezerLyricsGate g=new DeezerLyricsGate();
        DeezerLyricsGate.Ticket t=g.begin("session:123",100L);
        assertFalse(g.expire(t,2599L)); assertTrue(g.expire(t,2600L));
        assertFalse(g.consume(t,"session:123",2601L));
        t=g.begin("session:123",3000L);
        assertFalse(g.consume(t,"session:123",5500L));
    }
    @Test public void onlyFreshPositiveEvidenceMayOpenDeezer() {
        DeezerLyricsGate g=new DeezerLyricsGate();
        DeezerLyricsGate.Ticket t=g.begin("session:123",100L);
        assertEquals(DeezerLyricsGate.Action.NO_LYRICS,g.resolve(t,"session:123",200L,DeezerLyricsClient.Result.UNAVAILABLE));
        t=g.begin("session:123",300L);
        assertEquals(DeezerLyricsGate.Action.CANNOT_CHECK,g.resolve(t,"session:123",400L,DeezerLyricsClient.Result.UNKNOWN));
        t=g.begin("session:123",500L);
        assertEquals(DeezerLyricsGate.Action.OPEN,g.resolve(t,"session:123",600L,DeezerLyricsClient.Result.AVAILABLE));
        t=g.begin("session:123",700L); g.cancel();
        assertEquals(DeezerLyricsGate.Action.IGNORE,g.resolve(t,"session:123",800L,DeezerLyricsClient.Result.AVAILABLE));
    }
    @Test public void sameRecordingInAnotherSessionDoesNotMatch() {
        DeezerLyricsGate g=new DeezerLyricsGate();
        DeezerLyricsGate.Ticket t=g.begin("session-a:123",100L);
        assertEquals(DeezerLyricsGate.Action.IGNORE,g.resolve(t,"session-b:123",200L,DeezerLyricsClient.Result.AVAILABLE));
    }
}
