package com.boop.shieldoverlay;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class TvSettingsView extends LinearLayout {
    private static final int CYAN = Color.rgb(61, 220, 255);
    private static final int PANEL = Color.rgb(22, 22, 24);

    private final SettingCard firstCard;
    private final ScrollView scroll;
    private final MediaPuppetState puppetState;
    private final DeezerPuppetSettingsModel puppetModel;
    private SettingCard puppetToggle;
    private SettingCard puppetAccess;
    private Runnable unsubscribePuppet;
    private AlertDialog enableDialog;

    public TvSettingsView(
            Context context,
            AreaInfo selectedRoom,
            Runnable onChangeRoom,
            Runnable onContentLeft) {
        this(context, selectedRoom, onChangeRoom, onContentLeft, null, null);
    }

    public TvSettingsView(
            Context context,
            AreaInfo selectedRoom,
            Runnable onChangeRoom,
            Runnable onContentLeft,
            MediaPuppetState puppetState,
            DeezerPuppetSettingsModel.Actions puppetActions) {
        super(context);
        this.puppetState = puppetState;
        puppetModel = puppetActions == null ? null : new DeezerPuppetSettingsModel(puppetActions);
        setOrientation(VERTICAL);
        setGravity(Gravity.TOP);
        setBackgroundColor(Color.BLACK);

        scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setFocusable(false);
        scroll.setSmoothScrollingEnabled(true);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(VERTICAL);
        content.setPadding(dp(42), dp(34), dp(54), dp(46));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(scroll, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        content.addView(title("BOOP Settings", 44f));
        content.addView(detail(selectedRoom == null
                ? "Choose where this BOOP lives"
                : selectedRoom.name()));

        content.addView(section("HOUSE"));
        content.addView(infoPanel("Home Assistant", "Connected"), cardParams());

        String roomName = selectedRoom == null ? "Not set" : selectedRoom.name();
        firstCard = card(
                "Room",
                roomName,
                selectedRoom == null
                        ? "Choose a room before BOOP shows house controls"
                        : "Only " + selectedRoom.name() + " controls are shown",
                onContentLeft);
        firstCard.setOnClickListener(view -> {
            if (onChangeRoom != null) {
                onChangeRoom.run();
            }
        });
        content.addView(firstCard, cardParams());

        if (puppetState != null && puppetModel != null) {
            content.addView(section("PUPPET"));
            puppetToggle = card(
                    "Deezer headphones",
                    "Off",
                    "Let BOOP react to Deezer playback",
                    onContentLeft);
            puppetToggle.setOnClickListener(view -> {
                if (enableDialog == null && puppetModel.toggle(puppetState.snapshot())) {
                    showEnableConfirmation();
                }
            });
            content.addView(puppetToggle, cardParams());

            puppetAccess = card(
                    "Deezer access",
                    "Check access",
                    "Android access is used only for Deezer playback state",
                    onContentLeft);
            puppetAccess.setOnClickListener(view -> {
                puppetModel.manageAccess();
                renderPuppet(puppetState.snapshot());
            });
            content.addView(puppetAccess, cardParams());
            renderPuppet(puppetState.snapshot());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (puppetState != null && puppetModel != null && unsubscribePuppet == null) {
            unsubscribePuppet = puppetState.subscribe(this::renderPuppet);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        close();
        super.onDetachedFromWindow();
    }

    void close() {
        if (unsubscribePuppet != null) {
            unsubscribePuppet.run();
            unsubscribePuppet = null;
        }
        if (puppetModel != null) {
            puppetModel.cancelEnable();
        }
        if (enableDialog != null) {
            enableDialog.dismiss();
            enableDialog = null;
        }
    }

    private void renderPuppet(MediaPuppetState.Snapshot snapshot) {
        if (snapshot == null || puppetToggle == null || puppetAccess == null) {
            return;
        }
        puppetToggle.value(snapshot.status());
        puppetToggle.detail(puppetModel.explanation(snapshot));
        puppetAccess.value(snapshot.granted ? "Ready" : "Access needed");
    }

    private void showEnableConfirmation() {
        enableDialog = new AlertDialog.Builder(getContext())
                .setTitle("Enable Deezer headphones?")
                .setMessage("BOOP ignores notification contents and watches only Deezer playback. "
                        + "Android access is granted separately in the next screen.")
                .setNegativeButton("Cancel", (dialog, which) -> puppetModel.cancelEnable())
                .setPositiveButton("Enable", (dialog, which) -> puppetModel.confirmEnable())
                .create();
        enableDialog.setOnDismissListener(dialog -> {
            puppetModel.cancelEnable();
            enableDialog = null;
        });
        enableDialog.show();
        enableDialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();
    }

    public View firstFocusable() {
        return firstCard;
    }

    private SettingCard card(
            String title,
            String value,
            String detail,
            Runnable onContentLeft) {
        SettingCard card = new SettingCard(getContext())
                .title(title)
                .value(value)
                .detail(detail);
        card.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && onContentLeft != null) {
                onContentLeft.run();
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                return moveFocusOrScroll(view, View.FOCUS_DOWN);
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                return moveFocusOrScroll(view, View.FOCUS_UP);
            }
            return false;
        });
        return card;
    }

    private boolean moveFocusOrScroll(View from, int direction) {
        View next = from.focusSearch(direction);
        if (next != null && next != from && isInsideScroll(next)) {
            next.requestFocus();
            return true;
        }
        int sign = direction == View.FOCUS_DOWN ? 1 : -1;
        if (!scroll.canScrollVertically(sign)) {
            return false;
        }
        scroll.smoothScrollBy(0, sign * Math.max(dp(110), scroll.getHeight() * 2 / 3));
        return true;
    }

    private boolean isInsideScroll(View candidate) {
        View current = candidate;
        while (current != null) {
            if (current == scroll) {
                return true;
            }
            if (!(current.getParent() instanceof View)) {
                return false;
            }
            current = (View) current.getParent();
        }
        return false;
    }

    private View infoPanel(String label, String value) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(30), dp(22), dp(30), dp(22));
        row.setMinimumHeight(dp(86));
        row.setBackground(rounded(PANEL, Color.rgb(52, 52, 56), 1));
        row.setFocusable(false);

        TextView left = text(label, 24f, Color.WHITE, true);
        TextView right = text(value, 22f, CYAN, true);
        row.addView(left, new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(right, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private TextView title(String text, float size) {
        return text(text, size, Color.WHITE, true);
    }

    private TextView detail(String text) {
        TextView view = text(text, 22f, Color.rgb(176, 176, 184), false);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(4);
        params.bottomMargin = dp(28);
        view.setLayoutParams(params);
        return view;
    }

    private TextView section(String text) {
        TextView view = text(text, 18f, CYAN, true);
        view.setLetterSpacing(0.1f);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(14);
        params.bottomMargin = dp(12);
        view.setLayoutParams(params);
        return view;
    }

    private TextView text(String text, float size, int color, boolean bold) {
        TextView view = new TextView(getContext());
        view.setText(text);
        view.setTextColor(color);
        view.setTextSize(size);
        view.setGravity(Gravity.START);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return view;
    }

    private LayoutParams cardParams() {
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(16);
        return params;
    }

    private GradientDrawable rounded(int fill, int stroke, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(22));
        drawable.setStroke(dp(strokeDp), stroke);
        return drawable;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }

    private final class SettingCard extends LinearLayout {
        private final TextView titleView;
        private final TextView valueView;
        private final TextView detailView;

        SettingCard(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setGravity(Gravity.CENTER_VERTICAL);
            setPadding(dp(30), dp(20), dp(30), dp(20));
            setMinimumHeight(dp(108));
            setFocusable(true);
            setClickable(true);
            setFocusableInTouchMode(false);
            setStateListAnimator(null);

            LinearLayout top = new LinearLayout(context);
            top.setOrientation(HORIZONTAL);
            top.setGravity(Gravity.CENTER_VERTICAL);
            titleView = text("", 27f, Color.WHITE, true);
            valueView = text("", 23f, CYAN, true);
            top.addView(titleView, new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            top.addView(valueView, new LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            addView(top, new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            detailView = text("", 20f, Color.rgb(170, 170, 178), false);
            LayoutParams detailParams = new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            detailParams.topMargin = dp(8);
            addView(detailView, detailParams);

            applyFocus(false);
            setOnFocusChangeListener((view, hasFocus) -> applyFocus(hasFocus));
        }

        SettingCard title(String value) {
            titleView.setText(value == null ? "" : value);
            return this;
        }

        SettingCard value(String value) {
            valueView.setText(value == null ? "" : value);
            return this;
        }

        SettingCard detail(String value) {
            detailView.setText(value == null ? "" : value);
            return this;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                    || keyCode == KeyEvent.KEYCODE_ENTER
                    || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                return performClick();
            }
            return super.onKeyDown(keyCode, event);
        }

        private void applyFocus(boolean focused) {
            setBackground(focused
                    ? rounded(CYAN, Color.WHITE, 2)
                    : rounded(PANEL, Color.rgb(58, 58, 64), 1));
            int primary = focused ? Color.BLACK : Color.WHITE;
            int secondary = focused ? Color.rgb(18, 40, 44) : Color.rgb(170, 170, 178);
            int value = focused ? Color.BLACK : CYAN;
            titleView.setTextColor(primary);
            valueView.setTextColor(value);
            detailView.setTextColor(secondary);
            setTranslationZ(focused ? dp(6) : 0f);
        }
    }
}
