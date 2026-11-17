package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.List;
import java.util.Set;

/** Presentation-only installed-app grid for Shield remote navigation. */
public final class ShieldAppsView extends LinearLayout {
    public interface Callbacks {
        void onAppSelected(TvAppEntry entry);
        void onToggleFavourite(TvAppEntry entry);
    }

    public ShieldAppsView(Context context) {
        this(context, null);
    }

    public ShieldAppsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setBackgroundColor(Color.BLACK);
        setPadding(dp(48), dp(30), dp(48), dp(28));
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void render(List<TvAppEntry> apps, Set<String> favouriteComponents, Callbacks callbacks) {
        removeAllViews();
        List<TvAppEntry> safeApps = apps == null ? List.of() : List.copyOf(apps);
        Set<String> safeFavourites = favouriteComponents == null ? Set.of() : Set.copyOf(favouriteComponents);

        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout titleCol = new LinearLayout(getContext());
        titleCol.setOrientation(VERTICAL);
        TextView eyebrow = new TextView(getContext());
        BoopTvChrome.markCustomStyled(eyebrow);
        eyebrow.setText("INSTALLED APPLICATIONS");
        eyebrow.setTypeface(BoopTvChrome.FONT_MEDIUM);
        eyebrow.setLetterSpacing(0.08f);
        eyebrow.setTextColor(FocusChrome.accentColor(getContext()));
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        titleCol.addView(eyebrow, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(getContext());
        BoopTvChrome.markCustomStyled(title);
        title.setText("Apps");
        title.setTypeface(BoopTvChrome.FONT_MEDIUM);
        title.setLetterSpacing(0.015f);
        title.setTextColor(BoopTvChrome.TEXT_PRIMARY);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        titleCol.addView(title, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        header.addView(titleCol, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView hint = new TextView(getContext());
        BoopTvChrome.markCustomStyled(hint);
        hint.setText("OK Launch   ·   Hold OK Star to Home   ·   " + safeApps.size() + " apps");
        hint.setTypeface(BoopTvChrome.FONT_MEDIUM);
        hint.setLetterSpacing(0.03f);
        hint.setTextColor(BoopTvChrome.TEXT_SECONDARY);
        hint.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        hint.setPadding(dp(14), dp(6), dp(14), dp(6));
        hint.setBackground(BoopTvChrome.badgePill(getContext(), false));
        header.addView(hint, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        if (safeApps.isEmpty()) {
            TextView empty = new TextView(getContext());
            BoopTvChrome.markCustomStyled(empty);
            empty.setText("No launchable apps found");
            empty.setTypeface(BoopTvChrome.FONT_REGULAR);
            empty.setTextColor(BoopTvChrome.TEXT_SECONDARY);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            LayoutParams emptyParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            emptyParams.topMargin = dp(24);
            addView(empty, emptyParams);
            return;
        }

        GridView grid = new GridView(getContext());
        grid.setNumColumns(5);
        grid.setColumnWidth(dp(190));
        grid.setHorizontalSpacing(dp(20));
        grid.setVerticalSpacing(dp(20));
        grid.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
        grid.setGravity(Gravity.CENTER_HORIZONTAL);
        grid.setClipChildren(true);
        grid.setClipToPadding(true);
        grid.setPadding(dp(12), dp(18), dp(12), dp(18));
        grid.setSelector(android.R.color.transparent);
        grid.setAdapter(new AppsAdapter(getContext(), safeApps, safeFavourites));
        grid.setOnItemClickListener((parent, view, position, id) -> {
            if (callbacks != null) callbacks.onAppSelected(safeApps.get(position));
        });
        grid.setOnItemLongClickListener((parent, view, position, id) -> {
            if (callbacks != null) callbacks.onToggleFavourite(safeApps.get(position));
            return true;
        });

        LayoutParams gridParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
        gridParams.topMargin = dp(14);
        addView(grid, gridParams);
    }

    private static final class AppsAdapter extends BaseAdapter {
        private final Context context;
        private final List<TvAppEntry> apps;
        private final Set<String> favourites;

        AppsAdapter(Context context, List<TvAppEntry> apps, Set<String> favourites) {
            this.context = context;
            this.apps = apps;
            this.favourites = favourites;
        }

        @Override public int getCount() {
            return apps.size();
        }

        @Override public TvAppEntry getItem(int position) {
            return apps.get(position);
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            TvAppCardView card = convertView instanceof TvAppCardView
                    ? (TvAppCardView) convertView
                    : new TvAppCardView(context);
            TvAppEntry entry = getItem(position);
            card.bind(entry, favourites.contains(entry.component()));
            card.setFocusable(false);
            card.setClickable(false);
            card.setLayoutParams(new GridView.LayoutParams(dp(context, 190), dp(context, 160)));
            return card;
        }

        private static int dp(Context context, int value) {
            return Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    value,
                    context.getResources().getDisplayMetrics()));
        }
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
