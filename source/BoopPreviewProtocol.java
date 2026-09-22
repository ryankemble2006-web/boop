package com.boop.alpha1;

import java.util.List;

/** Only fixed synthetic previews cross the LAN. Never intents, shell commands or message bodies. */
public final class BoopPreviewProtocol {
    public static final String SERVICE_TYPE = "_boop-notify._tcp.";
    public static final List<String> SCENARIOS = List.of("FACEBOOK", "WHATSAPP", "GMAIL", "X",
            "YOUTUBE", "MESSENGER", "INSTAGRAM", "DISCORD", "SPOTIFY", "REDDIT", "LOCKED", "BUNDLE");
    private BoopPreviewProtocol() { }
    public static boolean validPin(String pin) { return pin != null && pin.matches("[0-9]{6}"); }
    public static boolean validCommand(String command) {
        return "PING".equals(command) || "STOP".equals(command) || SCENARIOS.contains(command);
    }
}
