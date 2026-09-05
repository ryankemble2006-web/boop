package com.boop.alpha1;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class BoopTimedRoutineFlow {
    enum Kind {
        NOT_TIMED,
        ASK_ONCE_OR_RECURRING,
        RUN_ONCE,
        RECURRING_REQUESTED
    }

    static final class Result {
        private final Kind kind;
        private final String haCommand;
        private final String actionText;
        private final int hour;
        private final int minute;

        private Result(Kind kind, String haCommand, String actionText, int hour, int minute) {
            this.kind = kind;
            this.haCommand = haCommand;
            this.actionText = actionText;
            this.hour = hour;
            this.minute = minute;
        }

        Kind kind() { return kind; }
        String haCommand() { return haCommand; }
        String actionText() { return actionText; }
        int hour() { return hour; }
        int minute() { return minute; }
    }

    private static final Pattern TIME_PATTERN = Pattern.compile(
            "(?i)\\bat\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(a\\.?m\\.?|p\\.?m\\.?)?\\b");

    private static final Set<String> ACTION_STARTERS = Set.of(
            "turn", "switch", "set", "lock", "unlock", "open", "close",
            "pause", "resume", "start", "stop", "run", "activate", "deactivate",
            "dim", "make");

    private Pending pending;

    Result process(String transcript, LocalDateTime now) {
        if (pending != null) {
            Choice choice = parseChoice(transcript);
            if (choice == Choice.ONCE) {
                Pending request = pending;
                pending = null;
                return new Result(
                        Kind.RUN_ONCE,
                        buildDelayedCommand(request, now),
                        request.actionText,
                        request.hour,
                        request.minute);
            }
            if (choice == Choice.RECURRING) {
                Pending request = pending;
                pending = null;
                return new Result(
                        Kind.RECURRING_REQUESTED,
                        null,
                        request.actionText,
                        request.hour,
                        request.minute);
            }
            return askResult();
        }

        Pending parsed = parseTimedRequest(transcript);
        if (parsed == null) {
            return new Result(Kind.NOT_TIMED, null, null, -1, -1);
        }
        pending = parsed;
        return askResult();
    }

    private Result askResult() {
        if (pending == null) {
            return new Result(Kind.NOT_TIMED, null, null, -1, -1);
        }
        return new Result(
                Kind.ASK_ONCE_OR_RECURRING,
                null,
                pending.actionText,
                pending.hour,
                pending.minute);
    }

    private static Pending parseTimedRequest(String transcript) {
        if (transcript == null) {
            return null;
        }
        String cleaned = transcript.trim();
        if (cleaned.isEmpty()) {
            return null;
        }

        Matcher matcher = TIME_PATTERN.matcher(cleaned);
        if (!matcher.find()) {
            return null;
        }

        Integer hour = parseHour(matcher.group(1), matcher.group(3));
        Integer minute = parseMinute(matcher.group(2));
        if (hour == null || minute == null) {
            return null;
        }

        String action = (cleaned.substring(0, matcher.start()) + " " + cleaned.substring(matcher.end()))
                .replaceAll("\\s+", " ")
                .trim();
        if (!looksLikeAction(action)) {
            return null;
        }

        return new Pending(action, hour, minute);
    }

    private static Integer parseHour(String rawHour, String rawMeridiem) {
        int hour;
        try {
            hour = Integer.parseInt(rawHour);
        } catch (NumberFormatException e) {
            return null;
        }

        if (rawMeridiem == null || rawMeridiem.isBlank()) {
            return hour >= 0 && hour <= 23 ? hour : null;
        }

        if (hour < 1 || hour > 12) {
            return null;
        }
        String meridiem = rawMeridiem.toLowerCase(Locale.ROOT).replace(".", "");
        if ("am".equals(meridiem)) {
            return hour == 12 ? 0 : hour;
        }
        if ("pm".equals(meridiem)) {
            return hour == 12 ? 12 : hour + 12;
        }
        return null;
    }

    private static Integer parseMinute(String rawMinute) {
        if (rawMinute == null || rawMinute.isBlank()) {
            return 0;
        }
        try {
            int minute = Integer.parseInt(rawMinute);
            return minute >= 0 && minute <= 59 ? minute : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static boolean looksLikeAction(String action) {
        if (action == null || action.isBlank()) {
            return false;
        }
        String normalized = action.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (normalized.isEmpty()) {
            return false;
        }
        int firstSpace = normalized.indexOf(' ');
        String firstWord = firstSpace < 0 ? normalized : normalized.substring(0, firstSpace);
        return ACTION_STARTERS.contains(firstWord);
    }

    private static Choice parseChoice(String transcript) {
        if (transcript == null) {
            return Choice.UNKNOWN;
        }
        String normalized = transcript.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.equals("once")
                || normalized.equals("just once")
                || normalized.equals("one time")
                || normalized.equals("only once")) {
            return Choice.ONCE;
        }
        if (normalized.equals("recurring")
                || normalized.equals("daily")
                || normalized.equals("every day")
                || normalized.equals("every night")
                || normalized.equals("repeat")) {
            return Choice.RECURRING;
        }
        return Choice.UNKNOWN;
    }

    private static String buildDelayedCommand(Pending request, LocalDateTime now) {
        LocalDateTime target = now
                .withHour(request.hour)
                .withMinute(request.minute)
                .withSecond(0)
                .withNano(0);
        if (!target.isAfter(now)) {
            target = target.plusDays(1);
        }

        long seconds = Math.max(1L, Duration.between(now, target).getSeconds());
        return request.actionText + " in " + describeDelay(seconds);
    }

    private static String describeDelay(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        StringBuilder delay = new StringBuilder();
        appendUnit(delay, hours, "hour");
        appendUnit(delay, minutes, "minute");
        appendUnit(delay, seconds, "second");
        return delay.toString();
    }

    private static void appendUnit(StringBuilder out, long amount, String unit) {
        if (amount <= 0) {
            return;
        }
        if (out.length() > 0) {
            out.append(" ");
        }
        out.append(amount).append(" ").append(unit);
        if (amount != 1) {
            out.append("s");
        }
    }

    private enum Choice {
        ONCE,
        RECURRING,
        UNKNOWN
    }

    private static final class Pending {
        private final String actionText;
        private final int hour;
        private final int minute;

        private Pending(String actionText, int hour, int minute) {
            this.actionText = actionText;
            this.hour = hour;
            this.minute = minute;
        }
    }
}
