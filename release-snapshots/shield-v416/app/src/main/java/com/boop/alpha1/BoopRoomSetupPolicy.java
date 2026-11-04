package com.boop.alpha1;

final class BoopRoomSetupPolicy {
    static final String PENDING_ROOM_ID = "__pending__";
    static boolean needsAssignment(String deviceId, String assignedRoomId, BoopRoom room) {
        if (deviceId == null || deviceId.trim().isEmpty()) return true;
        if (assignedRoomId == null || assignedRoomId.trim().isEmpty()) {
            return !BoopRoom.DEFAULT_ID.equals(room.id());
        }
        return PENDING_ROOM_ID.equals(assignedRoomId) || !assignedRoomId.equals(room.id());
    }

    static boolean canPersist(BoopRoom captured, BoopRoom current) {
        return captured != null && current != null
                && captured.id().equals(current.id())
                && captured.name().equals(current.name());
    }
}
