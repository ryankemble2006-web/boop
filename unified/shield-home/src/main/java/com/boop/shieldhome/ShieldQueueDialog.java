package com.boop.shieldhome;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Collections;
import java.util.List;

/** Remote-first charcoal queue panel. Plays actual queue IDs without leaving BOOP. */
final class ShieldQueueDialog extends Dialog {
    private final Activity activity;
    private final DeezerQueueController controller;
    private Runnable unsubscribe;
    private DeezerQueueController.State shown;
    private TextView subtitle, footer;
    private ListView list;
    private Rows adapter;
    private boolean initialSelection = true;

    ShieldQueueDialog(Activity activity, DeezerQueueController controller) {
        super(activity); this.activity = activity; this.controller = controller;
    }
    @Override protected void onCreate(Bundle saved) {
        super.onCreate(saved); requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel = new LinearLayout(getContext()); panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(20), dp(24), dp(16));
        TextView heading = text("Queue", 25, Color.WHITE);
        heading.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        panel.addView(heading, wrap());
        subtitle = text("", 15, Color.LTGRAY); subtitle.setSingleLine(true); subtitle.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams sub = wrap(); sub.topMargin = dp(4); sub.bottomMargin = dp(16);
        panel.addView(subtitle, sub);
        list = new ListView(getContext()); list.setId(View.generateViewId());
        list.setBackgroundColor(Color.TRANSPARENT); list.setDivider(null); list.setDividerHeight(dp(3));
        list.setCacheColorHint(Color.TRANSPARENT); list.setVerticalScrollBarEnabled(true);
        list.setSelector(FocusChrome.filled(getContext(), Color.rgb(38, 38, 38), 8, true));
        list.setDrawSelectorOnTop(false); list.setChoiceMode(ListView.CHOICE_MODE_NONE);
        list.setFocusable(true); list.setItemsCanFocus(false);
        adapter = new Rows(); list.setAdapter(adapter);
        list.setOnItemClickListener((parent, row, position, id) -> {
            if (!(row.getTag() instanceof Holder)) return;
            Holder clicked = (Holder) row.getTag();
            DeezerQueueController.Result result = controller.select(clicked.boundState, clicked.boundRow);
            if (result == DeezerQueueController.Result.STALE)
                Toast.makeText(getContext(), "The queue changed. Select a track from the updated list.", Toast.LENGTH_SHORT).show();
            else if (result == DeezerQueueController.Result.UNAVAILABLE)
                Toast.makeText(getContext(), "This queue is not ready for track selection.", Toast.LENGTH_SHORT).show();
        });
        panel.addView(list, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        footer = text("Select a track. Press Back to return to Now Playing.", 13, Color.LTGRAY);
        footer.setMaxLines(2); LinearLayout.LayoutParams foot = wrap(); foot.topMargin = dp(14);
        panel.addView(footer, foot); setContentView(panel);
        setCanceledOnTouchOutside(true);
        Window window = getWindow();
        if (window != null) {
            GradientDrawable surface = new GradientDrawable(); surface.setColor(Color.rgb(24, 24, 24));
            surface.setCornerRadius(dp(14)); surface.setStroke(dp(1), Color.rgb(58, 58, 58));
            window.setBackgroundDrawable(surface);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND); window.setDimAmount(0.65f);
            window.setLayout(Math.min(dp(680), activity.getResources().getDisplayMetrics().widthPixels - dp(56)),
                    Math.min(dp(540), activity.getResources().getDisplayMetrics().heightPixels - dp(64)));
        }
    }
    @Override protected void onStart() {
        super.onStart(); unsubscribe = controller.subscribe(this::render);
    }
    @Override protected void onStop() {
        if (unsubscribe != null) unsubscribe.run(); unsubscribe = null;
        super.onStop();
    }
    private void render(DeezerQueueController.State state) {
        if (!state.allowed) { dismiss(); return; }
        long selectedId = list.getSelectedItemId(); int oldPosition = list.getSelectedItemPosition();
        boolean changed = !adapter.rows.equals(state.rows);
        shown = state;
        subtitle.setText((state.title.isEmpty() ? "Current queue" : state.title) + "  Â·  " + state.rows.size() + " tracks available");
        adapter.rows = state.rows; adapter.notifyDataSetChanged();
        footer.setText(state.pending ? "Starting the selected trackâ€¦" : !state.message.isEmpty() ? state.message
                : !state.visible ? "Updating Deezer's queueâ€¦" : !state.canSelect ? "Deezer is not exposing track selection. Press Back to return."
                : "Showing Deezer's available queue. Select a track, or press Back.");
        if (initialSelection && state.visible) {
            initialSelection = false; int index = state.indexOf(state.activeId);
            list.requestFocus(); list.setSelection(Math.max(0, index));
        } else if (changed && !state.rows.isEmpty()) {
            int index = state.indexOf(selectedId);
            list.setSelection(index >= 0 ? index : Math.max(0, Math.min(oldPosition, state.rows.size() - 1)));
        }
    }
    private final class Rows extends BaseAdapter {
        List<DeezerQueueController.Row> rows = Collections.emptyList();
        @Override public int getCount() { return rows.size(); }
        @Override public Object getItem(int position) { return rows.get(position); }
        @Override public long getItemId(int position) { return rows.get(position).id; }
        @Override public boolean hasStableIds() { return true; }
        @Override public View getView(int position, View recycled, ViewGroup parent) {
            Holder h;
            if (recycled == null) {
                LinearLayout row = new LinearLayout(getContext()); row.setOrientation(LinearLayout.HORIZONTAL);
                row.setGravity(Gravity.CENTER_VERTICAL); row.setPadding(dp(14), dp(8), dp(14), dp(8));
                row.setLayoutParams(new android.widget.AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64)));
                LinearLayout lines = new LinearLayout(getContext()); lines.setOrientation(LinearLayout.VERTICAL);
                TextView title = text("", 18, Color.WHITE), artist = text("", 14, Color.LTGRAY);
                title.setSingleLine(true); title.setEllipsize(TextUtils.TruncateAt.END);
                artist.setSingleLine(true); artist.setEllipsize(TextUtils.TruncateAt.END);
                lines.addView(title, wrap()); lines.addView(artist, wrap());
                row.addView(lines, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                TextView status = text("", 12, Color.WHITE); status.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                LinearLayout.LayoutParams badge = new LinearLayout.LayoutParams(dp(82), ViewGroup.LayoutParams.WRAP_CONTENT); badge.leftMargin = dp(12);
                row.addView(status, badge); h = new Holder(title, artist, status); row.setTag(h); recycled = row;
            } else h = (Holder) recycled.getTag();
            DeezerQueueController.Row item = rows.get(position);
            h.boundState = shown; h.boundRow = item;
            boolean current = shown != null && shown.visible && item.id == shown.activeId;
            boolean waiting = shown != null && shown.pending && item.id == shown.pendingId;
            int accent = FocusChrome.accentColor(getContext());
            h.title.setText(item.title); h.title.setTextColor(current ? accent : Color.WHITE);
            h.artist.setText(item.artist); h.status.setText(waiting ? "Startingâ€¦" : current ? "Current" : "");
            h.status.setTextColor(current ? accent : Color.LTGRAY);
            recycled.setContentDescription(item.title + ", " + item.artist + (current ? ", current track" : ""));
            return recycled;
        }
    }
    private static final class Holder {
        final TextView title, artist, status;
        DeezerQueueController.State boundState;
        DeezerQueueController.Row boundRow;
        Holder(TextView title, TextView artist, TextView status) { this.title = title; this.artist = artist; this.status = status; }
    }
    private LinearLayout.LayoutParams wrap() { return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT); }
    private TextView text(String value, int sp, int colour) {
        TextView text = new TextView(getContext()); text.setText(value); text.setTextSize(sp); text.setTextColor(colour); return text;
    }
    private int dp(int value) { return Math.round(value * getContext().getResources().getDisplayMetrics().density); }
}
