package com.boop.shieldoverlay;

public final class EntityState {
    private final String entityId;
    private final String state;
    private final String friendlyName;

    public EntityState(String entityId, String state, String friendlyName) {
        this.entityId = requireText(entityId, "entity id");
        this.state = requireText(state, "state").toLowerCase();
        this.friendlyName = clean(friendlyName);
    }

    public String entityId() {
        return entityId;
    }

    public String state() {
        return state;
    }

    public String friendlyName() {
        return friendlyName;
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
