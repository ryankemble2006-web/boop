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
        title.setTextSize(50f);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        addView(title, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        detailView = new TextView(context);
        detailView.setText(R.string.room_picker_loading);
        detailView.setTextColor(Color.LTGRAY);
        detailView.setTextSize(25f);
        detailView.setGravity(Gravity.CENTER);
        LayoutParams detailParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        detailParams.topMargin = dp(10);
        detailParams.bottomMargin = dp(26);
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
        retryButton.setTextSize(24f);
        retryButton.setAllCaps(false);
        retryButton.setFocusable(true);
        retryButton.setMinHeight(dp(68));
        retryButton.setPadding(dp(36), dp(12), dp(36), dp(12));
        retryButton.setTextColor(Color.WHITE);
        retryButton.setBackgroundColor(Color.DKGRAY);
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(view -> listener.onRetry());
        retryButton.setOnFocusChangeListener((view, hasFocus) -> {
            retryButton.setBackgroundColor(hasFocus ? Color.WHITE : Color.DKGRAY);
            retryButton.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
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
            LayoutParams params = new LayoutParams(dp(270), dp(132));
            params.leftMargin = dp(10);
            params.rightMargin = dp(10);
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
        button.setTextSize(28f);
        button.setAllCaps(false);
        button.setGravity(Gravity.CENTER);
        button.setFocusable(true);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.DKGRAY);
        button.setOnClickListener(view -> listener.onSelected(area));
        button.setOnFocusChangeListener((view, hasFocus) -> {
            button.setBackgroundColor(hasFocus ? Color.WHITE : Color.DKGRAY);
            button.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
            button.animate()
                    .scaleX(hasFocus ? 1.06f : 1f)
                    .scaleY(hasFocus ? 1.06f : 1f)
                    .setDuration(110L)
                    .start();
        });
        return button;
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}
