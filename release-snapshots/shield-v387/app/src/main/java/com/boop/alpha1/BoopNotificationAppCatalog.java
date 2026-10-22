package com.boop.alpha1;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.os.Process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BoopNotificationAppCatalog {
    private BoopNotificationAppCatalog() { }

    static List<BoopNotificationAppEntry> load(
            Context context,
            Collection<BoopNotificationChannelInfo> observedChannels) {
        if (context == null) throw new IllegalArgumentException("context required");

        List<BoopNotificationAppEntry> launchable = new ArrayList<>();
        LauncherApps launcherApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        if (launcherApps != null) {
            try {
                for (LauncherActivityInfo info :
                        launcherApps.getActivityList(null, Process.myUserHandle())) {
                    if (info == null || info.getComponentName() == null) continue;
                    String packageName = info.getComponentName().getPackageName();
                    CharSequence label = info.getLabel();
                    launchable.add(new BoopNotificationAppEntry(
                            packageName,
                            label == null ? packageName : label.toString()));
                }
            } catch (RuntimeException ignored) {
                // Settings remains usable from the observed notification inventory.
            }
        }

        Map<String, BoopNotificationAppEntry> observed = new LinkedHashMap<>();
        if (observedChannels != null) {
            PackageManager packageManager = context.getPackageManager();
            for (BoopNotificationChannelInfo channel : observedChannels) {
                if (channel == null || channel.packageName().isEmpty()) continue;
                String packageName = channel.packageName();
                if (observed.containsKey(packageName)) continue;
                observed.put(packageName, new BoopNotificationAppEntry(
                        packageName,
                        appLabel(packageManager, packageName)));
            }
        }

        return BoopNotificationAppCatalogModel.merge(launchable, observed.values());
    }

    private static String appLabel(PackageManager packageManager, String packageName) {
        if (packageManager == null) return packageName;
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, 0);
            CharSequence label = packageManager.getApplicationLabel(info);
            return label == null || label.toString().trim().isEmpty()
                    ? packageName : label.toString();
        } catch (PackageManager.NameNotFoundException | RuntimeException ignored) {
            return packageName;
        }
    }
}
