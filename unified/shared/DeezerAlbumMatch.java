package com.boop.shared;
import java.util.List;
public final class DeezerAlbumMatch {
    public static final class Row {
        public final String title,artist,album; public final long albumId;
        public Row(String title,String artist,String album,long albumId) {
            this.title=title; this.artist=artist; this.album=album; this.albumId=albumId;
        }
    }
    public static long resolve(String title,String artist,String album,List<Row> rows) {
        String t=normal(title), a=normal(artist), b=normal(album);
        if(t.isEmpty() || a.isEmpty() || b.isEmpty() || rows==null) return 0;
        long found=0;
        for(Row row:rows) {
            if(row==null || row.albumId<=0 || !t.equals(normal(row.title))
                    || !a.equals(normal(row.artist)) || !b.equals(normal(row.album))) continue;
            if(found!=0 && found!=row.albumId) return 0;
            found=row.albumId;
        }
        return found;
    }
    private static String normal(String value) {
        return value==null ? "" : java.text.Normalizer.normalize(value,java.text.Normalizer.Form.NFKC)
            .trim().replaceAll("\\s+"," ").toLowerCase(java.util.Locale.ROOT);
    }
}
