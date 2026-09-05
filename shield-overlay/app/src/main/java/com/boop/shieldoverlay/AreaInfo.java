package com.boop.shieldoverlay;

public final class AreaInfo {
    private final String id;
    private final String name;

    public AreaInfo(String id, String name) {
        this.id = requireText(id, "area id");
        this.name = requireText(name, "area name");
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value.trim();
    }
}
