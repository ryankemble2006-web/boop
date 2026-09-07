package com.boop.alpha1;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Runs ONLY in the separate test APK on the disposable emulator. */
public final class WallPolishInstrumentation extends Instrumentation {
    private MainActivity activity;
    private File evidence;

    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }

    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            check(android.os.Build.FINGERPRINT.contains("generic")
                    || android.os.Build.MODEL.contains("sdk"), "Disposable emulator required");
            evidence = new File(getTargetContext().getExternalFilesDir(null), "wall-polish-evidence");
            check(evidence.mkdirs() || evidence.isDirectory(), "Evidence folder unavailable");
            activity = (MainActivity) startActivitySync(new Intent(getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            waitForIdleSync();
            SystemClock.sleep(1000);
            verifyNotice();
            // Restore Wall after the background-notice test, without injecting a speech command.
            getTargetContext().startActivity(new Intent(getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            SystemClock.sleep(700);
            verifyIdleBlinkAndSleep();
            verifyLandscapeAndBackground();
            result.putString("stream", "\nPASS: styled notice, visible background toast, awake blink, busy-state gates, original sleep deadline, landscape and background cancellation\n");
            finish(Activity.RESULT_OK, result);
        } catch (Throwable failure) {
            result.putString("stream", "\nFAIL: " + android.util.Log.getStackTraceString(failure));
            finish(Activity.RESULT_CANCELED, result);
        }
    }

    private void verifyNotice() throws Exception {
        CharSequence text = BoopFreeChatNotice.text(true);
        check(text.toString().equals("Question copied.\nPaste into Free Chat."), "Wrong notice wording");
        check(text instanceof Spanned, "Notice has lost its formatting");
        Spanned spans = (Spanned) text;
        RelativeSizeSpan[] sizes = spans.getSpans(0, text.length(), RelativeSizeSpan.class);
        StyleSpan[] styles = spans.getSpans(0, text.length(), StyleSpan.class);
        check(sizes.length == 1 && sizes[0].getSizeChange() == 1.5f, "Notice is not 50 percent larger");
        check(styles.length == 1 && styles[0].getStyle() == android.graphics.Typeface.BOLD, "Notice is not bold");
        check(!BoopFreeChatNotice.text(false).toString().contains("copied"), "Failed copy must not claim success");
        // A plain text toast is still visible after another app becomes foreground.
        // Settings is used instead of contacting a real chatbot or account.
        getTargetContext().startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        SystemClock.sleep(1000);
        captureNotice("notice-original.png", text.toString());
        captureNotice("notice-larger.png", text);
    }

    private void captureNotice(String name, CharSequence text) throws Exception {
        CountDownLatch shown = new CountDownLatch(1);
        AtomicReference<Toast> toast = new AtomicReference<>();
        runOnMainSync(() -> {
            Toast message = Toast.makeText(getTargetContext(), text, Toast.LENGTH_LONG);
            message.addCallback(new Toast.Callback() {
                @Override public void onToastShown() { shown.countDown(); }
            });
            toast.set(message);
            message.show();
        });
        check(shown.await(6, TimeUnit.SECONDS), "System did not show " + name);
        SystemClock.sleep(450);
        save(getUiAutomation().takeScreenshot(), name);
        runOnMainSync(() -> toast.get().cancel());
        SystemClock.sleep(500);
    }

    private void verifyIdleBlinkAndSleep() throws Exception {
        runOnMainSync(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT));
        SystemClock.sleep(600);
        onMain(() -> { invoke(activity, "wakeFaceForInteraction"); return null; });
        long wakeAt = SystemClock.uptimeMillis();
        SystemClock.sleep(500);
        BoopFaceView face = (BoopFaceView) onMain(() -> get(activity, "face"));
        saveFace(face, "eyes-open-portrait.png");
        check(onMain(() -> (Boolean) invoke(face, "canIdleBlink")), "Awake idle face is not eligible");
        for (String flag : new String[]{"listening", "thinking", "voiceSettingsOpen", "chatModeOpen", "faceTouchActive"}) {
            onMain(() -> {
                set(activity, flag, true);
                try { check(!(Boolean) invoke(face, "canIdleBlink"), "Blink overlaps " + flag); }
                finally { set(activity, flag, false); }
                return null;
            });
        }
        boolean closed = false;
        long firstDeadline = wakeAt + 8500;
        while (SystemClock.uptimeMillis() < firstDeadline) {
            if (onMain(() -> (Float) get(face, "idleBlinkOpenness")) < 0.15f) {
                saveFace(face, "eyes-blink-portrait.png"); closed = true; break;
            }
            SystemClock.sleep(12);
        }
        check(closed, "No actual idle blink within its bounded interval");
        SystemClock.sleep(350);
        check(onMain(() -> (Float) get(face, "idleBlinkOpenness")) == 1f, "Eyes did not reopen");
        // Deliberately do not call any interaction method again. Blinks must not
        // postpone the pre-existing 30-second idle deadline.
        long sleepAt = wakeAt + 31200;
        while (SystemClock.uptimeMillis() < sleepAt) SystemClock.sleep(100);
        check(onMain(() -> ((BoopPresenceState) get(activity, "presenceState")).isIdleBlack()), "Blink reset the sleep timer");
        check(onMain(() -> face.getAlpha()) == 0f, "Sleeping face is not black");
        check(!onMain(() -> (Boolean) get(face, "awakeForBlink")), "Sleeping blink scheduler is still enabled");
        check(onMain(() -> get(face, "idleBlinkAnimator")) == null, "Sleeping face still animates");
        save(getUiAutomation().takeScreenshot(), "asleep.png");
    }

    private void verifyLandscapeAndBackground() throws Exception {
        runOnMainSync(() -> activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
        SystemClock.sleep(700);
        onMain(() -> { invoke(activity, "wakeFaceForInteraction"); return null; });
        SystemClock.sleep(500);
        BoopFaceView face = (BoopFaceView) onMain(() -> get(activity, "face"));
        check(onMain(() -> face.getWidth() > face.getHeight()), "Landscape was not applied");
        saveFace(face, "eyes-open-landscape.png");
        // Trigger only the private visual callback, never the microphone or network.
        onMain(() -> { invoke(face, "runIdleBlink"); return null; });
        boolean closed = false;
        long deadline = SystemClock.uptimeMillis() + 500;
        while (SystemClock.uptimeMillis() < deadline) {
            if (onMain(() -> (Float) get(face, "idleBlinkOpenness")) < 0.15f) {
                saveFace(face, "eyes-blink-landscape.png"); closed = true; break;
            }
            SystemClock.sleep(10);
        }
        check(closed, "Landscape blink did not render");
        getTargetContext().startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        SystemClock.sleep(500);
        check(onMain(() -> get(face, "idleBlinkAnimator")) == null, "Off-screen blink remains active");
        check(!onMain(() -> face.getHandler().hasCallbacks((Runnable) get(face, "idleBlinkRunnable"))), "Off-screen blink callback remains queued");
    }

    private void saveFace(BoopFaceView face, String name) throws Exception {
        Bitmap image = onMain(() -> {
            Bitmap bitmap = Bitmap.createBitmap(face.getWidth(), face.getHeight(), Bitmap.Config.ARGB_8888);
            face.draw(new Canvas(bitmap));
            return bitmap;
        });
        save(image, name);
    }
    private void save(Bitmap image, String name) throws Exception {
        check(image != null, "Missing screenshot: " + name);
        try (FileOutputStream stream = new FileOutputStream(new File(evidence, name))) {
            check(image.compress(Bitmap.CompressFormat.PNG, 100, stream), "Screenshot encoding failed");
        } finally { image.recycle(); }
    }
    private interface MainCall<T> { T call() throws Exception; }
    private <T> T onMain(MainCall<T> call) throws Exception {
        AtomicReference<T> value = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> { try { value.set(call.call()); } catch (Throwable t) { failure.set(t); } });
        if (failure.get() != null) throw new Exception(failure.get());
        return value.get();
    }
    private static Object get(Object owner, String name) throws Exception {
        Field field = owner.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(owner);
    }
    private static void set(Object owner, String name, Object value) throws Exception {
        Field field = owner.getClass().getDeclaredField(name); field.setAccessible(true); field.set(owner, value);
    }
    private static Object invoke(Object owner, String name) throws Exception {
        Method method = owner.getClass().getDeclaredMethod(name); method.setAccessible(true); return method.invoke(owner);
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
