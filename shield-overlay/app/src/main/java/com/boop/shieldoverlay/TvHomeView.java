package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Room and working devices only. State updates retain the selected remote control. */
public final class TvHomeView extends ScrollView {
    private static final int CYAN = Color.rgb(61, 220, 255);
    private static final int PANEL = Color.rgb(22, 22, 24);
    private final LinearLayout devices;
    private final DeviceRow roomCard;
    private final TextView status;
    private final Runnable onContentLeft;
    private final List<View> focusOrder = new ArrayList<>();
    private final Map<String, DeviceRow> rows = new LinkedHashMap<>();
    private HomeDashboardController.ViewState currentState;

    public TvHomeView(Context context, AreaInfo room, Runnable onContentLeft, Runnable changeRoom) {
        super(context);
        this.onContentLeft = onContentLeft;
        setFillViewport(true);
        setSmoothScrollingEnabled(true);
        setBackgroundColor(Color.BLACK);
        setFocusable(false);
        setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(42), dp(32), dp(54), dp(46));
        addView(content, new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        content.addView(text("BOOP HOME", 42, Color.WHITE));
        content.addView(section("ROOM"));
        roomCard = new DeviceRow();
        roomCard.setLabels("Room", room == null ? "Choose a room" : room.name());
        roomCard.setOnClickListener(v -> { if (changeRoom != null) changeRoom.run(); });
        wireNavigation(roomCard);
        focusOrder.add(roomCard);
        content.addView(roomCard, rowParams());
        content.addView(section("DEVICES"));
        devices = new LinearLayout(context);
        devices.setOrientation(LinearLayout.VERTICAL);
        content.addView(devices, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        status = text(room == null ? "Choose a room before devices are shown." : "Finding this room's devices…", 20, Color.LTGRAY);
        status.setPadding(0, dp(8), 0, dp(20));
        content.addView(status);
    }

    public View firstFocusable() { return roomCard; }

    public void render(HomeDashboardController.ViewState state) {
        android.util.Log.i("BOOP-Control", "render enabled=" + (state != null && state.actionsEnabled()));
        currentState = state;
        List<EntityCard> cards = state == null ? Collections.emptyList() : state.cards();
        List<String> wanted = new ArrayList<>();
        for (EntityCard card : cards) if (card != null) wanted.add(card.entityId());
        if (!new ArrayList<>(rows.keySet()).equals(wanted)) rebuild(cards);
        for (EntityCard card : cards) {
            if (card == null) continue;
            DeviceRow row = rows.get(card.entityId());
            row.setLabels(card.displayName(), "on".equals(card.state()) ? "On" : "Off");
            row.setAlpha(state.actionsEnabled() ? 1f : .72f);
            // Do not disable/recreate the view while confirmation is pending: that loses D-pad focus.
        }
        if (state == null) status.setText("Connecting to Home Assistant…");
        else if (state.message() != null) status.setText(state.message());
        else if (cards.isEmpty()) status.setText("No controllable devices found in this room.");
        else if (state.stale()) status.setText("Home Assistant is unavailable.");
        else if (!state.actionsEnabled()) status.setText("Waiting for Home Assistant to confirm…");
        else status.setText("Select a device to switch it on or off.");
    }

    private void rebuild(List<EntityCard> cards) {
        String selected = null;
        boolean hadDeviceFocus = false;
        for (Map.Entry<String, DeviceRow> entry : rows.entrySet()) {
            if (entry.getValue().hasFocus()) { selected = entry.getKey(); hadDeviceFocus = true; break; }
        }
        devices.removeAllViews();
        rows.clear();
        focusOrder.clear();
        focusOrder.add(roomCard);
        for (EntityCard card : cards) {
            if (card == null || rows.containsKey(card.entityId())) continue;
            final String id = card.entityId();
            DeviceRow row = new DeviceRow();
            row.setOnClickListener(v -> {
                HomeDashboardController.ViewState current = currentState;
                if (current == null || !current.actionsEnabled()) return;
                for (EntityCard live : current.cards()) {
                    if (id.equals(live.entityId())) { current.toggle(live); return; }
                }
            });
            wireNavigation(row);
            rows.put(id, row);
            focusOrder.add(row);
            devices.addView(row, rowParams());
        }
        if (hadDeviceFocus) {
            View restore = rows.get(selected);
            if (restore == null) restore = roomCard;
            restore.requestFocus();
        }
    }

    private void wireNavigation(View row) {
        row.setOnKeyListener((v, key, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_LEFT && onContentLeft != null) {
                onContentLeft.run();
                return true;
            }
            if (key == KeyEvent.KEYCODE_DPAD_UP || key == KeyEvent.KEYCODE_DPAD_DOWN) {
                int index = focusOrder.indexOf(v);
                int next = index + (key == KeyEvent.KEYCODE_DPAD_DOWN ? 1 : -1);
                if (index >= 0 && next >= 0 && next < focusOrder.size()) focusOrder.get(next).requestFocus();
                return true;
            }
            return false;
        });
    }

    private TextView text(String value, float size, int color) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        return view;
    }

    private TextView section(String value) {
        TextView view = text(value, 18, CYAN);
        view.setLetterSpacing(.1f);
        view.setPadding(0, dp(18), 0, dp(12));
        return view;
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        return params;
    }

    private GradientDrawable background(boolean focused) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(focused ? CYAN : PANEL);
        drawable.setCornerRadius(dp(22));
        drawable.setStroke(dp(2), focused ? Color.WHITE : Color.rgb(58, 58, 64));
        return drawable;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private final class DeviceRow extends LinearLayout {
        private final TextView name = text("", 27, Color.WHITE);
        private final TextView value = text("", 23, CYAN);
        private final GradientDrawable idle = background(false);
        private final GradientDrawable focused = background(true);

        DeviceRow() {
            super(TvHomeView.this.getContext());
            setOrientation(HORIZONTAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setMinimumHeight(dp(96));
            setFocusable(true);
            setClickable(true);
            setStateListAnimator(null);
            setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
            addView(name, new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            value.setPadding(dp(20), 0, 0, 0);
            addView(value, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            applyFocus(false);
            setOnFocusChangeListener((v, hasFocus) -> {
                applyFocus(hasFocus);
                if (hasFocus) post(() -> requestRectangleOnScreen(new Rect(0, 0, getWidth(), getHeight()), true));
            });
        }

        void setLabels(String label, String state) {
            name.setText(label);
            value.setText(state);
            setContentDescription(label + ", " + state);
        }

        private void applyFocus(boolean hasFocus) {
            setBackground(hasFocus ? focused : idle);
            setPadding(dp(30), dp(20), dp(30), dp(20));
            name.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
            value.setTextColor(hasFocus ? Color.BLACK : CYAN);
        }
    }
}
