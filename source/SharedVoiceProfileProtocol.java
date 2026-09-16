package com.boop.alpha1;

import java.util.List;
import java.util.Objects;

/** Bounded voice payload and ownership marker for the opt-in HA helper. */
final class SharedVoiceProfileProtocol {
    static final String PREFIX = "BOOP_VOICE_V1|";
    static final String NAME = "BOOP shared voice profile";
    static final String MARKER = "^BOOP_VOICE_V1\\|(android|natural)\\|(bf_emma|bf_isabella|bm_george|bm_fable)\\|[0-9]{3,4}\\|[0-9]{3,4}$";
    static final int MIN_PITCH_MILLI = 750;
    static final int MAX_PITCH_MILLI = 1450;
    static final int MIN_RATE_MILLI = 700;
    static final int MAX_RATE_MILLI = 1250;

    static final class Profile {
        final String backend;
        final String naturalVoiceKey;
        final int pitchMilli;
        final int rateMilli;

        Profile(String backend, String naturalVoiceKey, int pitchMilli, int rateMilli) {
            requireBackend(backend);
            requireVoice(naturalVoiceKey);
            requireRange(pitchMilli, MIN_PITCH_MILLI, MAX_PITCH_MILLI, "pitch");
            requireRange(rateMilli, MIN_RATE_MILLI, MAX_RATE_MILLI, "rate");
            this.backend = backend;
            this.naturalVoiceKey = naturalVoiceKey;
            this.pitchMilli = pitchMilli;
            this.rateMilli = rateMilli;
        }

        @Override public boolean equals(Object other) {
            if (this == other) return true;
            if (!(other instanceof Profile)) return false;
            Profile that = (Profile) other;
            return pitchMilli == that.pitchMilli && rateMilli == that.rateMilli
                    && backend.equals(that.backend) && naturalVoiceKey.equals(that.naturalVoiceKey);
        }

        @Override public int hashCode() {
            return Objects.hash(backend, naturalVoiceKey, pitchMilli, rateMilli);
        }
    }

    private SharedVoiceProfileProtocol() { }

    static String encode(Profile profile) {
        if (profile == null) throw new IllegalArgumentException("Missing voice profile");
        return PREFIX + profile.backend + "|" + profile.naturalVoiceKey + "|"
                + profile.pitchMilli + "|" + profile.rateMilli;
    }

    static Profile decode(String value) {
        if (value == null || value.length() > 64 || !value.startsWith(PREFIX)) return null;
        String[] parts = value.substring(PREFIX.length()).split("\\|", -1);
        if (parts.length != 4) return null;
        if (!parts[2].matches("[0-9]{3,4}") || !parts[3].matches("[0-9]{3,4}")) return null;
        try {
            return new Profile(parts[0], parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }

    static boolean isOwnedHelper(String pattern, int min, int max, String mode, boolean hasInitial) {
        return MARKER.equals(pattern) && min == 0 && max == 64
                && "text".equals(mode) && !hasInitial;
    }

    static String chooseHelper(List<String> ids) {
        if (ids == null || ids.isEmpty()) return null;
        if (ids.size() != 1) throw new IllegalArgumentException("More than one BOOP voice helper");
        String id = ids.get(0);
        if (id == null || !id.matches("[a-z0-9_]{1,128}")) {
            throw new IllegalArgumentException("Invalid voice helper identity");
        }
        return id;
    }

    private static void requireBackend(String backend) {
        if (!BoopVoiceController.BACKEND_ANDROID.equals(backend)
                && !BoopVoiceController.BACKEND_NATURAL.equals(backend)) {
            throw new IllegalArgumentException("Invalid voice backend");
        }
    }

    private static void requireVoice(String key) {
        if (BoopVoiceController.findNaturalVoice(key) == null) {
            throw new IllegalArgumentException("Invalid natural voice");
        }
    }

    private static void requireRange(int value, int min, int max, String label) {
        if (value < min || value > max) throw new IllegalArgumentException("Invalid voice " + label);
    }
}
