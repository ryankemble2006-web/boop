package com.boop.shieldoverlay;

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

import java.util.List;

public final class TvHomeView extends ScrollView {
    private final LinearLayout favouritesContainer;
    private final FocusCardView firstCard;
    private final TextView favouriteStatus;
    private final Runnable onContentLeft;
    private HomeDashboardController.ViewState currentState;
    private EntityCard firstCardEntity;
    private boolean favouriteActionEnabled;

    public TvHomeView(
            Context context,
            AreaInfo selectedRoom,
            Runnable onContentLeft,
            Runnable ignoredLegacyFavouriteClick) {
        super(context);
        this.onContentLeft = onContentLeft;
        setFillViewport(true);
        setSmoothScrollingEnabled(true);
        setBackgroundColor(Color.BLACK);
        setFocusable(false);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.TOP);
        content.setPadding(dp(36), dp(34), dp(44), dp(34));
        addView(content, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(title("BOOP Home", 42f));
        content.addView(detail(selectedRoom == null ? "Home" : selectedRoom.name()));
        content.addView(section("Favourites"));

        favouritesContainer = new LinearLayout(context);
        favouritesContainer.setOrientation(LinearLayout.VERTICAL);
        content.addView(favouritesContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        firstCard = card("Finding useful controls…");
        firstCard.setOnClickListener(view -> {
            HomeDashboardController.ViewState current = currentState;
            if (favouriteActionEnabled && firstCardEntity != null && current != null) {
                current.toggle(firstCardEntity);
            }
        });
        favouritesContainer.addView(firstCard, cardParams());

        favouriteStatus = detail("Connecting to the house…");
        content.addView(favouriteStatus);

        content.addView(section("Rooms"));
        FocusCardView roomCard = card(
                selectedRoom == null ? "Choose a room" : selectedRoom.name());
        content.addView(roomCard, cardParams());
    }

    public View firstFocusable() {
        return firstCard;
    }

    public void render(HomeDashboardController.ViewState state) {
        currentState = state;
        clearExtraCards();

        if (state == null) {
            firstCardEntity = null;
            favouriteActionEnabled = false;
            firstCard.label("Finding useful controls…");
            firstCard.setAlpha(0.72f);
            favouriteStatus.setText("Connecting to the house…");
            return;
        }

        List<EntityCard> cards = state.cards();
        EntityCard favourite = state.favourite();
        EntityCard primary = favourite != null
                ? favourite
                : (cards.isEmpty() ? null : cards.get(0));
        firstCardEntity = primary;
        favouriteActionEnabled = state.actionsEnabled() && primary != null;

        if (primary == null) {
            firstCard.label(state.stale()
                    ? "No last-known controls for this room"
                    : "No simple on/off controls found in this room");
            firstCard.setAlpha(0.72f);
            favouriteStatus.setText(state.message() == null
                    ? "Nothing useful to put here yet."
                    : state.message());
            return;
        }

        firstCard.label(cardLabel(primary, state.stale()));
        firstCard.setAlpha(favouriteActionEnabled ? 1f : 0.72f);

        for (EntityCard card : cards) {
            if (card == null || card.entityId().equals(primary.entityId())) {
                continue;
            }
            FocusCardView extra = card(cardLabel(card, state.stale()));
            extra.setAlpha(state.actionsEnabled() ? 1f : 0.72f);
            extra.setOnClickListener(view -> {
                HomeDashboardController.ViewState current = currentState;
                if (current != null) {
                    current.toggle(card);
                }
            });
            favouritesContainer.addView(extra, cardParams());
        }

        if (state.stale()) {
            favouriteStatus.setText(state.message() == null
                    ? "Last known state — house controls are unavailable."
                    : state.message() + " · Last known state");
        } else if (state.actionsEnabled()) {
            favouriteStatus.setText(cards.size() <= 1
                    ? "Select to switch it " + ("on".equals(primary.state()) ? "off." : "on.")
                    : "Use Up/Down to see the room. Select a device to switch it on or off.");
        } else {
            favouriteStatus.setText("Waiting for Home Assistant to confirm…");
        }
    }

    private void clearExtraCards() {
        while (favouritesContainer.getChildCount() > 1) {
            favouritesContainer.removeViewAt(favouritesContainer.getChildCount() - 1);
        }
    }

    private String cardLabel(EntityCard card, boolean stale) {
        String stateLabel = "on".equals(card.state()) ? "On" : "Off";
        return card.displayName() + "\n" + stateLabel + (stale ? " · Last known" : "");
    }

    private FocusCardView card(String label) {
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
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(18);
        view.setLayoutParams(params);
        return view;
    }

    private TextView section(String text) {
        TextView view = title(text, 25f);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(14);
        params.bottomMargin = dp(8);
        view.setLayoutParams(params);
        return view;
    }

    private LinearLayout.LayoutParams cardParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
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
