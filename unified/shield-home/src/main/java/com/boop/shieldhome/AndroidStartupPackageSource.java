package com.boop.shieldhome;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class AndroidStartupPackageSource implements StartupPackageRepository.PackageSource {
    private final PackageManager pm;

    AndroidStartupPackageSource(Context context) {
        pm = context.getApplicationContext().getPackageManager();
    }

    @Override public List<StartupPackageRepository.RawPackage> loadInstalled() {
        Set<String> launchers = launcherPackages();
        ArrayList<StartupPackageRepository.RawPackage> out = new ArrayList<>();
        for (ApplicationInfo app : pm.getInstalledApplications(PackageManager.MATCH_DISABLED_COMPONENTS)) {
            String pkg = app.packageName;
            if (pkg == null || pkg.isBlank()) continue;
            boolean system = (app.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0;
            CharSequence rawLabel = pm.getApplicationLabel(app);
            String label = rawLabel == null || rawLabel.toString().isBlank() ? pkg : rawLabel.toString().trim();
            out.add(new StartupPackageRepository.RawPackage(
                    pkg, label, system, launchers.contains(pkg), enabledState(pkg, app.enabled)));
        }
        return out;
    }
    private Set<String> launcherPackages() {
        HashSet<String> out = new HashSet<>();
        for (String category : List.of(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_HOME)) {
            Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(category);
            for (ResolveInfo info : pm.queryIntentActivities(intent, PackageManager.MATCH_DISABLED_COMPONENTS)) {
                if (info.activityInfo != null && info.activityInfo.packageName != null) {
                    out.add(info.activityInfo.packageName);
                }
            }
        }
        return out;
    }

    private String enabledState(String packageName, boolean manifestEnabled) {
        try {
            return switch (pm.getApplicationEnabledSetting(packageName)) {
                case PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> "enabled";
                case PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> "disabled";
                case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER -> "disabled-user";
                case PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> "disabled-until-used";
                default -> manifestEnabled ? "default" : "manifest-disabled";
            };
        } catch (IllegalArgumentException ignored) {
            return manifestEnabled ? "default" : "manifest-disabled";
        }
    }
}
