package com.boop.alpha1;

final class BoopRoom {
    static final String DEFAULT_ID = "living_room";
    static final String DEFAULT_NAME = "Living Room";

    private final String id;
    private final String name;

    BoopRoom(String id, String name) {
        this.name = clean(name, DEFAULT_NAME);
        this.id = clean(id, areaId(this.name));
    }

    String id() { return id; }
    String name() { return name; }

    private static String clean(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }

    private static String areaId(String name) {
        String value = name.toLowerCase(java.util.Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return value.isEmpty() ? DEFAULT_ID : value;
    }
}
