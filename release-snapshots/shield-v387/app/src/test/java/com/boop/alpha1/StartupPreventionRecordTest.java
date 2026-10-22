import com.boop.shieldhome.StartupPreventionRecord;

public final class StartupPreventionRecordTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        StartupPreventionRecord record = new StartupPreventionRecord("com.fork2.app", "allow", "default", true);
        String encoded = record.encode();
        StartupPreventionRecord decoded = StartupPreventionRecord.decode(encoded);
        check(decoded != null, "decode");
        check(decoded.packageName().equals("com.fork2.app"), "package");
        check(decoded.originalRunInBackground().equals("allow"), "first original mode");
        check(decoded.originalRunAnyInBackground().equals("default"), "second original mode");
        check(decoded.managed(), "managed flag");
        check(StartupPreventionRecord.decode("junk") == null, "reject malformed");
        check(StartupPreventionRecord.decode("v1\tcom.boop.alpha1\tallow\tallow\ttrue") == null, "reject protected package");
        check(StartupPreventionRecord.decode("v1\tcom.fork2.app\tbanana\tallow\ttrue") == null, "reject bad mode");
        System.out.println("StartupPreventionRecordTest PASS");
    }
}
