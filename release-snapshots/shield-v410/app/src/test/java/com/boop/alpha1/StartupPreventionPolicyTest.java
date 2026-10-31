import com.boop.shieldhome.StartupPreventionPolicy;

public final class StartupPreventionPolicyTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(StartupPreventionPolicy.parseMode("RUN_IN_BACKGROUND: default").equals("default"), "explicit default");
        check(StartupPreventionPolicy.parseMode("No operations.\nDefault mode: allow").equals("default"), "default-mode allow");
        check(StartupPreventionPolicy.parseMode("RUN_ANY_IN_BACKGROUND: ignore; time=+1s").equals("ignore"), "ignore mode");
        check(StartupPreventionPolicy.blockCommands("com.example.app").size() == 2, "two app-op writes");
        check(StartupPreventionPolicy.blockCommands("com.example.app").get(0).contains("RUN_IN_BACKGROUND ignore"), "first block op");
        check(StartupPreventionPolicy.blockCommands("com.example.app").get(1).contains("RUN_ANY_IN_BACKGROUND ignore"), "second block op");
        check(StartupPreventionPolicy.restoreCommands("com.example.app", "allow", "default").get(0).endsWith("RUN_IN_BACKGROUND allow"), "restore first exact mode");
        check(StartupPreventionPolicy.restoreCommands("com.example.app", "allow", "default").get(1).endsWith("RUN_ANY_IN_BACKGROUND default"), "restore second exact mode");
        check(!StartupPreventionPolicy.eligible("deezer.android.app", false), "Deezer warm path protected");
        check(!StartupPreventionPolicy.eligible("com.netflix.ninja", false), "Netflix warm path protected");
        check(!StartupPreventionPolicy.eligible("com.plexapp.android", false), "Plex warm path protected");
        check(!StartupPreventionPolicy.eligible("com.google.android.youtube.tv", true), "system app protected");
        check(StartupPreventionPolicy.eligible("com.fork2.app", false), "ordinary user app eligible");
        System.out.println("StartupPreventionPolicyTest PASS");
    }
}
