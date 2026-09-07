package com.boop.alpha1;

import java.util.Locale;

final class BoopWakeNameIntent {
    enum Action { NONE, SET, RESET }

    static final class Result {
        private final Action action;
        private final String name;

        private Result(Action action, String name) {
            this.action = action;
            this.name = name;
        }

        static Result none() { return new Result(Action.NONE, null); }
        static Result set(String name) { return new Result(Action.SET, BoopWakeName.normalize(name)); }
        static Result reset() { return new Result(Action.RESET, BoopWakeName.DEFAULT); }
        Action action() { return action; }
        String name() { return name; }
    }

    private static final String[] SET_PREFIXES = {
            "your new name is ",
            "your name is ",
            "i'm calling you ",
            "im calling you ",
            "i am calling you ",
            "from now on you're ",
            "from now on youre ",
            "from now on you are "
    };

    private BoopWakeNameIntent() { }

    static Result parse(String transcript) {
        String raw = clean(transcript);
        if (raw == null) return Result.none();
        String canonical = raw.toLowerCase(Locale.ROOT);

        if (canonical.equals("reset your name")
                || canonical.equals("go back to boop")
                || canonical.equals("go back to boop again")
                || canonical.equals("reset name to boop")
                || canonical.equals("reset your name to boop")) {
            return Result.reset();
        }

        for (String prefix : SET_PREFIXES) {
            if (!canonical.startsWith(prefix)) continue;
            String candidate = raw.substring(prefix.length()).trim();
            String candidateCanonical = candidate.toLowerCase(Locale.ROOT);
            if (candidateCanonical.equals("boop again")
                    || candidateCanonical.equals("boop please")
                    || candidateCanonical.equals("boop again please")) {
                return Result.reset();
            }
            if (candidateCanonical.endsWith(" please")) {
                candidate = candidate.substring(0, candidate.length() - " please".length()).trim();
            }
            return candidate.isEmpty() ? Result.none() : Result.set(candidate);
        }
        return Result.none();
    }

    private static String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim()
                .replaceAll("[.!?,;:]+$", "")
                .replaceAll("\\s+", " ");
        return cleaned.isEmpty() ? null : cleaned;
    }
}
