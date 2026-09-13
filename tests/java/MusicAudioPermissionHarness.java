package com.boop.shieldhome;

public final class MusicAudioPermissionHarness {
    private static int checks;
    private static void expect(MusicAudioPermissionFlow.Action expected, MusicAudioPermissionFlow.Action actual) {
        checks++;
        if (expected != actual) throw new AssertionError("Expected " + expected + ", got " + actual);
    }
    public static void main(String[] args) {
        MusicAudioPermissionFlow granted = new MusicAudioPermissionFlow(false);
        expect(MusicAudioPermissionFlow.Action.READY, granted.begin(true));
        expect(MusicAudioPermissionFlow.Action.READY, granted.continueRequest(true));
        if (granted.isPending()) throw new AssertionError("Already granted must not request");

        MusicAudioPermissionFlow denied = new MusicAudioPermissionFlow(false);
        expect(MusicAudioPermissionFlow.Action.EXPLAIN, denied.begin(false));
        expect(MusicAudioPermissionFlow.Action.REQUEST, denied.continueRequest(false));
        expect(MusicAudioPermissionFlow.Action.WAIT, denied.continueRequest(false));
        expect(MusicAudioPermissionFlow.Action.WAIT, denied.begin(false));
        expect(MusicAudioPermissionFlow.Action.DENIED, denied.result(false));
        if (denied.isPending()) throw new AssertionError("Denial must clear pending state");
        // A denial cannot launch another request. Only a new explicit action can.
        expect(MusicAudioPermissionFlow.Action.EXPLAIN, denied.begin(false));
        expect(MusicAudioPermissionFlow.Action.REQUEST, denied.continueRequest(false));
        expect(MusicAudioPermissionFlow.Action.READY, denied.result(true));

        MusicAudioPermissionFlow restored = new MusicAudioPermissionFlow(true);
        expect(MusicAudioPermissionFlow.Action.WAIT, restored.begin(false));
        expect(MusicAudioPermissionFlow.Action.WAIT, restored.continueRequest(false));
        expect(MusicAudioPermissionFlow.Action.READY, restored.result(true));
        expect(MusicAudioPermissionFlow.Action.READY, restored.begin(true));

        MusicAudioPermissionFlow changedWhileExplaining = new MusicAudioPermissionFlow(false);
        expect(MusicAudioPermissionFlow.Action.EXPLAIN, changedWhileExplaining.begin(false));
        expect(MusicAudioPermissionFlow.Action.READY, changedWhileExplaining.continueRequest(true));
        if (changedWhileExplaining.isPending()) throw new AssertionError("Recheck must avoid stale request");

        MusicAudioPermissionFlow revoked = new MusicAudioPermissionFlow(false);
        expect(MusicAudioPermissionFlow.Action.READY, revoked.begin(true));
        expect(MusicAudioPermissionFlow.Action.EXPLAIN, revoked.begin(false));
        System.out.println("PASS: " + checks + " permission decisions; no Android device used");
    }
}
