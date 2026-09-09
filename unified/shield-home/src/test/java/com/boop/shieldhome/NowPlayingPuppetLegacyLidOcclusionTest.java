package com.boop.shieldhome;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;

/** Non-visual regression contract for the duplicate legacy headphone eyelid seen on Shield. */
public final class NowPlayingPuppetLegacyLidOcclusionTest {
    @Test public void headphonesDrawableMasksOnlyTheLegacyUpperLids() throws Exception {
        Path project = findProjectRoot();
        assertNotNull("shield-clean-launcher project root must be discoverable", project);

        Path wrapper = project.resolve("app/src/main/res/drawable/boop_headphones.xml");
        Path legacy = project.resolve("app/src/main/res/drawable-nodpi/boop_headphones_legacy.png");
        Path oldDirect = project.resolve("app/src/main/res/drawable-nodpi/boop_headphones.png");

        assertTrue("boop_headphones must be a masking drawable", Files.isRegularFile(wrapper));
        assertTrue("legacy headphones raster must remain intact behind the mask", Files.isRegularFile(legacy));
        assertFalse("the unmasked direct headphones resource must not remain selectable", Files.exists(oldDirect));

        String xml = new String(Files.readAllBytes(wrapper), StandardCharsets.UTF_8);
        assertTrue(xml.contains("@drawable/boop_headphones_legacy"));
        assertTrue("left legacy lid mask must stay pinned", xml.contains("M440,480"));
        assertTrue("right legacy lid mask must stay pinned", xml.contains("M800,527.5"));
        assertTrue("mask must be opaque black, not a new eyelid colour", xml.contains("#FF000000"));
    }

    private static Path findProjectRoot() {
        Path start = Paths.get(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        for (Path cursor = start; cursor != null; cursor = cursor.getParent()) {
            if (Files.isRegularFile(cursor.resolve("app/build.gradle"))) return cursor;
            Path nested = cursor.resolve("shield-clean-launcher");
            if (Files.isRegularFile(nested.resolve("app/build.gradle"))) return nested;
        }
        return null;
    }
}
