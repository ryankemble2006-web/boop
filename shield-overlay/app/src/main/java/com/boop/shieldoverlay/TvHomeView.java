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
    private final TextView favouriteStatus;
    private final Runnable onFavouriteClick;
    private boolean favouriteActionEnabled;

    public TvHomeView(
            Context context,
            AreaInfo selectedRoom,
            Runnable onContentLeft,
            Runnable onFavouriteClick) {
        super(context);
        this.onFavouriteClick = onFavouriteClick;
        setOrientation(VERTICAL);
        setGravity(Gravity.TOP);
        setPadding(dp(36), dp(34), dp(44), dp(34));
        setBackgroundColor(Color.BLACK);

        addView(title("BOOP Home", 42f));
        addView(detail(selectedRoom == null ? "Home" : selectedRoom.name()));
        addView(section("Favourites"));

        firstCard = card("Finding a useful control…", onContentLeft);
        firstCard.setOnClickListener(view -> {
            if (favouriteActionEnabled && this.onFavouriteClick != null) {
                this.onFavouriteClick.run();
            }
        });
        addView(firstCard, cardParams());

        favouriteStatus = detail("Connecting to the house…");
        addView(favouriteStatus);

        addView(section("Rooms"));
        FocusCardView roomCard = card(
                selectedRoom == null ? "Choose a room" : selectedRoom.name(),
                onContentLeft);
        addView(roomCard, cardParams());
    }

    public View firstFocusable() {
        return firstCard;
    }

    public void render(HomeDashboardController.ViewState state) {
        if (state == null) {
            favouriteActionEnabled = false;
            firstCard.label("Finding a useful control…");
            firstCard.setAlpha(0.72f);
            favouriteStatus.setText("Connecting to the house…");
            return;
        }

        EntityCard favourite = state.favourite();
        favouriteActionEnabled = state.actionsEnabled() && favourite != null;
        if (favourite == null) {
            firstCard.label(state.stale()
                    ? "No last-known control for this room"
                    : "No simple on/off controls found in this room");
            firstCard.setAlpha(0.72f);
            favouriteStatus.setText(state.message() == null
                    ? "Nothing useful to put here yet."
                    : state.message());
            return;
        }

        String stateLabel = "on".equals(favourite.state()) ? "On" : "Off";
        String staleLabel = state.stale() ? " · Last known" : "";
        firstCard.label(favourite.displayName() + "\n" + stateLabel + staleLabel);
        firstCard.setAlpha(favouriteActionEnabled ? 1f : 0.72f);

        if (state.stale()) {
            favouriteStatus.setText(state.message() == null
                    ? "Last known state — house controls are unavailable."
                    : state.message() + " · Last known state");
        } else if (state.actionsEnabled()) {
            favouriteStatus.setText("Select to switch it "
                    + ("on".equals(favourite.state()) ? "off." : "on."));
        } else {
            favouriteStatus.setText("Waiting for Home Assistant to confirm…");
        }
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
