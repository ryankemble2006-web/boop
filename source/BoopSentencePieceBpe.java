package com.boop.alpha1;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class BoopSentencePieceBpe implements BoopWakeKeywordBuilder.Tokenizer {
    private static final String SPACE = "\u2581";
    private final Map<String, Float> scores;

    BoopSentencePieceBpe(byte[] modelBytes) {
        this.scores = Collections.unmodifiableMap(parseModel(modelBytes));
        if (scores.isEmpty()) throw new IllegalArgumentException("SentencePiece model has no usable pieces");
    }

    BoopSentencePieceBpe(Map<String, Float> scores) {
        this.scores = Collections.unmodifiableMap(new HashMap<>(scores));
    }

    @Override
    public List<String> encode(String raw) {
        String text = normalize(raw);
        if (text == null) return Collections.emptyList();
        String surface = SPACE + text.replace(" ", SPACE);
        List<String> pieces = new ArrayList<>();
        surface.codePoints().forEach(cp -> pieces.add(new String(Character.toChars(cp))));

        while (pieces.size() > 1) {
            int bestIndex = -1;
            float bestScore = -Float.MAX_VALUE;
            for (int i = 0; i + 1 < pieces.size(); i++) {
                String merged = pieces.get(i) + pieces.get(i + 1);
                Float score = scores.get(merged);
                if (score != null && score > bestScore) {
                    bestScore = score;
                    bestIndex = i;
                }
            }
            if (bestIndex < 0) break;
            pieces.set(bestIndex, pieces.get(bestIndex) + pieces.get(bestIndex + 1));
            pieces.remove(bestIndex + 1);
        }

        for (String piece : pieces) {
            if (!scores.containsKey(piece)) return Collections.emptyList();
        }
        return Collections.unmodifiableList(pieces);
    }

    private static String normalize(String raw) {
        if (raw == null) return null;
        String value = raw.toUpperCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{N}]+", " ")
                .trim()
                .replaceAll("\\s+", " ");
        return value.isEmpty() ? null : value;
    }

    private static Map<String, Float> parseModel(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return Collections.emptyMap();
        Map<String, Float> result = new HashMap<>();
        Cursor cursor = new Cursor(bytes, 0, bytes.length);
        while (cursor.pos < cursor.end) {
            long key = cursor.readVarint();
            int field = (int) (key >>> 3);
            int wire = (int) (key & 7);
            if (field == 1 && wire == 2) {
                int length = cursor.readLength();
                int end = cursor.checkedEnd(length);
                parsePiece(bytes, cursor.pos, end, result);
                cursor.pos = end;
            } else {
                cursor.skip(wire);
            }
        }
        return result;
    }

    private static void parsePiece(byte[] bytes, int start, int end, Map<String, Float> result) {
        Cursor cursor = new Cursor(bytes, start, end);
        String piece = null;
        float score = 0f;
        int type = 1;
        while (cursor.pos < cursor.end) {
            long key = cursor.readVarint();
            int field = (int) (key >>> 3);
            int wire = (int) (key & 7);
            if (field == 1 && wire == 2) {
                int length = cursor.readLength();
                int next = cursor.checkedEnd(length);
                piece = new String(bytes, cursor.pos, length, StandardCharsets.UTF_8);
                cursor.pos = next;
            } else if (field == 2 && wire == 5) {
                score = Float.intBitsToFloat(cursor.readFixed32());
            } else if (field == 3 && wire == 0) {
                type = (int) cursor.readVarint();
            } else {
                cursor.skip(wire);
            }
        }
        if (piece != null && !piece.isEmpty() && !piece.startsWith("<")
                && (type == 1 || type == 4 || type == 6)) {
            result.put(piece, score);
        }
    }

    private static final class Cursor {
        private final byte[] bytes;
        private final int end;
        private int pos;
        Cursor(byte[] bytes, int start, int end) { this.bytes = bytes; this.pos = start; this.end = end; }
        long readVarint() {
            long value = 0; int shift = 0;
            while (pos < end && shift < 64) {
                int b = bytes[pos++] & 0xff;
                value |= (long) (b & 0x7f) << shift;
                if ((b & 0x80) == 0) return value;
                shift += 7;
            }
            throw new IllegalArgumentException("Invalid SentencePiece protobuf");
        }
        int readLength() {
            long value = readVarint();
            if (value < 0 || value > Integer.MAX_VALUE) throw new IllegalArgumentException("Invalid SentencePiece length");
            return (int) value;
        }
        int checkedEnd(int length) {
            if (length < 0 || pos + length < pos || pos + length > end) throw new IllegalArgumentException("Truncated SentencePiece protobuf");
            return pos + length;
        }
        int readFixed32() {
            if (pos + 4 > end) throw new IllegalArgumentException("Truncated SentencePiece float");
            int value = (bytes[pos] & 0xff) | ((bytes[pos + 1] & 0xff) << 8)
                    | ((bytes[pos + 2] & 0xff) << 16) | ((bytes[pos + 3] & 0xff) << 24);
            pos += 4;
            return value;
        }
        void skip(int wire) {
            switch (wire) {
                case 0: readVarint(); return;
                case 1: pos = checkedEnd(8); return;
                case 2: pos = checkedEnd(readLength()); return;
                case 5: pos = checkedEnd(4); return;
                default: throw new IllegalArgumentException("Unsupported SentencePiece wire type: " + wire);
            }
        }
    }
}
