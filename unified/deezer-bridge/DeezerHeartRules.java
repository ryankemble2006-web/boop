package com.boop.bridge;
/** Pure fail-closed policy shared by the source-built native UI bridge and its tests. */
public final class DeezerHeartRules {
    private DeezerHeartRules() { }
    public static boolean requestAllowed(String operation, int expected) {
        return expected >= -1 && expected <= 1 && ("read".equals(operation)
                || "toggle".equals(operation) || "dislike".equals(operation));
    }
    public static boolean sameTrack(String expected, String actual) {
        return expected != null && !expected.isEmpty() && expected.equals(actual);
    }
    public static boolean shouldToggle(int actual, int expected) {
        return (actual == 0 || actual == 1) && (expected == -1 || actual == expected);
    }
    /** Rectangles are de-duplicated and sorted by X by the Android adapter. */
    public static int heartIndex(int[][] row, int lyricsY, boolean dislike) {
        if (row == null || row.length < 5 || row.length > 7) return -1;
        for (int i = 0; i < row.length; i++) {
            int[] r = row[i];
            if (r == null || r.length != 4 || r[2]-r[0] < 40 || r[2]-r[0] > 70
                    || r[3]-r[1] < 40 || r[3]-r[1] > 70
                    || Math.abs((r[1]+r[3])/2-lyricsY) > 3
                    || (i > 0 && r[0] <= row[i-1][2])) return -1;
        }
        if (row[0][0] < 32 || row[1][2] > 256 || row[1][0]-row[0][2] > 24
                || row[2][0] < 360) return -1;
        return dislike ? 0 : 1;
    }
    /** The inspected finite-queue player has one heart, then shuffle/transport/repeat.
     * Its first middle control is NOT a favourite, and it has no dislike control.
     */
    public static int heartIndex(int[][] row, int lyricsY, boolean dislike, String contextType) {
        if (!"album_partner".equals(contextType) && !"playlist_partner".equals(contextType))
            return heartIndex(row, lyricsY, dislike);
        if (dislike || row == null || row.length != 6) return -1;
        int[] left = {64, 488, 552, 616, 680, 744};
        for (int i = 0; i < row.length; i++) {
            int[] r = row[i];
            if (r == null || r.length != 4 || Math.abs(r[0] - left[i]) > 2
                    || Math.abs(r[2] - left[i] - 48) > 2
                    || Math.abs(r[1] - lyricsY + 24) > 2
                    || Math.abs(r[3] - lyricsY - 24) > 2) return -1;
        }
        return 0;
    }
    private static boolean purple(int p) {
        int r=(p>>>16)&255,g=(p>>>8)&255,b=p&255;
        return b>190 && r>120 && r<235 && g<170 && b-g>55;
    }
    private static boolean white(int p) {
        return ((p>>>16)&255)>205 && ((p>>>8)&255)>205 && (p&255)>205;
    }
    /** Native Deezer 301000101 unfocused 41px glyph crop, not BOOP's theme colour. */
    public static int classify(int[] pixels, int width, int height) {
        if (pixels==null || width!=41 || height!=41 || pixels.length!=width*height) return -1;
        int centrePurple=0,centreWhite=0,outlineWhite=0,outsidePurple=0;
        for (int y=17;y<=23;y++) for(int x=17;x<=23;x++) {
            int p=pixels[y*width+x]; if(purple(p))centrePurple++; if(white(p))centreWhite++;
        }
        for(int y=7;y<=33;y++)for(int x=7;x<=33;x++)if(white(pixels[y*width+x]))outlineWhite++;
        for(int y=0;y<41;y++)for(int x=0;x<41;x++)
            if((x<5||x>35)&&(y<5||y>35)&&purple(pixels[y*width+x]))outsidePurple++;
        if(outsidePurple>4 || centreWhite>4) return -1;
        if(centrePurple>=40) return 1;
        return centrePurple<=2 && outlineWhite>=15 && outlineWhite<=280 ? 0 : -1;
    }
}