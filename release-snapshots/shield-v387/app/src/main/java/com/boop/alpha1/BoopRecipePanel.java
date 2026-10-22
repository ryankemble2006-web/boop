package com.boop.alpha1;

import android.app.Activity;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.function.Consumer;

/** A view of the existing activity's session; it never owns or starts listening. */
final class BoopRecipePanel {
    private final Activity activity;
    private final FrameLayout parent;
    private final Consumer<String> command;
    private LinearLayout panel;
    private TextView heading, body;
    private boolean priorAwake;
    BoopRecipePanel(Activity activity, FrameLayout parent, Consumer<String> command) {
        this.activity = activity; this.parent = parent; this.command = command;
    }
    void render(BoopRecipeSession session) {
        if (!session.active()) { close(); return; }
        if (panel == null) {
            priorAwake = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) != 0;
            panel = new LinearLayout(activity); panel.setOrientation(LinearLayout.VERTICAL);
            panel.setBackgroundColor(Color.BLACK); panel.setPadding(dp(24), dp(20), dp(24), dp(20));
            panel.setClickable(true);
            heading = text(30, Color.CYAN); panel.addView(heading);
            ScrollView scroll = new ScrollView(activity); scroll.setFillViewport(true);
            body = text(34, Color.WHITE); body.setPadding(0, dp(18), 0, dp(18));
            scroll.addView(body); panel.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
            TextView hint = text(18, Color.LTGRAY);
            hint.setText("Use your usual BOOP wake phrase. Say next step, go back, repeat, ingredients or finish cooking.");
            panel.addView(hint);
            LinearLayout buttons = new LinearLayout(activity);
            for (String[] item : new String[][]{{"Back", "previous step"}, {"Repeat", "repeat that"}, {"Ingredients", "show ingredients"}, {"Next", "next step"}, {"Finish", "finish cooking"}}) {
                Button b = new Button(activity); b.setText(item[0]); b.setTextSize(18);
                b.setMinHeight(dp(64)); b.setOnClickListener(v -> command.accept(item[1]));
                buttons.addView(b, new LinearLayout.LayoutParams(0, -2, 1));
            }
            panel.addView(buttons);
            parent.addView(panel, new FrameLayout.LayoutParams(-1, -1));
        }
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        heading.setText(session.title()); body.setText(session.content());
        panel.bringToFront();
    }
    void close() {
        if (panel == null) return;
        parent.removeView(panel); panel = null;
        if (!priorAwake) activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private int dp(int value) { return Math.round(value * activity.getResources().getDisplayMetrics().density); }
    private TextView text(int size, int color) {
        TextView v = new TextView(activity); v.setTextSize(size); v.setTextColor(color);
        v.setGravity(Gravity.START); return v;
    }
}
