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
        setPadding(dp(42), dp(34), dp(42), dp(30));
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void render(List<TvAppEntry> apps, Set<String> favouriteComponents, Callbacks callbacks) {
        removeAllViews();
        List<TvAppEntry> safeApps = apps == null ? List.of() : List.copyOf(apps);
        Set<String> safeFavourites = favouriteComponents == null ? Set.of() : Set.copyOf(favouriteComponents);

        TextView title = new TextView(getContext());
        title.setText("Apps");
        title.setTextColor(Color.WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        addView(title, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        if (safeApps.isEmpty()) {
            TextView empty = new TextView(getContext());
            empty.setText("No launchable apps found");
            empty.setTextColor(Color.WHITE);
            empty.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            LayoutParams emptyParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            emptyParams.topMargin = dp(24);
            addView(empty, emptyParams);
            return;
        }

        GridView grid = new GridView(getContext());
        grid.setNumColumns(5);
        grid.setColumnWidth(dp(190));
        grid.setHorizontalSpacing(dp(18));
        grid.setVerticalSpacing(dp(18));
        grid.setStretchMode(GridView.STRETCH_SPACING_UNIFORM);
        grid.setGravity(Gravity.CENTER_HORIZONTAL);
        grid.setClipChildren(false);
        grid.setClipToPadding(false);
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
            card.setLayoutParams(new GridView.LayoutParams(dp(context, 190), dp(context, 145)));
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
