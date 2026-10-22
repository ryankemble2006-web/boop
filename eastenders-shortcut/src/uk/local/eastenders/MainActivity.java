package uk.local.eastenders;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private boolean awaitingAccessibility;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        if (state == null && WatchNowService.ready()) {
            openProgramme();
            return;
        }
        showSetupScreen();
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (WatchNowService.ready()) {
            openProgramme();
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (awaitingAccessibility && WatchNowService.ready()) {
            awaitingAccessibility = false;
            openProgramme();
        }
    }

    private void openProgramme() {
        Intent player = new Intent(Intent.ACTION_VIEW, Uri.parse(Routes.TV_URL))
                .setPackage("com.nvidia.bbciplayer")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        if (getPackageManager().resolveActivity(player, 0) != null) {
            WatchNowService.arm("com.nvidia.bbciplayer");
            try {
                startActivity(player);
                finish();
                return;
            } catch (ActivityNotFoundException | SecurityException ignored) { }
        }

        // Fallback for devices running generic BBC iPlayer packages
        boolean launched = Routes.open(
                (pkg, url) -> {
                    Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            .setPackage(pkg)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (getPackageManager().resolveActivity(fallback, 0) == null) return false;
                    WatchNowService.arm(pkg);
                    try {
                        startActivity(fallback);
                        finish();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                },
                () -> {
                    Toast.makeText(this, "BBC iPlayer is not installed.", Toast.LENGTH_LONG).show();
                    return false;
                }
        );

        if (!launched) {
            WatchNowService.cancel();
        }
    }

    private void showSetupScreen() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
        root.setPadding(dp(72), dp(48), dp(72), dp(48));
        root.setBackgroundColor(Color.rgb(18, 22, 28));

        TextView title = new TextView(this);
        title.setText("EastEnders Shortcut");
        title.setTextColor(Color.WHITE);
        title.setTextSize(32);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView body = new TextView(this);
        body.setText("To choose your profile, select the newest episode, and return to Home automatically when finished, turn on 'EastEnders auto-play' in Accessibility settings.\n\nYou can also launch EastEnders right now without auto-play.");
        body.setTextColor(Color.rgb(200, 210, 220));
        body.setTextSize(19);
        body.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams bodyParams = new LinearLayout.LayoutParams(dp(700), ViewGroup.LayoutParams.WRAP_CONTENT);
        bodyParams.topMargin = dp(24);
        bodyParams.bottomMargin = dp(32);
        root.addView(body, bodyParams);

        Button enableButton = makeButton("Enable auto-play", v -> {
            awaitingAccessibility = true;
            try {
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } catch (Exception rejected) {
                try {
                    startActivity(new Intent(Settings.ACTION_SETTINGS));
                } catch (Exception ignored) {
                    Toast.makeText(this, "Open TV Settings > Accessibility to enable.", Toast.LENGTH_LONG).show();
                }
            }
        });
        root.addView(enableButton);

        Button watchButton = makeButton("Watch EastEnders now", v -> openProgramme());
        LinearLayout.LayoutParams watchParams = (LinearLayout.LayoutParams) watchButton.getLayoutParams();
        watchParams.topMargin = dp(14);
        root.addView(watchButton);

        Button closeButton = makeButton("Close", v -> finish());
        LinearLayout.LayoutParams closeParams = (LinearLayout.LayoutParams) closeButton.getLayoutParams();
        closeParams.topMargin = dp(14);
        root.addView(closeButton);

        setContentView(root);
        enableButton.requestFocus();
    }

    private Button makeButton(String text, View.OnClickListener onClick) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(18);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.setOnClickListener(onClick);

        button.setOnFocusChangeListener((v, focused) -> {
            GradientDrawable d = new GradientDrawable();
            d.setColor(focused ? Color.rgb(220, 30, 70) : Color.rgb(40, 50, 65));
            d.setCornerRadius(dp(8));
            d.setStroke(dp(focused ? 3 : 1), focused ? Color.WHITE : Color.rgb(80, 95, 115));
            v.setBackground(d);
        });

        GradientDrawable normal = new GradientDrawable();
        normal.setColor(Color.rgb(40, 50, 65));
        normal.setCornerRadius(dp(8));
        normal.setStroke(dp(1), Color.rgb(80, 95, 115));
        button.setBackground(normal);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(420), dp(56));
        button.setLayoutParams(lp);
        return button;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
