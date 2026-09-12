package com.boop.shieldhome;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    }

    public ShieldStartupManagerView(Context context) {
        super(context);
        setOrientation(VERTICAL);
        setBackgroundColor(Color.BLACK);
        setPadding(dp(44), dp(26), dp(44), dp(26));
        setClipToPadding(false);
    }

    public void renderOverview(boolean hasIdentity, boolean bootEnabled, int managedCount,
                               String lastSummary, Callbacks callbacks) {
        removeAllViews();
        addView(text("Startup Manager", 34, true), wrap());
        addSpacer(4);
        addView(text("Control what starts, what stays, and what gets out of the way.", 17, false), wrap());
        addSpacer(16);
        addView(text("Local control: " + (hasIdentity ? "READY" : "APPROVAL NEEDED")
                + "    •    Managed packages: " + managedCount
                + "    •    Clean after boot: " + (bootEnabled ? "ON" : "OFF"), 15, false), wrap());
        addSpacer(16);

        TextView first = card("Disable apps", "Turn off junk packages, launchers and companion services",
                "startup:overview:disable", () -> callbacks.onOpenPackages(StartupManagerUiModel.Mode.DISABLE));
        addView(first, cardParams()); addSpacer(10);
        addView(card("Clean after boot", "Close selected packages once after startup",
                "startup:overview:boot", () -> callbacks.onOpenPackages(StartupManagerUiModel.Mode.BOOT_CLEAN)), cardParams()); addSpacer(10);
        addView(card("Prevent background start", "Stop selected packages waking in the background",
                "startup:overview:bg", () -> callbacks.onOpenPackages(StartupManagerUiModel.Mode.BACKGROUND)), cardParams()); addSpacer(10);
        addView(card("Restore changes", "Undo BOOP package disables and startup rules",
                "startup:overview:restore", callbacks::onOpenRestore), cardParams());
        addSpacer(16);

        LinearLayout utilities = row();
        utilities.addView(button(hasIdentity ? "Check local link" : "Approve local link", 260, 58,
                "startup:overview:link", callbacks::onCheckLocalLink));
        addHSpacer(10, utilities);
        utilities.addView(button("Run boot cleanup now", 300, 58,
                "startup:overview:run", callbacks::onRunNow));
        addHSpacer(10, utilities);
        utilities.addView(button("Back", 180, 58, "startup:overview:back", callbacks::onBack));
        addView(utilities, wrap());
        if (lastSummary != null && !lastSummary.isBlank()) {
            addSpacer(14);
            addView(text("Last result: " + lastSummary, 14, false), wrap());
        }
        first.post(first::requestFocus);
    }

    public void renderPackages(List<StartupPackageState> rows,
                               StartupManagerUiModel.Filter filter,
                               StartupManagerUiModel.Mode mode,
                               StartupRecoveryPolicy.RecoveryCapabilities capabilities,
                               String focusPackage, int focusAction, Callbacks callbacks) {
        removeAllViews();
        addView(text("Package Control", 31, true), wrap());
        addSpacer(2);
        addView(text(modeSubtitle(mode) + "    •    " + rows.size() + " shown", 15, false), wrap());
        addSpacer(12);

        HorizontalScrollView filterScroll = new HorizontalScrollView(getContext());
        filterScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout filterRow = row();
        List<TextView> filterViews = new ArrayList<>();
        for (StartupManagerUiModel.Filter option : StartupManagerUiModel.Filter.values()) {
            TextView chip = filterChip(option, option == filter, () -> callbacks.onFilter(option));
            filterViews.add(chip);
            filterRow.addView(chip);
            addHSpacer(8, filterRow);
        }
        filterScroll.addView(filterRow);
        addView(filterScroll, new LayoutParams(LayoutParams.MATCH_PARENT, dp(56)));
        addSpacer(8);

        ScrollView scroll = new ScrollView(getContext());
        scroll.setFillViewport(true);
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(getContext());
        list.setOrientation(VERTICAL);
        List<List<TextView>> matrix = new ArrayList<>();
        for (StartupPackageState state : rows) {
            StartupRecoveryPolicy.Assessment assessment = StartupRecoveryPolicy.assess(state, capabilities);
            boolean protectedPackage = assessment.protectedPackage();
            LinearLayout packageRow = row();
            packageRow.setGravity(Gravity.CENTER_VERTICAL);
            ArrayList<TextView> actions = new ArrayList<>();
            String info = state.label() + "\n" + state.packageName() + "  •  "
                    + (state.systemApp() ? "SYSTEM" : "USER") + "  •  " + assessment.impact();
            TextView infoView = button(info, 500, 76, "startup:package:" + state.packageName() + ":0",
                    () -> callbacks.onPackageInfo(state.packageName()));
            actions.add(infoView); packageRow.addView(infoView); addHSpacer(8, packageRow);

            String primary = protectedPackage ? "Protected" : StartupManagerUiModel.primaryAction(state);
            actions.add(rowAction(packageRow, primary, state.packageName(), 1,
                    protectedPackage ? () -> callbacks.onProtected(state.packageName(), assessment.protectionReason())
                            : () -> callbacks.onPrimary(state.packageName())));
            boolean boot = state.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
            actions.add(rowAction(packageRow, "Boot: " + (boot ? "ON" : "OFF"), state.packageName(), 2,
                    protectedPackage ? () -> callbacks.onProtected(state.packageName(), assessment.protectionReason())
                            : () -> callbacks.onToggleBoot(state.packageName(), !boot)));
            boolean bg = state.managedActions().contains(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
            actions.add(rowAction(packageRow, "BG: " + (bg ? "ON" : "OFF"), state.packageName(), 3,
                    protectedPackage ? () -> callbacks.onProtected(state.packageName(), assessment.protectionReason())
                            : () -> callbacks.onToggleBackground(state.packageName(), !bg)));
            actions.add(rowAction(packageRow, "Stop", state.packageName(), 4,
                    protectedPackage ? () -> callbacks.onProtected(state.packageName(), assessment.protectionReason())
                            : () -> callbacks.onForceStop(state.packageName())));
            for (int i = 0; i < actions.size(); i++) {
                final int actionIndex = i;
                actions.get(i).setOnFocusChangeListener(focusListener(() ->
                        callbacks.onPackageFocus(state.packageName(), actionIndex)));
            }
            matrix.add(actions);
            list.addView(packageRow, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(84)));
            addSpacerTo(list, 6);
        }
        if (rows.isEmpty()) {
            list.addView(text("No packages match this filter.", 18, false), wrap());
        }
        scroll.addView(list);
        LayoutParams scrollParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f);
        addView(scroll, scrollParams);
        addSpacer(8);
        TextView back = button("Back to Startup Manager", 330, 56, "startup:packages:back", callbacks::onBack);
        addView(back, wrap());

        wirePackageNavigation(matrix, filterViews, mode, focusPackage, focusAction);
    }

    public void renderRestore(List<StartupRestoreRecord> records, Set<String> selected,
                              String focusPackage, Callbacks callbacks) {
        removeAllViews();
        addView(text("Restore", 31, true), wrap());
        addSpacer(2);
        addView(text("Return packages to the exact state BOOP found before changing them.", 15, false), wrap());
        addSpacer(12);
        TextView batch = button("Restore selected (" + selected.size() + ")", 330, 58,
                "startup:restore:batch", callbacks::onRestoreSelected);
        addView(batch, wrap());
        addSpacer(10);
        ScrollView scroll = new ScrollView(getContext());
        scroll.setVerticalScrollBarEnabled(false);
        LinearLayout list = new LinearLayout(getContext());
        list.setOrientation(VERTICAL);
        List<List<TextView>> matrix = new ArrayList<>();
        for (StartupRestoreRecord record : records) {
            String pkg = record.packageName();
            boolean checked = selected.contains(pkg);
            LinearLayout restoreRow = row();
            ArrayList<TextView> controls = new ArrayList<>();
            TextView toggle = button((checked ? "[✓] " : "[ ] ") + pkg + "\n"
                            + actionSummary(record.managedActions()), 780, 72,
                    "startup:restore:" + pkg + ":0",
                    () -> callbacks.onToggleRestoreSelection(pkg, !checked));
            TextView now = button("Restore now", 240, 72, "startup:restore:" + pkg + ":1",
                    () -> callbacks.onRestoreOne(pkg));
            controls.add(toggle); controls.add(now);
            restoreRow.addView(toggle); addHSpacer(8, restoreRow); restoreRow.addView(now);
            matrix.add(controls);
            list.addView(restoreRow, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(80)));
            addSpacerTo(list, 6);
        }
        if (records.isEmpty()) list.addView(text("Nothing to restore. BOOP has no managed package changes.", 18, false), wrap());
        scroll.addView(list);
        addView(scroll, new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f));
        addSpacer(8);
        TextView back = button("Back to Startup Manager", 330, 56, "startup:restore:back", callbacks::onBack);
        addView(back, wrap());
        wireRestoreNavigation(matrix, batch, focusPackage);
    }

    private TextView rowAction(LinearLayout parent, String label, String pkg, int index, Runnable action) {
        TextView view = button(label, 205, 76, "startup:package:" + pkg + ":" + index, action);
        parent.addView(view); addHSpacer(8, parent); return view;
    }

    private void wirePackageNavigation(List<List<TextView>> matrix, List<TextView> filters,
                                       StartupManagerUiModel.Mode mode, String focusPackage, int focusAction) {
        if (!filters.isEmpty()) {
            for (int i = 0; i < filters.size(); i++) {
                int index = i;
                filters.get(i).setOnKeyListener((v, key, event) -> {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                    if (key == KeyEvent.KEYCODE_DPAD_LEFT) { filters.get(Math.max(0, index - 1)).requestFocus(); return true; }
                    if (key == KeyEvent.KEYCODE_DPAD_RIGHT) { filters.get(Math.min(filters.size() - 1, index + 1)).requestFocus(); return true; }
                    if (key == KeyEvent.KEYCODE_DPAD_DOWN && !matrix.isEmpty()) {
                        matrix.get(0).get(preferredAction(mode)).requestFocus(); return true;
                    }
                    return key == KeyEvent.KEYCODE_DPAD_UP;
                });
            }
        }
        for (int r = 0; r < matrix.size(); r++) {
            List<TextView> row = matrix.get(r);
            for (int c = 0; c < row.size(); c++) {
                int rr = r, cc = c;
                row.get(c).setOnKeyListener((v, key, event) -> {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                    if (key == KeyEvent.KEYCODE_DPAD_LEFT) { row.get(Math.max(0, cc - 1)).requestFocus(); return true; }
                    if (key == KeyEvent.KEYCODE_DPAD_RIGHT) { row.get(Math.min(row.size() - 1, cc + 1)).requestFocus(); return true; }
                    if (key == KeyEvent.KEYCODE_DPAD_UP) {
                        if (rr == 0) { if (!filters.isEmpty()) filters.get(0).requestFocus(); }
                        else matrix.get(rr - 1).get(Math.min(cc, matrix.get(rr - 1).size() - 1)).requestFocus();
                        return true;
                    }
                    if (key == KeyEvent.KEYCODE_DPAD_DOWN) {
                        matrix.get(Math.min(matrix.size() - 1, rr + 1)).get(
                                Math.min(cc, matrix.get(Math.min(matrix.size() - 1, rr + 1)).size() - 1)).requestFocus();
                        return true;
                    }
                    return false;
                });
            }
        }
        if (!matrix.isEmpty()) {
            TextView target = null;
            if (focusPackage != null) {
                for (List<TextView> row : matrix) {
                    Object tag = row.get(0).getTag();
                    if (tag != null && tag.toString().startsWith("startup:package:" + focusPackage + ":")) {
                        target = row.get(Math.max(0, Math.min(row.size() - 1, focusAction))); break;
                    }
                }
            }
            if (target == null) target = matrix.get(0).get(preferredAction(mode));
            TextView finalTarget = target;
            post(finalTarget::requestFocus);
        } else if (!filters.isEmpty()) post(() -> filters.get(0).requestFocus());
    }

    private void wireRestoreNavigation(List<List<TextView>> matrix, TextView batch, String focusPackage) {
        for (int r = 0; r < matrix.size(); r++) {
            List<TextView> row = matrix.get(r);
            for (int c = 0; c < row.size(); c++) {
                int rr = r, cc = c;
                row.get(c).setOnKeyListener((v, key, event) -> {
                    if (event.getAction() != KeyEvent.ACTION_DOWN) return false;
                    if (key == KeyEvent.KEYCODE_DPAD_LEFT) { row.get(Math.max(0, cc - 1)).requestFocus(); return true; }
                    if (key == KeyEvent.KEYCODE_DPAD_RIGHT) { row.get(Math.min(row.size() - 1, cc + 1)).requestFocus(); return true; }
                    if (key == KeyEvent.KEYCODE_DPAD_UP) { if (rr == 0) batch.requestFocus(); else matrix.get(rr - 1).get(Math.min(cc, matrix.get(rr - 1).size() - 1)).requestFocus(); return true; }
                    if (key == KeyEvent.KEYCODE_DPAD_DOWN) { matrix.get(Math.min(matrix.size() - 1, rr + 1)).get(Math.min(cc, matrix.get(Math.min(matrix.size() - 1, rr + 1)).size() - 1)).requestFocus(); return true; }
                    return false;
                });
            }
        }
        batch.setOnKeyListener((v, key, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && key == KeyEvent.KEYCODE_DPAD_DOWN && !matrix.isEmpty()) {
                matrix.get(0).get(0).requestFocus(); return true;
            }
            return false;
        });
        TextView target = batch;
        if (focusPackage != null) {
            for (List<TextView> row : matrix) {
                if (String.valueOf(row.get(0).getTag()).startsWith("startup:restore:" + focusPackage + ":")) { target = row.get(0); break; }
            }
        }
        TextView finalTarget = target; post(finalTarget::requestFocus);
    }

    private int preferredAction(StartupManagerUiModel.Mode mode) {
        return mode == StartupManagerUiModel.Mode.BOOT_CLEAN ? 2 : mode == StartupManagerUiModel.Mode.BACKGROUND ? 3 : 1;
    }
    private String modeSubtitle(StartupManagerUiModel.Mode mode) {
        return switch (mode) {
            case DISABLE -> "Disable or re-enable any package";
            case BOOT_CLEAN -> "Choose packages BOOP should close once after boot";
            case BACKGROUND -> "Choose packages that should not wake in the background";
        };
    }
    private String actionSummary(Set<StartupRecoveryPolicy.ManagedAction> actions) {
        if (actions == null || actions.isEmpty()) return "Baseline captured";
        return actions.stream().map(a -> switch (a) {
            case DISABLED -> "Disabled"; case BOOT_CLEAN -> "Boot close"; case BACKGROUND_BLOCK -> "Background blocked";
        }).sorted().reduce((a,b) -> a + " • " + b).orElse("Baseline captured");
    }

    private TextView card(String title, String subtitle, String tag, Runnable action) {
        TextView view = button(title + "\n" + subtitle, 940, 84, tag, action);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        return view;
    }
    private TextView filterChip(StartupManagerUiModel.Filter filter, boolean selected, Runnable action) {
        TextView view = button(filter.name().replace('_',' '), 180, 48, "startup:filter:" + filter.name(), action);
        if (selected) view.setBackground(FocusChrome.outline(getContext(), 10));
        return view;
    }
    private TextView button(String label, int widthDp, int heightDp, String tag, Runnable action) {
        TextView view = text(label, 16, false);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setFocusable(true); view.setClickable(true); view.setTag(tag); view.setId(View.generateViewId());
        view.setPadding(dp(16), dp(7), dp(16), dp(7));
        view.setBackground(background(false));
        view.setOnFocusChangeListener(focusListener(null));
        view.setOnClickListener(v -> action.run());
        view.setLayoutParams(new LayoutParams(dp(widthDp), dp(heightDp)));
        return view;
    }
    private OnFocusChangeListener focusListener(Runnable whenFocused) {
        return (v, focused) -> {
            v.setBackground(background(focused));
            v.animate().scaleX(focused ? 1.015f : 1f).scaleY(focused ? 1.015f : 1f).setDuration(90).start();
            if (focused && whenFocused != null) whenFocused.run();
        };
    }
    private TextView text(String value, int sp, boolean strong) {
        TextView view = new TextView(getContext());
        view.setText(value); view.setTextColor(Color.WHITE); view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        if (strong) view.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        return view;
    }
    private GradientDrawable background(boolean focused) { return FocusChrome.filled(getContext(), Color.rgb(38,38,38), 10, focused); }
    private LinearLayout row() { LinearLayout row = new LinearLayout(getContext()); row.setOrientation(HORIZONTAL); row.setGravity(Gravity.CENTER_VERTICAL); return row; }
    private LayoutParams cardParams() { return new LayoutParams(dp(940), dp(84)); }
    private LayoutParams wrap() { return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); }
    private void addSpacer(int amount) { addView(new View(getContext()), new LayoutParams(1, dp(amount))); }
    private void addSpacerTo(LinearLayout parent, int amount) { parent.addView(new View(getContext()), new LayoutParams(1, dp(amount))); }
    private void addHSpacer(int amount, LinearLayout parent) { parent.addView(new View(getContext()), new LayoutParams(dp(amount), 1)); }
    private int dp(int value) { return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics())); }
}