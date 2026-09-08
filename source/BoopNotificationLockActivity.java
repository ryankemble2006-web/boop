package com.boop.alpha1;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public final class BoopNotificationLockActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean receiverRegistered;

    private final Runnable timeoutRunnable = () -> {
        try {
            BoopNotificationRuntime.get(this).onPresentationDismissed();
        } catch (RuntimeException ignored) {
            // Android's source notification remains untouched.
        }
        finish();
    };

    private final BroadcastReceiver userPresentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_USER_PRESENT.equals(intent == null ? null : intent.getAction())) {
                try {
                    BoopNotificationRuntime.get(BoopNotificationLockActivity.this)
                            .transitionAfterUnlock();
                } catch (RuntimeException ignored) {
                    // Source notification remains in Android.
                }
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setShowWhenLocked(true);
        setTurnScreenOn(true);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
        renderLockedPresentation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        renderLockedPresentation();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!receiverRegistered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(userPresentReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(userPresentReceiver, filter);
            }
            receiverRegistered = true;
        }
    }

    @Override
    protected void onStop() {
        if (receiverRegistered) {
            try {
                unregisterReceiver(userPresentReceiver);
            } catch (IllegalArgumentException ignored) {
                // Receiver was already detached by the framework.
            }
            receiverRegistered = false;
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(timeoutRunnable);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onDestroy();
    }

    private void renderLockedPresentation() {
        handler.removeCallbacks(timeoutRunnable);
        BoopNotificationRuntime runtime;
        try {
            runtime = BoopNotificationRuntime.get(this);
        } catch (RuntimeException unavailable) {
            finish();
            return;
        }
        List<BoopNotificationEnvelope> bundle = runtime.visibleBundle();
        if (bundle.isEmpty()) {
            finish();
            return;
        }

        BoopNotificationPresentation presentation = BoopNotificationPresentation.from(
                bundle, BoopNotificationSurface.LOCKED, true);
        View view = BoopNotificationInPlaceController.createPlainPresentationView(
                this, presentation);
        setContentView(view);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handler.postDelayed(timeoutRunnable, Math.max(1L, runtime.settings().timeoutMs()));
    }
}
