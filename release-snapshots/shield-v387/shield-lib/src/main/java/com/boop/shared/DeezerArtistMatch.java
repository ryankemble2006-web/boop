package com.boop.shared;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/** Resolve the displayed artist from matching recordings, never a name-only guess. */
public final class DeezerArtistMatch {
    private DeezerArtistMatch() { }
    public static final class Row {
        public final String title, artist, album;
        public final long artistId;
        public Row(String title, String artist, String album, long artistId) {
            this.title = title; this.artist = artist; this.album = album; this.artistId = artistId;
        }
    }
    public static long resolve(String title, String artist, String album, List<Row> rows) {
        String t = normal(title), a = normal(artist), b = normal(album);
        if (t.isEmpty() || a.isEmpty() || rows == null) return 0;
        long found = 0;
        for (Row row : rows) {
            if (row == null || row.artistId <= 0 || !t.equals(normal(row.title))
                    || !a.equals(normal(row.artist))
                    || (!b.isEmpty() && !b.equals(normal(row.album)))) continue;
            if (found != 0 && found != row.artistId) return 0;
            found = row.artistId;
        }
        return found;
    }
    private static String normal(String value) {
        return value == null ? "" : Normalizer.normalize(value, Normalizer.Form.NFKC)
                .trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
