package com.boop.shieldoverlay;

public final class EntityCard {
    private final String entityId;
    private final String areaId;
    private final String displayName;
    private final String state;
    private final boolean hidden;
    private final String entityCategory;

    public EntityCard(
            String entityId,
            String areaId,
            String displayName,
            String state,
            boolean hidden,
            String entityCategory) {
        this.entityId = requireText(entityId, "entity id");
        this.areaId = clean(areaId);
        this.displayName = requireText(displayName, "display name");
        this.state = requireText(state, "state").toLowerCase();
        this.hidden = hidden;
        this.entityCategory = clean(entityCategory);
    }

    public String entityId() {
        return entityId;
    }

    public String areaId() {
        return areaId;
    }

    public String displayName() {
        return displayName;
    }

    public String state() {
        return state;
    }

    public boolean hidden() {
        return hidden;
    }

    public String entityCategory() {
        return entityCategory;
    }

    public String domain() {
        int dot = entityId.indexOf('.');
        return dot <= 0 ? "" : entityId.substring(0, dot);
    }

    public EntityCard withState(String newState) {
        return new EntityCard(
                entityId,
                areaId,
                displayName,
                newState,
                hidden,
                entityCategory);
    }

    private static String requireText(String value, String label) {
        String clean = clean(value);
        if (clean == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        return clean;
    }

    private static String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
