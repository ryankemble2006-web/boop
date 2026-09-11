import com.boop.shieldhome.StartupCleanupPolicy;

public final class StartupCleanupPolicyTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(StartupCleanupPolicy.eligible("com.example.app", false), "ordinary user app eligible");
        check(!StartupCleanupPolicy.eligible("com.google.android.youtube.tv", true), "ordinary system app protected");
        check(StartupCleanupPolicy.eligibleForCleanStart("com.google.android.tvlauncher", true), "stock launcher clean-start exception");
        check(!StartupCleanupPolicy.eligibleForCleanStart("com.android.systemui", true), "SystemUI remains protected");
        check(!StartupCleanupPolicy.eligibleForCleanStart("com.nvidia.shieldservice", true), "NVIDIA service remains protected");
        check(!StartupCleanupPolicy.eligibleForCleanStart("deezer.android.app", false), "Deezer warm path protected");
        check(!StartupCleanupPolicy.eligibleForPrevention("com.google.android.tvlauncher", true), "stock launcher prevention remains forbidden");
        System.out.println("StartupCleanupPolicyTest PASS");
    }
}
