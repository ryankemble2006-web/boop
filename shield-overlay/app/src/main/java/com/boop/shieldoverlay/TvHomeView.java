package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class TvHomeView extends LinearLayout {
    private final FocusCardView firstCard;

    public TvHomeView(Context context, AreaInfo selectedRoom, Runnable onContentLeft) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.TOP);
        setPadding(dp(36), dp(34), dp(44), dp(34));
        setBackgroundColor(Color.BLACK);

        addView(title("BOOP Home", 42f));
        addView(detail(selectedRoom == null ? "Home" : selectedRoom.name()));
        addView(section("Favourites"));

        firstCard = card("Favourite controls will live here", onContentLeft);
        addView(firstCard, cardParams());

        addView(section("Rooms"));
        FocusCardView roomCard = card(
                selectedRoom == null ? "Choose a room" : selectedRoom.name(),
                onContentLeft);
        addView(roomCard, cardParams());
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
        params.bottomMargin = dp(18);
        view.setLayoutParams(params);
        return view;
    }

    private TextView section(String text) {
        TextView view = title(text, 25f);
        LayoutParams params = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(14);
        params.bottomMargin = dp(8);
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
