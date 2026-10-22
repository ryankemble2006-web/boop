package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class NowPlayingMetadataFieldsTest {
    @Test public void usesDescriptionWhenDeezerLeavesTheStandardFieldsBlank() {
        NowPlayingMetadataFields fields = NowPlayingMetadataFields.resolve(
                "", "", "", "", "", "The Power of Love", "Jennifer Rush");

        assertEquals("The Power of Love", fields.title());
        assertEquals("Jennifer Rush", fields.artist());
    }

    @Test public void standardMediaFieldsTakePriorityOverDescriptionFallbacks() {
        NowPlayingMetadataFields fields = NowPlayingMetadataFields.resolve(
                "Head Over Feet", "", "Alanis Morissette", "", "",
                "Description title", "Description artist");

        assertEquals("Head Over Feet", fields.title());
        assertEquals("Alanis Morissette", fields.artist());
    }
}
