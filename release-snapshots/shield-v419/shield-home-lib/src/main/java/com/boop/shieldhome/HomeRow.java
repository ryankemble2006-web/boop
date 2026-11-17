package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HomeRow {
    private final String title;
    private final List<HomeContentCard> cards;

    public HomeRow(String title, List<HomeContentCard> cards) {
        this.title = title == null ? "" : title;
        this.cards = cards == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(cards));
    }

    public String title() { return title; }
    public List<HomeContentCard> cards() { return cards; }
}
