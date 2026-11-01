package com.boop.shieldoverlay;

import android.content.Context;
import android.content.Intent;

public final class BoopOverlayController {
    private BoopOverlayController() { }

    public static void hide(Context context) {
        send(context, BoopOverlayService.ACTION_HIDE_EYES);
    }

    public static void show(Context context) {
        send(context, BoopOverlayService.ACTION_SHOW_EYES);
    }

    private static void send(Context context, String action) {
        if (context == null) {
            throw new IllegalArgumentException("context is required");
        }
        Intent intent = new Intent(context, BoopOverlayService.class);
        intent.setAction(action);
        context.startForegroundService(intent);
    }
}
