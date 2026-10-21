package com.boop.shieldhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.List;

/**
 * TV Settings screen for configuring remote hardware button remapping,
 * long-press actions, and the TV task switcher dismiss killer.
 * Ships with Ryan's reference living room configuration as the default.
 */
public final class BoopRemapperActivity extends Activity {
    private BoopKeyMappingStore store;
    private LinearLayout keysContainer;
    private int accentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);

        store = new BoopKeyMappingStore(this);
        accentColor = BoopTvChrome.accentColor(this);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.setPadding(dp(48), dp(36), dp(48), dp(36));

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        // Header
        TextView eyebrow = new TextView(this);
        eyebrow.setText("SHIELD REMOTE & INPUT");
        eyebrow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        eyebrow.setTextColor(accentColor);
        eyebrow.setTypeface(BoopTvChrome.FONT_MEDIUM);
        eyebrow.setLetterSpacing(0.08f);
        content.addView(eyebrow);

        TextView title = new TextView(this);
        title.setText("Button Remapper");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
        title.setTextColor(BoopTvChrome.TEXT_PRIMARY);
        title.setTypeface(BoopTvChrome.FONT_MEDIUM);
        LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tp.topMargin = dp(4);
        content.addView(title, tp);

        TextView subtitle = new TextView(this);
        subtitle.setText("Hardware button shortcuts, long-press actions, and task switcher killer.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        subtitle.setTextColor(BoopTvChrome.TEXT_SECONDARY);
        subtitle.setTypeface(BoopTvChrome.FONT_REGULAR);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sp.topMargin = dp(4);
        sp.bottomMargin = dp(20);
        content.addView(subtitle, sp);

        // Two-column layout
        LinearLayout columns = new LinearLayout(this);
        columns.setOrientation(LinearLayout.HORIZONTAL);

        // Left Column: Controls & System Actions
        LinearLayout leftCol = new LinearLayout(this);
        leftCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.42f);
        lp.rightMargin = dp(24);
        columns.addView(leftCol, lp);

        LinearLayout statusCard = sectionCard("SYSTEM & CONTROLS");

        TextView killerStatus = actionRow(
                "Task Switcher Killer: ACTIVE",
                "Dismissing an app in recents executes instant loopback force-stop");
        statusCard.addView(killerStatus);
        addCardSpacer(statusCard, 10);

        TextView resetBtn = actionRow(
                "Reset to Defaults",
                "Restore living room defaults: Menu=Next/Prev, Rewind=Prev, Netflix=Sleep, Home=Flow");
        resetBtn.setOnClickListener(v -> {
            store.resetToDefaults();
            refreshKeysList();
            Toast.makeText(this, "Reset all mappings to defaults", Toast.LENGTH_SHORT).show();
        });
        statusCard.addView(resetBtn);
        addCardSpacer(statusCard, 10);

        TextView evictBtn = actionRow(
                "Evict Button Mapper",
                "Disable flar2.homebutton and remove from accessibility services");
        evictBtn.setOnClickListener(v -> {
            BoopTaskKiller.evictButtonMapper(this, () -> {
                Toast.makeText(this, "Button Mapper evicted cleanly", Toast.LENGTH_SHORT).show();
            });
        });
        statusCard.addView(evictBtn);

        leftCol.addView(statusCard);

        // Right Column: Configured Remote Buttons
        ScrollView rightScroll = new ScrollView(this);
        rightScroll.setVerticalScrollBarEnabled(false);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.58f);
        columns.addView(rightScroll, rp);

        keysContainer = new LinearLayout(this);
        keysContainer.setOrientation(LinearLayout.VERTICAL);
        rightScroll.addView(keysContainer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        content.addView(columns, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        root.addView(content);
        setContentView(root);

        refreshKeysList();
        BoopTvChrome.install(this);
    }

    private void refreshKeysList() {
        keysContainer.removeAllViews();
        List<BoopKeyMappingStore.KeyEntry> entries = store.loadAll();

        for (BoopKeyMappingStore.KeyEntry entry : entries) {
            LinearLayout keyCard = sectionCard(entry.name.toUpperCase(java.util.Locale.ROOT));

            // Single Tap Row
            String singleLabel = "Single Tap: " + BoopKeyMappingStore.actionLabel(entry.singleTapAction);
            TextView singleRow = actionRow(singleLabel, "Press SELECT to change single-tap action");
            singleRow.setOnClickListener(v -> showActionPicker(entry, false));
            keyCard.addView(singleRow);
            addCardSpacer(keyCard, 8);

            // Long Press Row
            String longLabel = "Long Press (400ms): " + BoopKeyMappingStore.actionLabel(entry.longPressAction);
            TextView longRow = actionRow(longLabel, "Press SELECT to change long-press action");
            longRow.setOnClickListener(v -> showActionPicker(entry, true));
            keyCard.addView(longRow);

            keysContainer.addView(keyCard);
            addCardSpacer(keysContainer, 14);
        }
    }

    private void showActionPicker(BoopKeyMappingStore.KeyEntry entry, boolean isLongPress) {
        List<String> actions = BoopKeyMappingStore.availableActions();
        String[] labels = new String[actions.size()];
        int selectedIndex = 0;
        String currentAction = isLongPress ? entry.longPressAction : entry.singleTapAction;

        for (int i = 0; i < actions.size(); i++) {
            labels[i] = BoopKeyMappingStore.actionLabel(actions.get(i));
            if (actions.get(i).equals(currentAction)) {
                selectedIndex = i;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(entry.name + " (" + (isLongPress ? "Long Press" : "Single Tap") + ")")
                .setSingleChoiceItems(labels, selectedIndex, (dialog, which) -> {
                    String chosenAction = actions.get(which);
                    if (isLongPress) {
                        store.saveEntry(entry.keyCode, entry.singleTapAction, chosenAction, entry.targetComponent);
                    } else {
                        store.saveEntry(entry.keyCode, chosenAction, entry.longPressAction, entry.targetComponent);
                    }
                    dialog.dismiss();
                    refreshKeysList();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private LinearLayout sectionCard(String heading) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(BoopTvChrome.cardSurface(this, 16));

        TextView hdr = new TextView(this);
        hdr.setText(heading);
        hdr.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        hdr.setTypeface(BoopTvChrome.FONT_MEDIUM);
        hdr.setLetterSpacing(0.08f);
        hdr.setTextColor(accentColor);
        LinearLayout.LayoutParams hp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        hp.bottomMargin = dp(10);
        card.addView(hdr, hp);
        return card;
    }

    private TextView actionRow(String title, String subtitle) {
        TextView view = new TextView(this);
        view.setText(title + "\n" + subtitle);
        view.setPadding(dp(14), dp(10), dp(14), dp(10));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13.5f);
        view.setFocusable(true);
        view.setClickable(true);
        return view;
    }

    private void addCardSpacer(LinearLayout container, int dpVal) {
        View spacer = new View(this);
        container.addView(spacer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(dpVal)));
    }

    private int dp(int val) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics()));
    }
}
