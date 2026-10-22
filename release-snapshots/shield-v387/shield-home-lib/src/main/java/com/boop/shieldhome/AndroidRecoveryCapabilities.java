package com.boop.shieldhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;

final class AndroidRecoveryCapabilities {
    private AndroidRecoveryCapabilities() { }

    static StartupRecoveryPolicy.RecoveryCapabilities resolve(Context context) {
        Context app = context.getApplicationContext();
        PackageManager pm = app.getPackageManager();
        String boop = app.getPackageName();
        String settings = resolvedPackage(pm, new Intent(Settings.ACTION_SETTINGS), "com.android.tv.settings");
        String installer = resolvedPackage(pm,
                new Intent(Intent.ACTION_INSTALL_PACKAGE).setData(android.net.Uri.parse("package:" + boop)),
                "com.google.android.packageinstaller");
        String input = currentInputPackage(app);
        return new StartupRecoveryPolicy.RecoveryCapabilities(boop, settings, installer, input, boop);
    }

    private static String resolvedPackage(PackageManager pm, Intent intent, String fallback) {
        try {
            var info = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (info != null && info.activityInfo != null && info.activityInfo.packageName != null)
                return info.activityInfo.packageName;
        } catch (Exception ignored) { }
        return fallback;
    }

    private static String currentInputPackage(Context context) {
        try {
            String raw = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
            ComponentName component = raw == null ? null : ComponentName.unflattenFromString(raw);
            return component == null ? null : component.getPackageName();
        } catch (Exception ignored) {
            return null;
        }
    }
}
