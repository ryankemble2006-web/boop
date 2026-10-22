package com.boop.shieldhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import com.boop.shared.MediaRequest;

/**
 * Dispatches mapped hardware actions natively without third-party services or race conditions.
 */
public final class BoopKeyActionExecutor {
    private static final String TAG = "BOOPKeyAction";

    private BoopKeyActionExecutor() {}

    public static void execute(Context context, String action, String targetComponent) {
        if (action == null || BoopKeyMappingStore.ACTION_DEFAULT.equals(action)) return;

        Runnable r = () -> {
            try {
                switch (action) {
                    case BoopKeyMappingStore.ACTION_NEXT_TRACK:
                        if (!ShieldNowPlayingManager.get(context).requestTransport(MediaRequest.Kind.NEXT)) {
                            dispatchMediaKeyFallback(context, KeyEvent.KEYCODE_MEDIA_NEXT);
                        }
                        Log.i(TAG, "Dispatched: Next Track");
                        break;

                    case BoopKeyMappingStore.ACTION_PREV_TRACK:
                        if (!ShieldNowPlayingManager.get(context).requestTransport(MediaRequest.Kind.PREVIOUS)) {
                            dispatchMediaKeyFallback(context, KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                        }
                        Log.i(TAG, "Dispatched: Previous Track");
                        break;

                    case BoopKeyMappingStore.ACTION_PLAY_PAUSE:
                        ShieldNowPlayingManager.get(context).togglePlayPause();
                        Log.i(TAG, "Dispatched: Play / Pause toggle");
                        break;

                    case BoopKeyMappingStore.ACTION_DEEZER_FLOW:
                        ShieldNowPlayingManager.get(context).playDeezerFlow();
                        Log.i(TAG, "Dispatched: Background Deezer Flow");
                        break;

                    case BoopKeyMappingStore.ACTION_SLEEP_TIMER:
                        launchApp(context, "com.neon.sleeptvtimerpro", "com.neon.sleeptvtimerpro.MainActivity");
                        Log.i(TAG, "Dispatched: Sleep TV Timer Pro");
                        break;

                    case BoopKeyMappingStore.ACTION_RECENTS:
                        if (context instanceof android.accessibilityservice.AccessibilityService) {
                            boolean dispatched = ((android.accessibilityservice.AccessibilityService) context)
                                    .performGlobalAction(android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_RECENTS);
                            if (!dispatched) {
                                dispatchAppSwitchFallback(context);
                            }
                        } else {
                            dispatchAppSwitchFallback(context);
                        }
                        Log.i(TAG, "Dispatched: Recents / Task Switcher");
                        break;

                    case BoopKeyMappingStore.ACTION_CUSTOM_APP:
                        if (targetComponent != null && !targetComponent.isEmpty()) {
                            launchComponent(context, targetComponent);
                        }
                        break;

                    default:
                        Log.w(TAG, "Unknown action: " + action);
                        break;
                }
            } catch (Exception e) {
                Log.w(TAG, "Failed to execute key action: " + action, e);
            }
        };
        try {
            new Handler(Looper.getMainLooper()).post(r);
        } catch (Throwable t) {
            r.run();
        }
    }

    private static void dispatchMediaKeyFallback(Context context, int keyCode) {
        try {
            AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
                am.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            }
        } catch (Exception ignored) {}
    }

    private static void launchApp(Context context, String pkg, String cls) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName(pkg, cls));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                Intent fallback = context.getPackageManager().getLaunchIntentForPackage(pkg);
                if (fallback != null) {
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(fallback);
                }
            } catch (Exception fallbackFailed) {
                Log.w(TAG, "Unable to launch app: " + pkg, fallbackFailed);
            }
        }
    }

    private static void dispatchAppSwitchFallback(Context context) {
        try {
            Intent intent = new Intent("com.android.systemui.recents.TOGGLE_RECENTS");
            intent.setPackage("com.android.systemui");
            context.sendBroadcast(intent);
        } catch (Exception ignored) {}
        try {
            Runtime.getRuntime().exec(new String[]{"input", "keyevent", "KEYCODE_APP_SWITCH"});
        } catch (Exception ignored) {}
    }

    private static void launchComponent(Context context, String componentStr) {
        try {
            String[] parts = componentStr.split("/");
            if (parts.length == 2) {
                String pkg = parts[0];
                String cls = parts[1].startsWith(".") ? pkg + parts[1] : parts[1];
                launchApp(context, pkg, cls);
            }
        } catch (Exception e) {
            Log.w(TAG, "Unable to launch component: " + componentStr, e);
        }
    }
}
