package com.boop.shieldoverlay;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class TvPairingView extends LinearLayout {
    public interface AddressAction {
        void onSubmit(String address);
    }

    private final TextView titleView;
    private final TextView statusView;
    private final ImageView qrView;
    private final EditText addressInput;
    private final Button useAddressButton;
    private final Button signInOnDeviceButton;
    private final Button retryButton;
    private String currentQrPayload;

    public TvPairingView(Context context, Runnable retryAction, AddressAction addressAction) {
        super(context);
        setOrientation(VERTICAL);
        setGravity(Gravity.CENTER);
        setPadding(dp(48), dp(32), dp(48), dp(32));
        setBackgroundColor(Color.BLACK);

        titleView = new TextView(context);
        titleView.setTextColor(Color.WHITE);
        titleView.setTextSize(28f);
        titleView.setTypeface(TvMenuChrome.FONT_MEDIUM);
        titleView.setLetterSpacing(-0.015f);
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
        statusView.setTextColor(TvMenuChrome.TEXT_SECONDARY);
        statusView.setTextSize(16f);
        statusView.setTypeface(TvMenuChrome.FONT_REGULAR);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(4), 0, dp(12));
        addView(statusView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        addressInput = new EditText(context);
        addressInput.setTextColor(Color.WHITE);
        addressInput.setHintTextColor(TvMenuChrome.TEXT_SECONDARY);
        addressInput.setTextSize(17f);
        addressInput.setTypeface(TvMenuChrome.FONT_REGULAR);
        addressInput.setSingleLine(true);
        addressInput.setSelectAllOnFocus(true);
        addressInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        addressInput.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
        addressInput.setHint(R.string.pairing_address_hint);
        addressInput.setBackground(TvMenuChrome.background(context, false));
        addressInput.setPadding(dp(20), dp(12), dp(20), dp(12));
        addressInput.setOnFocusChangeListener((v, focused) -> {
            addressInput.setBackground(TvMenuChrome.background(context, focused));
            addressInput.setPadding(dp(20), dp(12), dp(20), dp(12));
        });
        addressInput.setVisibility(View.GONE);
        LayoutParams addressParams = new LayoutParams(
                dp(680),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        addressParams.topMargin = dp(10);
        addView(addressInput, addressParams);
        addressInput.setOnEditorActionListener((view, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE
                    && addressAction != null) {
                addressAction.onSubmit(addressInput.getText().toString());
                return true;
            }
            return false;
        });

        useAddressButton = new Button(context);
        useAddressButton.setText(R.string.pairing_use_address);
        useAddressButton.setTextSize(16f);
        useAddressButton.setTypeface(TvMenuChrome.FONT_MEDIUM);
        useAddressButton.setAllCaps(false);
        useAddressButton.setFocusable(true);
        useAddressButton.setDefaultFocusHighlightEnabled(false);
        useAddressButton.setMinHeight(dp(50));
        useAddressButton.setTextColor(Color.WHITE);
        useAddressButton.setBackground(TvMenuChrome.background(context, false));
        useAddressButton.setPadding(dp(28), dp(10), dp(28), dp(10));
        useAddressButton.setOnFocusChangeListener((v, hasFocus) -> {
            useAddressButton.setBackground(TvMenuChrome.background(context, hasFocus));
            useAddressButton.setPadding(dp(28), dp(10), dp(28), dp(10));
            useAddressButton.setTextColor(hasFocus ? TvMenuChrome.accent(context) : Color.WHITE);
        });
        useAddressButton.setVisibility(View.GONE);
        useAddressButton.setOnClickListener(view -> {
            if (addressAction != null) {
                addressAction.onSubmit(addressInput.getText().toString());
            }
        });
        LayoutParams useParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        useParams.topMargin = dp(12);
        addView(useAddressButton, useParams);

        signInOnDeviceButton = new Button(context);
        signInOnDeviceButton.setText("Sign in on this device");
        signInOnDeviceButton.setTextSize(16f);
        signInOnDeviceButton.setTypeface(TvMenuChrome.FONT_MEDIUM);
        signInOnDeviceButton.setAllCaps(false);
        signInOnDeviceButton.setFocusable(true);
        signInOnDeviceButton.setDefaultFocusHighlightEnabled(false);
        signInOnDeviceButton.setMinHeight(dp(50));
        signInOnDeviceButton.setTextColor(Color.WHITE);
        signInOnDeviceButton.setBackground(TvMenuChrome.background(context, false));
        signInOnDeviceButton.setPadding(dp(28), dp(10), dp(28), dp(10));
        signInOnDeviceButton.setVisibility(View.GONE);
        signInOnDeviceButton.setOnClickListener(view -> {
            if (currentQrPayload != null && !currentQrPayload.isEmpty()) {
                try {
                    context.startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW,
                            android.net.Uri.parse(currentQrPayload))
                            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception ignored) { }
            }
        });
        signInOnDeviceButton.setOnFocusChangeListener((view, hasFocus) -> {
            signInOnDeviceButton.setBackground(TvMenuChrome.background(context, hasFocus));
            signInOnDeviceButton.setPadding(dp(28), dp(10), dp(28), dp(10));
            signInOnDeviceButton.setTextColor(hasFocus ? TvMenuChrome.accent(context) : Color.WHITE);
        });
        LayoutParams signInParams = new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        signInParams.topMargin = dp(8);
        addView(signInOnDeviceButton, signInParams);

        retryButton = new Button(context);
        retryButton.setText(R.string.pairing_retry_discovery);
        retryButton.setTextSize(16f);
        retryButton.setTypeface(TvMenuChrome.FONT_MEDIUM);
        retryButton.setAllCaps(false);
        retryButton.setFocusable(true);
        retryButton.setDefaultFocusHighlightEnabled(false);
        retryButton.setMinHeight(dp(50));
        retryButton.setTextColor(Color.WHITE);
        retryButton.setBackground(TvMenuChrome.background(context, false));
        retryButton.setPadding(dp(28), dp(10), dp(28), dp(10));
        signInOnDeviceButton.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        retryButton.setOnClickListener(view -> {
            if (retryAction != null) {
                retryAction.run();
            }
        });
        retryButton.setOnFocusChangeListener((view, hasFocus) -> {
            retryButton.setBackground(TvMenuChrome.background(context, hasFocus));
            retryButton.setPadding(dp(28), dp(10), dp(28), dp(10));
            retryButton.setTextColor(hasFocus ? TvMenuChrome.accent(context) : Color.WHITE);
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
        signInOnDeviceButton.setVisibility(View.GONE);
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
        showRetry(message, null);
    }

    public void showRetry(String message, String suggestedAddress) {
        qrView.setImageDrawable(null);
        qrView.setVisibility(View.GONE);
        titleView.setText(R.string.pairing_reconnect_title);
        String detail = message == null || message.trim().isEmpty()
                ? getContext().getString(R.string.pairing_reconnect_message)
                : message;
        statusView.setText(detail + "\n" + getContext().getString(R.string.pairing_reenter_address));
        addressInput.setText(suggestedAddress == null ? "" : suggestedAddress);
        addressInput.setSelection(addressInput.getText().length());
        addressInput.setVisibility(View.VISIBLE);
        useAddressButton.setVisibility(View.VISIBLE);
        retryButton.setVisibility(View.VISIBLE);
        addressInput.requestFocus();
    }

    private void hideQrAndRetry() {
        qrView.setImageDrawable(null);
        qrView.setVisibility(View.GONE);
        addressInput.setVisibility(View.GONE);
        useAddressButton.setVisibility(View.GONE);
        signInOnDeviceButton.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
    }

    private int dp(int value) {
        float density = getResources().getDisplayMetrics().density;
        return Math.max(1, Math.round(value * density));
    }
}
