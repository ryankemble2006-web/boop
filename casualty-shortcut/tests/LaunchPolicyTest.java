package uk.local.casualty;
public final class LaunchPolicyTest {
    private static void check(boolean result, String message) { if(!result) throw new AssertionError(message); }
    public static void main(String[] args) {
        check(LaunchPolicy.prewarmDelayMs("com.nvidia.bbciplayer") == 0L,
                "Shield iPlayer must launch directly; prewarm poisons video surface");
        check(LaunchPolicy.prewarmDelayMs("bbc.iplayer.android") == 0L, "Other package stays direct");
        check(LaunchPolicy.prewarmDelayMs("uk.co.bbc.iplayer") == 0L, "Legacy package stays direct");
        System.out.println("PASS: all iPlayer packages use direct deep-link launch");
    }
}
