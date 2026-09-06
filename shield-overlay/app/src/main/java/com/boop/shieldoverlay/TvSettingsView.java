package com.boop.shieldoverlay;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class TvSettingsView extends LinearLayout {
    private final FocusCardView firstCard;
    private final MediaPuppetState puppetState;
    private final DeezerPuppetSettingsModel puppetModel;
    private FocusCardView puppetToggle;
    private TextView puppetExplanation;
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

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setFocusable(false);
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(VERTICAL);
        content.setPadding(dp(36), dp(34), dp(44), dp(34));
        scroll.addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(scroll, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        content.addView(title("Settings", 42f));
        content.addView(detail("The useful TV-safe bits only."));

        FocusCardView connection = card("House connection — Connected", onContentLeft);
        connection.setClickable(false);
        content.addView(connection, cardParams());

        String roomName = selectedRoom == null ? "Choose this Shield's room" : selectedRoom.name();
        firstCard = card("Where am I?  " + roomName, onContentLeft);
        firstCard.setOnClickListener(view -> {
            if (onChangeRoom != null) {
                onChangeRoom.run();
            }
        });
        content.addView(firstCard, cardParams());

        if (puppetState != null && puppetModel != null) {
            puppetToggle = card("Deezer headphones — Off", onContentLeft);
            puppetToggle.setOnClickListener(view -> {
                if (enableDialog == null && puppetModel.toggle(puppetState.snapshot())) {
                    showEnableConfirmation();
                }
            });
            content.addView(puppetToggle, cardParams());
            FocusCardView manageAccess = card("Manage Deezer access", onContentLeft);
            manageAccess.setOnClickListener(view -> {
                puppetModel.manageAccess();
                renderPuppet(puppetState.snapshot());
            });
            content.addView(manageAccess, cardParams());
            puppetExplanation = detail("");
            puppetExplanation.setFocusable(false);
            content.addView(puppetExplanation);
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
        puppetToggle.label("Deezer headphones — " + snapshot.status());
        puppetExplanation.setText(puppetModel.explanation(snapshot));
    }

    private void showEnableConfirmation() {
        enableDialog = new AlertDialog.Builder(getContext())
                .setTitle("Enable Deezer headphones?")
                .setMessage("Android notification access is broader than playback access. "
                        + "BOOP will ignore notification contents and observe only Deezer playback. "
                        + "Enabling this feature does not grant Android access. "
                        + "Use Manage Deezer access for setup.")
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

    private FocusCardView card(String label, Runnable onContentLeft) {
        FocusCardView card = new FocusCardView(getContext()).label(label);
        card.setOnKeyListener((view, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                    && onContentLeft != null) {
                onContentLeft.run();
                return true;
            }
            return false;
        });
        return card;
    }

    private TextView title(String text, float size) {
        TextView view = new TextView(getContext());
        view.setText(text);
        view.setTextColor(Color.WHITE);
        view.setTextSize(size);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setGravity(Gravity.START);
        return view;
    }

    private TextView detail(String text) {
        TextView view = title(text, 22f);
        view.setTextColor(Color.LTGRAY);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(22);
        view.setLayoutParams(params);
        return view;
    }

    private LayoutParams cardParams() {
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(10);
        return params;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}
