package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public final class TvRoomPickerView extends LinearLayout {
    public interface Listener {
        void onSelected(AreaInfo area);
        void onRetry();
    }

    private final Listener listener;
    private final TextView detailView;
    private final HorizontalScrollView scrollView;
    private final LinearLayout cardRow;
    private final Button retryButton;

    public TvRoomPickerView(Context context, Listener listener) {
        super(context);
        if (listener == null) {
            throw new IllegalArgumentException("room picker listener is required");
        }
        this.listener = listener;

        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setPadding(dp(56), dp(52), dp(56), dp(40));
        setBackgroundColor(Color.BLACK);

        TextView title = new TextView(context);
        title.setText(R.string.room_picker_title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(28f);
        title.setTypeface(TvMenuChrome.FONT_MEDIUM);
        title.setLetterSpacing(-0.015f);
        title.setGravity(Gravity.CENTER);
        addView(title, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        detailView = new TextView(context);
        detailView.setText(R.string.room_picker_loading);
        detailView.setTextColor(TvMenuChrome.TEXT_SECONDARY);
        detailView.setTextSize(16f);
        detailView.setTypeface(TvMenuChrome.FONT_REGULAR);
        detailView.setGravity(Gravity.CENTER);
        LayoutParams detailParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        detailParams.topMargin = dp(8);
        detailParams.bottomMargin = dp(24);
        addView(detailView, detailParams);

        scrollView = new HorizontalScrollView(context);
        scrollView.setFillViewport(false);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setVisibility(View.GONE);

        cardRow = new LinearLayout(context);
        cardRow.setOrientation(HORIZONTAL);
        cardRow.setGravity(Gravity.CENTER_VERTICAL);
        scrollView.addView(cardRow, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        addView(scrollView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f));

        retryButton = new Button(context);
        retryButton.setText(R.string.pairing_retry);
        retryButton.setTextSize(16f);
        retryButton.setTypeface(TvMenuChrome.FONT_MEDIUM);
        retryButton.setAllCaps(false);
        retryButton.setFocusable(true);
        retryButton.setDefaultFocusHighlightEnabled(false);
        retryButton.setMinHeight(dp(52));
        retryButton.setTextColor(Color.WHITE);
        retryButton.setBackground(TvMenuChrome.background(context, false));
        retryButton.setPadding(dp(32), dp(12), dp(32), dp(12));
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(view -> listener.onRetry());
        retryButton.setOnFocusChangeListener((view, hasFocus) -> {
            retryButton.setBackground(TvMenuChrome.background(context, hasFocus));
            retryButton.setPadding(dp(32), dp(12), dp(32), dp(12));
            retryButton.setTextColor(hasFocus ? TvMenuChrome.accent(context) : Color.WHITE);
        });
        addView(retryButton, new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void showLoading() {
        detailView.setText(R.string.room_picker_loading);
        cardRow.removeAllViews();
        scrollView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
    }

    public void showAreas(List<AreaInfo> areas) {
        cardRow.removeAllViews();
        if (areas == null || areas.isEmpty()) {
            showError("I couldn't find any rooms in Home Assistant.");
            return;
        }

        detailView.setText(R.string.room_picker_pick);
        retryButton.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);

        Button first = null;
        for (AreaInfo area : areas) {
            Button card = roomCard(area);
            LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(112));
            params.leftMargin = dp(8);
            params.rightMargin = dp(8);
            cardRow.addView(card, params);
            if (first == null) {
                first = card;
            }
        }
        if (first != null) {
            first.requestFocus();
        }
    }

    public void showError(String message) {
        cardRow.removeAllViews();
        scrollView.setVisibility(View.GONE);
        detailView.setText(message == null || message.trim().isEmpty()
                ? getContext().getString(R.string.room_picker_failed)
                : message);
        retryButton.setVisibility(View.VISIBLE);
        retryButton.requestFocus();
    }

    private Button roomCard(AreaInfo area) {
        Button button = new Button(getContext());
        button.setText(area.name());
        button.setTextSize(19f);
        button.setTypeface(TvMenuChrome.FONT_MEDIUM);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setMinimumWidth(dp(240));
        button.setFocusable(true);
        button.setDefaultFocusHighlightEnabled(false);
        button.setTextColor(Color.WHITE);
        button.setBackground(TvMenuChrome.background(getContext(), false));
        button.setPadding(dp(28), dp(14), dp(28), dp(14));
        button.setOnClickListener(view -> listener.onSelected(area));
        button.setOnFocusChangeListener((view, hasFocus) -> {
            button.setBackground(TvMenuChrome.background(getContext(), hasFocus));
            button.setPadding(dp(28), dp(14), dp(28), dp(14));
            button.setTextColor(hasFocus ? TvMenuChrome.accent(getContext()) : Color.WHITE);
            button.animate()
                    .scaleX(hasFocus ? 1.04f : 1f)
                    .scaleY(hasFocus ? 1.04f : 1f)
                    .setDuration(90L)
                    .start();
        });
        return button;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}
