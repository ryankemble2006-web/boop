package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;

/** Presentation-only Shield HOME surface. Data discovery and persistence live elsewhere. */
public final class ShieldHomeView extends LinearLayout {
    public interface Callbacks {
        void onAppSelected(TvAppEntry entry);
        void onFavouriteLongPressed(TvAppEntry entry);
        void onOpenApps();
        void onOpenSettings();
        void onContentSelected(HomeContentCard card);
    }

    public ShieldHomeView(Context context) {
        this(context, null);
    }

    public ShieldHomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundColor(Color.BLACK);
        setPadding(dp(42), dp(34), dp(42), dp(30));
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void render(List<TvAppEntry> favourites, List<HomeRow> optionalRows, Callbacks callbacks) {
        removeAllViews();
        List<TvAppEntry> safeFavourites = favourites == null ? List.of() : favourites;
        List<HomeRow> safeOptionalRows = optionalRows == null ? List.of() : optionalRows;

        addView(navRow(callbacks), wrap());
        addSpacer(dp(24));
        addView(sectionTitle("Favourite apps"), wrap());
        addSpacer(dp(12));

        if (safeFavourites.isEmpty()) {
            TextView add = actionButton("Add favourites");
            add.setOnClickListener(v -> callbacks.onOpenApps());
            addView(add, new LayoutParams(dp(260), dp(72)));
        } else {
            addView(appRow(safeFavourites, callbacks), new LayoutParams(
                    LayoutParams.MATCH_PARENT, dp(170)));
        }

        for (HomeRow row : safeOptionalRows) {
            if (row == null || row.cards() == null || row.cards().isEmpty()) continue;
            addSpacer(dp(26));
            addView(sectionTitle(row.title()), wrap());
            addSpacer(dp(10));
            addView(contentRow(row.cards(), callbacks), new LayoutParams(
                    LayoutParams.MATCH_PARENT, dp(150)));
        }
    }

    private View navRow(Callbacks callbacks) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView apps = actionButton("Apps");
        apps.setOnClickListener(v -> callbacks.onOpenApps());
        row.addView(apps, new LayoutParams(dp(150), dp(60)));

        TextView settings = actionButton("Settings");
        settings.setOnClickListener(v -> callbacks.onOpenSettings());
        LayoutParams settingsParams = new LayoutParams(dp(170), dp(60));
        settingsParams.leftMargin = dp(12);
        row.addView(settings, settingsParams);
        return row;
    }

    private View appRow(List<TvAppEntry> favourites, Callbacks callbacks) {
        HorizontalScrollView scroller = new HorizontalScrollView(getContext());
        scroller.setHorizontalScrollBarEnabled(false);
        scroller.setFillViewport(false);
        scroller.setClipChildren(false);
        scroller.setClipToPadding(false);

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClipChildren(false);
        row.setClipToPadding(false);

        for (TvAppEntry entry : favourites) {
            TvAppCardView card = new TvAppCardView(getContext());
            card.bind(entry, true);
            card.setOnClickListener(v -> callbacks.onAppSelected(entry));
            card.setOnLongClickListener(v -> {
                callbacks.onFavouriteLongPressed(entry);
                return true;
            });
            LayoutParams params = new LayoutParams(dp(190), dp(145));
            params.rightMargin = dp(18);
            row.addView(card, params);
        }
        scroller.addView(row, new HorizontalScrollView.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        return scroller;
    }

    private View contentRow(List<HomeContentCard> cards, Callbacks callbacks) {
        HorizontalScrollView scroller = new HorizontalScrollView(getContext());
        scroller.setHorizontalScrollBarEnabled(false);
        scroller.setClipChildren(false);

        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setClipChildren(false);

        for (HomeContentCard card : cards) {
            TextView tile = actionButton(card.title());
            tile.setContentDescription(card.title());
            tile.setOnClickListener(v -> callbacks.onContentSelected(card));
            tile.setTag(card.intentUri() == null ? null : Uri.parse(card.intentUri()));
            LayoutParams params = new LayoutParams(dp(250), dp(118));
            params.rightMargin = dp(16);
            row.addView(tile, params);
        }
        scroller.addView(row, new HorizontalScrollView.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        return scroller;
    }

    private TextView sectionTitle(String text) {
        TextView title = new TextView(getContext());
        title.setText(text == null ? "" : text);
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        return title;
    }

    private TextView actionButton(String text) {
        TextView view = new TextView(getContext());
        view.setText(text == null ? "" : text);
        view.setTextColor(Color.WHITE);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        view.setGravity(Gravity.CENTER);
        view.setFocusable(true);
        view.setClickable(true);
        view.setPadding(dp(16), dp(8), dp(16), dp(8));
        view.setBackground(actionBackground());
        view.setOnFocusChangeListener((v, focused) -> v.animate()
                .scaleX(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                .scaleY(focused ? TvAppCardView.FOCUSED_SCALE : 1f)
                .setDuration(TvAppCardView.FOCUS_DURATION_MS)
                .start());
        return view;
    }

    private GradientDrawable actionBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(42, 42, 42));
        background.setCornerRadius(dp(10));
        return background;
    }

    private LayoutParams wrap() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    private void addSpacer(int height) {
        View spacer = new View(getContext());
        addView(spacer, new LayoutParams(1, height));
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
