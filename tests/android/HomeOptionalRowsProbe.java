package local.boop.homeaudit;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/** Emulator-only regression fixture using installed production Home classes and synthetic rows.
 * It has no network permission and uses its own preferences, never the target app's data.
 * Removing vertical focus scrolling must make row focus/visibility assertions fail.
 */
public final class HomeOptionalRowsProbe extends Activity {
    private static final String TAG = "HomeRowsProbe";
    private final Handler handler = new Handler();
    private View home;
    private final List<String> failures = new ArrayList<>();
    private String selected = "";
    private String selectedRoomDevice = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(5894);
        try {
            Context installed = createPackageContext(getIntent().getStringExtra("target_package"),
                    Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            ClassLoader loader = installed.getClassLoader();
            boolean tallRoomPanel = getIntent().getBooleanExtra("tall_room_panel", false);
            Configuration layoutConfiguration = new Configuration(installed.getResources().getConfiguration());
            if (tallRoomPanel) {
                // A taller logical viewport without changing the emulator's display or app settings.
                layoutConfiguration.densityDpi = 160;
                layoutConfiguration.fontScale = 1f;
            }
            Context layoutContext = installed.createConfigurationContext(layoutConfiguration);
            Context isolated = new ContextWrapper(this) {
                @Override public Resources getResources() { return layoutContext.getResources(); }
                @Override public AssetManager getAssets() { return layoutContext.getAssets(); }
                @Override public ClassLoader getClassLoader() { return loader; }
            };
            Class<?> viewType = loader.loadClass("com.boop.shieldhome.ShieldHomeView");
            Class<?> rowType = loader.loadClass("com.boop.shieldhome.HomeRow");
            Class<?> cardType = loader.loadClass("com.boop.shieldhome.HomeContentCard");
            Class<?> callbackType = loader.loadClass("com.boop.shieldhome.ShieldHomeView$Callbacks");
            List<Object> rows = new ArrayList<>();
            for (int i = 1; i <= getIntent().getIntExtra("rows", 2); i++) {
                Object card = cardType.getConstructor(String.class, String.class, String.class)
                        .newInstance("Offline row " + i + " card", "boop-audit://row/" + i, "");
                rows.add(rowType.getConstructor(String.class, List.class)
                        .newInstance("Offline row " + i, List.of(card)));
            }
            Object callbacks = Proxy.newProxyInstance(loader, new Class<?>[]{callbackType}, (p, m, args) -> {
                if (m.getName().equals("onContentSelected")) {
                    selected = String.valueOf(cardType.getMethod("title").invoke(args[0]));
                    Log.i(TAG, "SELECTED " + selected);
                }
                if (m.getName().equals("onRoomDeviceSelected")) selectedRoomDevice = String.valueOf(args[1]);
                return null;
            });
            home = (View) viewType.getConstructor(Context.class).newInstance(isolated);
            viewType.getMethod("render", List.class, List.class, callbackType)
                    .invoke(home, List.of(), rows, callbacks);
            if (tallRoomPanel) bindOfflineRoomState(loader, viewType);
            setContentView(home);
            handler.postDelayed(() -> {
                View add = find(home, "Add favourites");
                if (add == null) throw new AssertionError("Missing add favourites control");
                add.requestFocus();
                snapshot("initial");
            }, 1200);
        } catch (Throwable failure) {
            Log.e(TAG, "HARNESS ERROR", failure);
            TextView error = new TextView(this);
            error.setText(failure.toString()); setContentView(error);
        }
    }

    private void bindOfflineRoomState(ClassLoader loader, Class<?> viewType) throws ReflectiveOperationException {
        Class<?> areaType = loader.loadClass("com.boop.shieldoverlay.AreaInfo");
        Class<?> entityType = loader.loadClass("com.boop.shieldoverlay.EntityCard");
        Class<?> phaseType = loader.loadClass("com.boop.shieldoverlay.RoomPanelController$Phase");
        Class<?> stateType = loader.loadClass("com.boop.shieldoverlay.RoomPanelController$State");
        Object room = areaType.getConstructor(String.class, String.class).newInstance("audit_room", "Offline room");
        Object lamp = entityType.getConstructor(String.class, String.class, String.class, String.class,
                boolean.class, String.class).newInstance("light.audit_lamp", "audit_room", "Offline lamp", "off", false, null);
        java.lang.reflect.Constructor<?> stateConstructor = stateType.getDeclaredConstructor(long.class,
                areaType, phaseType, List.class, String.class, String.class);
        stateConstructor.setAccessible(true);
        Object state = stateConstructor.newInstance(1L, room, phaseType.getField("LIVE").get(null), List.of(lamp), null, null);
        viewType.getMethod("setRoomPanelState", stateType, boolean.class).invoke(home, state, true);
    }

    /** Registered against this fixture package so key events traverse Android's real input route. */
    public static final class ProbeInstrumentation extends Instrumentation {
        private Bundle arguments;
        private int headerBottom;
        private int[] headerPixels;
        @Override public void onCreate(Bundle arguments) {
            super.onCreate(arguments); this.arguments = arguments == null ? new Bundle() : arguments; start();
        }
        @Override public void onStart() {
            Bundle result = new Bundle();
            try {
                setInTouchMode(false);
                HomeOptionalRowsProbe activity = (HomeOptionalRowsProbe) startActivitySync(
                        new Intent(getTargetContext(), HomeOptionalRowsProbe.class)
                                .putExtra("target_package", arguments.getString("target_package", "com.boop.shieldoverlay"))
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                android.os.SystemClock.sleep(1500);
                waitForIdleSync();
                runOnMainSync(() -> {
                    find(activity.home, "Add favourites").requestFocus();
                    View apps = find(activity.home, "Apps");
                    int[] xy = new int[2]; apps.getLocationOnScreen(xy);
                    headerBottom = xy[1] + apps.getHeight();
                });
                android.os.SystemClock.sleep(600); waitForIdleSync();
                capture(activity, "initial");
                step(activity, KeyEvent.KEYCODE_DPAD_DOWN, "Offline row 1 card");
                capture(activity, "row1");
                step(activity, KeyEvent.KEYCODE_DPAD_DOWN, "Offline row 2 card");
                capture(activity, "row2");
                sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER); waitForIdleSync();
                runOnMainSync(() -> {
                    if (!"Offline row 2 card".equals(activity.selected))
                        activity.failures.add("D-pad center did not select row 2");
                });
                step(activity, KeyEvent.KEYCODE_DPAD_UP, "Offline row 1 card");
                step(activity, KeyEvent.KEYCODE_DPAD_UP, "Add favourites");
                capture(activity, "returned");
                HomeOptionalRowsProbe tall = (HomeOptionalRowsProbe) startActivitySync(
                        new Intent(getTargetContext(), HomeOptionalRowsProbe.class)
                                .putExtra("target_package", arguments.getString("target_package", "com.boop.shieldoverlay"))
                                .putExtra("tall_room_panel", true).putExtra("rows", 1)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
                android.os.SystemClock.sleep(1800); waitForIdleSync();
                runOnMainSync(() -> tall.checkTallRoomPanel(true));
                step(tall, KeyEvent.KEYCODE_DPAD_DOWN, "Offline row 1 card");
                step(tall, KeyEvent.KEYCODE_DPAD_DOWN, "Offline lamp, Off");
                sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER); waitForIdleSync();
                runOnMainSync(() -> {
                    if (!"light.audit_lamp".equals(tall.selectedRoomDevice))
                        tall.failures.add("D-pad center did not select the offline room device");
                });
                headerPixels = null;
                capture(tall, "tall-room");
                step(tall, KeyEvent.KEYCODE_DPAD_UP, "Offline row 1 card");
                step(tall, KeyEvent.KEYCODE_DPAD_UP, "Add favourites");
                activity.failures.addAll(tall.failures);
                HomeOptionalRowsProbe noRows = (HomeOptionalRowsProbe) startActivitySync(
                        new Intent(getTargetContext(), HomeOptionalRowsProbe.class)
                                .putExtra("target_package", arguments.getString("target_package", "com.boop.shieldoverlay"))
                                .putExtra("tall_room_panel", true).putExtra("rows", 0)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK));
                android.os.SystemClock.sleep(1800); waitForIdleSync();
                runOnMainSync(() -> noRows.checkTallRoomPanel(false));
                step(noRows, KeyEvent.KEYCODE_DPAD_DOWN, "Offline lamp, Off");
                headerPixels = null;
                capture(noRows, "tall-no-rows");
                step(noRows, KeyEvent.KEYCODE_DPAD_UP, "Add favourites");
                activity.failures.addAll(noRows.failures);
                String verdict = activity.failures.isEmpty()
                        ? "PASS optional rows focus, visibility, selection and return; tall room panel visibility and action"
                        : "FAIL " + activity.failures;
                result.putString("stream", verdict + "\n"); Log.i(TAG, verdict);
                finish(activity.failures.isEmpty() ? Activity.RESULT_OK : Activity.RESULT_CANCELED, result);
            } catch (Throwable failure) {
                Log.e(TAG, "HARNESS ERROR", failure);
                result.putString("stream", "HARNESS ERROR " + failure + "\n");
                finish(Activity.RESULT_CANCELED, result);
            }
        }
        private void step(HomeOptionalRowsProbe activity, int key, String expected) {
            sendKeyDownUpSync(key);
            android.os.SystemClock.sleep(600); waitForIdleSync();
            runOnMainSync(() -> { activity.checkFocusedVisible(expected); activity.snapshot(expected); });
        }
        private void capture(HomeOptionalRowsProbe activity, String name) throws java.io.IOException {
            Bitmap screenshot = getUiAutomation().takeScreenshot();
            if (screenshot == null) throw new java.io.IOException("Screenshot unavailable");
            int[] current = new int[screenshot.getWidth() * headerBottom];
            screenshot.getPixels(current, 0, screenshot.getWidth(), 0, 0, screenshot.getWidth(), headerBottom);
            if (headerPixels == null) headerPixels = current;
            else if (headerChanged(headerPixels, current))
                activity.failures.add(name + " scrolled content over fixed navigation");
            try (java.io.FileOutputStream output = new java.io.FileOutputStream(
                    new java.io.File(getTargetContext().getExternalFilesDir(null), name + ".png"))) {
                screenshot.compress(Bitmap.CompressFormat.PNG, 100, output);
            } finally { screenshot.recycle(); }
        }
        private static boolean headerChanged(int[] before, int[] after) {
            if (before.length != after.length) return true;
            for (int i = 0; i < before.length; i++) {
                // Hardware rendering can round antialiased edges by a few levels.
                for (int shift : new int[]{0, 8, 16}) {
                    if (Math.abs(((before[i] >> shift) & 255) - ((after[i] >> shift) & 255)) > 8)
                        return true;
                }
            }
            return false;
        }
    }

    private void checkTallRoomPanel(boolean expectOptionalRow) {
        View panel = findType(home, "com.boop.shieldhome.ShieldRoomPanelView");
        float density = home.getResources().getDisplayMetrics().density;
        if (home.getHeight() / density < 900) failures.add("Tall room fixture needs at least 900dp height");
        View row = find(home, "Offline row 1 card");
        if (expectOptionalRow && (row == null || !fullyVisible(row)))
            failures.add("Optional row missing in tall room fixture");
        if (panel == null || panel.getVisibility() != View.VISIBLE || panel.getHeight() < 110 * density
                || !fullyVisible(panel)) failures.add("Room panel hidden or clipped despite spare vertical space");
        if (expectOptionalRow && panel != null && row != null) {
            int[] panelPosition = new int[2], rowPosition = new int[2];
            panel.getLocationOnScreen(panelPosition); row.getLocationOnScreen(rowPosition);
            if (panelPosition[1] < rowPosition[1] + row.getHeight())
                failures.add("Room panel overlaps the optional row");
        }
        View device = find(home, "Offline lamp, Off");
        if (device == null || !device.isShown() || !fullyVisible(device))
            failures.add("Offline room device is hidden or clipped");
    }

    private static View findType(View root, String name) {
        if (root == null) return null;
        if (name.equals(root.getClass().getName())) return root;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i = 0; i < group.getChildCount(); i++) {
                View found = findType(group.getChildAt(i), name); if (found != null) return found;
            }
        }
        return null;
    }

    private void checkFocusedVisible(String label) {
        View target = find(home, label);
        if (target == null || !target.hasFocus()) failures.add(label + " did not receive D-pad focus");
        if (target == null || !fullyVisible(target)) failures.add(label + " is clipped or outside viewport");
    }

    private boolean fullyVisible(View target) {
        int[] position = new int[2]; target.getLocationOnScreen(position);
        Rect bounds = new Rect(position[0], position[1],
                position[0] + Math.round(target.getWidth() * target.getScaleX()),
                position[1] + Math.round(target.getHeight() * target.getScaleY()));
        int[] root = new int[2]; home.getLocationOnScreen(root);
        Rect viewport = new Rect(root[0], root[1], root[0] + home.getWidth(), root[1] + home.getHeight());
        Rect visible = new Rect();
        return target.getGlobalVisibleRect(visible) && viewport.contains(bounds) && visible.contains(bounds);
    }

    private void snapshot(String phase) {
        Log.i(TAG, phase + " focus=" + label(home.findFocus()));
        for (String name : new String[]{"Add favourites", "Offline row 1 card", "Offline row 2 card"}) {
            View item = find(home, name);
            if (item != null) {
                int[] xy = new int[2]; item.getLocationOnScreen(xy);
                Log.i(TAG, phase + " " + name + " bounds=" + xy[0] + "," + xy[1] + ","
                        + (xy[0] + item.getWidth()) + "," + (xy[1] + item.getHeight())
                        + " fullyVisible=" + fullyVisible(item));
            }
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        if (event.getAction() == KeyEvent.ACTION_UP && home != null)
            handler.postDelayed(() -> snapshot("key" + event.getKeyCode()), 450);
        return result;
    }

    private static String label(View view) {
        if (view == null) return "none";
        if (view.getContentDescription() != null) return view.getContentDescription().toString();
        return view instanceof TextView ? ((TextView)view).getText().toString() : view.getClass().getSimpleName();
    }

    private static View find(View root, String value) {
        if (value.equals(label(root))) return root;
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            for (int i=0; i<group.getChildCount(); i++) {
                View found = find(group.getChildAt(i), value); if (found != null) return found;
            }
        }
        return null;
    }
}
