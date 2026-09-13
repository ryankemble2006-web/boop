package com.boop.lyricstest;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.json.JSONArray;
import org.json.JSONObject;

/** LOCAL execution only. Exercises the exact installed production APK through real Android sessions. */
public final class LyricsRuntimeTest extends Instrumentation {
    private Activity activity;
    private int checks;
    private Bundle arguments;
    private interface Op { void run() throws Exception; }
    private static final class State {
        String recording = "", document = "", title = "", cue = "", status = "", elapsed = "";
        boolean playing, focused;
    }
    @Override public void onCreate(Bundle arguments) { super.onCreate(arguments); this.arguments = arguments; start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            EmulatorOnly.require();
            if (!"com.boop.lyricslab".equals(getTargetContext().getPackageName()))
                throw new AssertionError("Only the isolated Lyrics Lab may be instrumented");
            ComponentName listener = new ComponentName("com.boop.lyricslab", "com.boop.shieldhome.LyricsLabMediaListener");
            if (!getTargetContext().getSystemService(NotificationManager.class).isNotificationListenerAccessGranted(listener))
                throw new AssertionError("Enable the lab's local-emulator notification access in Android first");
            main(this::seed);
            command("configure", 0, 0);
            SystemClock.sleep(150);
            launch();
            track(0, "Initial document displayed by real lab activity");
            await("Initial current lyric follows the clock", () -> inspect().cue.equals("Local one, opening cue"));
            click(4);
            await("Old lyrics clear during next-track gap", () -> inspect().document.isEmpty());
            track(1, "Next track replaces lyrics without exiting");
            click(0);
            track(0, "Previous track replaces lyrics without exiting");
            for (int transition : new int[]{0,1,7,9,10,11,4,5,-1,6,8}) {
                int index = transition == 0 ? 1 : transition == 1 ? 2 : Math.floorMod(transition, 3);
                command("transition", index, transition);
                SystemClock.sleep(150);
                track(index, "Real Binder updates survive state " + transition);
            }
            command("configure", 0, 0);
            track(0, "Return to controlled paused recording");
            await("Paused clock shows starting position", () -> inspect().elapsed.equals("0:01"));
            SystemClock.sleep(1200);
            await("Paused clock does not advance", () -> inspect().elapsed.equals("0:01"));
            click(2);
            await("Play callback updates production controls", () -> inspect().playing);
            SystemClock.sleep(1300);
            click(2);
            await("Pause callback updates production controls", () -> !inspect().playing);
            String frozen = inspect().elapsed;
            SystemClock.sleep(1100);
            await("Resumed then paused clock remains fixed", () -> inspect().elapsed.equals(frozen));
            command("configure", 0, 0);
            track(0, "Seek fixture ready");
            await("Seek begins at one second", () -> inspect().elapsed.equals("0:01"));
            focus(2);
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            await("Remote forward-ten control reaches media session", () -> inspect().elapsed.equals("0:11"));
            await("Forward seek updates highlighted lyric", () -> inspect().cue.equals("Local one, middle cue"));
            focus(2);
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
            await("Remote back-ten control reaches media session", () -> inspect().elapsed.equals("0:01"));
            await("Backward seek restores earlier lyric", () -> inspect().cue.equals("Local one, opening cue"));
            focus(2);
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
            await("Progress-bar Right seeks ten seconds", () -> inspect().elapsed.equals("0:11"));
            sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
            await("Progress-bar Down returns to Play/Pause", () -> {
                final boolean[] focused = {false};
                main(() -> focused[0] = buttons()[2].hasFocus());
                return focused[0];
            });
            command("missing", 0, 0);
            await("Missing metadata clears the displayed document", () -> inspect().document.isEmpty());
            command("restore", 0, 0);
            track(0, "Metadata recovery reloads automatically");
            command("replace", 2, 0);
            track(2, "Replacement Android session refreshes without reopening");
            for (int i = 0; i < 8; i++) {
                command("transition", i % 3, 0);
                SystemClock.sleep(70);
            }
            track(1, "Rapid changes settle on the latest recording");
            await("Same activity still owns the visible screen", () -> inspect().focused && !activity.isFinishing());
            main(() -> activity.finish());
            await("Leave closes the activity", () -> activity.isDestroyed());
            command("configure", 2, 0);
            SystemClock.sleep(200);
            launch();
            track(2, "Reopening catches the latest recording");
            await("Reopened presentation has the matching current lyric", () -> inspect().cue.equals("Local three, opening cue"));
            if (arguments != null && arguments.getBoolean("hold", false)) SystemClock.sleep(12000);
            result.putString("stream", "\nPASS: " + checks + " local real-session / production-presentation checks. Synthetic provider and words; no Shield touched.\n");
            result.putInt("checksPassed", checks);
            finish(Activity.RESULT_OK, result);
        } catch (Throwable failure) {
            result.putString("stream", "\nFAIL after " + checks + " checks: " + failure + "\n");
            result.putString("failure", failure.toString());
            finish(Activity.RESULT_CANCELED, result);
        }
    }
    private void main(Op operation) throws Exception {
        Throwable[] failure = new Throwable[1];
        runOnMainSync(() -> { try { operation.run(); } catch (Throwable error) { failure[0] = error; } });
        if (failure[0] != null) throw new IllegalStateException(failure[0]);
    }
    private static Object field(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
    private static Object call(Object target, String name) throws Exception {
        Method method = target.getClass().getMethod(name);
        method.setAccessible(true);
        return method.invoke(target);
    }
    private void seed() throws Exception {
        ClassLoader classes = getTargetContext().getClassLoader();
        Class<?> document = Class.forName("com.boop.shieldhome.DeezerLyricsDocument", true, classes);
        Class<?> loader = Class.forName("com.boop.shieldhome.NativeLyricsLoader", true, classes);
        Method remember = loader.getDeclaredMethod("remember", document);
        remember.setAccessible(true);
        String[] names = {"one", "two", "three"};
        for (int i = 0; i < names.length; i++) {
            String id = Integer.toString(990001 + i);
            JSONArray lines = new JSONArray();
            for (int j = 0; j < 3; j++) {
                String label = j == 0 ? "opening" : j == 1 ? "middle" : "last";
                lines.put(new JSONObject().put("line", "Local " + names[i] + ", " + label + " cue")
                        .put("milliseconds", j * 10000).put("duration", 8000));
            }
            JSONObject lyrics = new JSONObject().put("synchronizedLines", lines)
                    .put("synchronizedWordByWordLines", JSONObject.NULL).put("licence", "Invented local regression fixture");
            String body = new JSONObject().put("data", new JSONObject().put("track", new JSONObject().put("id", id).put("lyrics", lyrics))).toString();
            Object parsed = document.getMethod("parse", String.class, String.class).invoke(null, body, id);
            if (!"AVAILABLE".equals(call(parsed, "status").toString())) throw new AssertionError("Invalid invented fixture");
            remember.invoke(null, parsed);
        }
    }
    private void launch() {
        Intent intent = new Intent().setComponent(new ComponentName("com.boop.lyricslab", "com.boop.shieldhome.LyricsLabActivity"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity = startActivitySync(intent);
    }
    private void command(String command, int index, int state) {
        Intent intent = new Intent().setComponent(new ComponentName("deezer.android.app", "com.boop.lyricstest.FixtureService$Commands"))
                .putExtra("command", command).putExtra("index", index).putExtra("state", state)
                .addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        getTargetContext().sendBroadcast(intent);
    }
    private View[] buttons() throws Exception { return (View[]) field(field(activity, "presentation"), "buttons"); }
    private void click(int index) throws Exception {
        main(() -> {
            View button = buttons()[index];
            if (!button.isEnabled() || !button.performClick()) throw new AssertionError("Control unavailable: " + index);
        });
    }
    private void focus(int index) throws Exception {
        main(() -> { if (!buttons()[index].requestFocus()) throw new AssertionError("Control focus unavailable: " + index); });
    }
    private State inspect() throws Exception {
        State state = new State();
        main(() -> {
            Object presentation = field(activity, "presentation");
            View lyrics = (View) field(presentation, "lyrics");
            Object document = field(lyrics, "document");
            Object snapshot = field(presentation, "snapshot");
            Object frame = field(field(activity, "media"), "frame");
            if (frame != null) state.recording = (String) field(frame, "trackId");
            if (document != null) state.document = call(document, "trackId").toString();
            if (snapshot != null) state.playing = (Boolean) call(snapshot, "isPlaying");
            state.title = ((TextView) field(presentation, "title")).getText().toString();
            state.elapsed = ((TextView) field(presentation, "elapsed")).getText().toString();
            state.status = ((TextView) field(presentation, "status")).getText().toString();
            state.cue = lyrics.getContentDescription() == null ? "" : lyrics.getContentDescription().toString();
            state.focused = activity.hasWindowFocus();
        });
        return state;
    }
    private void track(int index, String why) throws Exception {
        String id = Integer.toString(990001 + index);
        await(why, () -> {
            State state = inspect();
            return state.recording.equals(id) && state.document.equals(id) && state.title.equals("Local track " + (index + 1));
        });
    }
    private void await(String why, Callable<Boolean> assertion) throws Exception {
        long deadline = SystemClock.uptimeMillis() + 6000;
        do {
            if (assertion.call()) {
                checks++;
                Bundle status = new Bundle(); status.putString("stream", "PASS " + checks + ": " + why + "\n");
                sendStatus(0, status);
                return;
            }
            SystemClock.sleep(60);
        } while (SystemClock.uptimeMillis() < deadline);
        State actual = activity == null ? new State() : inspect();
        throw new AssertionError(why + " [recording=" + actual.recording + ", document=" + actual.document
                + ", title=" + actual.title + ", status=" + actual.status + "]");
    }
}
