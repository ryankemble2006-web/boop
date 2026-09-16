package uk.local.casualty;
public final class ReturnHomeTest {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        ClickGate gate = new ClickGate();
        check(!gate.claimReturnHome(100, true, true), "Unarmed browsing must not go Home");
        gate.arm(1000);
        check(!gate.claimReturnHome(1001, true, true), "Launch page must remain open");
        check(gate.claimEpisode(1002, true, true), "Start own episode");
        check(!gate.claimReturnHome(1003, true, true), "Do not exit the initial programme page");
        check(!gate.claimReturnHome(1004, false, false), "Player arms return only");
        check(!gate.claimReturnHome(1005, false, false), "Playing or paused player stays open");
        check(!gate.claimReturnHome(1006, true, false), "Player title without programme cards is not a return");
        check(gate.claimTrailer(1007, true), "Exact trailer skip remains available");
        check(!gate.claimTrailer(1008, true), "Skip once only");
        check(gate.claimReturnHome(1009, true, true), "Programme page after stop returns Home");
        check(!gate.claimReturnHome(1010, true, true), "Return Home once only");
        check(!gate.active(1011), "Home disarms the whole session");
        gate.arm(2000); gate.claimEpisode(2001, true, true);
        gate.claimReturnHome(2002, false, false);
        gate.observePackage("another.app", "iplayer", "shortcut");
        check(!gate.claimReturnHome(2003, true, true), "Leaving iPlayer cancels Home guard");
        gate.arm(3000); gate.claimEpisode(3001, true, true);
        gate.claimReturnHome(3002, false, false);
        check(!gate.claimReturnHome(7203001, true, true), "Old sessions expire");
        gate.arm(4000); gate.claimEpisode(4001, true, true);
        check(!gate.claimTrailer(4002, false), "No trailer means no click");
        check(!gate.claimTrailer(64001, true), "Trailer window is bounded");
        gate.cancel();
        check(!gate.claimReturnHome(4003, true, true), "Cancelled launch cannot go Home");
        System.out.println("PASS: return Home, pause safety, one-shot, cancellation, expiry, trailer parity");
    }
}
