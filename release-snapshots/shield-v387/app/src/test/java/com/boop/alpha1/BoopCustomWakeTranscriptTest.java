package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class BoopCustomWakeTranscriptTest {
    @Test public void customCallIsRemovedBeforeMirrorRouting() {
        String command = BoopWakeTranscriptNormalizer.stripLeadingWakeWord("Hey Steve, mirror", "Steve");
        assertEquals("mirror", command);
        assertEquals(BoopMirrorIntent.Action.OPEN, BoopMirrorIntent.actionFor(command));
    }

    @Test public void sameNaturalPhraseSetWorksWithAndWithoutACommand() {
        for (String name : new String[]{"Steve", "Jarvis", "Computer", "AI Dood", "Dave", "Clanker", "Sir Boopington"}) {
            for (String phrase : BoopWakeKeywordBuilder.naturalPhrases(name)) {
                assertEquals(phrase, "", BoopWakeTranscriptNormalizer.stripLeadingWakeWord(phrase, name));
                assertEquals(phrase, "Turn on the fan",
                        BoopWakeTranscriptNormalizer.stripLeadingWakeWord(phrase + ", Turn on the fan", name));
            }
        }
    }

    @Test public void longestCallIsRemovedBeforeBareName() {
        assertEquals("", BoopWakeTranscriptNormalizer.stripLeadingWakeWord("Steve wake up", "Steve"));
        assertEquals("mirror", BoopWakeTranscriptNormalizer.stripLeadingWakeWord("Steve are you there, mirror", "Steve"));
    }

    @Test public void verbalRenameCanFollowCustomCall() {
        String command = BoopWakeTranscriptNormalizer.stripLeadingWakeWord("Hey Steve, your new name is Jarvis", "Steve");
        BoopWakeNameIntent.Result result = BoopWakeNameIntent.parse(command);
        assertEquals(BoopWakeNameIntent.Action.SET, result.action());
        assertEquals("Jarvis", result.name());
    }

    @Test public void boopFallbackAndDefaultNormalizationStayUnchanged() {
        assertEquals("mirror", BoopWakeTranscriptNormalizer.stripLeadingWakeWord("BOOP, mirror", "Steve"));
        for (String text : new String[]{null, "", "  BOOP  ", "boop, pause music", "Hey BOOP mirror", "turn on the fan"}) {
            assertEquals(BoopWakeTranscriptNormalizer.stripLeadingWakeWord(text),
                    BoopWakeTranscriptNormalizer.stripLeadingWakeWord(text, "BOOP"));
        }
    }

    @Test public void aliasDoesNotRewriteDeviceNamesOrLongerWords() {
        for (String text : new String[]{"Hey Steven mirror", "Turn on Steve's lamp", "Steve's lamp is on"}) {
            assertEquals(text, BoopWakeTranscriptNormalizer.stripLeadingWakeWord(text, "Steve"));
        }
        assertEquals("Steve's lamp is on",
                BoopWakeTranscriptNormalizer.stripLeadingWakeWord("BOOP Steve's lamp is on", "Steve"));
    }

    @Test public void multiwordNamesSupportSpeechPunctuationAndCase() {
        assertEquals("mirror", BoopWakeTranscriptNormalizer.stripLeadingWakeWord("hey, ai dood: mirror", "AI Dood"));
        assertEquals("mirror", BoopWakeTranscriptNormalizer.stripLeadingWakeWord("Excuse me Sir Boopington! mirror", "Sir Boopington"));
    }
}
