package com.boop.shieldhome;

/** Pixel geometry for a viewport containing complete posters, including its focus padding. */
final class SerenPosterLayout {
    final int posterWidth, visibleCount, viewportWidth;
    private final int stride;

    private SerenPosterLayout(int width, int count, int gap, int padding) {
        posterWidth = width;
        visibleCount = count;
        stride = width + gap;
        viewportWidth = count * stride - gap + padding * 2;
    }

    static SerenPosterLayout fit(int available, int width, int gap, int padding, int items) {
        int usable = Math.max(1, available - padding * 2);
        int poster = Math.min(width, usable);
        int count = Math.max(1, Math.min(Math.max(1, items), (usable + gap) / (poster + gap)));
        return new SerenPosterLayout(poster, count, gap, padding);
    }

    int snapOffset(int offset, int items) {
        return Math.max(0, Math.min(Math.max(0, items - visibleCount),
                Math.round((float) offset / stride))) * stride;
    }

    int offsetForFocus(int index, int offset, int items) {
        int first = snapOffset(offset, items) / stride;
        if (index < first) first = index;
        else if (index >= first + visibleCount) first = index - visibleCount + 1;
        return Math.max(0, Math.min(Math.max(0, items - visibleCount), first)) * stride;
    }
}
