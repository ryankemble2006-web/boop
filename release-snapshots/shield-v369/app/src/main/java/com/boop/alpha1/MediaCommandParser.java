package com.boop.alpha1;

import java.util.Locale;

final class MediaCommandParser {
    private MediaCommandParser() { }

    static MediaCommand parse(String text) {
        if (text == null) {
            return null;
        }
        String value = text.toLowerCase(Locale.ROOT)
                .trim()
                .replaceAll("[.!?]+$", "")
                .replaceAll("\\s+", " ");

        switch (value) {
            case "pause":
            case "pause music":
            case "pause the music":
            case "pause media":
            case "pause the media":
                return MediaCommand.PAUSE;
            case "resume":
            case "resume music":
            case "resume the music":
            case "continue":
            case "continue music":
            case "continue the music":
                return MediaCommand.RESUME;
            case "skip":
            case "skip track":
            case "skip the track":
            case "next":
            case "next track":
            case "next song":
                return MediaCommand.NEXT;
            case "previous":
            case "previous track":
            case "previous song":
            case "go back a track":
                return MediaCommand.PREVIOUS;
            case "volume up":
            case "turn volume up":
            case "turn the volume up":
            case "increase volume":
            case "increase the volume":
                return MediaCommand.VOLUME_UP;
            case "volume down":
            case "turn volume down":
            case "turn the volume down":
            case "decrease volume":
            case "decrease the volume":
                return MediaCommand.VOLUME_DOWN;
            default:
                return null;
        }
    }
}
