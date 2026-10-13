package com.boop.alpha1;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Set;

/** Reference fixtures from SentencePiece 0.2.1 and the actual pinned wake model. */
public final class BoopWakeModelCompatibilityTest {
    @Test public void actualModelMatchesReferenceNameTokenization() throws Exception {
        byte[] model = readAsset("bpe.model");
        StringBuilder digest = new StringBuilder();
        for (byte value : MessageDigest.getInstance("SHA-256").digest(model)) {
            digest.append(String.format("%02x", value & 0xff));
        }
        assertEquals("c8a2a0129c4ab8e463164c142f82d25649661b122c8cd0b7aab5c9e80b90ad24", digest.toString());
        BoopSentencePieceBpe tokenizer = new BoopSentencePieceBpe(model);
        String[][] cases = {
            {"STEVE", "▁ST E VE"}, {"HEY STEVE", "▁HE Y ▁ST E VE"},
            {"GOOD MORNING STEVE", "▁GOOD ▁MO R N ING ▁ST E VE"}, {"EXCUSE ME STEVE", "▁EX C U SE ▁ME ▁ST E VE"},
            {"JARVIS", "▁JA R VI S"}, {"HEY JARVIS", "▁HE Y ▁JA R VI S"},
            {"GOOD MORNING JARVIS", "▁GOOD ▁MO R N ING ▁JA R VI S"}, {"EXCUSE ME JARVIS", "▁EX C U SE ▁ME ▁JA R VI S"},
            {"COMPUTER", "▁COMP U TER"}, {"HEY COMPUTER", "▁HE Y ▁COMP U TER"},
            {"GOOD MORNING COMPUTER", "▁GOOD ▁MO R N ING ▁COMP U TER"}, {"EXCUSE ME COMPUTER", "▁EX C U SE ▁ME ▁COMP U TER"},
            {"AI DOOD", "▁A I ▁DO O D"}, {"HEY AI DOOD", "▁HE Y ▁A I ▁DO O D"},
            {"GOOD MORNING AI DOOD", "▁GOOD ▁MO R N ING ▁A I ▁DO O D"}, {"EXCUSE ME AI DOOD", "▁EX C U SE ▁ME ▁A I ▁DO O D"},
            {"DAVE", "▁DA VE"}, {"HEY DAVE", "▁HE Y ▁DA VE"},
            {"GOOD MORNING DAVE", "▁GOOD ▁MO R N ING ▁DA VE"}, {"EXCUSE ME DAVE", "▁EX C U SE ▁ME ▁DA VE"},
            {"CLANKER", "▁C LA N K ER"}, {"HEY CLANKER", "▁HE Y ▁C LA N K ER"},
            {"GOOD MORNING CLANKER", "▁GOOD ▁MO R N ING ▁C LA N K ER"}, {"EXCUSE ME CLANKER", "▁EX C U SE ▁ME ▁C LA N K ER"},
            {"SIR BOOPINGTON", "▁S IR ▁BO O P ING T ON"}, {"HEY SIR BOOPINGTON", "▁HE Y ▁S IR ▁BO O P ING T ON"},
            {"GOOD MORNING SIR BOOPINGTON", "▁GOOD ▁MO R N ING ▁S IR ▁BO O P ING T ON"}, {"EXCUSE ME SIR BOOPINGTON", "▁EX C U SE ▁ME ▁S IR ▁BO O P ING T ON"},
            {"BOOP", "▁BO O P"}, {"HEY BOOP", "▁HE Y ▁BO O P"},
            {"GOOD MORNING BOOP", "▁GOOD ▁MO R N ING ▁BO O P"}, {"EXCUSE ME BOOP", "▁EX C U SE ▁ME ▁BO O P"}
        };
        for (String[] fixture : cases) assertEquals(fixture[0], fixture[1], String.join(" ", tokenizer.encode(fixture[0])));
    }

    @Test public void generatedCustomAndFallbackKeywordsUseRealModelVocabulary() throws Exception {
        String base = new String(readAsset("keywords.txt"), StandardCharsets.UTF_8).trim();
        BoopSentencePieceBpe tokenizer = new BoopSentencePieceBpe(readAsset("bpe.model"));
        Set<String> vocabulary = new HashSet<>();
        for (String line : new String(readAsset("tokens.txt"), StandardCharsets.UTF_8).split("\n")) {
            String entry = line.trim();
            if (!entry.isEmpty()) vocabulary.add(entry.split("\\s+")[0]);
        }
        String combined = BoopWakeKeywordBuilder.combinedKeywords(base, tokenizer, "Sir Boopington");
        assertTrue(combined.startsWith(base + "\n"));
        assertTrue(combined.contains("@BOOP"));
        assertTrue(combined.contains("@CUSTOM_WAKE"));
        for (String line : combined.split("\n")) for (String token : line.trim().split("\\s+")) {
            if (token.startsWith(":") || token.startsWith("#") || token.startsWith("@")) continue;
            assertTrue("Unknown wake-model token: " + token, vocabulary.contains(token));
        }
    }

    private static byte[] readAsset(String name) throws Exception {
        for (String prefix : new String[]{"src/main/assets/boop-kws/", "app/src/main/assets/boop-kws/", "boop-build/BOOP-Alpha1/app/src/main/assets/boop-kws/"}) {
            Path path = Path.of(prefix + name);
            if (Files.isRegularFile(path)) return Files.readAllBytes(path);
        }
        throw new AssertionError("Materialized wake asset is missing: " + name);
    }
}
