package com.boop.alpha1;

/** Only general conversation changes provider; house control always stays local. */
enum BoopChatMode {
    OPENCODE("opencode"), FREE_CHAT("free_chat"), NATIVE_CHAT("native_chat");

    private final String stored;
    BoopChatMode(String stored) { this.stored = stored; }
    String storedValue() { return stored; }
    static BoopChatMode fromStored(String stored) {
        for (BoopChatMode mode : values()) {
            if (mode.stored.equals(stored)) return mode;
        }
        return OPENCODE;
    }
}
