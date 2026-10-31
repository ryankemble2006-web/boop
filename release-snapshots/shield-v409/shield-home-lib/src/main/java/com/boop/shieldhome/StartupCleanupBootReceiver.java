package com.boop.shieldhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public final class StartupCleanupBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            ShieldSelfGrant.tryDirectSecureSettingsGrant(context);
            reviveSpectrumIfWanted(context);
            return;
        }
        if (!Intent.ACTION_BOOT_COMPLETED.equals(action)) return;
        ShieldSelfGrant.tryDirectSecureSettingsGrant(context);
        reviveSpectrumIfWanted(context);
        StartupCleanupStore store = new StartupCleanupStore(context);
        if (store.autoEnabled() && !store.targets().isEmpty())
            StartupCleanupScheduler.schedule(context.getApplicationContext(), 0);
    }

    private static void reviveSpectrumIfWanted(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("boop_shield_home_v1", Context.MODE_PRIVATE);
            if (prefs.getBoolean("spectrum_enabled", false) && !SpectrumState.running) {
                Intent spectrum = new Intent(context, BassCaptureActivity.class);
                spectrum.putExtra("auto_start", true);
                spectrum.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(spectrum);
            }
        } catch (Throwable ignored) {}
    }
}
