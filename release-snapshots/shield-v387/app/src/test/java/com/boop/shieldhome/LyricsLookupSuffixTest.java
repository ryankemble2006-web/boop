package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public final class LyricsLookupSuffixTest {

    @Test
    public void stripsParentheticalSuffixes() {
        assertEquals("Jerusalema", LyricsLookupDialog.stripSuffix("Jerusalema (Acoustic Version)"));
        assertEquals("Billie Jean", LyricsLookupDialog.stripSuffix("Billie Jean (2008 Remaster)"));
        assertEquals("Comfortably Numb", LyricsLookupDialog.stripSuffix("Comfortably Numb (Live at Pompeii)"));
    }

    @Test
    public void stripsBracketedSuffixes() {
        assertEquals("Song", LyricsLookupDialog.stripSuffix("Song [Deluxe Edition]"));
        assertEquals("Track", LyricsLookupDialog.stripSuffix("Track [2011 Remaster]"));
    }

    @Test
    public void stripsDashedSuffixes() {
        assertEquals("Song", LyricsLookupDialog.stripSuffix("Song - Live from Wembley"));
        assertEquals("Track", LyricsLookupDialog.stripSuffix("Track - Acoustic Version"));
        assertEquals("Master", LyricsLookupDialog.stripSuffix("Master - Remastered 2020"));
        assertEquals("Hit", LyricsLookupDialog.stripSuffix("Hit - Radio Edit"));
    }

    @Test
    public void stripsFeaturingSuffixes() {
        assertEquals("Somebody That I Used to Know", LyricsLookupDialog.stripSuffix("Somebody That I Used to Know feat. Kimbra"));
        assertEquals("Starboy", LyricsLookupDialog.stripSuffix("Starboy ft. Daft Punk"));
        assertEquals("Empire State of Mind", LyricsLookupDialog.stripSuffix("Empire State of Mind featuring Alicia Keys"));
    }

    @Test
    public void stripsChainedOrNestedSuffixes() {
        assertEquals("Song", LyricsLookupDialog.stripSuffix("Song (Live) [Remastered]"));
        assertEquals("Track", LyricsLookupDialog.stripSuffix("Track - Acoustic [Bonus Track]"));
    }

    @Test
    public void preservesCleanTitles() {
        assertEquals("when the party's over", LyricsLookupDialog.stripSuffix("when the party's over"));
        assertEquals("Fast Car", LyricsLookupDialog.stripSuffix("Fast Car"));
    }

    @Test
    public void doesNotErasePureParenthesizedTitle() {
        // Guard against clearing empty strings if the entire title is inside parens or brackets
        assertEquals("(Hold On)", LyricsLookupDialog.stripSuffix("(Hold On)"));
        assertEquals("[Intro]", LyricsLookupDialog.stripSuffix("[Intro]"));
    }

    @Test
    public void handlesNullAndEmpty() {
        assertEquals("", LyricsLookupDialog.stripSuffix(null));
        assertEquals("", LyricsLookupDialog.stripSuffix(""));
        assertEquals("", LyricsLookupDialog.stripSuffix("   "));
    }
}
