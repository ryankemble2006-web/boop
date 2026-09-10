package com.boop.alpha1;

public final class CloseGateCheck {
    static void check(boolean value) { if (!value) throw new AssertionError(); }
    public static void main(String[] args) {
        String nonce = "0123456789abcdef0123456789abcdef";
        LocalPlayerCloseGate gate = new LocalPlayerCloseGate(7, "deezer.android.app", nonce);
        check(gate.matches(7, "deezer.android.app"));
        check(!gate.matches(8, "deezer.android.app"));
        check(!gate.matches(7, "com.google.android.youtube.tv"));
        check(gate.closeCommand().contains("am force-stop deezer.android.app"));
        check(gate.closeCommand().indexOf("run-as") < gate.closeCommand().indexOf("am force-stop"));
        check(gate.closeCommand().contains("pidof deezer.android.app"));
        gate.cancel();
        check(!gate.matches(7, "deezer.android.app"));
        try { gate.closeCommand(); throw new AssertionError(); }
        catch (IllegalStateException expected) { }
        for (String invalid : new String[]{"com.google.android.apps.mediashell", "com.boop.alpha1", "a;reboot", "", null}) {
            try { new LocalPlayerCloseGate(7, invalid, nonce); throw new AssertionError(); }
            catch (IllegalArgumentException expected) { }
        }
        try { new LocalPlayerCloseGate(7, "deezer.android.app", "'; reboot"); throw new AssertionError(); }
        catch (IllegalArgumentException expected) { }
        LocalPlayerCloseGate youtube = new LocalPlayerCloseGate(9, "com.google.android.youtube.tv", nonce);
        check(youtube.closeCommand().contains("am force-stop com.google.android.youtube.tv"));
        System.out.println("Close gate checks passed: selection, cancellation, marker guard, package allowlist");
    }
}
