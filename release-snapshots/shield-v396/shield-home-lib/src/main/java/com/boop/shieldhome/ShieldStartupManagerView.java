package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StrikethroughSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** MSConfig-style 10-foot Android TV Startup & Silent Batch Uninstall table. */
public final class ShieldStartupManagerView extends LinearLayout {
    public interface Callbacks {
        void onOpenPackages(StartupManagerUiModel.Mode mode);
        void onOpenRestore();
        void onCheckLocalLink();
        void onRunNow();
        void onFilter(StartupManagerUiModel.Filter filter);
        void onPackageInfo(String packageName);
        void onPrimary(String packageName);
        void onToggleBoot(String packageName, boolean enabled);
        void onToggleBackground(String packageName, boolean enabled);
        void onForceStop(String packageName);
        void onProtected(String packageName, String reason);
        void onPackageFocus(String packageName, int actionIndex);
        void onToggleRestoreSelection(String packageName, boolean selected);
        void onRestoreOne(String packageName);
        void onRestoreSelected();
        void onBack();
        default void onOpenOverview() { onBack(); }
        default void onSetAuto(boolean enabled) { }
        default void onRefresh() { }
        default void onBoopDefaults(boolean undo) { }
        default void onToggleRipSelect(String packageName, boolean selected) { }
        default void onSelectAllUser(boolean selected) { }
        default void onRipOutSelected() { }
    }

    private static final int MUTED = BoopTvChrome.TEXT_SECONDARY;
    private static final int PANEL = BoopTvChrome.SURFACE_PANEL;
    private static final int CARD = BoopTvChrome.SURFACE_CARD;

    private static final int COLOR_EMERALD = Color.rgb(52, 211, 153);
    private static final int COLOR_AMBER = Color.rgb(251, 191, 36);
    private static final int COLOR_ROSE_TEXT = Color.rgb(248, 113, 113);
    private static final int COLOR_CRIMSON_FILL = Color.rgb(185, 28, 28);
    private static final int COLOR_CRIMSON_ACTIVE = Color.rgb(220, 38, 38);
    private static final int COLOR_ROW_ENABLED = Color.rgb(15, 18, 24);
    private static final int COLOR_ROW_DISABLED = Color.rgb(10, 12, 16);
    private static final int COLOR_ROW_BORDER = Color.rgb(32, 37, 49);
    private static final int COLOR_ROW_RIP_BORDER = Color.rgb(127, 29, 29);

    private LinearLayout body;
    private final Map<String, String> packageLabels = new HashMap<>();
    private boolean busy;
    private String status = "";

    public ShieldStartupManagerView(Context context) {
        super(context);
        BoopTvChrome.applyShieldMetrics(context);
        setOrientation(VERTICAL);
        setBackgroundColor(Color.BLACK);
        setPadding(dp(38), dp(20), dp(38), dp(18));
    }

    public void setPackageLabels(List<StartupPackageState> rows) {
        if (rows == null) return;
        for (var row : rows) packageLabels.put(row.packageName(), row.label());
    }

    public void setStatus(String text, boolean working) {
        status = text == null ? "" : text;
        busy = working;
    }

    public void renderOverview(boolean linkReady, boolean auto, int managedCount, String lastSummary, Callbacks callbacks) {
        renderPackages(List.of(), List.of(), StartupManagerUiModel.Filter.ALL, StartupManagerUiModel.Mode.DISABLE,
                AndroidRecoveryCapabilities.resolve(getContext()), Set.of(), null, 0, callbacks);
    }

    public void renderPackages(List<StartupPackageState> rows, StartupManagerUiModel.Filter filter,
            StartupManagerUiModel.Mode mode, StartupRecoveryPolicy.RecoveryCapabilities caps,
            String focusPackage, int focusAction, Callbacks callbacks) {
        renderPackages(rows, rows, filter, mode, caps, Set.of(), focusPackage, focusAction, callbacks);
    }

    public void renderPackages(
            List<StartupPackageState> rows,
            List<StartupPackageState> allRows,
            StartupManagerUiModel.Filter filter,
            StartupManagerUiModel.Mode mode,
            StartupRecoveryPolicy.RecoveryCapabilities caps,
            Set<String> ripSelected,
            String focusPackage,
            int focusAction,
            Callbacks callbacks) {
        removeAllViews();
        body = this;

        List<StartupPackageState> inventory = (allRows != null && !allRows.isEmpty()) ? allRows : rows;
        int enabledCount = 0;
        int disabledCount = 0;
        int userCount = 0;
        int systemCount = 0;
        for (StartupPackageState item : inventory) {
            if (StartupManagerUiModel.isDisabled(item)) disabledCount++;
            else enabledCount++;
            if (item.systemApp()) systemCount++;
            else userCount++;
        }

        // --- 1. TOP HEADER BAR ---
        LinearLayout topHeader = row();
        topHeader.setGravity(Gravity.CENTER_VERTICAL);

        TextView boltBadge = text("\u26A1", 18, true);
        boltBadge.setGravity(Gravity.CENTER);
        boltBadge.setTextColor(FocusChrome.accentColor(getContext()));
        GradientDrawable boltBg = new GradientDrawable();
        boltBg.setCornerRadius(dp(10));
        boltBg.setColor(Color.rgb(20, 24, 32));
        boltBg.setStroke(Math.max(1, dp(1)), FocusChrome.accentColor(getContext()));
        boltBadge.setBackground(boltBg);
        topHeader.addView(boltBadge, new LayoutParams(dp(40), dp(40)));
        hspace(topHeader, 12);

        LinearLayout titleCol = column();
        TextView title = text("STARTUP & UNINSTALL MANAGER", 20, true);
        TextView subtitle = caption("Toggle Startup to enable or disable  \u2022  Tick Rip Out on multiple apps to silently batch remove", 12);
        titleCol.addView(title);
        titleCol.addView(subtitle);
        topHeader.addView(titleCol, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView allowedBadge = pillBadge("\u25CF " + enabledCount + " Allowed", COLOR_EMERALD);
        TextView disabledBadge = pillBadge("\u25CB " + disabledCount + " Disabled", COLOR_AMBER);
        topHeader.addView(allowedBadge, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(34)));
        hspace(topHeader, 8);
        topHeader.addView(disabledBadge, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(34)));
        body.addView(topHeader, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        space(body, 10);

        // --- 2. FILTER TABS & REFRESH STRIP ---
        LinearLayout filterStrip = row();
        List<TextView> filterButtons = new ArrayList<>();
        StartupManagerUiModel.Filter[] shownFilters = {
            StartupManagerUiModel.Filter.ALL,
            StartupManagerUiModel.Filter.USER,
            StartupManagerUiModel.Filter.DISABLED,
            StartupManagerUiModel.Filter.SYSTEM
        };
        String[] filterLabels = {
            "All Apps (" + inventory.size() + ")",
            "User Installed (" + userCount + ")",
            "Disabled (" + disabledCount + ")",
            "System (" + systemCount + ")"
        };
        for (int i = 0; i < shownFilters.length; i++) {
            final StartupManagerUiModel.Filter option = shownFilters[i];
            boolean active = option == filter;
            TextView chip = filterButton(filterLabels[i], "startup:filter:" + option, active, () -> callbacks.onFilter(option));
            filterStrip.addView(chip, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(38)));
            hspace(filterStrip, 8);
            filterButtons.add(chip);
        }
        TextView refreshBtn = filterButton("Refresh", "startup:packages:refresh", false, callbacks::onRefresh);
        filterStrip.addView(refreshBtn, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(38)));
        filterButtons.add(refreshBtn);

        View spacer = new View(getContext());
        filterStrip.addView(spacer, new LayoutParams(0, 1, 1f));

        TextView dpadHint = caption("[OK] Toggle Box   [Right] Rip Out Box   [Down] Next App", 11);
        filterStrip.addView(dpadHint, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        body.addView(filterStrip, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        space(body, 10);

        // --- 3. MSCONFIG COLUMN HEADERS ---
        LinearLayout colHeader = row();
        colHeader.setPadding(dp(12), dp(4), dp(14), dp(6));

        TextView hStartup = caption("STARTUP", 11);
        hStartup.setTypeface(BoopTvChrome.FONT_MEDIUM);
        hStartup.setGravity(Gravity.CENTER);
        colHeader.addView(hStartup, new LayoutParams(dp(116), LayoutParams.WRAP_CONTENT));
        hspace(colHeader, 10);

        TextView hRip = caption("SELECT RIP OUT", 11);
        hRip.setTypeface(BoopTvChrome.FONT_MEDIUM);
        hRip.setTextColor(COLOR_ROSE_TEXT);
        hRip.setGravity(Gravity.CENTER);
        colHeader.addView(hRip, new LayoutParams(dp(126), LayoutParams.WRAP_CONTENT));
        hspace(colHeader, 16);

        TextView hApp = caption("APPLICATION & PACKAGE ID", 11);
        hApp.setTypeface(BoopTvChrome.FONT_MEDIUM);
        colHeader.addView(hApp, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView hType = caption("STARTUP STATUS", 11);
        hType.setTypeface(BoopTvChrome.FONT_MEDIUM);
        hType.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        colHeader.addView(hType, new LayoutParams(dp(170), LayoutParams.WRAP_CONTENT));

        body.addView(colHeader, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        View headerRule = new View(getContext());
        headerRule.setBackgroundColor(Color.rgb(26, 31, 41));
        body.addView(headerRule, new LayoutParams(LayoutParams.MATCH_PARENT, Math.max(1, dp(1))));
        space(body, 6);

        // --- 4. SCROLLABLE CHECKBOX TABLE ---
        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(OVER_SCROLL_NEVER);
        LinearLayout list = column();
        scroll.addView(list);
        body.addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));

        List<List<TextView>> matrix = new ArrayList<>();
        List<String> ids = new ArrayList<>();

        for (StartupPackageState state : rows) {
            String pkg = state.packageName();
            ids.add(pkg);
            var risk = StartupRecoveryPolicy.assess(state, caps);
            boolean locked = risk.protectedPackage();
            boolean disabled = StartupManagerUiModel.isDisabled(state);
            boolean queuedForRip = ripSelected != null && ripSelected.contains(pkg);

            LinearLayout rowCard = row();
            rowCard.setPadding(dp(12), dp(7), dp(14), dp(7));
            GradientDrawable rowBg = new GradientDrawable();
            rowBg.setCornerRadius(dp(11));
            rowBg.setColor(disabled ? COLOR_ROW_DISABLED : COLOR_ROW_ENABLED);
            rowBg.setStroke(Math.max(1, dp(1)), queuedForRip ? COLOR_ROW_RIP_BORDER : COLOR_ROW_BORDER);
            rowCard.setBackground(rowBg);

            // Column 0: Startup Checkbox ([✓] ON / [ ] OFF)
            Runnable startupAction = locked
                    ? () -> callbacks.onProtected(pkg, risk.protectionReason())
                    : () -> callbacks.onPrimary(pkg);
            TextView startupBox = startupCheckbox(state, !disabled, locked, tag(state, 0), startupAction);
            rowCard.addView(startupBox, new LayoutParams(dp(116), dp(44)));
            hspace(rowCard, 10);

            // Column 1: Multi-Select Rip Out Checkbox ([✕] RIP / [ ] Rip)
            Runnable ripAction = locked
                    ? () -> callbacks.onProtected(pkg, risk.protectionReason())
                    : () -> callbacks.onToggleRipSelect(pkg, !queuedForRip);
            TextView ripBox = ripCheckbox(queuedForRip, locked, tag(state, 1), ripAction);
            rowCard.addView(ripBox, new LayoutParams(dp(126), dp(44)));
            hspace(rowCard, 16);

            // Column 2: Application Name + Package ID
            TextView appInfo = appTitleView(state.label(), pkg, disabled);
            rowCard.addView(appInfo, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
            hspace(rowCard, 12);

            // Column 3: Status / Type Pill
            TextView statusPill = rowStatusText(state, disabled, locked, queuedForRip);
            rowCard.addView(statusPill, new LayoutParams(dp(170), LayoutParams.WRAP_CONTENT));

            List<TextView> controls = List.of(startupBox, ripBox);
            for (int c = 0; c < controls.size(); c++) {
                final int colIndex = c;
                final TextView box = controls.get(c);
                box.setOnFocusChangeListener((v, focused) -> {
                    if (colIndex == 0) styleStartupCheckbox(box, !disabled, locked, focused);
                    else styleRipCheckbox(box, queuedForRip, locked, focused);
                    if (focused) callbacks.onPackageFocus(pkg, colIndex);
                });
            }

            matrix.add(controls);
            list.addView(rowCard, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            space(list, 6);
        }

        if (rows.isEmpty()) {
            SpaceOrMessage(list, busy ? "Reading installed packages on Shield..." : "No packages match this filter.");
        }

        space(body, 10);

        // --- 5. BOTTOM ACTION BAR + BIG SILENT RIP-OUT BUTTON ---
        LinearLayout bottomBar = row();
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);

        TextView selectAllBtn = actionButton("Select All User Apps", "startup:packages:select-all",
                () -> callbacks.onSelectAllUser(true));
        TextView clearBtn = actionButton("Clear Selection", "startup:packages:clear-all",
                () -> callbacks.onSelectAllUser(false));
        TextView backBtn = actionButton("Back", "startup:packages:back", callbacks::onBack);

        bottomBar.addView(selectAllBtn, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(46)));
        hspace(bottomBar, 8);
        bottomBar.addView(clearBtn, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(46)));
        hspace(bottomBar, 8);
        bottomBar.addView(backBtn, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(46)));

        View bottomSpacer = new View(getContext());
        bottomBar.addView(bottomSpacer, new LayoutParams(0, 1, 1f));

        int ripCount = ripSelected == null ? 0 : ripSelected.size();
        TextView bigRipButton = bigRipOutButton(ripCount, "startup:packages:rip-out", callbacks::onRipOutSelected);
        bottomBar.addView(bigRipButton, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(50)));

        body.addView(bottomBar, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // --- 6. STATUS FOOTER ---
        footer();

        // --- 7. D-PAD MATRIX NAVIGATION WIRING ---
        wireTableNavigation(matrix, ids, filterButtons, selectAllBtn, clearBtn, backBtn, bigRipButton,
                focusPackage, focusAction);
    }

    public void renderRestore(List<StartupRestoreRecord> records, Set<String> selected, String focusPackage, Callbacks callbacks) {
        removeAllViews();
        body = this;
        body.addView(text("Restore Saved Settings", 22, true));
        body.addView(caption("Original package states saved before BOOP changes.", 13));
        space(body, 12);

        TextView batch = actionButton("Restore Selected (" + selected.size() + ")", "startup:restore:batch", callbacks::onRestoreSelected);
        body.addView(batch, new LayoutParams(LayoutParams.MATCH_PARENT, dp(46)));
        space(body, 10);

        ScrollView scroll = new ScrollView(getContext());
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setOverScrollMode(OVER_SCROLL_NEVER);
        LinearLayout list = column();
        for (StartupRestoreRecord record : records) {
            String pkg = record.packageName();
            LinearLayout card = row();
            String time = new SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()).format(new Date(record.firstChangedAtMillis()));
            TextView select = actionButton((selected.contains(pkg) ? "[\u2713]  " : "[ ]  ")
                    + packageLabels.getOrDefault(pkg, pkg) + "  (" + pkg + " \u2022 " + time + ")",
                    "startup:restore:" + pkg + ":0",
                    () -> callbacks.onToggleRestoreSelection(pkg, !selected.contains(pkg)));
            TextView now = actionButton("Restore", "startup:restore:" + pkg + ":1", () -> callbacks.onRestoreOne(pkg));
            card.addView(select, new LayoutParams(0, dp(52), 1f));
            hspace(card, 10);
            card.addView(now, new LayoutParams(dp(120), dp(52)));
            list.addView(card, new LayoutParams(LayoutParams.MATCH_PARENT, dp(52)));
            space(list, 8);
        }
        if (records.isEmpty()) {
            list.addView(caption("Nothing to undo. No saved package changes.", 16));
        }
        scroll.addView(list);
        body.addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
        space(body, 10);

        TextView back = actionButton("Back", "startup:restore:back", callbacks::onBack);
        body.addView(back, new LayoutParams(LayoutParams.MATCH_PARENT, dp(46)));
        footer();
        post(batch::requestFocus);
    }

    private void wireTableNavigation(
            List<List<TextView>> matrix,
            List<String> ids,
            List<TextView> filterButtons,
            TextView selectAllBtn,
            TextView clearBtn,
            TextView backBtn,
            TextView bigRipButton,
            String focusPackage,
            int focusAction) {

        for (int i = 0; i < filterButtons.size(); i++) {
            TextView chip = filterButtons.get(i);
            chip.setNextFocusLeftId(i == 0 ? chip.getId() : filterButtons.get(i - 1).getId());
            chip.setNextFocusRightId(i == filterButtons.size() - 1 ? chip.getId() : filterButtons.get(i + 1).getId());
            chip.setNextFocusUpId(chip.getId());
            if (!matrix.isEmpty()) {
                chip.setNextFocusDownId(matrix.get(0).get(0).getId());
            } else {
                chip.setNextFocusDownId(selectAllBtn.getId());
            }
        }

        List<TextView> bottomControls = List.of(selectAllBtn, clearBtn, backBtn, bigRipButton);
        for (int i = 0; i < bottomControls.size(); i++) {
            TextView ctrl = bottomControls.get(i);
            ctrl.setNextFocusLeftId(i == 0 ? ctrl.getId() : bottomControls.get(i - 1).getId());
            ctrl.setNextFocusRightId(i == bottomControls.size() - 1 ? ctrl.getId() : bottomControls.get(i + 1).getId());
            ctrl.setNextFocusDownId(ctrl.getId());
            if (!matrix.isEmpty()) {
                int targetCol = (ctrl == bigRipButton) ? 1 : 0;
                ctrl.setNextFocusUpId(matrix.get(matrix.size() - 1).get(targetCol).getId());
            } else if (!filterButtons.isEmpty()) {
                ctrl.setNextFocusUpId(filterButtons.get(0).getId());
            }
        }

        for (int r = 0; r < matrix.size(); r++) {
            final int rr = r;
            List<TextView> rowControls = matrix.get(r);
            for (int c = 0; c < rowControls.size(); c++) {
                final int cc = c;
                rowControls.get(c).setOnKeyListener((v, key, event) -> {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                    if ((key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER) && event.getRepeatCount() > 0) {
                        return true;
                    }
                    switch (key) {
                        case KeyEvent.KEYCODE_DPAD_UP -> {
                            if (rr == 0) {
                                filterButtons.get(0).requestFocus();
                            } else {
                                matrix.get(rr - 1).get(cc).requestFocus();
                            }
                            return true;
                        }
                        case KeyEvent.KEYCODE_DPAD_DOWN -> {
                            if (rr == matrix.size() - 1) {
                                if (cc == 1) bigRipButton.requestFocus();
                                else selectAllBtn.requestFocus();
                            } else {
                                matrix.get(rr + 1).get(cc).requestFocus();
                            }
                            return true;
                        }
                        case KeyEvent.KEYCODE_DPAD_LEFT -> {
                            if (cc > 0) {
                                rowControls.get(cc - 1).requestFocus();
                            }
                            return true;
                        }
                        case KeyEvent.KEYCODE_DPAD_RIGHT -> {
                            if (cc < rowControls.size() - 1) {
                                rowControls.get(cc + 1).requestFocus();
                            } else {
                                bigRipButton.requestFocus();
                            }
                            return true;
                        }
                        default -> {
                            return false;
                        }
                    }
                });
            }
        }

        if (!matrix.isEmpty()) {
            int rowIdx = focusPackage == null ? 0 : Math.max(0, ids.indexOf(focusPackage));
            int colIdx = Math.min(Math.max(0, focusAction), matrix.get(rowIdx).size() - 1);
            TextView target = matrix.get(rowIdx).get(colIdx);
            post(target::requestFocus);
        } else {
            post(backBtn::requestFocus);
        }
    }

    private TextView startupCheckbox(StartupPackageState state, boolean enabled, boolean locked, String tag, Runnable action) {
        TextView v = new TextView(getContext());
        BoopTvChrome.markCustomStyled(v);
        v.setTypeface(BoopTvChrome.FONT_MEDIUM);
        v.setTextSize(13);
        v.setGravity(Gravity.CENTER);
        v.setSingleLine(true);
        v.setDefaultFocusHighlightEnabled(false);
        v.setFocusable(true);
        v.setClickable(true);
        v.setId(View.generateViewId());
        v.setTag(tag);
        v.setPadding(dp(10), dp(6), dp(10), dp(6));
        styleStartupCheckbox(v, enabled, locked, false);
        v.setOnClickListener(ignored -> action.run());
        return v;
    }

    private void styleStartupCheckbox(TextView v, boolean enabled, boolean locked, boolean focused) {
        int accent = FocusChrome.accentColor(getContext());
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(9));
        if (locked) {
            v.setText("\uD83D\uDD12 CORE");
            v.setTextColor(focused ? Color.WHITE : Color.rgb(95, 103, 117));
            bg.setColor(focused ? Color.rgb(28, 33, 43) : Color.rgb(16, 19, 25));
            bg.setStroke(Math.max(1, dp(focused ? 2 : 1)), focused ? accent : Color.rgb(36, 42, 54));
        } else if (enabled) {
            v.setText("[\u2713]  ON");
            v.setTextColor(focused ? Color.WHITE : accent);
            bg.setColor(focused ? Color.rgb(29, 35, 46) : Color.rgb(18, 23, 31));
            bg.setStroke(Math.max(1, dp(focused ? 3 : 1)), accent);
        } else {
            v.setText("[  ]  OFF");
            v.setTextColor(focused ? Color.WHITE : Color.rgb(122, 130, 148));
            bg.setColor(focused ? Color.rgb(29, 35, 46) : Color.rgb(14, 17, 23));
            bg.setStroke(Math.max(1, dp(focused ? 3 : 1)), focused ? accent : Color.rgb(45, 52, 66));
        }
        v.setBackground(bg);
        v.setPadding(dp(10), dp(6), dp(10), dp(6));
    }

    private TextView ripCheckbox(boolean queued, boolean locked, String tag, Runnable action) {
        TextView v = new TextView(getContext());
        BoopTvChrome.markCustomStyled(v);
        v.setTypeface(BoopTvChrome.FONT_MEDIUM);
        v.setTextSize(13);
        v.setGravity(Gravity.CENTER);
        v.setSingleLine(true);
        v.setDefaultFocusHighlightEnabled(false);
        v.setFocusable(true);
        v.setClickable(true);
        v.setId(View.generateViewId());
        v.setTag(tag);
        v.setPadding(dp(10), dp(6), dp(10), dp(6));
        styleRipCheckbox(v, queued, locked, false);
        v.setOnClickListener(ignored -> action.run());
        return v;
    }

    private void styleRipCheckbox(TextView v, boolean queued, boolean locked, boolean focused) {
        int accent = FocusChrome.accentColor(getContext());
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(9));
        if (locked) {
            v.setText("\uD83D\uDD12 System");
            v.setTextColor(focused ? Color.WHITE : Color.rgb(82, 89, 102));
            bg.setColor(focused ? Color.rgb(26, 30, 39) : Color.rgb(13, 15, 20));
            bg.setStroke(Math.max(1, dp(focused ? 2 : 1)), focused ? accent : Color.rgb(32, 37, 48));
        } else if (queued) {
            v.setText("[\u2715] RIP");
            v.setTextColor(Color.WHITE);
            bg.setColor(focused ? COLOR_CRIMSON_ACTIVE : COLOR_CRIMSON_FILL);
            bg.setStroke(Math.max(1, dp(focused ? 3 : 1)), focused ? Color.WHITE : COLOR_ROSE_TEXT);
        } else {
            v.setText("[  ] Rip");
            v.setTextColor(focused ? Color.WHITE : Color.rgb(148, 156, 172));
            bg.setColor(focused ? Color.rgb(36, 22, 26) : Color.rgb(16, 19, 25));
            bg.setStroke(Math.max(1, dp(focused ? 3 : 1)), focused ? COLOR_ROSE_TEXT : Color.rgb(47, 54, 68));
        }
        v.setBackground(bg);
        v.setPadding(dp(10), dp(6), dp(10), dp(6));
    }

    private TextView bigRipOutButton(int count, String tag, Runnable action) {
        String label = count > 0
                ? "\uD83D\uDCA5  SILENTLY RIP OUT " + count + (count == 1 ? " APP" : " APPS") + " (ZERO CLICKING)"
                : "\uD83D\uDDD1  SILENTLY RIP OUT (0 SELECTED)";
        TextView v = new TextView(getContext());
        BoopTvChrome.markCustomStyled(v);
        v.setText(label);
        v.setTypeface(BoopTvChrome.FONT_MEDIUM);
        v.setTextSize(14);
        v.setGravity(Gravity.CENTER);
        v.setSingleLine(true);
        v.setDefaultFocusHighlightEnabled(false);
        v.setFocusable(true);
        v.setClickable(true);
        v.setId(View.generateViewId());
        v.setTag(tag);
        styleBigRipButton(v, count, false);
        v.setOnFocusChangeListener((view, focused) -> styleBigRipButton(v, count, focused));
        v.setOnClickListener(ignored -> action.run());
        return v;
    }

    private void styleBigRipButton(TextView v, int count, boolean focused) {
        int accent = FocusChrome.accentColor(getContext());
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(12));
        if (count > 0) {
            v.setTextColor(Color.WHITE);
            bg.setColor(focused ? Color.rgb(239, 68, 68) : COLOR_CRIMSON_ACTIVE);
            bg.setStroke(Math.max(1, dp(focused ? 3 : 1)), focused ? Color.WHITE : COLOR_ROSE_TEXT);
        } else {
            v.setTextColor(focused ? Color.WHITE : Color.rgb(120, 128, 142));
            bg.setColor(focused ? BoopTvChrome.SURFACE_ELEVATED : Color.rgb(20, 24, 32));
            bg.setStroke(Math.max(1, dp(focused ? 3 : 1)), focused ? accent : Color.rgb(42, 48, 60));
        }
        v.setBackground(bg);
        v.setPadding(dp(24), dp(10), dp(24), dp(10));
    }

    private TextView appTitleView(String label, String pkg, boolean disabled) {
        String full = label + "\n" + pkg;
        SpannableString span = new SpannableString(full);
        int split = full.indexOf('\n');
        if (disabled && split > 0) {
            span.setSpan(new StrikethroughSpan(), 0, split, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.rgb(142, 149, 165)), 0, split, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (split >= 0) {
            span.setSpan(new RelativeSizeSpan(0.78f), split + 1, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new ForegroundColorSpan(Color.rgb(112, 120, 136)), split + 1, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        TextView tv = text("", 15, false);
        tv.setTypeface(BoopTvChrome.FONT_MEDIUM);
        tv.setText(span);
        tv.setMaxLines(2);
        tv.setEllipsize(TextUtils.TruncateAt.END);
        return tv;
    }

    private TextView rowStatusText(StartupPackageState state, boolean disabled, boolean locked, boolean queuedForRip) {
        TextView tv = text("", 12, false);
        tv.setTypeface(BoopTvChrome.FONT_MEDIUM);
        tv.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        if (queuedForRip) {
            tv.setText("\u25CF Queued for Rip");
            tv.setTextColor(COLOR_ROSE_TEXT);
        } else if (locked) {
            tv.setText("\uD83D\uDD12 Protected");
            tv.setTextColor(Color.rgb(108, 116, 128));
        } else if (disabled) {
            tv.setText("\u25CB Disabled");
            tv.setTextColor(COLOR_AMBER);
        } else if (!state.systemApp()) {
            tv.setText("\u25CF User App");
            tv.setTextColor(COLOR_EMERALD);
        } else {
            tv.setText("\u25CF System App");
            tv.setTextColor(Color.rgb(148, 163, 184));
        }
        return tv;
    }

    private TextView pillBadge(String label, int textColor) {
        TextView tv = text(label, 12, false);
        tv.setTypeface(BoopTvChrome.FONT_MEDIUM);
        tv.setTextColor(textColor);
        tv.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setCornerRadius(dp(9));
        bg.setColor(Color.rgb(20, 24, 32));
        bg.setStroke(Math.max(1, dp(1)), Color.rgb(37, 44, 58));
        tv.setBackground(bg);
        tv.setPadding(dp(12), dp(4), dp(12), dp(4));
        return tv;
    }

    private TextView filterButton(String label, String tag, boolean active, Runnable action) {
        TextView v = actionButton(label, tag, action);
        v.setTextSize(13);
        if (active) {
            v.setTextColor(FocusChrome.accentColor(getContext()));
        }
        v.setPadding(dp(16), dp(6), dp(16), dp(6));
        return v;
    }

    private TextView actionButton(String label, String tag, Runnable action) {
        TextView v = text(label, 14, false);
        v.setTypeface(BoopTvChrome.FONT_MEDIUM);
        v.setGravity(Gravity.CENTER);
        v.setSingleLine(true);
        v.setDefaultFocusHighlightEnabled(false);
        v.setFocusable(true);
        v.setClickable(true);
        v.setId(View.generateViewId());
        v.setTag(tag);
        v.setContentDescription(label);
        v.setBackground(FocusChrome.filled(getContext(), CARD, 11, false));
        v.setPadding(dp(18), dp(8), dp(18), dp(8));
        v.setOnFocusChangeListener(this::focus);
        v.setOnClickListener(ignored -> action.run());
        return v;
    }

    private void SpaceOrMessage(LinearLayout list, String message) {
        space(list, 18);
        TextView empty = caption(message, 16);
        empty.setGravity(Gravity.CENTER);
        list.addView(empty, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private void footer() {
        space(body, 6);
        LinearLayout foot = row();
        foot.setGravity(Gravity.CENTER_VERTICAL);
        TextView result = caption((busy ? "Working: " : "") + status, 12);
        result.setSingleLine(true);
        result.setEllipsize(TextUtils.TruncateAt.END);
        result.setTextColor(busy ? FocusChrome.accentColor(getContext()) : MUTED);
        foot.addView(result, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        hspace(foot, 12);
        TextView rightHint = caption("Menu: Restore Originals", 11);
        foot.addView(rightHint, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        body.addView(foot, new LayoutParams(LayoutParams.MATCH_PARENT, dp(24)));
    }

    private void focus(View view, boolean focused) {
        int l = view.getPaddingLeft(), t = view.getPaddingTop(), r = view.getPaddingRight(), b = view.getPaddingBottom();
        view.setBackground(FocusChrome.filled(getContext(), focused ? BoopTvChrome.SURFACE_ELEVATED : CARD, 11, focused));
        view.setPadding(l, t, r, b);
    }

    private TextView text(String value, int size, boolean bold) {
        TextView v = new TextView(getContext());
        v.setText(value);
        v.setTextSize(size);
        v.setTextColor(Color.WHITE);
        v.setTypeface(bold ? BoopTvChrome.FONT_MEDIUM : BoopTvChrome.FONT_REGULAR);
        if (size >= 20) v.setLetterSpacing(-0.01f);
        return v;
    }

    private TextView caption(String value, int size) {
        TextView v = text(value, size, false);
        v.setTextColor(MUTED);
        return v;
    }

    private String tag(StartupPackageState s, int a) {
        return "startup:package:" + s.packageName() + ":" + a;
    }

    private LinearLayout column() {
        LinearLayout v = new LinearLayout(getContext());
        v.setOrientation(VERTICAL);
        return v;
    }

    private LinearLayout row() {
        LinearLayout v = new LinearLayout(getContext());
        v.setOrientation(HORIZONTAL);
        v.setGravity(Gravity.CENTER_VERTICAL);
        return v;
    }

    private void space(LinearLayout parent, int size) {
        parent.addView(new View(getContext()), new LayoutParams(1, dp(size)));
    }

    private void hspace(LinearLayout parent, int size) {
        parent.addView(new View(getContext()), new LayoutParams(dp(size), 1));
    }

    private int dp(int n) {
        Paint p = null;
        return Math.round(n * getResources().getDisplayMetrics().density);
    }
}
