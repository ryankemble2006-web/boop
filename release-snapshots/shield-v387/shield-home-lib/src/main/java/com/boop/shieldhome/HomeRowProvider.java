package com.boop.shieldhome;

import java.util.List;

@FunctionalInterface
public interface HomeRowProvider {
    List<HomeRow> load();
}
