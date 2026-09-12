package com.boop.shieldhome;

import java.util.Set;

public record StartupPackageState(
        String packageName,
        String label,
        boolean systemApp,
        boolean launcher,
        String enabledState,
        String runInBackgroundMode,
        String runAnyInBackgroundMode,
        Set<StartupRecoveryPolicy.ManagedAction> managedActions) {

    public StartupPackageState {
        if (packageName == null || packageName.isBlank()) throw new IllegalArgumentException("packageName");
        if (label == null || label.isBlank()) label = packageName;
        if (enabledState == null || enabledState.isBlank()) enabledState = "unknown";
        if (runInBackgroundMode == null || runInBackgroundMode.isBlank()) runInBackgroundMode = "default";
        if (runAnyInBackgroundMode == null || runAnyInBackgroundMode.isBlank()) runAnyInBackgroundMode = "default";
        managedActions = managedActions == null ? Set.of() : Set.copyOf(managedActions);
    }
}
