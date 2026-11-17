package com.boop.shieldhome;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Window;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

/** Held Back opens Shield settings from child launcher screens; short Back keeps their own behaviour. */
public final class LauncherRemoteNavigation {
    private LauncherRemoteNavigation() { }

    public static void install(Activity activity) {
        if (activity instanceof ShieldLauncherActivity) return;
        Window.Callback original = activity.getWindow().getCallback();
        if (original == null || handler(original) != null) return;
        State state = new State(activity, original);
        activity.getWindow().setCallback((Window.Callback) Proxy.newProxyInstance(
                activity.getClassLoader(), new Class<?>[]{Window.Callback.class}, state));
    }

    public static void cancel(Activity activity) {
        State state = handler(activity.getWindow().getCallback());
        if (state != null) state.cancel();
    }

    private static State handler(Window.Callback callback) {
        if (callback == null || !Proxy.isProxyClass(callback.getClass())) return null;
        InvocationHandler handler = Proxy.getInvocationHandler(callback);
        return handler instanceof State ? (State) handler : null;
    }

    private static final class State implements InvocationHandler {
        final WeakReference<Activity> activity;
        final Window.Callback original;
        final Handler timer = new Handler(Looper.getMainLooper());
        KeyEvent down;
        boolean held;
        final Runnable hold = this::onHold;
        private void onHold() {
            Activity owner = activity.get();
            if (down == null || owner == null || owner.isFinishing()) return;
            held = true;
            owner.startActivity(new Intent(owner, ShieldLauncherActivity.class)
                    .setAction(ShieldLauncherActivity.ACTION_OPEN_SHIELD_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        State(Activity activity, Window.Callback original) {
            this.activity = new WeakReference<>(activity); this.original = original;
        }

        void cancel() { timer.removeCallbacks(hold); down = null; held = false; }

        @Override public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
            if (method.getName().equals("dispatchKeyEvent") && args[0] instanceof KeyEvent) {
                KeyEvent event = (KeyEvent) args[0];
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        if (event.getRepeatCount() == 0) {
                            cancel(); down = new KeyEvent(event);
                            timer.postDelayed(hold, ShieldLauncherActivity.SHIELD_SETTINGS_HOLD_MS);
                        }
                        return true;
                    }
                    if (event.getAction() == KeyEvent.ACTION_UP) {
                        KeyEvent press = down;
                        boolean shortBack = press != null && !held && !event.isCanceled();
                        cancel();
                        if (shortBack) { original.dispatchKeyEvent(press); original.dispatchKeyEvent(event); }
                        return true;
                    }
                }
            }
            try { return method.invoke(original, args); }
            catch (InvocationTargetException error) { throw error.getCause(); }
        }
    }
}
