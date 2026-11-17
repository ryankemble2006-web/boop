package com.boop.alpha1;

final class BoopToastEgg {
    private static final String[] LINES = {
            "Would you like some toast?",
            "Excellent. You have summoned the toast department. We have no department. We do have bread.",
            "Toast status remains critically low. I have opened a case. Against breakfast.",
            "You keep asking for toast. This is how cults start, only crunchier.",
            "The bread has unionised. Their demands are butter, heat, and recognition.",
            "I have diverted all nonessential thoughts to toast. There were alarmingly few essential thoughts.",
            "I can hear the slices whispering from the cupboard. They know you called me.",
            "TOAST EMERGENCY. THIS IS NOT A DRILL. IT IS A GRILL-ADJACENT CRISIS."
    };

    static final int CYCLE_LENGTH = LINES.length;

    private int level;

    static boolean matches(String transcript) {
        return transcript != null && "toast".equalsIgnoreCase(transcript.trim());
    }

    Moment next() {
        int current = level;
        String line = LINES[current];
        level = (level + 1) % CYCLE_LENGTH;
        return new Moment(current, line);
    }

    static final class Moment {
        private final int level;
        private final String line;

        Moment(int level, String line) {
            this.level = level;
            this.line = line;
        }

        int level() {
            return level;
        }

        String line() {
            return line;
        }
    }
}
