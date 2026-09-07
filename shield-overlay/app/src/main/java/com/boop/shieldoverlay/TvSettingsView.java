package com.boop.shieldoverlay;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public final class TvSettingsView extends LinearLayout {
    private static final int CYAN = Color.rgb(61, 220, 255);
    private static final int PANEL = Color.rgb(22, 22, 24);
    private static final String VOICE_PREFS = "boop_voice";
    private static final String WAKE_NAME_KEY = "wake_name";

    private final AreaInfo room;
    private final ScrollView scroll;
    private final LinearLayout haItems;
    private final SettingCard firstCard;
    private final MediaPuppetState puppetState;
    private final DeezerPuppetSettingsModel puppetModel;
    private SettingCard wakeNameCard;
    private SettingCard puppetToggle;
    private SettingCard puppetAccess;
    private Runnable unsubscribePuppet;
    private Runnable unsubscribeDashboard;
    private AlertDialog enableDialog;
    private AlertDialog wakeNameDialog;

    public TvSettingsView(Context c, AreaInfo room, Runnable changeRoom, Runnable left) {
        this(c, room, changeRoom, left, null, null);
    }

    public TvSettingsView(Context c, AreaInfo room, Runnable changeRoom, Runnable left,
            MediaPuppetState puppetState, DeezerPuppetSettingsModel.Actions puppetActions) {
        super(c);
        this.room = room;
        this.puppetState = puppetState;
        this.puppetModel = puppetActions == null ? null : new DeezerPuppetSettingsModel(puppetActions);
        setOrientation(VERTICAL);
        setBackgroundColor(Color.BLACK);

        scroll = new ScrollView(c);
        scroll.setFillViewport(true);
        scroll.setFocusable(false);
        LinearLayout content = new LinearLayout(c);
        content.setOrientation(VERTICAL);
        content.setPadding(dp(42), dp(32), dp(54), dp(46));
        scroll.addView(content, new ScrollView.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        addView(scroll, new LayoutParams(MATCH_PARENT, MATCH_PARENT));

        content.addView(text("BOOP SETTINGS", 42, Color.WHITE, true));
        TextView where = text(room == null ? "Choose where this BOOP lives" : room.name(), 22,
                Color.rgb(176, 176, 184), false);
        LayoutParams whereParams = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        whereParams.bottomMargin = dp(26);
        content.addView(where, whereParams);

        content.addView(section("HOME ASSISTANT"));
        content.addView(info("House", "Room-scoped"), spaced());
        firstCard = card("Room", room == null ? "Not set" : room.name(),
                room == null ? "Choose a room before controls are shown"
                        : "Only " + room.name() + " items are shown", left);
        firstCard.setOnClickListener(v -> { if (changeRoom != null) changeRoom.run(); });
        content.addView(firstCard, spaced());
        haItems = new LinearLayout(c);
        haItems.setOrientation(VERTICAL);
        content.addView(haItems, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
        renderDashboard(HomeDashboardStateBus.latest(room));

        content.addView(section("VOICE"));
        wakeNameCard = card("BOOP's name", wakeName(),
                "Spoken wake name only. BOOP always works too.", left);
        wakeNameCard.setOnClickListener(v -> showWakeNameDialog());
        content.addView(wakeNameCard, spaced());

        if (puppetState != null && puppetModel != null) {
            content.addView(section("PUPPET"));
            puppetToggle = card("Deezer headphones", "Off",
                    "Let BOOP react to Deezer playback", left);
            puppetToggle.setOnClickListener(v -> {
                if (enableDialog == null && puppetModel.toggle(puppetState.snapshot())) showEnable();
            });
            content.addView(puppetToggle, spaced());
            puppetAccess = card("Deezer access", "Check access",
                    "Used only for Deezer playback state", left);
            puppetAccess.setOnClickListener(v -> {
                puppetModel.manageAccess();
                renderPuppet(puppetState.snapshot());
            });
            content.addView(puppetAccess, spaced());
            renderPuppet(puppetState.snapshot());
        }
    }

    void renderDashboard(HomeDashboardController.ViewState state) {
        haItems.removeAllViews();
        if (state == null) {
            haItems.addView(info("Room controls", "Loading"), spaced());
            return;
        }
        List<EntityCard> cards = state.cards() == null ? Collections.emptyList() : state.cards();
        if (cards.isEmpty()) {
            haItems.addView(info("Room controls", state.message() == null ? "None confirmed" : "Hidden safely"), spaced());
            return;
        }
        for (EntityCard card : cards) {
            if (card != null) haItems.addView(info(card.displayName(), friendly(card.state())), spaced());
        }
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (puppetState != null && puppetModel != null && unsubscribePuppet == null)
            unsubscribePuppet = puppetState.subscribe(this::renderPuppet);
        if (room != null && unsubscribeDashboard == null)
            unsubscribeDashboard = HomeDashboardStateBus.subscribe(room, state -> post(() -> renderDashboard(state)));
    }

    @Override protected void onDetachedFromWindow() {
        close();
        super.onDetachedFromWindow();
    }

    void close() {
        if (unsubscribePuppet != null) { unsubscribePuppet.run(); unsubscribePuppet = null; }
        if (unsubscribeDashboard != null) { unsubscribeDashboard.run(); unsubscribeDashboard = null; }
        if (puppetModel != null) puppetModel.cancelEnable();
        if (enableDialog != null) { enableDialog.dismiss(); enableDialog = null; }
        if (wakeNameDialog != null) { wakeNameDialog.dismiss(); wakeNameDialog = null; }
    }

    public View firstFocusable() { return firstCard; }

    private void renderPuppet(MediaPuppetState.Snapshot s) {
        if (s == null || puppetToggle == null || puppetAccess == null) return;
        puppetToggle.value(s.status());
        puppetToggle.detail(puppetModel.explanation(s));
        puppetAccess.value(s.granted ? "Ready" : "Access needed");
    }

    private void showWakeNameDialog() {
        if (wakeNameDialog != null) return;
        EditText input = new EditText(getContext());
        input.setSingleLine(true);
        input.setText(wakeName());
        input.setSelectAllOnFocus(true);
        input.setTextSize(24);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setPadding(dp(24), dp(18), dp(24), dp(18));
        wakeNameDialog = new AlertDialog.Builder(getContext())
                .setTitle("BOOP's name")
                .setMessage("This changes only the spoken wake name. BOOP always stays available.")
                .setView(input)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (d, w) -> saveWakeName(input.getText().toString()))
                .create();
        wakeNameDialog.setOnDismissListener(d -> wakeNameDialog = null);
        wakeNameDialog.show();
        input.requestFocus();
    }

    private String wakeName() {
        SharedPreferences p = getContext().getSharedPreferences(VOICE_PREFS, Context.MODE_PRIVATE);
        return normalizeName(p.getString(WAKE_NAME_KEY, "BOOP"));
    }

    private void saveWakeName(String raw) {
        String value = normalizeName(raw);
        getContext().getSharedPreferences(VOICE_PREFS, Context.MODE_PRIVATE).edit()
                .putString(WAKE_NAME_KEY, value).apply();
        wakeNameCard.value(value);
    }

    private static String normalizeName(String raw) {
        if (raw == null) return "BOOP";
        String v = raw.trim().replaceAll("\\s+", " ");
        if (v.isEmpty()) return "BOOP";
        if (v.length() > 48) v = v.substring(0, 48).trim();
        return v.isEmpty() ? "BOOP" : v;
    }

    private void showEnable() {
        enableDialog = new AlertDialog.Builder(getContext())
                .setTitle("Enable Deezer headphones?")
                .setMessage("BOOP ignores notification contents and watches only Deezer playback. Android access is granted separately in the next screen.")
                .setNegativeButton("Cancel", (d, w) -> puppetModel.cancelEnable())
                .setPositiveButton("Enable", (d, w) -> puppetModel.confirmEnable())
                .create();
        enableDialog.setOnDismissListener(d -> { puppetModel.cancelEnable(); enableDialog = null; });
        enableDialog.show();
        enableDialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();
    }

    private SettingCard card(String title, String value, String detail, Runnable left) {
        SettingCard c = new SettingCard(getContext()).title(title).value(value).detail(detail);
        c.setOnKeyListener((v, key, e) -> {
            if (e.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_LEFT && left != null) { left.run(); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_DOWN) return move(v, View.FOCUS_DOWN);
            if (key == KeyEvent.KEYCODE_DPAD_UP) return move(v, View.FOCUS_UP);
            return false;
        });
        return c;
    }

    private boolean move(View from, int direction) {
        View next = from.focusSearch(direction);
        if (next != null && next != from && inside(next)) { next.requestFocus(); return true; }
        int sign = direction == View.FOCUS_DOWN ? 1 : -1;
        if (!scroll.canScrollVertically(sign)) return false;
        scroll.smoothScrollBy(0, sign * Math.max(dp(110), scroll.getHeight() * 2 / 3));
        return true;
    }

    private boolean inside(View v) {
        View x = v;
        while (x != null) {
            if (x == scroll) return true;
            if (!(x.getParent() instanceof View)) return false;
            x = (View) x.getParent();
        }
        return false;
    }

    private View info(String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(30), dp(20), dp(30), dp(20));
        row.setMinimumHeight(dp(82));
        row.setBackground(round(PANEL, Color.rgb(52, 52, 56), 1));
        TextView l = text(label, 23, Color.WHITE, true);
        TextView r = text(value, 21, CYAN, true);
        row.addView(l, new LayoutParams(0, WRAP_CONTENT, 1));
        row.addView(r, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
        return row;
    }

    private TextView section(String s) {
        TextView v = text(s, 18, CYAN, true);
        v.setLetterSpacing(.1f);
        LayoutParams p = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        p.topMargin = dp(14); p.bottomMargin = dp(12); v.setLayoutParams(p);
        return v;
    }

    private TextView text(String s, float size, int color, boolean bold) {
        TextView v = new TextView(getContext());
        v.setText(s); v.setTextSize(size); v.setTextColor(color); v.setGravity(Gravity.START);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    private LayoutParams spaced() {
        LayoutParams p = new LayoutParams(MATCH_PARENT, WRAP_CONTENT);
        p.bottomMargin = dp(16); return p;
    }

    private GradientDrawable round(int fill, int stroke, int width) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(fill); d.setCornerRadius(dp(22)); d.setStroke(dp(width), stroke); return d;
    }

    private static String friendly(String state) {
        if (state == null || state.isBlank()) return "Unknown";
        if ("on".equalsIgnoreCase(state)) return "On";
        if ("off".equalsIgnoreCase(state)) return "Off";
        String s = state.trim(); return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private int dp(int n) { return Math.max(1, Math.round(n * getResources().getDisplayMetrics().density)); }

    private final class SettingCard extends LinearLayout {
        private final TextView title;
        private final TextView value;
        private final TextView detail;
        SettingCard(Context c) {
            super(c); setOrientation(VERTICAL); setGravity(Gravity.CENTER_VERTICAL);
            setPadding(dp(30), dp(20), dp(30), dp(20)); setMinimumHeight(dp(108));
            setFocusable(true); setClickable(true); setFocusableInTouchMode(false); setStateListAnimator(null);
            LinearLayout top = new LinearLayout(c); top.setOrientation(HORIZONTAL); top.setGravity(Gravity.CENTER_VERTICAL);
            title = text("", 27, Color.WHITE, true); value = text("", 23, CYAN, true);
            top.addView(title, new LayoutParams(0, WRAP_CONTENT, 1)); top.addView(value, new LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
            addView(top, new LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            detail = text("", 20, Color.rgb(170,170,178), false);
            LayoutParams p = new LayoutParams(MATCH_PARENT, WRAP_CONTENT); p.topMargin = dp(8); addView(detail, p);
            focus(false); setOnFocusChangeListener((v, f) -> focus(f));
        }
        SettingCard title(String s) { title.setText(s); return this; }
        SettingCard value(String s) { value.setText(s); return this; }
        SettingCard detail(String s) { detail.setText(s); return this; }
        @Override public boolean onKeyDown(int key, KeyEvent e) {
            if (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER || key == KeyEvent.KEYCODE_NUMPAD_ENTER) return performClick();
            return super.onKeyDown(key, e);
        }
        private void focus(boolean f) {
            setBackground(f ? round(CYAN, Color.WHITE, 2) : round(PANEL, Color.rgb(58,58,64), 1));
            title.setTextColor(f ? Color.BLACK : Color.WHITE);
            value.setTextColor(f ? Color.BLACK : CYAN);
            detail.setTextColor(f ? Color.rgb(18,40,44) : Color.rgb(170,170,178));
            setTranslationZ(f ? dp(6) : 0);
        }
    }
}
