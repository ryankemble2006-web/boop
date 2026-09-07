package com.boop.shieldoverlay;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public final class LaunchCrashRecorderTest {
    @Test
    public void recordsAndConsumesCrashReportOnce() {
        MemoryStore store = new MemoryStore();
        LaunchCrashRecorder recorder = new LaunchCrashRecorder(store);

        RuntimeException crash = new RuntimeException(
                "outer launch failure",
                new IllegalStateException("actual root cause"));
        recorder.record(crash);

        String report = recorder.consume();
        assertTrue(report.contains("RuntimeException: outer launch failure"));
        assertTrue(report.contains("Caused by: java.lang.IllegalStateException: actual root cause"));
        assertNull(recorder.consume());
    }

    @Test
    public void emptyRecorderHasNothingToShow() {
        LaunchCrashRecorder recorder = new LaunchCrashRecorder(new MemoryStore());
        assertNull(recorder.consume());
    }

    private static final class MemoryStore implements LaunchCrashRecorder.Store {
        private final Map<String, String> values = new HashMap<>();

        @Override
        public String getString(String key) {
            return values.get(key);
        }

        @Override
        public void putString(String key, String value) {
            values.put(key, value);
        }

        @Override
        public void remove(String key) {
            values.remove(key);
        }
    }
}
