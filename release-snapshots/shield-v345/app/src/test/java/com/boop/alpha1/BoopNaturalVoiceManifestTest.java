package com.boop.alpha1;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BoopNaturalVoiceManifestTest {
    private static final String JSON = "{"
            + "\"version\":\"kokoro-multi-lang-v1_0\","
            + "\"archiveRoot\":\"kokoro-multi-lang-v1_0\","
            + "\"url\":\"https://github.com/k2-fsa/sherpa-onnx/releases/download/tts-models/kokoro-multi-lang-v1_0.tar.bz2\","
            + "\"sha256\":\"c5f7e2d2caf082bc1d20fb70334a61d99d20b484500aad32e7cf84c128ea3298\","
            + "\"archiveSizeBytes\":349906910,"
            + "\"minimumFreeBytes\":900000000,"
            + "\"requiredFiles\":[\"model.onnx\",\"voices.bin\",\"tokens.txt\",\"espeak-ng-data\",\"lexicon-gb-en.txt\"],"
            + "\"voices\":["
            + "{\"name\":\"Emma\",\"key\":\"bf_emma\",\"sid\":21},"
            + "{\"name\":\"Isabella\",\"key\":\"bf_isabella\",\"sid\":22},"
            + "{\"name\":\"George\",\"key\":\"bm_george\",\"sid\":26},"
            + "{\"name\":\"Fable\",\"key\":\"bm_fable\",\"sid\":25}]}";

    @Test
    public void parsesPinnedPackAndVoiceOrder() {
        BoopNaturalVoiceManifest manifest = BoopNaturalVoiceManifest.parseJson(JSON);
        assertEquals("kokoro-multi-lang-v1_0", manifest.version());
        assertEquals(349906910L, manifest.archiveSizeBytes());
        assertEquals("Emma", manifest.voices().get(0).name());
        assertEquals("bf_emma", manifest.voices().get(0).key());
        assertEquals(21, manifest.voices().get(0).sid());
        assertEquals("Fable", manifest.voices().get(3).name());
        assertEquals(25, manifest.voices().get(3).sid());
    }

    @Test
    public void rejectsNonHttpsPackUrl() {
        String bad = JSON.replace("https://github.com", "http://github.com");
        assertThrows(IllegalArgumentException.class, () -> BoopNaturalVoiceManifest.parseJson(bad));
    }
}
