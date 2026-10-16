package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Stable native poster row. Refreshes never rebuild the rest of Home. */
final class SerenNextUpView extends LinearLayout {
    private final SerenMarqueeTextView detail;
    private final TextView empty;
    private final HorizontalScrollView scroll;
    private final LinearLayout row;
    private List<SerenEpisode> entries = List.of();
    private final List<ImageView> images = new ArrayList<>();
    private SerenPosterLoader artwork;
    private Consumer<SerenEpisode> select;
    private Runnable open;
    private String signature = "";
    private final int posterWidthPx, posterHeightPx, posterGapPx;
    private String lastFocusedFile;

    SerenNextUpView(Context context) {
        super(context);
        // Match favourites once. Playback can change resource density while Home survives.
        // Refreshes and focus must never recalculate the card dimensions.
        posterWidthPx = dp(TvAppCardView.HOME_ARTWORK_WIDTH_DP);
        posterHeightPx = dp(TvAppCardView.HOME_ARTWORK_HEIGHT_DP);
        posterGapPx = dp(16);
        setOrientation(VERTICAL); setClipChildren(false); setClipToPadding(false);
        LinearLayout heading = new LinearLayout(context); heading.setGravity(Gravity.CENTER_VERTICAL);
        detail = new SerenMarqueeTextView(context);
        BoopTvChrome.markCustomStyled(detail);
        detail.setTypeface(BoopTvChrome.FONT_MEDIUM);
        detail.setLetterSpacing(0.015f);
        detail.setTextSize(13.5f); detail.setTextColor(BoopTvChrome.TEXT_SECONDARY); detail.setIncludeFontPadding(false);
        detail.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
        LayoutParams info = new LayoutParams(0, dp(24), 1); heading.addView(detail, info);
        scroll = new HorizontalScrollView(context);
        scroll.setFocusable(false); scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipChildren(true); scroll.setClipToPadding(true); scroll.setPadding(0, dp(5), 0, dp(5));
        row = new LinearLayout(context); row.setOrientation(HORIZONTAL); row.setClipChildren(false);
        scroll.addView(row, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, posterHeightPx + dp(10)));
        empty = text("Open Kodi to load your Next Up episodes", 16); empty.setGravity(Gravity.CENTER);
        empty.setTextColor(BoopTvChrome.TEXT_SECONDARY);
        empty.setBackground(FocusChrome.filled(context, BoopTvChrome.SURFACE_PANEL, 12, false));
        empty.setFocusable(true); empty.setClickable(true); empty.setOnClickListener(v -> { if (open != null) open.run(); });
        empty.setOnFocusChangeListener((v, focused) -> {
            empty.setBackground(FocusChrome.filled(context, BoopTvChrome.SURFACE_PANEL, 12, focused));
            empty.setTextColor(focused ? Color.WHITE : BoopTvChrome.TEXT_SECONDARY);
        });
        empty.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                return true;
            }
            return false;
        });
        addView(empty, new LayoutParams(LayoutParams.MATCH_PARENT, dp(100)));
        addView(heading, new LayoutParams(LayoutParams.MATCH_PARENT, dp(24)));
        scroll.setVisibility(GONE);
        scroll.getViewTreeObserver().addOnScrollChangedListener(this::loadVisible);
        addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob) -> {
            setClipBounds(new Rect(0, 0, r-l, b-t));
            loadVisible();
        });
    }
    void bind(List<SerenEpisode> next, String status, SerenPosterLoader loader,
              Consumer<SerenEpisode> select, Runnable open) {
        this.select = select; this.open = open; this.artwork = loader;
        empty.setText(status.isEmpty() ? "Open Kodi to load your Next Up episodes" : status + " · Open Kodi");
        StringBuilder key = new StringBuilder();
        for (SerenEpisode e : next) key.append(e.file).append(e.poster).append(e.title).append(e.detail).append(e.overview);
        if (signature.equals(key.toString()) && entries.size() == next.size()) {
            if (!hasFocus()) detail.showText(status);
            post(this::loadVisible);
            return;
        }
        boolean restore = hasFocus();
        String focused = row.findFocus() == null ? null : String.valueOf(row.findFocus().getTag());
        int focusedIndex = 0;
        String focusedShow = null;
        for (int i = 0; i < entries.size(); i++) if (entries.get(i).file.equals(focused)) {
            focusedIndex = i; focusedShow = entries.get(i).title; break;
        }
        signature = key.toString(); entries = List.copyOf(next); row.removeAllViews(); images.clear();
        for (SerenEpisode e : entries) {
            FrameLayout tile = new FrameLayout(getContext()); tile.setFocusable(true); tile.setClickable(true);
            BoopTvChrome.markCustomStyled(tile);
            tile.setTag(e.file); tile.setContentDescription(episodeDescription(e));
            TextView label = text(e.title, 14); label.setGravity(Gravity.CENTER); label.setMaxLines(3);
            label.setPadding(dp(12), dp(8), dp(12), dp(8));
            tile.addView(label, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            ImageView image = new ImageView(getContext()); image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            tile.addView(image, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); images.add(image);
            FocusChrome.clipRounded(tile, 12);
            tile.setBackgroundColor(BoopTvChrome.SURFACE_PANEL);
            tile.setForeground(FocusChrome.artworkIdleOutline(getContext(), 12));
            tile.setOnFocusChangeListener((v, selected) -> {
                tile.setForeground(selected
                        ? FocusChrome.artworkOutline(getContext(), 12)
                        : FocusChrome.artworkIdleOutline(getContext(), 12));
                detail.post(() -> detail.setSelected(row.hasFocus()));
                if (selected) {
                    lastFocusedFile = e.file; detail.showText(episodeDescription(e));
                }
            });
            tile.setOnClickListener(v -> { if (this.select != null) this.select.accept(e); });
            tile.setOnKeyListener((v, keyCode, event) -> {
                int index = row.indexOfChild(v);
                if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (index <= 0) {
                        return true; // Stop on entry 1 (home position)!
                    }
                    if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                        row.getChildAt(index - 1).requestFocus();
                    }
                    return true;
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (index >= row.getChildCount() - 1) {
                        return true; // Stop on last entry!
                    }
                    if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                        row.getChildAt(index + 1).requestFocus();
                    }
                    return true;
                }
                return false;
            });
            LayoutParams p = new LayoutParams(posterWidthPx, posterHeightPx); p.rightMargin = posterGapPx;
            row.addView(tile, p);
        }
        empty.setVisibility(entries.isEmpty() ? VISIBLE : GONE);
        scroll.setVisibility(entries.isEmpty() ? GONE : VISIBLE);
        if (restore) {
            View target = focused == null ? null : row.findViewWithTag(focused);
            if (target == null && focusedShow != null) for (int i = 0; i < entries.size(); i++)
                if (focusedShow.equals(entries.get(i).title)) { target = row.getChildAt(i); break; }
            if (target == null && !entries.isEmpty()) target = row.getChildAt(Math.min(focusedIndex, entries.size() - 1));
            (target == null ? empty : target).requestFocus();
        }
        if (!hasFocus()) detail.showText(status);
        post(this::loadVisible);
    }
    private static String episodeDescription(SerenEpisode episode) {
        String title = episode.title + "  ·  " + episode.detail;
        if (episode.overview.isEmpty()) return title;
        return title + "  —  " + episode.overview;
    }

    boolean focusEpisode(String file) {
        View target = row.findViewWithTag(file);
        if (target == null && lastFocusedFile != null) target = row.findViewWithTag(lastFocusedFile);
        if (target == null && row.getChildCount() > 0) target = row.getChildAt(0);
        return (target == null ? empty : target).requestFocus();
    }
    private void loadVisible() {
        if (artwork == null || !isAttachedToWindow()) return;
        Rect viewport = new Rect(); if (!scroll.getGlobalVisibleRect(viewport)) return;
        viewport.inset(-dp(200), 0);
        for (int i=0; i<images.size(); i++) {
            ImageView image=images.get(i); if (image.getTag()!=null || entries.get(i).poster.isEmpty()) continue;
            int[] xy=new int[2]; image.getLocationOnScreen(xy);
            if (Rect.intersects(viewport, new Rect(xy[0],xy[1],xy[0]+image.getWidth(),xy[1]+image.getHeight())))
                artwork.load(entries.get(i).poster, image);
        }
    }
    private TextView text(String value,int size) {
        TextView v=new TextView(getContext());
        BoopTvChrome.markCustomStyled(v);
        v.setTypeface(BoopTvChrome.FONT_MEDIUM);
        v.setLetterSpacing(0.015f);
        v.setText(value);v.setTextColor(BoopTvChrome.TEXT_PRIMARY);v.setTextSize(size);v.setIncludeFontPadding(false);return v;
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
