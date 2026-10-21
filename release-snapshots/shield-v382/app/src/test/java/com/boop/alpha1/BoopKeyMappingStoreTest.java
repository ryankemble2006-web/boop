package com.boop.alpha1;

import android.view.KeyEvent;
import com.boop.shieldhome.BoopKeyMappingStore;
import com.boop.shieldhome.BoopTaskKiller;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.List;

public final class BoopKeyMappingStoreTest {

    @Test
    public void testWatchedKeyCodesContainReferenceRigKeys() {
        List<Integer> watched = BoopKeyMappingStore.watchedKeyCodes();
        assertTrue(watched.contains(KeyEvent.KEYCODE_MEDIA_REWIND));
        assertTrue(watched.contains(BoopKeyMappingStore.KEYCODE_NETFLIX));
        assertTrue(watched.contains(KeyEvent.KEYCODE_HOME));
        assertTrue(watched.contains(KeyEvent.KEYCODE_MENU));
        assertTrue(watched.contains(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
        assertEquals(5, watched.size());
    }

    @Test
    public void testActionLabels() {
        assertEquals("Recents / Task Switcher", BoopKeyMappingStore.actionLabel(BoopKeyMappingStore.ACTION_RECENTS));
        assertEquals("Next Track", BoopKeyMappingStore.actionLabel(BoopKeyMappingStore.ACTION_NEXT_TRACK));
        assertEquals("Previous Track", BoopKeyMappingStore.actionLabel(BoopKeyMappingStore.ACTION_PREV_TRACK));
        assertEquals("Play / Pause", BoopKeyMappingStore.actionLabel(BoopKeyMappingStore.ACTION_PLAY_PAUSE));
        assertEquals("Deezer Flow", BoopKeyMappingStore.actionLabel(BoopKeyMappingStore.ACTION_DEEZER_FLOW));
        assertEquals("Sleep TV Timer Pro", BoopKeyMappingStore.actionLabel(BoopKeyMappingStore.ACTION_SLEEP_TIMER));
        assertEquals("Default", BoopKeyMappingStore.actionLabel(BoopKeyMappingStore.ACTION_DEFAULT));
    }

    @Test
    public void testAvailableActions() {
        List<String> actions = BoopKeyMappingStore.availableActions();
        assertTrue(actions.contains(BoopKeyMappingStore.ACTION_DEFAULT));
        assertTrue(actions.contains(BoopKeyMappingStore.ACTION_RECENTS));
        assertTrue(actions.contains(BoopKeyMappingStore.ACTION_NEXT_TRACK));
        assertTrue(actions.contains(BoopKeyMappingStore.ACTION_PREV_TRACK));
        assertTrue(actions.contains(BoopKeyMappingStore.ACTION_PLAY_PAUSE));
        assertTrue(actions.contains(BoopKeyMappingStore.ACTION_DEEZER_FLOW));
        assertTrue(actions.contains(BoopKeyMappingStore.ACTION_SLEEP_TIMER));
    }

    @Test
    public void testKeyEntryDoubleTap() {
        BoopKeyMappingStore.KeyEntry entry = new BoopKeyMappingStore.KeyEntry(
                KeyEvent.KEYCODE_HOME, "Home",
                BoopKeyMappingStore.ACTION_DEFAULT,
                BoopKeyMappingStore.ACTION_RECENTS,
                BoopKeyMappingStore.ACTION_DEEZER_FLOW,
                null);
        assertEquals(BoopKeyMappingStore.ACTION_DEFAULT, entry.singleTapAction);
        assertEquals(BoopKeyMappingStore.ACTION_RECENTS, entry.doubleTapAction);
        assertEquals(BoopKeyMappingStore.ACTION_DEEZER_FLOW, entry.longPressAction);
    }

    @Test
    public void testTaskKillerKnownPackageResolution() {
        assertEquals("net.kodinerds.maven.kodi21", BoopTaskKiller.resolvePackageName(null, "Kodinerds Omega"));
        assertEquals("org.xbmc.kodi", BoopTaskKiller.resolvePackageName(null, "Kodi"));
        assertEquals("com.forki.app", BoopTaskKiller.resolvePackageName(null, "forki"));
        assertEquals("com.fork2.app", BoopTaskKiller.resolvePackageName(null, "forked"));
        assertEquals("deezer.android.app", BoopTaskKiller.resolvePackageName(null, "Deezer"));
        assertEquals("com.neon.sleeptvtimerpro", BoopTaskKiller.resolvePackageName(null, "Sleep TV Timer Pro"));
        assertNull(BoopTaskKiller.resolvePackageName(null, "NonExistentAppXYZ"));
    }
}
