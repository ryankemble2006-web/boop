package com.boop.alpha1;

final class GenericHomeCommand {
    private final String target;
    private final String service;
    private final boolean explicitOtherRoom;

    GenericHomeCommand(String target, String service, boolean explicitOtherRoom) {
        this.target = target;
        this.service = service;
        this.explicitOtherRoom = explicitOtherRoom;
    }

    String target() { return target; }
    String service() { return service; }
    boolean explicitOtherRoom() { return explicitOtherRoom; }
    boolean groupTarget() {
        String value = target.toLowerCase(java.util.Locale.ROOT).trim();
        return value.matches("(?:all\\s+|the\\s+)?(?:lights|fans|switches)");
    }
}
