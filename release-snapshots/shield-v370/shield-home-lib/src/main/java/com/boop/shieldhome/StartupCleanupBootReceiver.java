package com.boop.shieldhome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public final class StartupCleanupBootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        StartupCleanupStore store = new StartupCleanupStore(context);
        if (store.autoEnabled() && !store.targets().isEmpty())
            StartupCleanupScheduler.schedule(context.getApplicationContext(), 0);
    }
}
