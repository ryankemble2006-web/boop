package com.boop.shieldoverlay;

public final class DiscoveredHomeAssistant {
    private final String name;
    private final String uuid;
    private final String baseUrl;

    public DiscoveredHomeAssistant(String name, String uuid, String baseUrl) {
        this.name = requireText(name, "name");
        this.uuid = uuid == null ? "" : uuid;
        this.baseUrl = requireText(baseUrl, "base URL");
    }

    public String name() {
        return name;
    }

    public String uuid() {
        return uuid;
    }

    public String baseUrl() {
        return baseUrl;
    }

    private static String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }
}
