package com.boop.shieldoverlay;

public final class RoutineItem {
    public enum Type {
        SCRIPT,
        SCENE
    }

    private final String entityId;
    private final String displayName;
    private final Type type;

    public RoutineItem(String entityId, String displayName, Type type) {
        this.entityId = requireText(entityId, "entity ID");
        this.displayName = requireText(displayName, "display name");
        if (type == null) {
            throw new IllegalArgumentException("routine type is required");
        }
        this.type = type;
    }

    public String entityId() {
        return entityId;
    }

    public String displayName() {
        return displayName;
    }

    public Type type() {
        return type;
    }

    public String domain() {
        return type == Type.SCRIPT ? "script" : "scene";
    }

    public String typeLabel() {
        return type == Type.SCRIPT ? "Script" : "Scene";
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
