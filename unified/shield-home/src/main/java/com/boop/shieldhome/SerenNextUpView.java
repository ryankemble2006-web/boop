package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.Gravity;
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
    private final TextView detail, empty;
    private final HorizontalScrollView scroll;
    private final LinearLayout row;
    private List<SerenEpisode> entries = List.of();
    private final List<ImageView> images = new ArrayList<>();
    private SerenPosterLoader artwork;
    private Consumer<SerenEpisode> select;
    private Runnable open;
    private String signature = "";
    private int posterHeight = 150;
    private String lastFocusedFile;

    SerenNextUpView(Context context) {
        super(context); setOrientation(VERTICAL); setClipChildren(false); setClipToPadding(false);
        LinearLayout heading = new LinearLayout(context); heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("Seren · Next Up", 20); heading.addView(title, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(32)));
        detail = text("", 14); detail.setTextColor(Color.LTGRAY); detail.setSingleLine(true); detail.setEllipsize(TextUtils.TruncateAt.END);
        detail.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        LayoutParams info = new LayoutParams(0, dp(32), 1); info.leftMargin = dp(16); heading.addView(detail, info);
        addView(heading, new LayoutParams(LayoutParams.MATCH_PARENT, dp(32)));
        scroll = new HorizontalScrollView(context); scroll.setFocusable(false); scroll.setHorizontalScrollBarEnabled(false);
        scroll.setClipChildren(true); scroll.setClipToPadding(false); scroll.setPadding(dp(5), dp(5), dp(5), dp(5));
        row = new LinearLayout(context); row.setOrientation(HORIZONTAL); row.setClipChildren(false);
        scroll.addView(row, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, dp(posterHeight + 10)));
        empty = text("Open Kodi to load your Next Up episodes", 17); empty.setGravity(Gravity.CENTER);
        empty.setFocusable(true); empty.setClickable(true); empty.setOnClickListener(v -> { if (open != null) open.run(); });
        empty.setOnFocusChangeListener((v, focused) -> empty.setBackground(FocusChrome.filled(context, Color.rgb(25,25,25), 8, focused)));
        addView(empty, new LayoutParams(LayoutParams.MATCH_PARENT, dp(100)));
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
        for (SerenEpisode e : next) key.append(e.file).append(e.poster).append(e.title).append(e.detail);
        if (signature.equals(key.toString()) && entries.size() == next.size()) {
            if (!hasFocus()) detail.setText(status);
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
            tile.setTag(e.file); tile.setContentDescription(e.title + ": " + e.detail);
            TextView label = text(e.title, 15); label.setGravity(Gravity.CENTER); label.setMaxLines(3);
            tile.addView(label, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            ImageView image = new ImageView(getContext()); image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            tile.addView(image, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)); images.add(image);
            FocusChrome.clipRounded(tile, 7);
            tile.setBackgroundColor(Color.rgb(28,28,28));
            tile.setOnFocusChangeListener((v, selected) -> {
                tile.setForeground(FocusChrome.filled(getContext(), Color.TRANSPARENT, 7, selected));
                if (selected) { lastFocusedFile = e.file; detail.setText(e.title + " · " + e.detail); }
            });
            tile.setOnClickListener(v -> { if (this.select != null) this.select.accept(e); });
            LayoutParams p = new LayoutParams(dp(posterHeight * 2 / 3), dp(posterHeight)); p.rightMargin = dp(12);
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
        if (!hasFocus()) detail.setText(status);
        post(this::loadVisible);
    }
    void fitHeight(int availablePx) {
        int height = Math.max(100, Math.min(210, Math.round(availablePx / getResources().getDisplayMetrics().density) - 42));
        if (height == posterHeight) return;
        posterHeight = height;
        scroll.getLayoutParams().height = dp(height + 10); scroll.requestLayout();
        for (int i=0; i<row.getChildCount(); i++) {
            View tile=row.getChildAt(i); tile.getLayoutParams().width=dp(height * 2 / 3);
            tile.getLayoutParams().height=dp(height); tile.requestLayout();
        }
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
    private TextView text(String value,int size) { TextView v=new TextView(getContext());v.setText(value);v.setTextColor(Color.WHITE);v.setTextSize(size);v.setIncludeFontPadding(false);return v; }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
