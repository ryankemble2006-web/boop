package uk.local.eastenders;
public final class AutoplayPolicyTest {
    private static void check(boolean result, String message) { if (!result) throw new AssertionError(message); }
    public static void main(String[] args) {
        check(UiPolicy.isProfileChooser("Select who's watching...."), "Recognise iPlayer profile chooser");
        check(UiPolicy.isExistingProfile("avatar-7c3d3a33-8d3a-47c5-baac-2c5eb2cfe9ec", "Polly"), "Accept existing adult profile");
        check(!UiPolicy.isExistingProfile("avatar-add-profile", "Add child"), "Never click Add child");
        check(!UiPolicy.isExistingProfile("avatar-add-adult", "Add adult"), "Never click Add adult");
        check(UiPolicy.isEpisodeRow("programme-grid:row_0"), "Recognise newest episode row");
        check(!UiPolicy.isEpisodeRow("programme-grid:row_1"), "Ignore older episode rows");
        check(UiPolicy.isEpisodeCard("m0031blw", "29 mins 10/09/2026 Alfie encourages Kat to make amends with Zoe."), "Accept first real episode card");
        check(!UiPolicy.isEpisodeCard("programme-grid:row_0", ""), "Do not click row container");
        System.out.println("PASS: profile chooser and newest-episode UI policy");
    }
}