package com.boop.alpha1;

enum MediaCommand {
    PAUSE("media_pause"),
    RESUME("media_play"),
    NEXT("media_next_track"),
    PREVIOUS("media_previous_track"),
    VOLUME_UP("volume_up"),
    VOLUME_DOWN("volume_down");

    private final String service;

    MediaCommand(String service) {
        this.service = service;
    }

    String service() {
        return service;
    }
}
