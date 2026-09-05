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

public final class TvSettingsView extends LinearLayout {
    private final FocusCardView firstCard;

    public TvSettingsView(
            Context context,
            AreaInfo selectedRoom,
            Runnable onChangeRoom,
            Runnable onContentLeft) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.TOP);
        setPadding(dp(36), dp(34), dp(44), dp(34));
        setBackgroundColor(Color.BLACK);

        addView(title("Settings", 42f));
        addView(detail("The useful TV-safe bits only."));

        FocusCardView connection = card("House connection — Connected", onContentLeft);
        connection.setClickable(false);
        addView(connection, cardParams());

        String roomName = selectedRoom == null ? "Choose this Shield's room" : selectedRoom.name();
        firstCard = card("Where am I?  " + roomName, onContentLeft);
        firstCard.setOnClickListener(view -> {
            if (onChangeRoom != null) {
                onChangeRoom.run();
            }
        });
        addView(firstCard, cardParams());
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
