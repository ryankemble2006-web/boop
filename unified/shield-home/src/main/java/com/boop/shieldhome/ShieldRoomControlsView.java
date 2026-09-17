package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.boop.shieldoverlay.AreaInfo;
import com.boop.shieldoverlay.EntityCard;
import com.boop.shieldoverlay.HomeDashboardController;

import java.util.List;

/**
 * Remote-first launcher panel for controls confirmed inside BOOP's selected room.
 * This view deliberately owns its chrome so opening/using Home never falls back
 * to an Android default menu or dialog.
 */
public final class ShieldRoomControlsView extends LinearLayout {
    private static final int PANEL_CHARCOAL = Color.rgb(42, 42, 42);
    private static final int TILE_CHARCOAL = Color.rgb(31, 31, 31);

    private AreaInfo room;
    private HomeDashboardController.ViewState state;

    public ShieldRoomControlsView(Context context) { this(context, null); }

    public ShieldRoomControlsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(dp(18), dp(13), dp(18), dp(13));
        setClipChildren(false);
        setClipToPadding(false);
        setFocusable(false);
        setBackground(panelBackground());
    }

    public void bind(AreaInfo room, HomeDashboardController.ViewState state) {
        this.room = room;
        this.state = state;
        render();
    }

    private void render() {
        removeAllViews();

        LinearLayout heading = new LinearLayout(getContext());
        heading.setOrientation(HORIZONTAL);
        heading.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = label(room == null ? "Home" : room.name(), 20, Color.WHITE);
        heading.addView(title, new LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView status = label(statusText(), 14, statusColor());
        heading.addView(status, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(heading, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addSpacer(dp(9));

        if (room == null) {
            addView(message("Set this device room in BOOP settings to show its controls."), wrap());
            return;
        }
        if (state == null) {
            addView(message("Connecting to Home Assistant…"), wrap());
            return;
        }

        List<EntityCard> cards = state.cards();
        if (cards == null || cards.isEmpty()) {
            String detail = state.message();
            if (detail == null || detail.trim().isEmpty()) {
                detail = state.stale()
                        ? "Home Assistant is unavailable right now."
                        : "No lights, switches or fans are assigned to this room yet.";
            }
            addView(message(detail), wrap());
            return;
        }

        HorizontalScrollView scroller = new HorizontalScrollView(getContext());
        scroller.setHorizontalScrollBarEnabled(false);
        scroller.setFillViewport(false);
        scroller.setFocusable(false);
        scroller.setClipChildren(false);
        scroller.setClipToPadding(false);

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClipChildren(false);
        row.setClipToPadding(false);

        for (int index = 0; index < cards.size(); index++) {
            EntityCard card = cards.get(index);
            if (card == null) continue;
            View tile = controlTile(card, state);
            LayoutParams params = new LayoutParams(dp(196), dp(82));
            if (index < cards.size() - 1) params.rightMargin = dp(12);
            row.addView(tile, params);
        }
        scroller.addView(row, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        addView(scroller, new LayoutParams(LayoutParams.MATCH_PARENT, dp(88)));
    }

    private View controlTile(EntityCard card, HomeDashboardController.ViewState boundState) {
        LinearLayout tile = new LinearLayout(getContext());
        tile.setOrientation(VERTICAL);
        tile.setGravity(Gravity.CENTER_VERTICAL);
        tile.setPadding(dp(14), dp(8), dp(14), dp(8));
        tile.setFocusable(true);
        tile.setClickable(true);
        tile.setEnabled(boundState.actionsEnabled());
        tile.setBackground(tileBackground(false));
        tile.setContentDescription(card.displayName() + ", " + card.state());

        TextView name = label(card.displayName(), 17, Color.WHITE);
        name.setSingleLine(true);
        name.setEllipsize(android.text.TextUtils.TruncateAt.END);
        tile.addView(name, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        TextView value = label(stateLabel(card), 14,
                "on".equals(card.state()) ? FocusChrome.accentColor(getContext()) : Color.LTGRAY);
        value.setSingleLine(true);
        tile.addView(value, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        tile.setOnFocusChangeListener((view, focused) -> {
            view.setBackground(tileBackground(focused));
            view.animate()
                    .scaleX(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                    .scaleY(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                    .setDuration(TvAppCardView.FOCUS_DURATION_MS)
                    .start();
        });
        tile.setOnClickListener(view -> {
            HomeDashboardController.ViewState current = state;
            if (current == boundState && current.actionsEnabled()) {
                current.toggle(card);
            }
        });
        tile.setOnKeyListener((view, keyCode, event) -> {
            if (event == null || event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                return false;
            }
            return false;
        });
        return tile;
    }

    private String statusText() {
        if (room == null) return "ROOM NOT SET";
        if (state == null) return "CONNECTING";
        return state.stale() ? "OFFLINE" : "HOME ASSISTANT";
    }

    private int statusColor() {
        if (state != null && !state.stale()) return FocusChrome.accentColor(getContext());
        return Color.LTGRAY;
    }

    private static String stateLabel(EntityCard card) {
        String domain = card.domain();
        String kind;
        if ("light".equals(domain)) kind = "Light";
        else if ("fan".equals(domain)) kind = "Fan";
        else if ("switch".equals(domain)) kind = "Switch";
        else kind = "Control";
        return kind + " · " + ("on".equals(card.state()) ? "On" : "Off");
    }

    private TextView message(String text) {
        TextView view = label(text, 15, Color.LTGRAY);
        view.setMaxLines(2);
        return view;
    }

    private TextView label(String text, int sp, int color) {
        TextView view = new TextView(getContext());
        view.setText(text == null ? "" : text);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        return view;
    }

    private GradientDrawable panelBackground() {
        GradientDrawable background = FocusChrome.filled(getContext(), PANEL_CHARCOAL, 13, false);
        background.setStroke(dp(1), Color.rgb(63, 63, 63));
        return background;
    }

    private GradientDrawable tileBackground(boolean focused) {
        return FocusChrome.filled(getContext(), TILE_CHARCOAL, 10, focused);
    }

    private LayoutParams wrap() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    private void addSpacer(int height) {
        View spacer = new View(getContext());
        addView(spacer, new LayoutParams(1, height));
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()));
    }
}
