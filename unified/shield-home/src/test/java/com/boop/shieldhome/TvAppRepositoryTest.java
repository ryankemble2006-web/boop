package com.boop.shieldhome;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public final class TvAppRepositoryTest {
    @Test public void catalogueMergesDedupesExcludesBoopAndSorts() {
        List<TvAppEntry> out = TvAppRepository.merge(
                "com.boop.alpha1",
                List.of(
                        new TvAppEntry("pkg/.Tv", "pkg", "Zulu"),
                        new TvAppEntry("com.boop.alpha1/.Entry", "com.boop.alpha1", "BOOP")),
                List.of(
                        new TvAppEntry("pkg/.Tv", "pkg", "Zulu"),
                        new TvAppEntry("other/.Main", "other", "Alpha")));

        assertEquals(
                List.of("other/.Main", "pkg/.Tv"),
                out.stream().map(TvAppEntry::component).collect(Collectors.toList()));
    }

    @Test public void duplicateComponentKeepsLeanbackEntry() {
        List<TvAppEntry> out = TvAppRepository.merge(
                "com.boop.alpha1",
                List.of(new TvAppEntry("pkg/.Tv", "pkg", "TV label")),
                List.of(new TvAppEntry("pkg/.Tv", "pkg", "Phone label")));

        assertEquals(1, out.size());
        assertEquals("TV label", out.get(0).label());
    }
}
