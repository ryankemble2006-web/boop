package com.boop.shieldhome;
import java.util.*;

/** Recovery must survive partial writes, stale UI snapshots and private-store failures. */
public final class StartupSafetyRegressionTest {
    static int failed;
    interface Case { void run() throws Exception; }
    static void test(String name, Case body) {
        try { body.run(); System.out.println("PASS " + name); }
        catch (Throwable e) { failed++; System.out.println("FAIL " + name + ": " + e); }
    }
    static void check(boolean value, String why) { if (!value) throw new AssertionError(why); }
    static final StartupRecoveryPolicy.RecoveryCapabilities CAPS =
        new StartupRecoveryPolicy.RecoveryCapabilities("com.boop.alpha1", "com.android.tv.settings",
            "com.google.android.packageinstaller", "com.google.android.inputmethod.latin", "com.boop.alpha1");
    static StartupPackageState state(String pkg, String enabled, String run, String any, boolean boot) {
        return new StartupPackageState(pkg, pkg, pkg.startsWith("com.android"), false,
            enabled, run, any, boot ? Set.of(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN) : Set.of());
    }
    static final class Wire implements StartupPackageController.Bridge {
        StartupPackageState now = state("com.example.app", "default", "allow", "default", false);
        boolean partialBackground;
        int writes;
        public StartupPackageState probe(String pkg) { return now; }
        public void setEnabledState(String pkg, String enabled) {
            writes++; now=state(pkg, enabled, now.runInBackgroundMode(), now.runAnyInBackgroundMode(), false);
        }
        public void setBackgroundModes(String pkg, String run, String any) {
            writes++;
            if (partialBackground) {
                partialBackground=false;
                now=state(pkg, now.enabledState(), run, now.runAnyInBackgroundMode(), false);
                throw new IllegalStateException("second app-op write lost");
            }
            now=state(pkg, now.enabledState(), run, any, false);
        }
        public void forceStop(String pkg) { writes++; }
    }
    static final class Boot implements StartupPackageController.BootStore {
        boolean selected, ignoreWrite;
        public boolean setTarget(String pkg, boolean enabled) { if (!ignoreWrite) selected=enabled; return true; }
        public boolean contains(String pkg) { return selected; }
    }
    static StartupRestoreStore ledger() { return new StartupRestoreStore(StartupRestoreStore.memoryBackend(), () -> 123L); }
    static StartupPackageController controller(Wire wire, Boot boot, StartupRestoreStore store) {
        return new StartupPackageController(wire, boot, store, CAPS);
    }
    public static void main(String[] args) {
        test("partial background write can be restored", () -> {
            Wire wire=new Wire(); wire.partialBackground=true; var store=ledger();
            var c=controller(wire,new Boot(),store);
            check(!c.setBackgroundBlock("com.example.app",true).success(),"partial operation must not succeed");
            check(store.record("com.example.app")!=null,"baseline survives partial write");
            check(c.restore("com.example.app").success(),"Restore must repair the partial write");
            check(wire.now.runInBackgroundMode().equals("allow"),"exact first app-op restored");
        });
        test("boot selection requires read-back", () -> {
            Wire wire=new Wire(); Boot boot=new Boot(); boot.ignoreWrite=true;
            check(!controller(wire,boot,ledger()).setBootClean("com.example.app",true).success(),"ignored store write must not report ON");
        });
        test("baseline boot selection is restored", () -> {
            Wire wire=new Wire(); Boot boot=new Boot(); boot.selected=true;
            var c=controller(wire,boot,ledger());
            check(c.disable("com.example.app").success(),"disable succeeds");
            check(c.restore("com.example.app").success(),"restore preserves pre-existing boot choice");
            check(boot.selected,"pre-existing boot choice remains selected");
        });
        test("probe cannot mutate a different package", () -> {
            Wire wire=new Wire(); wire.now=state("com.other.app","default","allow","default",false);
            check(!controller(wire,new Boot(),ledger()).disable("com.example.app").success(),"mismatched identity rejected");
            check(wire.writes==0,"no wrong-target write");
        });
        test("unknown state is not a restore baseline", () -> {
            Wire wire=new Wire(); wire.now=state("com.example.app","default","unknown","unknown",false);
            check(!controller(wire,new Boot(),ledger()).disable("com.example.app").success(),"unknown app-ops fail closed");
            check(wire.writes==0,"nothing mutated without an exact baseline");
        });
        test("System UI and remote input are protected", () -> {
            for(String pkg: List.of("android","com.android.systemui","com.android.bluetooth","com.android.shell")) {
                check(StartupRecoveryPolicy.assess(state(pkg,"default","default","default",false),CAPS).protectedPackage(),pkg+" is recovery-critical");
            }
            check(!StartupRecoveryPolicy.assess(state("com.google.android.tvlauncher","default","default","default",false),CAPS).protectedPackage(),"stock launcher stays manageable");
        });
        test("Restore cannot disable the recovery console", () -> {
            Wire wire=new Wire(); wire.now=state("com.boop.alpha1","enabled","default","default",false);
            var store=ledger(); store.captureIfAbsent("com.boop.alpha1",state("com.boop.alpha1","disabled-user","default","default",false));
            check(!controller(wire,new Boot(),store).restore("com.boop.alpha1").success(),"unsafe recovery rollback refused");
            check(wire.writes==0,"BOOP remains enabled");
        });
        test("corrupt receipt is never overwritten", () -> {
            Map<String,String> disk=new HashMap<>(); disk.put("entry.com.example.app","broken-original");
            var store=new StartupRestoreStore(StartupRestoreStore.mapBackend(disk),()->123L);
            Wire wire=new Wire();
            check(!controller(wire,new Boot(),store).disable("com.example.app").success(),"corrupt original blocks mutation");
            check(disk.get("entry.com.example.app").equals("broken-original"),"keep damaged receipt for recovery");
            check(wire.writes==0,"no write when receipt is damaged");
        });
        test("record key must match its package", () -> {
            var store=ledger(); boolean rejected=false;
            try { store.captureIfAbsent("com.other.app",state("com.example.app","default","allow","default",false)); }
            catch(IllegalArgumentException expected) { rejected=true; }
            check(rejected,"key mismatch rejected");
        });
        test("schema ten cannot masquerade as one", () -> {
            String raw=StartupRestoreRecord.fromBaseline(state("com.example.app","default","allow","default",false),123L).encode();
            check(StartupRestoreRecord.decode(raw.replace("\"v\":1", "\"v\":10").replace("\"v\":2", "\"v\":20").replace("\"v\":2", "\"v\":20"))==null,"unknown schema rejected");
        });
        test("protected apps can be re-enabled but not disabled", () -> {
            var enabled=state("com.android.systemui","default","default","default",false);
            var disabled=state("com.android.systemui","disabled-user","default","default",false);
            check(!StartupManagerUiModel.canUsePrimary(enabled,CAPS),"protected Disable is unavailable");
            check(StartupManagerUiModel.canUsePrimary(disabled,CAPS),"recovery Re-enable stays available");
            check(StartupRecoveryPolicy.assess(state("com.android.providers.settings","default","default","default",false),CAPS).protectedPackage(),"settings storage is part of recovery");
        });
        if(failed!=0) throw new AssertionError(failed+" recovery regressions");
        System.out.println("StartupSafetyRegressionTest PASS");
    }
}
