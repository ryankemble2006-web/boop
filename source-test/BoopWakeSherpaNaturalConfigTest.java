package com.boop.alpha1;

import org.junit.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.Assert.assertTrue;

/** Regression for the physically observed BOOP + command no-pause boundary. */
public final class BoopWakeSherpaNaturalConfigTest {
    @Test public void defaultBoopDoesNotRequireTrailingSilenceBeforeTrigger() throws Exception {
        Path path = Path.of("../../source/BoopSherpaWakeSpotter.java");
        if (!Files.isRegularFile(path)) path = Path.of("source/BoopSherpaWakeSpotter.java");
        String source = Files.readString(path);
        assertTrue("BOOP Sherpa wake must finalize without a trailing blank for natural one-breath commands",
                source.contains("config.setNumTrailingBlanks(0);"));
    }
}
