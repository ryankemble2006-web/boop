package com.boop.alpha1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/** Resolves an exact Home Assistant area or named light/group before room context is applied. */
final class NamedHomeTargetResolver {
    enum Kind { AREA, GROUP, NONE, AMBIGUOUS }

    static final class Result {
        private final Kind kind;
        private final List<String> entityIds;
        private final String serviceDomain;

        private Result(Kind kind, List<String> entityIds, String serviceDomain) {
            this.kind = kind;
            this.entityIds = Collections.unmodifiableList(new ArrayList<>(entityIds));
            this.serviceDomain = serviceDomain;
        }
        Kind kind() { return kind; }
        List<String> entityIds() { return entityIds; }
        String serviceDomain() { return serviceDomain; }
    }

    static Result resolve(String target, String areaId, Collection<String> areaEntityIds,
            Set<String> exposedIds, List<HomeAssistantEntity> entities) {
        if (areaId != null && !areaId.trim().isEmpty()) {
            Set<String> inArea = new HashSet<>(areaEntityIds);
            List<String> devices = new ArrayList<>();
            boolean hasLight = false;
            boolean hasSwitch = false;
            for (HomeAssistantEntity entity : entities) {
                if (isUsable(entity) && exposedIds.contains(entity.entityId())
                        && inArea.contains(entity.entityId())
                        && entity.entityId().startsWith("light.")) {
                    devices.add(entity.entityId());
                    hasLight = true;
                }
            }
            for (HomeAssistantEntity entity : entities) {
                if (isUsable(entity) && exposedIds.contains(entity.entityId())
                        && inArea.contains(entity.entityId())
                        && entity.entityId().startsWith("switch.")) {
                    String name = normalize(entity.name());
                    String id = entity.entityId().toLowerCase(Locale.ROOT);
                    boolean isLightSwitch = name.contains("light") || name.contains("lamp")
                            || id.contains("light") || id.contains("lamp");
                    if (!hasLight || isLightSwitch) {
                        devices.add(entity.entityId());
                        hasSwitch = true;
                    }
                }
            }
            Collections.sort(devices);
            String domain = (hasLight && hasSwitch) ? "homeassistant" : (hasSwitch ? "switch" : "light");
            return new Result(Kind.AREA, devices, domain);
        }

        String wanted = normalize(target);
        if (wanted.isEmpty()) return new Result(Kind.NONE, Collections.emptyList(), "");
        HomeAssistantEntity match = null;
        for (HomeAssistantEntity entity : entities) {
            if (!isUsable(entity) || !entity.exposed() || !isLightOrGroup(entity.entityId())
                    || !normalize(entity.name()).equals(wanted)) continue;
            if (match != null && !match.entityId().equals(entity.entityId())) {
                return new Result(Kind.AMBIGUOUS, Collections.emptyList(), "");
            }
            match = entity;
        }
        if (match == null) return new Result(Kind.NONE, Collections.emptyList(), "");
        String domain = match.entityId().substring(0, match.entityId().indexOf('.'));
        return new Result(Kind.GROUP, Collections.singletonList(match.entityId()),
                "group".equals(domain) ? "homeassistant" : domain);
    }

    private static boolean isUsable(HomeAssistantEntity entity) {
        return entity != null && !entity.hidden() && !entity.disabled()
                && !"unavailable".equalsIgnoreCase(entity.state())
                && !"unknown".equalsIgnoreCase(entity.state());
    }

    private static boolean isLightOrGroup(String entityId) {
        return entityId != null && (entityId.startsWith("light.") || entityId.startsWith("switch.") || entityId.startsWith("group."));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replaceAll("[^\\p{L}\\p{M}\\p{N}]+", " ").trim();
    }
}
