package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/** Immutable, exact-recording timed data. Unknown data is never reported as absent. */
public final class DeezerLyricsDocument {
    public enum Status { AVAILABLE, UNAVAILABLE, UNKNOWN }
    private static final int MAX_BODY = 262144;
    private static final int MAX_LINES = 1024;
    private static final int MAX_TEXT = 2048;
    private static final long MAX_TIME = 24L * 60L * 60L * 1000L;

    public static final class Line {
        private final long start;
        private final long end;
        private final String text;
        private Line(long start, long end, String text) {
            this.start = start;
            this.end = end;
            this.text = text;
        }
        public long startMs() { return start; }
        public long endMs() { return end; }
        public String text() { return text; }
    }
    private final Status status;
    private final String trackId;
    private final String credit;
    private final List<Line> lines;
    private DeezerLyricsDocument(Status status, String id, String credit, List<Line> lines) {
        this.status = status;
        this.trackId = id == null ? "" : id;
        this.credit = credit;
        this.lines = Collections.unmodifiableList(new ArrayList<>(lines));
    }
    public Status status() { return status; }
    public String trackId() { return trackId; }
    public String credit() { return credit; }
    public List<Line> lines() { return lines; }
    public static DeezerLyricsDocument unknown(String id) {
        return new DeezerLyricsDocument(Status.UNKNOWN, id, "", Collections.emptyList());
    }
    private static DeezerLyricsDocument unavailable(String id) {
        return new DeezerLyricsDocument(Status.UNAVAILABLE, id, "", Collections.emptyList());
    }
    public static boolean validTrackId(String id) {
        return id != null && id.matches("[1-9][0-9]{0,18}");
    }
    public static DeezerLyricsDocument parse(String body, String expectedId) {
        if (!validTrackId(expectedId) || body == null || body.length() > MAX_BODY) return unknown(expectedId);
        try {
            JSONObject root = new JSONObject(body);
            if (root.has("errors") && !root.isNull("errors")) {
                JSONArray errors = root.optJSONArray("errors");
                if (errors == null || errors.length() != 0) return unknown(expectedId);
            }
            JSONObject data = root.optJSONObject("data");
            JSONObject track = data == null ? null : data.optJSONObject("track");
            if (track == null || !expectedId.equals(track.opt("id")) || !track.has("lyrics")) return unknown(expectedId);
            if (track.isNull("lyrics")) return unavailable(expectedId);
            JSONObject lyrics = track.optJSONObject("lyrics");
            if (lyrics == null || !lyrics.has("synchronizedLines") || !lyrics.has("synchronizedWordByWordLines"))
                return unknown(expectedId);
            List<Line> timed = readLines(lyrics);
            if (timed.isEmpty()) timed = readWordLines(lyrics);
            if (timed.isEmpty()) return unavailable(expectedId);
            timed.sort(Comparator.comparingLong(Line::startMs));
            List<Line> merged = new ArrayList<>();
            for (Line line : timed) {
                if (!merged.isEmpty() && merged.get(merged.size() - 1).start == line.start) {
                    Line previous = merged.remove(merged.size() - 1);
                    String text = previous.text.equals(line.text) || line.text.isEmpty() ? previous.text
                            : previous.text.isEmpty() ? line.text : previous.text + "\n" + line.text;
                    if (text.length() > MAX_TEXT) return unknown(expectedId);
                    merged.add(new Line(line.start, Math.max(previous.end, line.end), text));
                } else merged.add(line);
            }
            boolean hasText = false;
            int totalText = 0;
            for (int i = 0; i < merged.size(); i++) {
                Line line = merged.get(i);
                long end = i + 1 < merged.size() ? Math.min(line.end, merged.get(i + 1).start) : line.end;
                merged.set(i, new Line(line.start, end, line.text));
                hasText |= !line.text.isEmpty();
                totalText += line.text.length();
            }
            if (totalText > MAX_BODY / 2) return unknown(expectedId);
            if (!hasText) return unavailable(expectedId);
            String licence = optionalText(lyrics, "licence");
            String copyright = optionalText(lyrics, "copyright");
            String credit = licence.isEmpty() ? copyright : copyright.isEmpty() ? licence : licence + "\n" + copyright;
            return new DeezerLyricsDocument(Status.AVAILABLE, expectedId, credit, merged);
        } catch (Exception malformed) {
            return unknown(expectedId);
        }
    }
    private static JSONArray array(JSONObject object, String key) throws Exception {
        if (object.isNull(key)) return new JSONArray();
        Object raw = object.get(key);
        if (!(raw instanceof JSONArray)) throw new IllegalArgumentException("Invalid timed list");
        JSONArray list = (JSONArray) raw;
        if (list.length() > MAX_LINES) throw new IllegalArgumentException("Too many timed lines");
        return list;
    }
    private static List<Line> readLines(JSONObject lyrics) throws Exception {
        JSONArray list = array(lyrics, "synchronizedLines");
        List<Line> result = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject line = list.getJSONObject(i);
            // The schema allows nullable timing. Use the verified word format instead
            // of manufacturing a timestamp for an incomplete line-format response.
            if (line.isNull("milliseconds") || line.isNull("duration")) return Collections.emptyList();
            long start = milliseconds(line.get("milliseconds"));
            long duration = milliseconds(line.get("duration"));
            if (start + duration > MAX_TIME) throw new IllegalArgumentException("Invalid line end");
            result.add(new Line(start, start + duration, text(line.get("line"))));
        }
        return result;
    }
    private static List<Line> readWordLines(JSONObject lyrics) throws Exception {
        JSONArray list = array(lyrics, "synchronizedWordByWordLines");
        List<Line> result = new ArrayList<>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject line = list.getJSONObject(i);
            long start = milliseconds(line.get("start"));
            long end = milliseconds(line.get("end"));
            if (end < start) throw new IllegalArgumentException("Invalid word-line end");
            JSONArray words = line.getJSONArray("words");
            if (words.length() > 512) throw new IllegalArgumentException("Too many words");
            StringBuilder sentence = new StringBuilder();
            for (int j = 0; j < words.length(); j++) {
                String word = text(words.getJSONObject(j).get("word"));
                if (word.isEmpty()) continue;
                if (sentence.length() > 0) sentence.append(' ');
                sentence.append(word);
                if (sentence.length() > MAX_TEXT) throw new IllegalArgumentException("Word line too long");
            }
            result.add(new Line(start, end, sentence.toString()));
        }
        return result;
    }
    private static long milliseconds(Object value) {
        if (!(value instanceof Number)) throw new IllegalArgumentException("Timing is not numeric");
        double number = ((Number) value).doubleValue();
        if (Double.isNaN(number) || Double.isInfinite(number) || number < 0 || number > MAX_TIME
                || number != Math.rint(number)) throw new IllegalArgumentException("Invalid milliseconds");
        return ((Number) value).longValue();
    }
    private static String text(Object value) {
        if (!(value instanceof String) || ((String) value).length() > MAX_TEXT)
            throw new IllegalArgumentException("Invalid lyric text");
        return ((String) value).replace('\r', '\n').trim();
    }
    private static String optionalText(JSONObject object, String key) {
        Object value = object.opt(key);
        return value instanceof String && ((String) value).length() <= 4096 ? ((String) value).trim() : "";
    }
    private int preceding(long positionMs) {
        int low = 0, high = lines.size() - 1, found = -1;
        while (low <= high) {
            int middle = (low + high) >>> 1;
            if (lines.get(middle).start <= positionMs) { found = middle; low = middle + 1; }
            else high = middle - 1;
        }
        return found;
    }
    public int activeIndex(long positionMs) {
        if (positionMs < 0) return -1;
        int index = preceding(positionMs);
        if (index < 0) return -1;
        Line line = lines.get(index);
        return !line.text.isEmpty() && positionMs < line.end ? index : -1;
    }
    public int anchorIndex(long positionMs) {
        return lines.isEmpty() ? -1 : Math.max(0, preceding(positionMs));
    }
}
