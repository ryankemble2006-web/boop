package com.boop.alpha1;

final class HomeAssistantEntity {
    private final String entityId;
    private final String name;
    private final boolean exposed;
    private final boolean hidden;
    private final boolean disabled;
    private final String state;

    HomeAssistantEntity(String entityId, String name, boolean exposed,
            boolean hidden, boolean disabled, String state) {
        this.entityId = entityId;
        this.name = name;
        this.exposed = exposed;
        this.hidden = hidden;
        this.disabled = disabled;
        this.state = state;
    }
    String entityId() { return entityId; }
    String name() { return name; }
    boolean exposed() { return exposed; }
    boolean hidden() { return hidden; }
    boolean disabled() { return disabled; }
    String state() { return state; }
}
