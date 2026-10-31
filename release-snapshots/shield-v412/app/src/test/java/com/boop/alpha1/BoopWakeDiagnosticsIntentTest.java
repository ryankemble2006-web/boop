package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BoopWakeDiagnosticsIntentTest {
    @Test public void showDiagnosticsIsRecognizedWithoutHijackingOtherCommands() {
        assertTrue(BoopWakeDiagnosticsIntent.matches("show diagnostics"));
        assertTrue(BoopWakeDiagnosticsIntent.matches("Show diagnostics."));
        assertTrue(BoopWakeDiagnosticsIntent.matches("show diagnostic"));
        assertFalse(BoopWakeDiagnosticsIntent.matches("show settings"));
        assertFalse(BoopWakeDiagnosticsIntent.matches("diagnostics"));
        assertFalse(BoopWakeDiagnosticsIntent.matches(null));
    }
}
