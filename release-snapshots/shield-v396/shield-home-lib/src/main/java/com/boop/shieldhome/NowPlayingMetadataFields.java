package com.boop.shieldhome;

/** Resolves stable song and artist labels from media metadata and its display description. */
final class NowPlayingMetadataFields {
    private final String title;
    private final String artist;

    private NowPlayingMetadataFields(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    static NowPlayingMetadataFields resolve(String title, String displayTitle,
            String artist, String displaySubtitle, String albumArtist,
            String descriptionTitle, String descriptionSubtitle) {
        return new NowPlayingMetadataFields(
                firstNonEmpty(title, displayTitle, descriptionTitle),
                firstNonEmpty(artist, displaySubtitle, albumArtist, descriptionSubtitle));
    }

    String title() { return title; }
    String artist() { return artist; }

    private static String firstNonEmpty(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) return value.trim();
        }
        return "";
    }
}
