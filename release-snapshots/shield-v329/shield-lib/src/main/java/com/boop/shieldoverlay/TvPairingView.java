package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class TvPairingView extends LinearLayout {
    private final TextView titleView;
    private final TextView statusView;
    private final ImageView qrView;
    private final Button retryButton;

    public TvPairingView(Context context, Runnable retryAction) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setPadding(dp(48), dp(32), dp(48), dp(32));
        setBackgroundColor(Color.BLACK);

        titleView = new TextView(context);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(42f);
        titleView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        titleView.setGravity(Gravity.CENTER);
        addView(titleView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        qrView = new ImageView(context);
        qrView.setContentDescription(context.getString(R.string.pairing_scan));
        qrView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        qrView.setVisibility(View.GONE);
        LayoutParams qrParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        qrParams.topMargin = dp(18);
        qrParams.bottomMargin = dp(18);
        addView(qrView, qrParams);

        statusView = new TextView(context);
        statusView.setTextColor(Color.LTGRAY);
        statusView.setTextSize(24f);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(4), 0, dp(12));
        addView(statusView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

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
        retryButton.setOnClickListener(view -> {
            if (retryAction != null) {
                retryAction.run();
            }
        });
        retryButton.setOnFocusChangeListener((view, hasFocus) -> {
            retryButton.setBackgroundColor(hasFocus ? Color.WHITE : Color.DKGRAY);
            retryButton.setTextColor(hasFocus ? Color.BLACK : Color.WHITE);
        });
        LayoutParams retryParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        retryParams.topMargin = dp(10);
        addView(retryButton, retryParams);
    }

    public void showSearching(String message) {
        titleView.setText(R.string.pairing_title_searching);
        statusView.setText(message == null ? "" : message);
        hideQrAndRetry();
    }

    public void showQr(String payload, int sizePx) {
        titleView.setText(R.string.pairing_title_found);
        statusView.setText(R.string.pairing_scan);
        retryButton.setVisibility(View.GONE);
        ViewGroup.LayoutParams params = qrView.getLayoutParams();
        params.width = sizePx;
        params.height = sizePx;
        qrView.setLayoutParams(params);
        qrView.setImageBitmap(QrCodeBitmap.render(payload, sizePx));
        qrView.setVisibility(View.VISIBLE);
    }

    public void showAuthorizing() {
        titleView.setText(R.string.pairing_title_found);
        statusView.setText(R.string.pairing_authorizing);
        hideQrAndRetry();
    }

    public void showConnected() {
        titleView.setText(R.string.pairing_found);
        statusView.setText("");
        hideQrAndRetry();
    }

    public void showRetry(String message) {
        qrView.setImageDrawable(null);
        qrView.setVisibility(View.GONE);
        titleView.setText(message == null || message.trim().isEmpty()
                ? getContext().getString(R.string.pairing_retry)
                : message);
        statusView.setText("");
        retryButton.setVisibility(View.VISIBLE);
        retryButton.requestFocus();
    }

    private void hideQrAndRetry() {
        qrView.setImageDrawable(null);
        qrView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}
