package com.boop.alpha1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class BoopWakeDiagnosticTraceTest {
    @Test public void summarizesSuccessfulWakeRecognitionCallbacks() {
        BoopWakeDiagnosticTrace trace = new BoopWakeDiagnosticTrace(1_000L);
        trace.ready(1_100L);
        trace.begin(1_250L);
        trace.partial("change name to steve", 1_500L);
        trace.end(1_800L);
        trace.result("hey boop change name to steve", 1_900L);

        assertTrue(trace.terminal());
        assertEquals(
                "WAKE ASR RESULT +900ms ready=100 begin=250 end=800 partial=\"change name to steve\" final=\"hey boop change name to steve\"",
                trace.summary(1_900L));
    }

    @Test public void summarizesSilentAndroidRecognizerErrors() {
        BoopWakeDiagnosticTrace trace = new BoopWakeDiagnosticTrace(2_000L);
        trace.ready(2_080L);
        trace.error(7, 2_600L);

        assertTrue(trace.terminal());
        assertEquals(
                "WAKE ASR ERROR 7 +600ms ready=80 begin=- end=- partial=- final=-",
                trace.summary(2_600L));
    }

    @Test public void pendingTraceShowsLastCallbackWithoutPretendingItFinished() {
        BoopWakeDiagnosticTrace trace = new BoopWakeDiagnosticTrace(3_000L);
        trace.ready(3_100L);
        trace.begin(3_300L);
        trace.partial("change name", 3_700L);

        assertFalse(trace.terminal());
        assertEquals(
                "WAKE ASR PENDING +4500ms ready=100 begin=300 end=- partial=\"change name\" final=-",
                trace.summary(7_500L));
    }
}
