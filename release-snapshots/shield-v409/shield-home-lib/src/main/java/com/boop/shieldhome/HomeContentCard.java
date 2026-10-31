package com.boop.shieldhome;

public final class HomeContentCard {
    private final String title;
    private final String intentUri;
    private final String posterArtUri;

    public HomeContentCard(String title, String intentUri, String posterArtUri) {
        this.title = title == null ? "" : title;
        this.intentUri = intentUri == null ? "" : intentUri;
        this.posterArtUri = posterArtUri == null ? "" : posterArtUri;
    }

    public String title() { return title; }
    public String intentUri() { return intentUri; }
    public String posterArtUri() { return posterArtUri; }
}
