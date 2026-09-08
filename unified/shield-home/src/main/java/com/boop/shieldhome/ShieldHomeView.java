package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Presentation-only Shield HOME surface. Data discovery and persistence live elsewhere. */
public final class ShieldHomeView extends LinearLayout {
    public interface Callbacks {
        void onAppSelected(TvAppEntry entry);
        void onFavouriteOrderCommitted(List<String> components);
        void onOpenApps();
        void onOpenHomeRows();
        void onOpenSystemSettings();
        void onContentSelected(HomeContentCard card);
        default void onNowPlayingPrevious() { }
        default void onNowPlayingRewind() { }
        default void onNowPlayingPlayPause() { }
        default void onNowPlayingFastForward() { }
        default void onNowPlayingNext() { }
        default void onOpenNowPlayingSource() { }
    }

    private FavouriteGrabSession grabSession;
    private HorizontalScrollView favouriteScroller;
    private LinearLayout favouriteRow;
    private TvAppCardView grabbedCard;
    private ShieldNowPlayingView nowPlayingView;
    private View nowPlayingSpacer;
    private NowPlayingSnapshot nowPlayingSnapshot;
    private Callbacks activeCallbacks;

    public ShieldHomeView(Context context) {
        this(context, null);
    }

    public ShieldHomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setBackgroundColor(Color.BLACK);
        setPadding(dp(42), dp(28), dp(42), dp(30));
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void render(List<TvAppEntry> favourites, List<HomeRow> optionalRows, Callbacks callbacks) {
        render(favourites, optionalRows, null, callbacks);
    }

    public void render(
            List<TvAppEntry> favourites,
            List<HomeRow> optionalRows,
            NowPlayingSnapshot snapshot,
            Callbacks callbacks) {
        removeAllViews();
        activeCallbacks = callbacks;
        nowPlayingSnapshot = snapshot;
        favouriteScroller = null;
        favouriteRow = null;
        nowPlayingView = null;
        nowPlayingSpacer = null;

        List<TvAppEntry> safeFavourites = favourites == null ? List.of() : favourites;
        List<HomeRow> safeOptionalRows = optionalRows == null ? List.of() : optionalRows;

        if (grabSession != null) {
            safeFavourites = orderEntries(safeFavourites, grabSession.current());
            if (grabSession.index() < 0) {
                grabSession = null;
                grabbedCard = null;
            }
        }

        addView(navRow(callbacks), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addSpacer(dp(16));

        nowPlayingView = new ShieldNowPlayingView(getContext());
        addView(nowPlayingView, new LayoutParams(LayoutParams.MATCH_PARENT, dp(182)));
        nowPlayingSpacer = new View(getContext());
        addView(nowPlayingSpacer, new LayoutParams(1, dp(16)));
        setNowPlaying(snapshot);

        addView(sectionTitle("Favourite apps"), wrap());
        addSpacer(dp(10));

        if (safeFavourites.isEmpty()) {
            TextView add = actionButton("Add favourites");
            add.setOnClickListener(v -> callbacks.onOpenApps());
            addView(add, new LayoutParams(dp(260), dp(72)));
        } else {
            addView(appRow(safeFavourites, callbacks), new LayoutParams(
                    LayoutParams.MATCH_PARENT, dp(215)));
        }

        for (HomeRow row : safeOptionalRows) {
            if (row == null || row.cards() == null || row.cards().isEmpty()) continue;
            addSpacer(dp(22));
            addView(sectionTitle(row.title()), wrap());
            addSpacer(dp(8));
            addView(contentRow(row.cards(), callbacks), new LayoutParams(
                    LayoutParams.MATCH_PARENT, dp(150)));
        }
    }

    /** Updates only the media subview. Favourite/app rows are intentionally left untouched. */
    public void setNowPlaying(NowPlayingSnapshot snapshot) {
        nowPlayingSnapshot = snapshot;
        ShieldNowPlayingView panel = nowPlayingView;
        if (panel == null) {
            return;
        }
        panel.bind(snapshot, activeCallbacks);
        boolean visible = snapshot != null
                && NowPlayingSelectionPolicy.eligible(snapshot.playbackState());
        panel.setVisibility(visible ? VISIBLE : GONE);
        if (nowPlayingSpacer != null) {
            nowPlayingSpacer.setVisibility(visible ? VISIBLE : GONE);
        }
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (handleGrabKeyEvent(event)) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    private boolean handleGrabKeyEvent(KeyEvent event) {
        if (grabSession == null || grabbedCard == null || event == null) {
            return false;
        }

        int keyCode = event.getKeyCode();
        boolean grabOwnedKey = keyCode == KeyEvent.KEYCODE_DPAD_LEFT
                || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                || keyCode == KeyEvent.KEYCODE_DPAD_CENTER
                || keyCode == KeyEvent.KEYCODE_ENTER
                || keyCode == KeyEvent.KEYCODE_DPAD_UP
                || keyCode == KeyEvent.KEYCODE_DPAD_DOWN;
        if (!grabOwnedKey) {
            return false;
        }

        if (event.getAction() == KeyEvent.ACTION_UP) {
            return true;
        }
        if (event.getAction() != KeyEvent.ACTION_DOWN) {
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (grabSession.move(-1)) {
                reorderFavouriteChildren(grabSession.current());
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (grabSession.move(1)) {
                reorderFavouriteChildren(grabSession.current());
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (event.getRepeatCount() == 0 && activeCallbacks != null) {
                commitGrab(activeCallbacks);
            }
            return true;
        }

        return true;
    }

    boolean resetToFirstFavourite() {
        if (grabSession != null) {
            List<String> original = grabSession.cancel();
            reorderFavouriteChildren(original);
            if (grabbedCard != null) {
                grabbedCard.setGrabbed(false);
            }
            grabSession = null;
            grabbedCard = null;
        }

        if (favouriteRow == null || favouriteRow.getChildCount() == 0) {
            return false;
        }
        View first = favouriteRow.getChildAt(0);
        if (first == null) {
            return false;
        }
        boolean requested = first.requestFocus();
        if (favouriteScroller != null) {
            favouriteScroller.post(() -> favouriteScroller.smoothScrollTo(0, 0));
        }
        return requested || first.hasFocus();
    }

    private View navRow(Callbacks callbacks) {
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setTranslationY(dp(8));

        TextView apps = actionButton("Apps");
        apps.setOnClickListener(v -> callbacks.onOpenApps());
        row.addView(apps, new LayoutParams(dp(150), dp(60)));

        TextView homeRows = actionButton(ShieldHomeSettingsView.launcherSettingsLabel());
        homeRows.setSingleLine(true);
        homeRows.setOnClickListener(v -> callbacks.onOpenHomeRows());
        LayoutParams homeRowsParams = new LayoutParams(dp(220), dp(60));
        homeRowsParams.leftMargin = dp(12);
        row.addView(homeRows, homeRowsParams);

        View spacer = new View(getContext());
        row.addView(spacer, new LayoutParams(0, 1, 1f));

        TextView settings = actionButton("Settings");
        settings.setContentDescription("Shield settings");
        settings.setOnClickListener(v -> callbacks.onOpenSystemSettings());
        row.addView(settings, new LayoutParams(dp(170), dp(60)));
        return row;
    }

    private View appRow(List<TvAppEntry> favourites, Callbacks callbacks) {
        favouriteScroller = new HorizontalScrollView(getContext());
        favouriteScroller.setHorizontalScrollBarEnabled(false);
        favouriteScroller.setFillViewport(false);
        favouriteScroller.setClipChildren(false);
        favouriteScroller.setClipToPadding(false);

        favouriteRow = new LinearLayout(getContext());
        favouriteRow.setOrientation(HORIZONTAL);
        favouriteRow.setGravity(Gravity.CENTER_VERTICAL);
        favouriteRow.setClipChildren(false);
        favouriteRow.setClipToPadding(false);

        String grabbedComponent = grabSession == null ? null : grabSession.grabbedComponent();
        for (TvAppEntry entry : favourites) {
            TvAppCardView card = new TvAppCardView(getContext());
            card.bindFavourite(entry);
            card.setTag(entry.component());
            card.setOnClickListener(v -> {
                if (grabSession != null && v == grabbedCard) {
                    commitGrab(callbacks);
                } else {
                    callbacks.onAppSelected(entry);
                }
            });
            card.setOnLongClickListener(v -> {
                beginGrab(entry, card);
                return true;
            });
            LayoutParams params = new LayoutParams(dp(240), dp(185));
            params.rightMargin = dp(6);
            favouriteRow.addView(card, params);

            if (grabbedComponent != null && grabbedComponent.equals(entry.component())) {
                grabbedCard = card;
                card.setGrabbed(true);
                card.post(card::requestFocus);
            }
        }
        favouriteScroller.addView(favouriteRow, new HorizontalScrollView.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        return favouriteScroller;
    }

    private void beginGrab(TvAppEntry entry, TvAppCardView card) {
        if (entry == null || card == null || favouriteRow == null) {
            return;
        }
        ArrayList<String> current = new ArrayList<>();
        for (int i = 0; i < favouriteRow.getChildCount(); i++) {
            Object tag = favouriteRow.getChildAt(i).getTag();
            if (tag instanceof String) current.add((String) tag);
        }
        grabSession = FavouriteGrabSession.begin(current, entry.component());
        if (grabbedCard != null && grabbedCard != card) {
            grabbedCard.setGrabbed(false);
        }
        grabbedCard = card;
        card.setGrabbed(true);
        card.requestFocus();
        scrollGrabbedIntoView();
    }

    private void commitGrab(Callbacks callbacks) {
        if (grabSession == null) return;
        List<String> committed = grabSession.commit();
        clearGrabState();
        callbacks.onFavouriteOrderCommitted(committed);
    }

    private void clearGrabState() {
        if (grabbedCard != null) {
            grabbedCard.setGrabbed(false);
            grabbedCard.requestFocus();
        }
        grabSession = null;
        grabbedCard = null;
    }

    private void reorderFavouriteChildren(List<String> order) {
        if (favouriteRow == null || order == null) return;
        Map<String, View> byComponent = new HashMap<>();
        for (int i = 0; i < favouriteRow.getChildCount(); i++) {
            View child = favouriteRow.getChildAt(i);
            Object tag = child.getTag();
            if (tag instanceof String) byComponent.put((String) tag, child);
        }
        favouriteRow.removeAllViews();
        for (String component : order) {
            View child = byComponent.get(component);
            if (child != null) {
                favouriteRow.addView(child);
            }
        }
        if (grabbedCard != null) {
            grabbedCard.requestFocus();
            scrollGrabbedIntoView();
        }
    }

    private void scrollGrabbedIntoView() {
        if (favouriteScroller == null || grabbedCard == null) return;
        grabbedCard.post(() -> favouriteScroller.smoothScrollTo(
                Math.max(0, grabbedCard.getLeft() - dp(70)), 0));
    }

    private List<TvAppEntry> orderEntries(List<TvAppEntry> entries, List<String> order) {
        Map<String, TvAppEntry> byComponent = new HashMap<>();
        for (TvAppEntry entry : entries) {
            if (entry != null) byComponent.put(entry.component(), entry);
        }
        ArrayList<TvAppEntry> out = new ArrayList<>();
        for (String component : order) {
            TvAppEntry entry = byComponent.remove(component);
            if (entry != null) out.add(entry);
        }
        out.addAll(byComponent.values());
        return out;
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
