package com.boop.alpha1;

import java.util.List;
import java.util.Locale;

final class HomeAssistantEntityResolver {
    enum Kind { MATCH, NONE, AMBIGUOUS }
    static final class Result {
        private final Kind kind;
        private final HomeAssistantEntity entity;
        Result(Kind kind, HomeAssistantEntity entity) { this.kind = kind; this.entity = entity; }
        Kind kind() { return kind; }
        HomeAssistantEntity entity() { return entity; }
    }

    static HomeAssistantEntity resolve(GenericHomeCommand command, List<HomeAssistantEntity> entities) {
        return resolveResult(command, entities).entity();
    }

    static Result resolveResult(GenericHomeCommand command, List<HomeAssistantEntity> entities) {
        HomeAssistantEntity best = null;
        int bestScore = 0;
        boolean tie = false;
        String wanted = normalize(command.target());
        for (HomeAssistantEntity entity : entities) {
            if (!entity.exposed() || entity.hidden() || entity.disabled()
                    || "unavailable".equalsIgnoreCase(entity.state())
                    || "unknown".equalsIgnoreCase(entity.state())) continue;
            String domain = entity.entityId().contains(".") ? entity.entityId().substring(0, entity.entityId().indexOf('.')) : "";
            if (!("light".equals(domain) || "fan".equals(domain) || "switch".equals(domain))) continue;
            String name = normalize(entity.name());
            int score = name.equals(wanted) ? 100 : (name.contains(wanted) || wanted.contains(name) ? 50 : 0);
            if (wanted.equals(domain) || wanted.equals(domain + "s")) score = 25;
            if (score > bestScore) { best = entity; bestScore = score; tie = false; }
            else if (score > 0 && score == bestScore) tie = true;
        }
        if (tie) return new Result(Kind.AMBIGUOUS, null);
        return best == null ? new Result(Kind.NONE, null) : new Result(Kind.MATCH, best);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", " ").trim();
    }
}
