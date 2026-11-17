package com.boop.alpha1;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class BoopNotificationAppCatalogModelTest {
    @Test
    public void mergesLaunchableAndObservedPackagesWithoutDuplicates() {
        List<BoopNotificationAppEntry> result = BoopNotificationAppCatalogModel.merge(
                List.of(new BoopNotificationAppEntry("com.chat", "Chat")),
                List.of(
                        new BoopNotificationAppEntry("com.chat", "Chat"),
                        new BoopNotificationAppEntry("com.sync", "Sync Service")));

        assertEquals(
                List.of("com.chat", "com.sync"),
                result.stream()
                        .map(BoopNotificationAppEntry::packageName)
                        .collect(Collectors.toList()));
    }

    @Test
    public void sortsByLabelThenPackageIgnoringCase() {
        List<BoopNotificationAppEntry> result = BoopNotificationAppCatalogModel.merge(
                List.of(
                        new BoopNotificationAppEntry("com.zeta", "beta"),
                        new BoopNotificationAppEntry("com.alpha.two", "Alpha")),
                List.of(new BoopNotificationAppEntry("com.alpha.one", "alpha")));

        assertEquals(
                List.of("com.alpha.one", "com.alpha.two", "com.zeta"),
                result.stream()
                        .map(BoopNotificationAppEntry::packageName)
                        .collect(Collectors.toList()));
    }
}
