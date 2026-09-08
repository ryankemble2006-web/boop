package com.boop.alpha1;

import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.*;

public final class BoopWakeKeywordBuilderTest {
    @Test public void customNameGetsAllThirtyThreeNaturalWakePhrases() {
        List<String> p = BoopWakeKeywordBuilder.naturalPhrases("Steve");
        assertEquals(33, p.size());
        for (String phrase : new String[]{"STEVE", "HEY STEVE", "EY STEVE", "HI STEVE", "HELLO STEVE",
                "YO STEVE", "OI STEVE", "OK STEVE", "OKAY STEVE", "HEY THERE STEVE",
                "HELLO THERE STEVE", "HI THERE STEVE", "YO THERE STEVE", "GOOD MORNING STEVE",
                "WAKE UP STEVE", "STEVE WAKE UP", "COME ON STEVE", "YOU THERE STEVE",
                "ARE YOU THERE STEVE", "STEVE YOU THERE", "STEVE ARE YOU THERE",
                "HEY STEVE WAKE UP", "OK STEVE WAKE UP", "OKAY STEVE WAKE UP",
                "MORNING STEVE", "EVENING STEVE", "GOOD EVENING STEVE", "STEVE HELLO",
                "STEVE HI", "LISTEN STEVE", "STEVE LISTEN", "EXCUSE ME STEVE",
                "HEY STEVE YOU THERE"}) assertTrue(phrase, p.contains(phrase));
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
