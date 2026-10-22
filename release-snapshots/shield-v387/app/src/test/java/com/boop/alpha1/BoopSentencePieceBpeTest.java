package com.boop.alpha1;

import org.junit.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;

public final class BoopSentencePieceBpeTest {
    @Test public void mergesPiecesUsingModelScores() {
        Map<String, Float> scores = new HashMap<>();
        for (String s : new String[]{"▁", "S", "T", "E", "V", "▁S", "▁ST", "▁STE", "▁STEV"}) scores.put(s, 0f);
        scores.put("▁S", 1f);
        scores.put("▁ST", 2f);
        scores.put("▁STE", 3f);
        scores.put("▁STEV", 4f);
        BoopSentencePieceBpe bpe = new BoopSentencePieceBpe(scores);
        assertEquals(List.of("▁STEV", "E"), bpe.encode("Steve"));
    }
}
