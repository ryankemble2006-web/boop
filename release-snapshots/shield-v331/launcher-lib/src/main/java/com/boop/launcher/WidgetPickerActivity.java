package com.boop.launcher;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import java.text.Collator;
import java.util.ArrayList;

/** A full-screen chooser; WidgetController still owns binding and configuration. */
public final class WidgetPickerActivity extends Activity {
    private final ArrayList<Choice> choices = new ArrayList<>();
    private int widgetId;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        if (widgetId < 0) { finish(); return; }
        EdgeToEdge.apply(this);

        PackageManager pm = getPackageManager();
        for (AppWidgetProviderInfo provider : AppWidgetManager.getInstance(this).getInstalledProviders()) {
            String label = provider.loadLabel(pm);
            String appLabel = provider.provider.getPackageName();
            try {
                appLabel = pm.getApplicationLabel(pm.getApplicationInfo(appLabel, 0)).toString();
            } catch (PackageManager.NameNotFoundException ignored) { }
            choices.add(new Choice(provider, label == null || label.isEmpty() ? appLabel : label, appLabel));
        }
        Collator collator = Collator.getInstance();
        choices.sort((a, b) -> {
            int order = collator.compare(a.label, b.label);
            return order == 0 ? collator.compare(a.appLabel, b.appLabel) : order;
        });

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.BLACK);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int left = 0, top = 0, right = 0, bottom = 0;
            if (Build.VERSION.SDK_INT >= 30) {
                Insets safe = insets.getInsetsIgnoringVisibility(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                left = safe.left; top = safe.top; right = safe.right; bottom = safe.bottom;
            } else {
                left = insets.getSystemWindowInsetLeft();
                top = insets.getSystemWindowInsetTop();
                right = insets.getSystemWindowInsetRight();
                bottom = insets.getSystemWindowInsetBottom();
                DisplayCutout cutout = insets.getDisplayCutout();
                if (cutout != null) {
                    left = Math.max(left, cutout.getSafeInsetLeft());
                    top = Math.max(top, cutout.getSafeInsetTop());
                    right = Math.max(right, cutout.getSafeInsetRight());
                    bottom = Math.max(bottom, cutout.getSafeInsetBottom());
                }
            }
            view.setPadding(left, top, right, bottom);
            return insets;
        });

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(20), dp(12), dp(12), dp(12));
        TextView title = text("Choose widget", 24, Color.WHITE);
        title.setAccessibilityHeading(true);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        Button close = new Button(this);
        close.setText("Close");
        close.setAllCaps(false);
        close.setMinHeight(dp(48));
        close.setOnClickListener(view -> finish());
        LinearLayout.LayoutParams closeParams = new LinearLayout.LayoutParams(-2, -2);
        closeParams.setMarginStart(dp(12));
        header.addView(close, closeParams);
        root.addView(header, new LinearLayout.LayoutParams(-1, -2));

        ListView list = new ListView(this);
        list.setDivider(new ColorDrawable(0xff252525));
        list.setDividerHeight(dp(1));
        list.setClipToPadding(false);
        list.setPadding(0, 0, 0, dp(12));
        list.setAdapter(new BaseAdapter() {
            @Override public int getCount() { return choices.size(); }
            @Override public Object getItem(int position) { return choices.get(position); }
            @Override public long getItemId(int position) { return position; }
            @Override public View getView(int position, View recycled, ViewGroup parent) {
                Choice choice = choices.get(position);
                LinearLayout row = new LinearLayout(WidgetPickerActivity.this);
                row.setGravity(Gravity.CENTER_VERTICAL);
                row.setMinimumHeight(dp(88));
                row.setPadding(dp(20), dp(14), dp(20), dp(14));
                ImageView icon = new ImageView(WidgetPickerActivity.this);
                icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                icon.setImageDrawable(choice.provider.loadIcon(WidgetPickerActivity.this,
                        getResources().getDisplayMetrics().densityDpi));
                row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));
                LinearLayout labels = new LinearLayout(WidgetPickerActivity.this);
                labels.setOrientation(LinearLayout.VERTICAL);
                labels.addView(text(choice.label, 18, Color.WHITE), new LinearLayout.LayoutParams(-1, -2));
                TextView app = text(choice.appLabel, 14, 0xffb8b8b8);
                LinearLayout.LayoutParams appParams = new LinearLayout.LayoutParams(-1, -2);
                appParams.topMargin = dp(4);
                labels.addView(app, appParams);
                LinearLayout.LayoutParams labelsParams = new LinearLayout.LayoutParams(0, -2, 1);
                labelsParams.setMarginStart(dp(16));
                row.addView(labels, labelsParams);
                return row;
            }
        });
        list.setOnItemClickListener((parent, view, position, id) -> {
            Intent result = new Intent()
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, choices.get(position).provider.provider);
            setResult(RESULT_OK, result);
            finish();
        });
        root.addView(list, new LinearLayout.LayoutParams(-1, 0, 1));
        if (choices.isEmpty()) {
            TextView empty = text("No widgets are available from your installed apps.", 18, Color.WHITE);
            empty.setPadding(dp(24), dp(24), dp(24), dp(24));
            root.addView(empty, new LinearLayout.LayoutParams(-1, 0, 1));
            list.setEmptyView(empty);
        }
        setContentView(root);
        root.requestApplyInsets();
    }

    @Override public void onWindowFocusChanged(boolean focused) {
        super.onWindowFocusChanged(focused);
        if (focused) EdgeToEdge.hideBars(this);
    }

    private TextView text(String value, int sp, int color) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextSize(sp);
        text.setTextColor(color);
        return text;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private static final class Choice {
        final AppWidgetProviderInfo provider;
        final String label, appLabel;
        Choice(AppWidgetProviderInfo provider, String label, String appLabel) {
            this.provider = provider; this.label = label; this.appLabel = appLabel;
        }
    }
}
