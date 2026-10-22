package com.boop.alpha1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

final class BoopWakeKeywordBuilder {
    interface Tokenizer { List<String> encode(String text); }

    private static final String SCORE = ":1.5";
    private static final String THRESHOLD = "#0.25";
    private static final String LABEL = "@CUSTOM_WAKE";

    private BoopWakeKeywordBuilder() { }

    static String combinedKeywords(String baseKeywords, Tokenizer tokenizer, String selectedName) {
        String base = baseKeywords == null ? "" : baseKeywords.trim();
        String name = BoopWakeName.normalize(selectedName);
        if (BoopWakeName.isDefault(name) || tokenizer == null) return base;

        StringBuilder out = new StringBuilder(base);
        int added = 0;
        for (String phrase : naturalPhrases(name)) {
            List<String> tokens = tokenizer.encode(phrase);
            if (tokens == null || tokens.isEmpty()) continue;
            if (out.length() > 0) out.append('\n');
            out.append(String.join(" ", tokens))
                    .append(' ').append(SCORE)
                    .append(' ').append(THRESHOLD)
                    .append(' ').append(LABEL);
            added++;
        }
        return added == 0 ? base : out.toString();
    }

    static List<String> naturalPhrases(String selectedName) {
        String name = keywordText(selectedName);
        if (name == null) return Collections.emptyList();

        Set<String> phrases = new LinkedHashSet<>();
        phrases.add(name);
        phrases.add("HEY " + name);
        phrases.add("EY " + name);
        phrases.add("HI " + name);
        phrases.add("HELLO " + name);
        phrases.add("YO " + name);
        phrases.add("OI " + name);
        phrases.add("OK " + name);
        phrases.add("OKAY " + name);
        phrases.add("HEY THERE " + name);
        phrases.add("HELLO THERE " + name);
        phrases.add("HI THERE " + name);
        phrases.add("YO THERE " + name);
        phrases.add("WAKE UP " + name);
        phrases.add(name + " WAKE UP");
        phrases.add("COME ON " + name);
        phrases.add("YOU THERE " + name);
        phrases.add("ARE YOU THERE " + name);
        phrases.add(name + " YOU THERE");
        phrases.add(name + " ARE YOU THERE");
        phrases.add("HEY " + name + " WAKE UP");
        phrases.add("OK " + name + " WAKE UP");
        phrases.add("OKAY " + name + " WAKE UP");
        phrases.add("MORNING " + name);
        phrases.add("GOOD MORNING " + name);
        phrases.add("EVENING " + name);
        phrases.add("GOOD EVENING " + name);
        phrases.add(name + " HELLO");
        phrases.add(name + " HI");
        phrases.add("LISTEN " + name);
        phrases.add(name + " LISTEN");
        phrases.add("EXCUSE ME " + name);
        phrases.add("HEY " + name + " YOU THERE");
        return Collections.unmodifiableList(new ArrayList<>(phrases));
    }

    private static String keywordText(String raw) {
        String value = BoopWakeName.normalize(raw)
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return value.isEmpty() ? null : value;
    }
}
