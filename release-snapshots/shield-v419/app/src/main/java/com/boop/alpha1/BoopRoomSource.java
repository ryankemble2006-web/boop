package com.boop.alpha1;

interface BoopRoomSource {
    BoopRoom currentRoom();

    static BoopRoomSource fixed(String name) {
        return () -> new BoopRoom(BoopRoom.DEFAULT_ID, name);
    }
}
