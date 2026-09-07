package com.boop.alpha1;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public final class BoopWakeKeywordBuilderTest {
    @Test public void customNameGetsNaturalWakePhrases() {
        List<String> p = BoopWakeKeywordBuilder.naturalPhrases("Steve");
        for (String phrase : new String[]{"STEVE", "HEY STEVE", "HI STEVE", "HELLO STEVE",
                "GOOD MORNING STEVE", "WAKE UP STEVE", "STEVE WAKE UP", "COME ON STEVE",
                "YOU THERE STEVE", "ARE YOU THERE STEVE", "STEVE YOU THERE", "LISTEN STEVE",
                "EXCUSE ME STEVE"}) assertTrue(phrase, p.contains(phrase));
    }

    @Test public void customNameKeepsBoopFallbackKeywords() {
        String base = "▁BO O P :1.5 #0.25 @BOOP\n▁HE Y ▁BO O P :1.5 #0.25 @BOOP";
        String combined = BoopWakeKeywordBuilder.combinedKeywords(base,
                text -> Arrays.asList("▁" + text.replace(" ", "_")), "Steve");
        assertTrue(combined.startsWith(base));
        assertTrue(combined.contains("@BOOP"));
        assertTrue(combined.contains("@CUSTOM_WAKE"));
    }

    @Test public void boopSelectionDoesNotDuplicateBaseKeywords() {
        String base = "▁BO O P :1.5 #0.25 @BOOP";
        assertEquals(base, BoopWakeKeywordBuilder.combinedKeywords(base,
                text -> Arrays.asList("X"), "BOOP"));
    }
}
