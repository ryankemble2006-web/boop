package uk.local.casualty;
public final class ClickGateTest {
    private static void check(boolean result, String message) { if(!result) throw new AssertionError(message); }
    public static void main(String[] args) {
        ClickGate gate = new ClickGate();
        check(!gate.claimEpisode(10, true, true), "Never click without launch");
        gate.arm(100);
        check(gate.claimProfile(101, true, true), "Choose first focused existing profile");
        check(!gate.claimProfile(146, true, true), "Debounce duplicate accessibility event from same chooser");
        check(!gate.claimProfile(900, true, true), "Keep duplicate chooser blocked inside debounce window");
        check(gate.claimProfile(2300, true, true), "Allow genuine second chooser after cold-start handoff");
        check(!gate.claimProfile(4000, true, true), "Never choose more than two profiles per launch");
        check(!gate.claimEpisode(4001, false, true), "Never click episode outside Casualty");
        check(gate.claimEpisode(4002, true, true), "Click newest Casualty episode");
        check(!gate.claimEpisode(4003, true, true), "Only click episode once");
        check(gate.active(4003), "Keep a bounded guard alive after playback starts so return focus can be repaired");
        check(!gate.returnPageReady(4004, true), "Do not repair while launch page is still fading into playback");
        check(!gate.returnPageReady(4005, false), "Seeing playback leave the programme page only arms return detection");
        check(gate.returnPageReady(4006, true), "Repair only after Casualty page genuinely returns from playback");
        check(gate.claimTrailer(4010, true), "Click exact trailer control once during early playback");
        check(!gate.claimTrailer(4011, true), "Never click trailer twice");
        gate.arm(200000);
        check(gate.claimEpisode(200001, true, true), "Arm a fresh playback window");
        check(!gate.claimTrailer(260002, true), "Ignore trailer controls after bounded startup window");
        check(gate.recoveryActive(4003), "Return-focus guard is active after episode launch");
        check(!gate.claimProfile(4004, true, true), "Never revisit profile selection during playback recovery");
        gate.arm(5000);
        check(gate.claimEpisode(5001, true, true), "Allow direct episode when iPlayer skips chooser");
        gate.arm(6000);
        check(!gate.claimEpisode(126001, true, true), "Expire instead of clicking later");
        gate.arm(7000);
        gate.observePackage("other.app", "player.app", "launcher.app");
        check(!gate.claimEpisode(7001, true, true), "Cancel aborted launch on foreground change");
        System.out.println("PASS: debounced two-profile cold handoff, newest episode, once-only, expiry, cancellation");
    }
}
