package uk.local.eastenders;
/** Fixed allowlist: no caller-supplied package or shell command. */
final class PlayerReset {
    static final String PLAYER="com.nvidia.bbciplayer";
    interface Shell { String run(String command) throws Exception; }
    static void reset(Shell shell) throws Exception {
        shell.run("am force-stop --user current com.nvidia.bbciplayer");
        String remaining=shell.run("ps -A -o NAME | grep -E '^com[.]nvidia[.]bbciplayer(:.*)?$' || true");
        if (!remaining.trim().isEmpty()) throw new java.io.IOException("iPlayer is still running");
    }
}
