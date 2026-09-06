package com.boop.shieldoverlay;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public final class DeezerPuppetSettingsModelTest {
    private final MediaPuppetState state = new MediaPuppetState();
    private final List<Boolean> changes = new ArrayList<>();
    private int launches;
    private boolean available;
    private final DeezerPuppetSettingsModel model = new DeezerPuppetSettingsModel(
            new DeezerPuppetSettingsModel.Actions() {
                public void setEnabled(boolean enabled) { changes.add(enabled); }
                public boolean openAccessSettings() { launches++; return available; }
            });

    @Test public void enablingRequiresConfirmationAndCancelLeavesOff() {
        assertTrue(model.toggle(state.snapshot()));
        assertTrue(changes.isEmpty());
        model.cancelEnable();
        model.confirmEnable();
        assertTrue(changes.isEmpty());
        assertTrue(model.toggle(state.snapshot()));
        model.confirmEnable();
        model.confirmEnable();
        assertEquals(java.util.Arrays.asList(true), changes);
        assertEquals(0, launches);
    }

    @Test public void offIsImmediateAndGrantStillVisible() {
        state.updateAccess(true, true, true);
        assertFalse(model.toggle(state.snapshot()));
        assertEquals(java.util.Arrays.asList(false), changes);
        state.updateAccess(false, true, false);
        assertTrue(model.explanation(state.snapshot()).contains("Granted"));
        state.updateAccess(false, false, false);
        assertTrue(model.explanation(state.snapshot()).contains("Not granted"));
    }

    @Test public void unavailableSettingsExplainsComputerSetupWithoutRetryOrEnabling() {
        model.manageAccess();
        assertTrue(model.explanation(state.snapshot()).contains("one-time computer setup"));
        assertTrue(model.explanation(state.snapshot()).contains("Ordinary eyes still work"));
        model.explanation(state.snapshot());
        assertEquals(1, launches);
        assertTrue(changes.isEmpty());
        available = true;
        model.manageAccess();
        assertFalse(model.explanation(state.snapshot()).contains("one-time computer setup"));
        assertEquals(2, launches);
    }
}
