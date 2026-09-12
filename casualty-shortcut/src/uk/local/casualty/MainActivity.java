package uk.local.casualty;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.LinkedHashMap;

public final class MainActivity extends Activity {
    private TextView status;
    private Button browserButton;
    private boolean leftActivity;
    private boolean enabling;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingDeepLink;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(48), dp(24), dp(48), dp(24));
        root.setBackgroundColor(Color.rgb(12, 20, 30));
        TextView title = new TextView(this);
        title.setText("Casualty");
        title.setTextSize(42);
        title.setTextColor(Color.WHITE);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title);
        status = new TextView(this);
        status.setTextSize(20);
        status.setTextColor(Color.rgb(208, 220, 230));
        status.setGravity(Gravity.CENTER);
        status.setPadding(0, dp(16), 0, dp(20));
        root.addView(status, new LinearLayout.LayoutParams(-1, dp(100)));
        Button retry = button(root, "Watch Casualty", v -> openProgramme());
        browserButton = button(root, "Open in browser", v -> openBrowser());
        Button setup = button(root, WatchNowService.ready() ? "Auto-play settings" : "Enable auto-play", v -> {
            WatchNowService.cancel();
            enabling = true;
            if (!launch(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))) {
                enabling = false;
                status.setText("Open Shield Settings, then Accessibility, and enable Casualty auto-play.");
            }
        });
        button(root, "Close", v -> finish());
        setContentView(root);
        status.setText("If iPlayer did not open Casualty, try the browser.");
        retry.requestFocus();
        if (state == null) openProgramme();
    }

    @Override protected void onPause() { super.onPause(); leftActivity = true; }
    @Override protected void onResume() {
        super.onResume();
        if (leftActivity) {
            leftActivity = false;
            if (pendingDeepLink != null) {
                handler.removeCallbacks(pendingDeepLink);
                pendingDeepLink = null;
            }
            WatchNowService.cancel();
            if (enabling) {
                enabling = false;
                if (WatchNowService.ready()) openProgramme();
            }
        }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        leftActivity = false;
        openProgramme();
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private Button button(LinearLayout parent, String text, View.OnClickListener click) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(20);
        button.setAllCaps(false);
        button.setFocusableInTouchMode(true);
        button.setTextColor(Color.WHITE);
        button.setOnClickListener(click);
        button.setOnFocusChangeListener((v, focused) -> {
            GradientDrawable background = new GradientDrawable();
            background.setColor(focused ? Color.rgb(0, 85, 112) : Color.rgb(35, 48, 61));
            background.setCornerRadius(dp(10));
            background.setStroke(dp(focused ? 3 : 1), focused ? Color.WHITE : Color.rgb(66, 82, 98));
            v.setBackground(background);
        });
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(35, 48, 61));
        background.setCornerRadius(dp(10));
        button.setBackground(background);
        LinearLayout.LayoutParams size = new LinearLayout.LayoutParams(dp(400), dp(58));
        size.setMargins(0, dp(5), 0, dp(5));
        parent.addView(button, size);
        return button;
    }

    private void openProgramme() {
        WatchNowService.cancel();
        status.setText(WatchNowService.ready()
                ? "Opening Casualty. If iPlayer ignores the link, press Back and try the browser."
                : "Opening Casualty. To select the existing iPlayer profile and newest episode automatically, choose Enable auto-play and enable Casualty auto-play in Accessibility settings.");
        Routes.open(this::openApp, this::openBrowser);
        browserButton.requestFocus();
    }

    private boolean openApp(String packageName, String url) {
        Intent deepLink = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(packageName);
        deepLink.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (getPackageManager().resolveActivity(deepLink, 0) == null) return false;
        WatchNowService.arm(packageName);
        long delay = LaunchPolicy.prewarmDelayMs(packageName);
        Intent warm = getPackageManager().getLeanbackLaunchIntentForPackage(packageName);
        if (delay > 0 && warm != null && launch(warm)) {
            pendingDeepLink = () -> {
                pendingDeepLink = null;
                if (!launch(deepLink)) WatchNowService.cancel();
            };
            handler.postDelayed(pendingDeepLink, delay);
            return true;
        }
        boolean launched = launch(deepLink);
        if (!launched) WatchNowService.cancel();
        return launched;
    }

    private boolean launch(Intent intent) {
        try { startActivity(intent); return true; }
        catch (ActivityNotFoundException | SecurityException rejected) { return false; }
    }

    private boolean openBrowser() {
        WatchNowService.cancel();
        // Query generic HTTPS handlers, then explicitly target them to avoid app-link loops.
        Intent query = new Intent(Intent.ACTION_VIEW, Uri.parse("https://example.org/"));
        query.addCategory(Intent.CATEGORY_BROWSABLE);
        LinkedHashMap<String, ResolveInfo> browsers = new LinkedHashMap<>();
        ResolveInfo preferred = getPackageManager().resolveActivity(query, 0);
        if (preferred != null) browsers.put(preferred.activityInfo.packageName, preferred);
        for (ResolveInfo info : getPackageManager().queryIntentActivities(query, 0)) {
            browsers.put(info.activityInfo.packageName, info);
        }
        for (ResolveInfo info : browsers.values()) {
            String pkg = info.activityInfo.packageName;
            if (pkg.equals("android") || pkg.equals(getPackageName()) || pkg.contains("frameworkpackagestubs")
                    || pkg.equals("com.nvidia.bbciplayer") || pkg.equals("bbc.iplayer.android")
                    || pkg.equals("uk.co.bbc.iplayer") || !info.activityInfo.exported) continue;
            Intent web = new Intent(Intent.ACTION_VIEW, Uri.parse(Routes.WEB_URL));
            web.addCategory(Intent.CATEGORY_BROWSABLE);
            web.setComponent(new ComponentName(pkg, info.activityInfo.name));
            web.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (launch(web)) return true;
        }
        status.setText("No browser is available. Install a TV browser, then choose Open in browser. You can also retry iPlayer.");
        return false;
    }
}
