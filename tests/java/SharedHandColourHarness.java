package com.boop.alpha1;

import java.util.List;

public final class SharedHandColourHarness {
    private static int checks;
    private static void check(boolean value, String message) {
        checks++;
        if (!value) throw new AssertionError(message);
    }
    private static void rejects(Runnable action, String message) {
        try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(SharedHandColourProtocol.decode(SharedEyeColourProtocol.encode(190)) == null, "Hand rejects iris payload");
        check(SharedEyeColourProtocol.decode(SharedHandColourProtocol.encode(0)) == null, "Iris rejects hand payload");
        check(!SharedHandColourProtocol.isOwnedHelper(SharedEyeColourProtocol.MARKER,0,32,"text",false), "Hand never claims iris helper");
        check(SharedHandColourProtocol.decode(SharedFeltColourProtocol.encode(120)) == null, "Hand rejects felt payload");
        check(SharedFeltColourProtocol.decode(SharedHandColourProtocol.encode(0)) == null, "Felt rejects hand payload");
        check(!SharedHandColourProtocol.isOwnedHelper(SharedFeltColourProtocol.MARKER,0,32,"text",false), "Hand rejects felt ownership");
        check(!SharedFeltColourProtocol.isOwnedHelper(SharedHandColourProtocol.MARKER,0,32,"text",false), "Felt rejects hand ownership");
        for (int hue = 0; hue < 360; hue++) {
            check(SharedHandColourProtocol.decode(SharedHandColourProtocol.encode(hue)) == hue,
                    "Every saved hue must round-trip exactly");
        }
        for (String bad : new String[]{"", "190", "BOOP_HAND_V1|360", "BOOP_HAND_V1|-1",
                "BOOP_HAND_V1|1.5", "BOOP_HAND_V1|NaN", "BOOP_HAND_V1|0190",
                "BOOP_HAND_V1|190\n", "BOOP_EYE_V2|190", "unknown", "unavailable", null}) {
            check(SharedHandColourProtocol.decode(bad) == null, "Reject invalid shared value");
        }
        rejects(() -> SharedHandColourProtocol.encode(360), "Never clamp remote errors to a colour");
        rejects(() -> SharedHandColourProtocol.encode(-1), "Never encode out-of-range hue");
        check(SharedHandColourProtocol.isOwnedHelper(SharedHandColourProtocol.MARKER, 0, 32, "text", false),
                "Recognize only the marked BOOP helper");
        check(!SharedHandColourProtocol.isOwnedHelper(".*", 0, 32, "text", false), "No name-only helper claim");
        check(!SharedHandColourProtocol.isOwnedHelper(SharedHandColourProtocol.MARKER, 0, 32, "text", true),
                "Initial values must not defeat HA restart restoration");
        check(!SharedHandColourProtocol.isOwnedHelper(SharedHandColourProtocol.MARKER, 0, 2, "text", false),
                "Reject wrong helper shape");
        check(SharedHandColourProtocol.chooseHelper(List.of()) == null, "Missing helper is not invented");
        check("owned".equals(SharedHandColourProtocol.chooseHelper(List.of("owned"))), "Use returned helper ID");
        rejects(() -> SharedHandColourProtocol.chooseHelper(List.of("a", "b")), "Ambiguous helper is not guessed");
        SharedEyeColourState a = new SharedEyeColourState(280);
        a.localChanged(281);
        check(a.localHue() == 281 && a.nextWrite() == null, "Local slider works while sharing is off");
        a.connected(190);
        check(a.localHue() == 190 && a.nextWrite() == null, "Read shared value before publishing");
        a.remoteChanged(100);
        check(a.localHue() == 100 && a.nextWrite() == null, "Remote update must not echo");
        a.localChanged(120);
        a.localChanged(140);
        check(a.nextWrite() == 140, "Coalesce slider changes");
        check(a.nextWrite() == null, "Only one write in flight");
        a.localChanged(160);
        a.remoteChanged(140);
        check(a.localHue() == 160, "Earlier acknowledgement cannot pull back active slider");
        a.writeConfirmed(140);
        check(a.nextWrite() == 160, "Latest local colour follows the in-flight write");
        a.remoteChanged(160);
        a.writeConfirmed(160);
        check(a.localHue() == 160 && a.nextWrite() == null, "Confirmed colour converges without echoes");
        a.localChanged(170);
        check(a.nextWrite() == 170, "New user edit may publish");
        a.disconnected();
        check(a.localHue() == 170 && a.nextWrite() == null, "Network failure preserves current local hue");
        a.localChanged(180);
        a.connected(220);
        check(a.localHue() == 220 && a.nextWrite() == null, "Reconnect must not push stale/offline cache");
        SharedEyeColourState b = new SharedEyeColourState(42);
        b.connected(220);
        a.localChanged(32);
        int accepted = a.nextWrite();
        b.remoteChanged(accepted);
        a.writeConfirmed(accepted);
        check(a.localHue() == b.localHue(), "Two devices converge on the HA-accepted value");
        b.localChanged(72);
        accepted = b.nextWrite();
        a.remoteChanged(accepted);
        b.writeConfirmed(accepted);
        check(a.localHue() == 72 && b.localHue() == 72, "Sharing is bidirectional");
        a.localChanged(72);
        check(a.nextWrite() == null, "Writing existing hue must not start an echo loop");
        rejects(() -> a.remoteChanged(400), "Bad remote value cannot mutate state");
        check(a.localHue() == 72, "Invalid remote data leaves the good hue intact");
        System.out.println("SharedHandColourHarness: " + checks + " checks passed");
    }
}
