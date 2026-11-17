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
        // Background broadcasts must never open MediaProjection UI over a player.
        // Home retains spectrum startup when the user explicitly returns there.
    }
}
