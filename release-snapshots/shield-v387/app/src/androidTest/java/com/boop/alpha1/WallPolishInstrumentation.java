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
            // Initialize screenshot/accessibility transport before a short-lived notice.
            getUiAutomation();
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
        // Each capture models the real foreground Activity -> browser handoff.
        // Two unrelated background toasts do not exercise that production path.
        captureNotice("notice-original.png", text.toString());
        captureNotice("notice-larger.png", text);
    }

    private void bringWallToForeground() throws Exception {
        getTargetContext().startActivity(new Intent(getTargetContext(), MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        long deadline = SystemClock.uptimeMillis() + 6000;
        while (SystemClock.uptimeMillis() < deadline) {
            if (onMain(() -> activity.hasWindowFocus()
                    && (Boolean) get(activity, "activityInForeground"))) return;
            SystemClock.sleep(50);
        }
        throw new AssertionError("Wall did not regain foreground focus");
    }

    private void captureNotice(String name, CharSequence text) throws Exception {
        bringWallToForeground();
        CountDownLatch shown = new CountDownLatch(1);
        CountDownLatch hidden = new CountDownLatch(1);
        runOnMainSync(() -> {
            // Same ordering and Activity context as BoopFreeChat.open(). The
            // real destination is substituted only to avoid a network/account.
            activity.startActivity(new Intent(Settings.ACTION_SETTINGS));
            Toast message = Toast.makeText(activity, text, Toast.LENGTH_LONG);
            message.addCallback(new Toast.Callback() {
                @Override public void onToastShown() { shown.countDown(); }
                @Override public void onToastHidden() { hidden.countDown(); }
            });
            message.show();
        });
        check(shown.await(6, TimeUnit.SECONDS), "System did not show " + name);
        long shownAt = SystemClock.uptimeMillis();
        for (int capture = 0; capture < 3; capture++) {
            if (capture > 0) SystemClock.sleep(200);
            String suffix = capture == 0 ? name : name.replace(".png", "-" + capture + ".png");
            save(getUiAutomation().takeScreenshot(), suffix);
            System.out.println("NOTICE_CAPTURE " + suffix + " elapsed="
                    + (SystemClock.uptimeMillis() - shownAt));
        }
        check(hidden.await(6, TimeUnit.SECONDS), "System did not finish " + name);
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
        captureNaturalBlink(face, "eyes-blink-portrait.png");
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
        check(onMain(() -> (Boolean) invoke(face, "canIdleBlink")),
                "Landscape face is not awake/foreground/settled: " + blinkDiagnostic(face));
        captureNaturalBlink(face, "eyes-blink-landscape.png");
        getTargetContext().startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        SystemClock.sleep(500);
        check(onMain(() -> get(face, "idleBlinkAnimator")) == null, "Off-screen blink remains active");
        check(!onMain(() -> face.getHandler().hasCallbacks((Runnable) get(face, "idleBlinkRunnable"))), "Off-screen blink callback remains queued");
    }

    private String blinkDiagnostic(BoopFaceView face) throws Exception {
        return onMain(() -> "awake=" + get(face, "awakeForBlink")
                + ", focused=" + face.hasWindowFocus() + ", attached=" + face.isAttachedToWindow()
                + ", alpha=" + face.getAlpha() + ", foreground=" + get(activity, "activityInForeground")
                + ", thinking=" + get(activity, "thinking") + ", listening=" + get(activity, "listening"));
    }

    private void captureNaturalBlink(BoopFaceView face, String name) throws Exception {
        Runnable scheduled = (Runnable) onMain(() -> get(face, "idleBlinkRunnable"));
        check(scheduled != null, "Natural blink runnable is missing");
        check(onMain(() -> face.getHandler().hasCallbacks(scheduled)),
                "Natural blink callback is not queued: " + blinkDiagnostic(face));
        int[] size = onMain(() -> new int[]{face.getWidth(), face.getHeight()});
        Bitmap image = Bitmap.createBitmap(size[0], size[1], Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        CountDownLatch captured = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        // Prove the production scheduler is naturally queued above, then fire
        // the production animator and capture near its midpoint on the same
        // main looper. This avoids depending on emulator frame dispatch during
        // a blink whose complete close-and-open cycle lasts only 183 ms.
        onMain(() -> {
            invoke(face, "runIdleBlink");
            face.getHandler().postDelayed(() -> {
                try {
                    float openness = (Float) get(face, "idleBlinkOpenness");
                    check(openness < 0.20f,
                            "Production blink did not close at midpoint: openness=" + openness);
                    face.draw(canvas);
                } catch (Throwable error) {
                    failure.set(error);
                } finally {
                    captured.countDown();
                }
            }, 92L);
            return null;
        });
        boolean observed = captured.await(2500, TimeUnit.MILLISECONDS);
        if (failure.get() != null) { image.recycle(); throw new Exception(failure.get()); }
        if (!observed) {
            image.recycle();
            throw new AssertionError("No production blink midpoint: " + name + "; " + blinkDiagnostic(face));
        }
        save(image, name);
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
