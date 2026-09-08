package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/** Shield Home: selected room plus real room-scoped controllable devices. */
public final class TvHomeView extends ScrollView {
    private static final int CYAN = Color.rgb(61, 220, 255);
    private static final int PANEL = Color.rgb(22, 22, 24);
    private final LinearLayout deviceContainer;
    private final RoomCard roomCard;
    private final TextView status;
    private final Runnable onContentLeft;
    private HomeDashboardController.ViewState currentState;

    public TvHomeView(Context context, AreaInfo selectedRoom, Runnable onContentLeft, Runnable ignoredLegacyFavouriteClick) {
        super(context); this.onContentLeft = onContentLeft;
        setFillViewport(true); setSmoothScrollingEnabled(true); setBackgroundColor(Color.BLACK); setFocusable(false);
        LinearLayout content = new LinearLayout(context); content.setOrientation(LinearLayout.VERTICAL); content.setGravity(Gravity.TOP);
        content.setPadding(dp(42), dp(32), dp(54), dp(46));
        addView(content, new ScrollView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        content.addView(title("BOOP HOME", 42f));
        content.addView(section("ROOM"));
        roomCard = new RoomCard(context, selectedRoom == null ? "Room not set" : selectedRoom.name());
        wireCard(roomCard); content.addView(roomCard, cardParams());
        content.addView(section("DEVICES"));
        deviceContainer = new LinearLayout(context); deviceContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(deviceContainer, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        status = detail(selectedRoom == null ? "Choose a room before devices are shown." : "Finding devices in " + selectedRoom.name() + "…");
        content.addView(status);
    }

    public View firstFocusable() { return roomCard; }

    public void render(HomeDashboardController.ViewState state) {
        currentState = state; deviceContainer.removeAllViews();
        if (state == null) { status.setText("Connecting to Home Assistant…"); return; }
        List<EntityCard> cards = state.cards() == null ? Collections.emptyList() : state.cards();
        if (cards.isEmpty()) {
            status.setText(state.message() == null ? "No controllable devices found in this room." : state.message());
            return;
        }
        for (EntityCard entity : cards) {
            if (entity == null) continue;
            DeviceCard card = new DeviceCard(getContext(), entity);
            card.setEnabled(state.actionsEnabled()); card.setAlpha(state.actionsEnabled() ? 1f : .72f);
            card.setOnClickListener(v -> { HomeDashboardController.ViewState current = currentState; if (current != null) current.toggle(entity); });
            wireCard(card); deviceContainer.addView(card, cardParams());
        }
        if (state.stale()) status.setText(state.message() == null ? "Home Assistant is unavailable." : state.message());
        else if (state.actionsEnabled()) status.setText("Select a device to switch it on or off.");
        else status.setText("Waiting for Home Assistant to confirm…");
    }

    private void wireCard(View card) {
        card.setOnKeyListener((view, key, event) -> {
            if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
            if (key == KeyEvent.KEYCODE_DPAD_LEFT && onContentLeft != null) { onContentLeft.run(); return true; }
            if (key == KeyEvent.KEYCODE_DPAD_DOWN || key == KeyEvent.KEYCODE_DPAD_UP) {
                int direction = key == KeyEvent.KEYCODE_DPAD_DOWN ? View.FOCUS_DOWN : View.FOCUS_UP;
                View next = view.focusSearch(direction);
                if (next != null && next != view) { next.requestFocus(); ensureVisible(next); return true; }
            }
            return false;
        });
    }

    private void ensureVisible(View child) {
        post(() -> { Rect rect = new Rect(); child.getDrawingRect(rect); offsetDescendantRectToMyCoords(child, rect); smoothScrollTo(0, Math.max(0, rect.centerY() - getHeight() / 2)); });
    }

    private TextView title(String text, float size) { TextView v = new TextView(getContext()); v.setText(text); v.setTextColor(Color.WHITE); v.setTextSize(size); v.setTypeface(Typeface.DEFAULT, Typeface.BOLD); v.setGravity(Gravity.START); return v; }
    private TextView detail(String text) { TextView v = title(text, 20f); v.setTextColor(Color.rgb(170,170,178)); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.topMargin = dp(8); p.bottomMargin = dp(18); v.setLayoutParams(p); return v; }
    private TextView section(String text) { TextView v = title(text, 18f); v.setTextColor(CYAN); v.setLetterSpacing(.1f); LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.topMargin=dp(14); p.bottomMargin=dp(12); v.setLayoutParams(p); return v; }
    private LinearLayout.LayoutParams cardParams() { LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); p.bottomMargin=dp(16); return p; }
    private GradientDrawable round(int fill, int stroke, int width) { GradientDrawable d=new GradientDrawable(); d.setColor(fill); d.setCornerRadius(dp(22)); d.setStroke(dp(width),stroke); return d; }
    private int dp(int n) { return Math.max(1, Math.round(n * getResources().getDisplayMetrics().density)); }

    private class RoomCard extends TextView {
        RoomCard(Context c, String label) { super(c); setText(label); setTextSize(27); setTypeface(Typeface.DEFAULT,Typeface.BOLD); setGravity(Gravity.CENTER_VERTICAL); setPadding(dp(30),dp(20),dp(30),dp(20)); setMinimumHeight(dp(96)); setFocusable(true); setClickable(false); setTextColor(Color.WHITE); setBackground(round(PANEL,Color.rgb(58,58,64),1)); setOnFocusChangeListener((v,f)->focus(f)); }
        private void focus(boolean f) { setBackground(f?round(CYAN,Color.WHITE,2):round(PANEL,Color.rgb(58,58,64),1)); setTextColor(f?Color.BLACK:Color.WHITE); setTranslationZ(f?dp(6):0); }
    }
    private final class DeviceCard extends LinearLayout {
        private final TextView name; private final TextView value;
        DeviceCard(Context c, EntityCard entity) { super(c); setOrientation(HORIZONTAL); setGravity(Gravity.CENTER_VERTICAL); setPadding(dp(30),dp(20),dp(30),dp(20)); setMinimumHeight(dp(96)); setFocusable(true); setClickable(true); setStateListAnimator(null); name=title(entity.displayName(),27); value=title("on".equals(entity.state())?"On":"Off",23); value.setTextColor(CYAN); addView(name,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1)); addView(value,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT)); focus(false); setOnFocusChangeListener((v,f)->focus(f)); }
        @Override public boolean onKeyDown(int key, KeyEvent e) { if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER||key==KeyEvent.KEYCODE_NUMPAD_ENTER)return performClick(); return super.onKeyDown(key,e); }
        private void focus(boolean f) { setBackground(f?round(CYAN,Color.WHITE,2):round(PANEL,Color.rgb(58,58,64),1)); name.setTextColor(f?Color.BLACK:Color.WHITE); value.setTextColor(f?Color.BLACK:CYAN); setTranslationZ(f?dp(6):0); }
    }
}
