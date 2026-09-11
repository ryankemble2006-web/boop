package uk.local.eastenders;
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
        check(!gate.claimEpisode(4001, false, true), "Never click episode outside EastEnders");
        check(gate.claimEpisode(4002, true, true), "Click newest EastEnders episode");
        check(!gate.claimEpisode(4003, true, true), "Only click episode once");
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
