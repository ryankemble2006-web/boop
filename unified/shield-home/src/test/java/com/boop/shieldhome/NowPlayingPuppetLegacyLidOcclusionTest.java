package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import org.junit.Test;

/** Non-visual regression contract for the duplicate legacy headphone eyelid seen on Shield. */
public final class NowPlayingPuppetLegacyLidOcclusionTest {
    private static final String CLEAN_HEADPHONES_SHA256 =
            "0bb4712dde392056edf177ef299e656aec488fdf409111cbef11a92cdcbc3670";

    @Test public void headphonesRasterHasLegacyUpperLidsRemoved() throws Exception {
        Path artwork = findHeadphonesArtwork();
        assertNotNull("boop_headphones.png must be present in the standalone launcher", artwork);
        assertEquals(CLEAN_HEADPHONES_SHA256, sha256(artwork));
    }

    private static Path findHeadphonesArtwork() {
        Path cwd = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path[] candidates = {
                cwd.resolve("app/src/main/res/drawable-nodpi/boop_headphones.png"),
                cwd.resolve("shield-clean-launcher/app/src/main/res/drawable-nodpi/boop_headphones.png"),
                cwd.getParent() == null ? cwd : cwd.getParent().resolve(
                        "shield-clean-launcher/app/src/main/res/drawable-nodpi/boop_headphones.png")
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) return candidate;
        }
        return null;
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[16 * 1024];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read > 0) digest.update(buffer, 0, read);
            }
        }
        StringBuilder out = new StringBuilder();
        for (byte b : digest.digest()) out.append(String.format("%02x", b & 0xff));
        return out.toString();
    }
}
