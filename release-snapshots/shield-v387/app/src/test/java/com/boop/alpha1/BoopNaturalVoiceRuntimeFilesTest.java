package com.boop.alpha1;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class BoopNaturalVoiceRuntimeFilesTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void officialKokoroRuntimeFilesPassPreflightWithoutInventedInnoFiles() throws Exception {
        File root = temporaryFolder.newFolder("kokoro-multi-lang-v1_0");
        writeFile(root, "model.onnx");
        writeFile(root, "voices.bin");
        writeFile(root, "tokens.txt");
        writeFile(root, "lexicon-gb-en.txt");
        File espeak = new File(root, "espeak-ng-data");
        assertTrue(espeak.mkdirs());
        writeFile(espeak, "phontab");

        assertTrue(BoopNaturalSpeechBackend.runtimeFilesReadyForSherpa(root));
    }

    @Test
    public void missingActualSherpaInputStillFailsPreflight() throws Exception {
        File root = temporaryFolder.newFolder("kokoro-incomplete");
        writeFile(root, "model.onnx");
        writeFile(root, "voices.bin");
        writeFile(root, "tokens.txt");
        File espeak = new File(root, "espeak-ng-data");
        assertTrue(espeak.mkdirs());
        writeFile(espeak, "phontab");

        assertFalse(BoopNaturalSpeechBackend.runtimeFilesReadyForSherpa(root));
    }

    private static void writeFile(File root, String relative) throws Exception {
        File target = new File(root, relative);
        File parent = target.getParentFile();
        if (parent != null && !parent.exists()) assertTrue(parent.mkdirs());
        try (FileOutputStream output = new FileOutputStream(target)) {
            output.write("x".getBytes(StandardCharsets.UTF_8));
        }
    }
}
