package com.boop.alpha1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class BoopNotificationAppCatalogModel {
    private BoopNotificationAppCatalogModel() { }

    static List<BoopNotificationAppEntry> merge(
            Collection<BoopNotificationAppEntry> launchable,
            Collection<BoopNotificationAppEntry> observed) {
        Map<String, BoopNotificationAppEntry> byPackage = new LinkedHashMap<>();
        addAll(byPackage, launchable, false);
        addAll(byPackage, observed, true);

        List<BoopNotificationAppEntry> result = new ArrayList<>(byPackage.values());
        Comparator<BoopNotificationAppEntry> comparator = Comparator
                .comparing((BoopNotificationAppEntry item) -> item.label().toLowerCase(Locale.ROOT))
                .thenComparing(item -> item.packageName().toLowerCase(Locale.ROOT))
                .thenComparing(BoopNotificationAppEntry::packageName);
        result.sort(comparator);
        return result;
    }

    private static void addAll(
            Map<String, BoopNotificationAppEntry> target,
            Collection<BoopNotificationAppEntry> entries,
            boolean onlyImproveFallbackLabel) {
        if (entries == null) return;
        for (BoopNotificationAppEntry entry : entries) {
            if (entry == null || entry.packageName().isEmpty()) continue;
            BoopNotificationAppEntry existing = target.get(entry.packageName());
            if (existing == null) {
                target.put(entry.packageName(), entry);
                continue;
            }
            if (onlyImproveFallbackLabel
                    && existing.label().equals(existing.packageName())
                    && !entry.label().equals(entry.packageName())) {
                target.put(entry.packageName(), entry);
            }
        }
    }
}
