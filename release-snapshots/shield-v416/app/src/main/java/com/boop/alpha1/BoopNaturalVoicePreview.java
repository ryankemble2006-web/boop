package com.boop.alpha1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/** Neutral Kokoro PCM for the eight fixed settings demos; tuning happens at playback. */
final class BoopNaturalVoicePreview {
    static final int SAMPLE_RATE = 24000;
    private static final int MAX_BYTES = SAMPLE_RATE * 2 * 8;

    static String assetName(String text, int speakerId) {
        String name;
        switch (speakerId) {
            case 21: name = "Emma"; break;
            case 22: name = "Isabella"; break;
            case 26: name = "George"; break;
            case 25: name = "Fable"; break;
            default: return null;
        }
        String suffix;
        if (("Hello. I'm " + name + ".").equals(text)) suffix = "-name.pcm";
        else if ("This is how BOOP sounds.".equals(text)) suffix = "-test.pcm";
        else return null;
        return "boop-natural-voices/previews/" + speakerId + suffix;
    }

    static short[] read(InputStream input) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int count;
        while ((count = input.read(buffer)) != -1) {
            if (bytes.size() + count > MAX_BYTES) throw new IOException("Voice demo is too large");
            bytes.write(buffer, 0, count);
        }
        byte[] raw = bytes.toByteArray();
        if (raw.length == 0 || raw.length % 2 != 0) throw new IOException("Invalid voice demo PCM");
        short[] pcm = new short[raw.length / 2];
        for (int i = 0; i < pcm.length; i++) {
            pcm[i] = (short) ((raw[i * 2] & 255) | ((raw[i * 2 + 1] & 255) << 8));
        }
        return pcm;
    }
}
