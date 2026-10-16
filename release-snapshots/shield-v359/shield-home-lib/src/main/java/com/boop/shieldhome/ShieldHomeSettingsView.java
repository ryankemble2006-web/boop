package com.boop.shieldhome;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Locale;

/** Presentation-only launcher controls; platform actions are owned by the activity. */
public final class ShieldHomeSettingsView extends LinearLayout {
    public interface Callbacks {
        void onSetRowEnabled(OptionalRowRegistry.Key key, boolean enabled);
        void onChooseHomeApp();
        default void onMakeBoopHome() { onChooseHomeApp(); }
        default void onRetireStockHome() { }
        default void onRestoreStockHome() { onChooseHomeApp(); }
        default void onEnableHomeOverride() { onChooseHomeApp(); }
        default void onOpenNowPlayingAccess() { }
        default void onChooseNowPlayingPlayer() { }
        default void onSetSmartHomePanelEnabled(boolean enabled) { }
        default void onSetAccentHue(int hue) { }
        default void onSetAccentRgb(int red, int green, int blue) { }
        void onBackHome();
    }

    public ShieldHomeSettingsView(Context context) { this(context, null); }

    public ShieldHomeSettingsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(VERTICAL);
        setGravity(Gravity.TOP);
        setBackgroundColor(Color.BLACK);
        setPadding(dp(48), dp(28), dp(48), dp(28));
        setClipChildren(false);
        setClipToPadding(false);
    }

    public static String launcherSettingsLabel() { return "Launcher Settings"; }

    public void render(boolean playNext, boolean appChannels, Callbacks callbacks) {
        render(playNext, appChannels, false, false, "Automatic", callbacks);
    }

    public void render(boolean playNext, boolean appChannels, boolean homeOverrideEnabled, Callbacks callbacks) {
        render(playNext, appChannels, homeOverrideEnabled, false, "Automatic", callbacks);
    }

    public void render(boolean playNext, boolean appChannels, boolean homeOverrideEnabled,
                       boolean nowPlayingAccess, String nowPlayingPlayerLabel, Callbacks callbacks) {
        render(playNext, appChannels, homeOverrideEnabled, nowPlayingAccess, nowPlayingPlayerLabel, true, callbacks);
    }

    public void render(boolean playNext, boolean appChannels, boolean homeOverrideEnabled,
                       boolean nowPlayingAccess, String nowPlayingPlayerLabel, boolean smartHomePanel, Callbacks callbacks) {
        render(playNext, appChannels, homeOverrideEnabled, nowPlayingAccess, nowPlayingPlayerLabel,
                smartHomePanel, ShieldHomeStore.DEFAULT_ACCENT_HUE, callbacks);
    }

    public void render(boolean playNext, boolean appChannels, boolean homeOverrideEnabled,
                       boolean nowPlayingAccess, String nowPlayingPlayerLabel, boolean smartHomePanel,
                       int accentHue, Callbacks callbacks) {
        removeAllViews();

        // Top Header Bar
        LinearLayout header = new LinearLayout(getContext());
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titleStack = new LinearLayout(getContext());
        titleStack.setOrientation(VERTICAL);
        TextView eyebrow = eyebrow("BOOP SHIELD LAUNCHER");
        eyebrow.setTextColor(FocusChrome.accentColor(getContext()));
        titleStack.addView(eyebrow, wrap());
        TextView title = text(launcherSettingsLabel(), 26);
        title.setTypeface(BoopTvChrome.FONT_MEDIUM);
        title.setLetterSpacing(0.015f);
        titleStack.addView(title, wrap());
        header.addView(titleStack, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView back = action("Back to Home");
        back.setGravity(Gravity.CENTER);
        back.setMinimumWidth(dp(164));
        back.setOnClickListener(v -> { if (callbacks != null) callbacks.onBackHome(); });
        header.addView(back, new LayoutParams(LayoutParams.WRAP_CONTENT, dp(48)));
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addSpacer(dp(20));

        // Two-Column Dashboard Grid
        LinearLayout columns = new LinearLayout(getContext());
        columns.setOrientation(HORIZONTAL);
        columns.setGravity(Gravity.TOP);
        LinearLayout leftCol = new LinearLayout(getContext());
        leftCol.setOrientation(VERTICAL);
        LinearLayout rightCol = new LinearLayout(getContext());
        rightCol.setOrientation(VERTICAL);
        LayoutParams leftParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        leftParams.rightMargin = dp(18);
        LayoutParams rightParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
        columns.addView(leftCol, leftParams);
        columns.addView(rightCol, rightParams);
        addView(columns, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // --- LEFT COLUMN: CARD 1 (SHIELD HOME & ROOM) ---
        LinearLayout homeCard = sectionCard("HOME & SMART ROOM");
        TextView overrideHome = actionWithSubtitle("BOOP device and room settings",
                "Room assignment, voice & permissions");
        overrideHome.setOnClickListener(v -> { if (callbacks != null) callbacks.onEnableHomeOverride(); });
        homeCard.addView(overrideHome, cardRowParams());
        addCardSpacer(homeCard, 8);

        TextView stockInfo = actionWithSubtitle("Home Assistant controls",
                "Open full room devices & routines view");
        stockInfo.setOnClickListener(v -> { if (callbacks != null) callbacks.onRetireStockHome(); });
        homeCard.addView(stockInfo, cardRowParams());
        addCardSpacer(homeCard, 8);

        final boolean[] panelEnabled = {smartHomePanel};
        TextView roomPanel = action("Smart home panel: " + (smartHomePanel ? "ON" : "OFF"));
        styleStatusRow(roomPanel, "Smart home panel: " + (smartHomePanel ? "ON" : "OFF"),
                "Uses your current room device selection on Home", smartHomePanel);
        roomPanel.setOnClickListener(v -> {
            panelEnabled[0] = !panelEnabled[0];
            String nextLabel = "Smart home panel: " + (panelEnabled[0] ? "ON" : "OFF");
            styleStatusRow(roomPanel, nextLabel,
                    "Uses your current room device selection on Home", panelEnabled[0]);
            if (callbacks != null) callbacks.onSetSmartHomePanelEnabled(panelEnabled[0]);
        });
        homeCard.addView(roomPanel, cardRowParams());
        leftCol.addView(homeCard, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addCardSpacer(leftCol, 16);

        // --- LEFT COLUMN: CARD 2 (NOW PLAYING & OPTIONAL ROWS) ---
        LinearLayout mediaCard = sectionCard("NOW PLAYING & CHANNELS");
        TextView access = action("Media access: " + (nowPlayingAccess ? "ON" : "OFF"));
        styleStatusRow(access, "Media access: " + (nowPlayingAccess ? "ON" : "OFF"),
                "Read track titles, artwork & transport state", nowPlayingAccess);
        access.setOnClickListener(v -> { if (callbacks != null) callbacks.onOpenNowPlayingAccess(); });
        mediaCard.addView(access, cardRowParams());
        addCardSpacer(mediaCard, 8);

        boolean audioAllowed = MusicAudioPermissionActivity.hasAudioAccess(getContext());
        TextView musicAudio = action("Music audio access: " + (audioAllowed ? "Allowed" : "Ask"));
        styleStatusRow(musicAudio, "Music audio access: " + (audioAllowed ? "Allowed" : "Ask"),
                "Live PCM spectrum analyser in Now Playing", audioAllowed);
        musicAudio.setOnClickListener(v -> getContext().startActivity(
                new Intent(getContext(), MusicAudioPermissionActivity.class)));
        mediaCard.addView(musicAudio, cardRowParams());
        addCardSpacer(mediaCard, 8);

        String playerLabel = nowPlayingPlayerLabel == null || nowPlayingPlayerLabel.trim().isEmpty()
                ? "Automatic" : nowPlayingPlayerLabel.trim();
        TextView player = action("Player: " + playerLabel);
        styleStatusRow(player, "Player: " + playerLabel, "Preferred media session source", true);
        player.setOnClickListener(v -> { if (callbacks != null) callbacks.onChooseNowPlayingPlayer(); });
        mediaCard.addView(player, cardRowParams());
        addCardSpacer(mediaCard, 10);

        LinearLayout optionalPair = new LinearLayout(getContext());
        optionalPair.setOrientation(HORIZONTAL);
        TextView playNextRow = action("Play Next: " + (playNext ? "ON" : "OFF"));
        playNextRow.setGravity(Gravity.CENTER);
        playNextRow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        playNextRow.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onSetRowEnabled(OptionalRowRegistry.Key.PLAY_NEXT, !playNext);
        });
        TextView channelsRow = action("App content rows: " + (appChannels ? "ON" : "OFF"));
        channelsRow.setGravity(Gravity.CENTER);
        channelsRow.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14.5f);
        channelsRow.setOnClickListener(v -> {
            if (callbacks != null) callbacks.onSetRowEnabled(OptionalRowRegistry.Key.APP_CHANNELS, !appChannels);
        });
        LayoutParams halfLeft = new LayoutParams(0, dp(48), 1f);
        halfLeft.rightMargin = dp(8);
        LayoutParams halfRight = new LayoutParams(0, dp(48), 1f);
        optionalPair.addView(playNextRow, halfLeft);
        optionalPair.addView(channelsRow, halfRight);
        mediaCard.addView(optionalPair, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        leftCol.addView(mediaCard, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // --- RIGHT COLUMN: CARD 3 (ACCENT THEME STUDIO) ---
        LinearLayout accentCard = sectionCard("ACCENT THEME");
        int initialColour = new ShieldHomeStore(getContext()).accentColor();
        final int[] channels = {Color.red(initialColour), Color.green(initialColour), Color.blue(initialColour)};

        LinearLayout swatchRow = new LinearLayout(getContext());
        swatchRow.setOrientation(HORIZONTAL);
        swatchRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView accentLabel = text("Highlight colour", 16);
        accentLabel.setTypeface(BoopTvChrome.FONT_MEDIUM);
        swatchRow.addView(accentLabel, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

        TextView colourValue = text(String.format(Locale.US, "#%06X", initialColour & 0xffffff), 13);
        colourValue.setTypeface(BoopTvChrome.FONT_MEDIUM);
        colourValue.setFontFeatureSettings("tnum");
        colourValue.setLetterSpacing(0.06f);
        colourValue.setPadding(dp(12), dp(4), dp(12), dp(4));
        GradientDrawable swatchBg = new GradientDrawable();
        swatchBg.setCornerRadius(dp(999));
        swatchBg.setColor(Color.argb(48, channels[0], channels[1], channels[2]));
        swatchBg.setStroke(dp(1), initialColour);
        colourValue.setBackground(swatchBg);
        colourValue.setTextColor(initialColour);
        swatchRow.addView(colourValue, wrap());
        accentCard.addView(swatchRow, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addCardSpacer(accentCard, 12);

        String[] names = {"Red", "Green", "Blue"};
        int[] tints = {Color.rgb(255, 82, 82), Color.rgb(64, 214, 128), Color.rgb(77, 184, 255)};
        final SeekBar[] sliders = new SeekBar[3];
        final TextView[] valueLabels = new TextView[3];

        for (int i = 0; i < 3; i++) {
            final int channel = i;
            LinearLayout sliderRow = new LinearLayout(getContext());
            sliderRow.setOrientation(HORIZONTAL);
            sliderRow.setGravity(Gravity.CENTER_VERTICAL);

            TextView chLabel = text(names[i], 13);
            chLabel.setTextColor(BoopTvChrome.TEXT_SECONDARY);
            sliderRow.addView(chLabel, new LayoutParams(dp(48), LayoutParams.WRAP_CONTENT));

            SeekBar slider = new SeekBar(getContext());
            BoopTvChrome.markCustomStyled(slider);
            slider.setTag("launcher-settings:rgb:" + i);
            slider.setMax(255);
            slider.setKeyProgressIncrement(1);
            slider.setProgress(channels[i]);
            slider.setFocusable(true);
            slider.setClickable(true);
            slider.setSplitTrack(false);
            slider.setContentDescription("Highlight " + names[i] + ", 0 to 255");
            slider.setProgressTintList(ColorStateList.valueOf(tints[i]));
            slider.setThumbTintList(ColorStateList.valueOf(tints[i]));
            slider.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(40, 45, 54)));
            slider.setOnFocusChangeListener((v, focused) ->
                    v.setForeground(focused ? FocusChrome.outline(getContext(), 10) : null));
            sliders[i] = slider;
            sliderRow.addView(slider, new LayoutParams(0, dp(36), 1f));

            TextView value = text(String.valueOf(channels[i]), 13);
            value.setFontFeatureSettings("tnum");
            value.setGravity(Gravity.END);
            value.setTextColor(BoopTvChrome.TEXT_PRIMARY);
            valueLabels[i] = value;
            sliderRow.addView(value, new LayoutParams(dp(38), LayoutParams.WRAP_CONTENT));

            slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                    channels[channel] = progress;
                    int preview = Color.rgb(channels[0], channels[1], channels[2]);
                    value.setText(String.valueOf(progress));
                    colourValue.setTextColor(preview);
                    colourValue.setText(String.format(Locale.US, "#%06X", preview & 0xffffff));
                    swatchBg.setColor(Color.argb(48, channels[0], channels[1], channels[2]));
                    swatchBg.setStroke(dp(1), preview);
                    eyebrow.setTextColor(preview);
                    if (fromUser && callbacks != null) callbacks.onSetAccentRgb(channels[0], channels[1], channels[2]);
                }
                @Override public void onStartTrackingTouch(SeekBar bar) { }
                @Override public void onStopTrackingTouch(SeekBar bar) { }
            });
            accentCard.addView(sliderRow, new LayoutParams(LayoutParams.MATCH_PARENT, dp(38)));
            if (i < 2) addCardSpacer(accentCard, 4);
        }

        addCardSpacer(accentCard, 12);
        LinearLayout presets = new LinearLayout(getContext());
        presets.setOrientation(HORIZONTAL);
        String[] presetNames = {"HiFi Orange", "Cyber Blue", "Emerald", "Amethyst"};
        int[][] presetRgb = {{255, 119, 0}, {77, 184, 255}, {46, 204, 113}, {165, 110, 255}};
        for (int p = 0; p < presetNames.length; p++) {
            final int[] rgb = presetRgb[p];
            TextView chip = action(presetNames[p]);
            chip.setGravity(Gravity.CENTER);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
            chip.setPadding(dp(8), dp(6), dp(8), dp(6));
            chip.setOnClickListener(v -> {
                for (int c = 0; c < 3; c++) {
                    channels[c] = rgb[c];
                    sliders[c].setProgress(rgb[c]);
                    valueLabels[c].setText(String.valueOf(rgb[c]));
                }
                int preview = Color.rgb(rgb[0], rgb[1], rgb[2]);
                colourValue.setTextColor(preview);
                colourValue.setText(String.format(Locale.US, "#%06X", preview & 0xffffff));
                swatchBg.setColor(Color.argb(48, rgb[0], rgb[1], rgb[2]));
                swatchBg.setStroke(dp(1), preview);
                eyebrow.setTextColor(preview);
                if (callbacks != null) callbacks.onSetAccentRgb(rgb[0], rgb[1], rgb[2]);
            });
            LayoutParams cp = new LayoutParams(0, dp(38), 1f);
            if (p < presetNames.length - 1) cp.rightMargin = dp(6);
            presets.addView(chip, cp);
        }
        accentCard.addView(presets, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        rightCol.addView(accentCard, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addCardSpacer(rightCol, 16);

        // --- RIGHT COLUMN: CARD 4 (SYSTEM & STARTUP) ---
        LinearLayout sysCard = sectionCard("SYSTEM & STARTUP");
        TextView startupManager = actionWithSubtitle("Startup Manager",
                "Boot cleanup, background limits & package control");
        startupManager.setOnClickListener(v -> getContext().startActivity(
                new Intent(getContext(), ShieldStartupManagerActivity.class)));
        sysCard.addView(startupManager, cardRowParams());
        addCardSpacer(sysCard, 8);

        TextView restoreHome = actionWithSubtitle("Android settings",
                "System network, display, audio & device settings");
        restoreHome.setOnClickListener(v -> { if (callbacks != null) callbacks.onRestoreStockHome(); });
        sysCard.addView(restoreHome, cardRowParams());
        rightCol.addView(sysCard, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private LinearLayout sectionCard(String heading) {
        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.setBackground(BoopTvChrome.cardSurface(getContext(), 16));
        TextView hdr = eyebrow(heading);
        LayoutParams hp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        hp.bottomMargin = dp(10);
        card.addView(hdr, hp);
        return card;
    }

    private TextView eyebrow(String value) {
        TextView view = text(value, 11);
        view.setTypeface(BoopTvChrome.FONT_MEDIUM);
        view.setLetterSpacing(0.08f);
        view.setTextColor(BoopTvChrome.TEXT_SECONDARY);
        return view;
    }

    private TextView text(String value, int sp) {
        TextView view = new TextView(getContext());
        BoopTvChrome.markCustomStyled(view);
        view.setTypeface(BoopTvChrome.FONT_REGULAR);
        view.setLetterSpacing(0.01f);
        view.setText(value);
        view.setTextColor(BoopTvChrome.TEXT_PRIMARY);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        return view;
    }

    private TextView action(String value) {
        TextView view = text(value, 15);
        view.setTypeface(BoopTvChrome.FONT_MEDIUM);
        view.setLetterSpacing(0.015f);
        view.setTag("launcher-settings:" + value.split(":", 2)[0]);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setFocusable(true);
        view.setClickable(true);
        view.setBackground(actionBackground(false));
        view.setPadding(dp(22), dp(10), dp(22), dp(10));
        view.setDefaultFocusHighlightEnabled(false);
        view.setStateListAnimator(null);
        view.setBackgroundTintList(null);
        view.setOnFocusChangeListener((v, focused) -> {
            v.setBackground(actionBackground(focused));
            v.setPadding(dp(22), dp(10), dp(22), dp(10));
        });
        return view;
    }

    private TextView actionWithSubtitle(String title, String subtitle) {
        TextView view = action(title);
        String full = title + "\n" + subtitle;
        SpannableString span = new SpannableString(full);
        int split = title.length() + 1;
        span.setSpan(new RelativeSizeSpan(0.80f), split, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(BoopTvChrome.TEXT_SECONDARY), split, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(span);
        return view;
    }

    private void styleStatusRow(TextView view, String labelWithState, String subtitle, boolean active) {
        String full = labelWithState + "\n" + subtitle;
        SpannableString span = new SpannableString(full);
        int colon = labelWithState.indexOf(':');
        if (colon >= 0) {
            int stateColor = active ? FocusChrome.accentColor(getContext()) : BoopTvChrome.TEXT_SECONDARY;
            span.setSpan(new ForegroundColorSpan(stateColor), colon + 1, labelWithState.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        int split = labelWithState.length() + 1;
        span.setSpan(new RelativeSizeSpan(0.80f), split, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        span.setSpan(new ForegroundColorSpan(BoopTvChrome.TEXT_SECONDARY), split, full.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        view.setText(span);
    }

    private GradientDrawable actionBackground(boolean focused) {
        return FocusChrome.filled(getContext(), BoopTvChrome.SURFACE_CARD, 12, focused);
    }

    private LayoutParams cardRowParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    private void addCardSpacer(LinearLayout parent, int dpHeight) {
        View spacer = new View(getContext());
        parent.addView(spacer, new LayoutParams(1, dp(dpHeight)));
    }

    private LayoutParams wrap() { return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT); }
    private void addSpacer(int height) { View spacer = new View(getContext()); addView(spacer, new LayoutParams(1, height)); }
    private int dp(int value) { return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics())); }
}
