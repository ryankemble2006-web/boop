package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class StartupPackageControllerTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static StartupPackageState state(String pkg, String enabled, String run, String runAny) {
        return new StartupPackageState(pkg, pkg, pkg.contains("google.android.tvlauncher"),
                pkg.contains("launcher"), enabled, run, runAny, Set.of());
    }

    private static final class FakeBridge implements StartupPackageController.Bridge {
        final List<String> calls = new ArrayList<>();
        StartupPackageState state = state("com.example.app", "default", "allow", "default");
        boolean ignoreDisable;
        @Override public StartupPackageState probe(String pkg) { calls.add("probe"); return state; }
        @Override public void setEnabledState(String pkg, String value) {
            calls.add("enabled:" + value);
            if (!ignoreDisable) state = state(pkg, value.equals("disabled-user") ? "disabled-user" : value,
                    state.runInBackgroundMode(), state.runAnyInBackgroundMode());
        }
        @Override public void forceStop(String pkg) { calls.add("force-stop"); }
        @Override public void setBackgroundModes(String pkg, String run, String runAny) {
            calls.add("appops:" + run + ":" + runAny);
            state = state(pkg, state.enabledState(), run, runAny);
        }
    }

    private static final class FakeBootStore implements StartupPackageController.BootStore {
        final Set<String> targets = new LinkedHashSet<>();
        @Override public boolean setTarget(String pkg, boolean enabled) {
            if (enabled) targets.add(pkg); else targets.remove(pkg);
            return true;
        }
        @Override public boolean contains(String pkg) { return targets.contains(pkg); }
    }

    public static void main(String[] args) {
        StartupRecoveryPolicy.RecoveryCapabilities caps = new StartupRecoveryPolicy.RecoveryCapabilities(
                "com.boop.alpha1", "com.android.tv.settings", "com.google.android.packageinstaller", null, "com.boop.alpha1");
        FakeBridge bridge = new FakeBridge();
        FakeBootStore boot = new FakeBootStore();
        StartupRestoreStore restore = new StartupRestoreStore(StartupRestoreStore.memoryBackend(), () -> 10L);
        StartupPackageController controller = new StartupPackageController(bridge, boot, restore, caps);

        StartupPackageController.Result disabled = controller.disable("com.example.app");
        check(disabled.success(), "disable succeeds");
        check(bridge.calls.equals(List.of("probe", "enabled:disabled-user", "probe")), "disable order verified");
        check(restore.record("com.example.app").managedActions().contains(StartupRecoveryPolicy.ManagedAction.DISABLED),
                "disable receipt armed");

        StartupPackageController.Result bg = controller.setBackgroundBlock("com.example.app", true);
        check(bg.success(), "background block succeeds");
        check(restore.record("com.example.app").managedActions().contains(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK),
                "background receipt armed");
        check(bg.current().runInBackgroundMode().equals("ignore"), "background block verified");

        StartupPackageController.Result bootOn = controller.setBootClean("com.example.app", true);
        check(bootOn.success() && boot.contains("com.example.app"), "boot clean enabled");
        check(restore.record("com.example.app").managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN),
                "boot clean receipt armed");

        int receiptCountBeforeForce = restore.records().size();
        StartupPackageController.Result stopped = controller.forceStop("com.example.app");
        check(stopped.success(), "force stop succeeds");
        check(restore.records().size() == receiptCountBeforeForce, "force stop adds no restore receipt");

        StartupPackageController.Result restored = controller.restore("com.example.app");
        check(restored.success(), "full restore succeeds");
        check(bridge.state.enabledState().equals("default"), "exact default enabled state restored");
        check(bridge.state.runInBackgroundMode().equals("allow"), "exact app-op restored");
        check(!boot.contains("com.example.app"), "boot clean removed on restore");
        check(restore.record("com.example.app") == null, "receipt removed only after full verification");

        FakeBridge mismatchBridge = new FakeBridge();
        mismatchBridge.ignoreDisable = true;
        StartupRestoreStore mismatchRestore = new StartupRestoreStore(StartupRestoreStore.memoryBackend(), () -> 20L);
        StartupPackageController mismatchController = new StartupPackageController(
                mismatchBridge, new FakeBootStore(), mismatchRestore, caps);
        StartupPackageController.Result mismatch = mismatchController.disable("com.example.app");
        check(!mismatch.success(), "verification mismatch fails");
        StartupRestoreRecord mismatchReceipt = mismatchRestore.record("com.example.app");
        check(mismatchReceipt != null && mismatchReceipt.managedActions().isEmpty(),
                "failed mutation keeps retryable baseline without false managed action");

        FakeBridge protectedBridge = new FakeBridge();
        protectedBridge.state = state("com.boop.alpha1", "default", "default", "default");
        StartupPackageController protectedController = new StartupPackageController(
                protectedBridge, new FakeBootStore(),
                new StartupRestoreStore(StartupRestoreStore.memoryBackend(), () -> 30L), caps);
        check(!protectedController.disable("com.boop.alpha1").success(), "protected package rejected");
        check(protectedBridge.calls.stream().allMatch("probe"::equals), "protected package not mutated");

        FakeBridge launcherBridge = new FakeBridge();
        launcherBridge.state = state("com.google.android.tvlauncher", "default", "default", "default");
        StartupPackageController launcherController = new StartupPackageController(
                launcherBridge, new FakeBootStore(),
                new StartupRestoreStore(StartupRestoreStore.memoryBackend(), () -> 40L), caps);
        check(launcherController.disable("com.google.android.tvlauncher").success(),
                "Google TV Launcher allowed despite system/high impact");

        FakeBridge reenableBridge = new FakeBridge();
        reenableBridge.state = state("com.example.previouslydisabled", "disabled-user", "allow", "default");
        StartupRestoreStore reenableRestore = new StartupRestoreStore(
                StartupRestoreStore.memoryBackend(), () -> 50L);
        StartupPackageController reenableController = new StartupPackageController(
                reenableBridge, new FakeBootStore(), reenableRestore, caps);
        check(reenableController.reenable("com.example.previouslydisabled").success(),
                "re-enable externally disabled package succeeds");
        check(reenableBridge.state.enabledState().equals("enabled"), "package is enabled");
        check(reenableController.restore("com.example.previouslydisabled").success(),
                "re-enable has exact restore path");
        check(reenableBridge.state.enabledState().equals("disabled-user"),
                "restore returns externally disabled baseline");

        check(StartupPackageController.validPackageName("com.android.systemui"), "system package syntax accepted");
        check(!StartupPackageController.validPackageName("com.android.systemui; reboot"), "shell injection rejected");
        System.out.println("StartupPackageControllerTest PASS");
    }
}
