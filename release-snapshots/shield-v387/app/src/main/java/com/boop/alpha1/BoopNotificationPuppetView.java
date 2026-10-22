package com.boop.alpha1;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.opengl.GLSurfaceView;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ScrollView;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class BoopNotificationPuppetView extends FrameLayout {
    private static final long FRAME_MS = 33L;

    interface Callback {
        void onOpen(String notificationKey);
        void onOpenBundle();
        void onDismiss();
    }

    private final Callback callback;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final GLSurfaceView eyeSurface;
    private final com.boop.eyes.CanonicalEyeRenderer eyeRenderer;
    private final com.boop.eyes.NotificationSignView signView;
    private final FrameLayout cardHost;
    private final BoundedCardScroll cardScroll;
    private final PowerManager powerManager;
    private final com.boop.eyes.AnimationClock signClock =
            new com.boop.eyes.AnimationClock(SystemClock.uptimeMillis());
    private BoopNotificationPresentation presentation;
    private long signStartMs;
    private boolean frameScheduled;

    BoopNotificationPuppetView(
            Context context,
            BoopNotificationPresentation presentation,
            Callback callback) {
        super(context);
        if (callback == null) throw new IllegalArgumentException("callback required");
        this.callback = callback;
        this.presentation = presentation;

        setBackgroundColor(Color.BLACK);
        setClickable(true);
        setFocusable(true);
        setContentDescription("BOOP notification");
        setClipChildren(false);
        setClipToPadding(false);

        powerManager = context.getSystemService(PowerManager.class);
        com.boop.eyes.AnimationSpeedBinding.install(this,
                speed -> signClock.setSpeed(speed, SystemClock.uptimeMillis()));
        eyeSurface = new GLSurfaceView(context);
        eyeSurface.setEGLContextClientVersion(2);
        eyeSurface.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        eyeSurface.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        eyeSurface.setZOrderOnTop(true);
        eyeSurface.setPreserveEGLContextOnPause(true);
        eyeRenderer = new com.boop.eyes.CanonicalEyeRenderer(
                context.getAssets(), detail -> android.util.Log.e("BOOPEyes", detail));
        eyeSurface.setRenderer(eyeRenderer);
        eyeSurface.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        com.boop.eyes.EyeColourBinding.install(eyeSurface, eyeRenderer);
        eyeSurface.setFocusable(false);
        eyeSurface.setClickable(false);
        addView(eyeSurface, match());

        signView = new com.boop.eyes.NotificationSignView(context);
        signView.setFocusable(false);
        signView.setClickable(false);
        addView(signView, match());

        cardHost = new FrameLayout(context);
        cardScroll = new BoundedCardScroll(context);
        cardScroll.setFillViewport(false);
        cardScroll.setClipToPadding(true);
        cardScroll.setContentDescription("Notification details, scroll for more");
        cardHost.addView(cardScroll, new FrameLayout.LayoutParams(-1, -2));
        FrameLayout.LayoutParams hostParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        int margin = dp(28);
        hostParams.setMargins(margin, margin, margin, margin);
        addView(cardHost, hostParams);

        rebuildCard();
        setOnClickListener(v -> openCurrentPresentation());
        BoopNotificationSwipeGesture.attach(this, callback::onDismiss);
        startEntrance();
    }

    void updatePresentation(BoopNotificationPresentation updated) {
        presentation = updated;
        rebuildCard();
        signStartMs = signClock.now(SystemClock.uptimeMillis());
        renderCanonical();
    }

    void setFaceVisible(boolean visible) {
        eyeSurface.setVisibility(visible ? View.VISIBLE : View.GONE);
        signView.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void openCurrentPresentation() {
        List<BoopNotificationEnvelope> cards = cards();
        if (cards.isEmpty()) return;
        if (cards.size() == 1) {
            callback.onOpen(cards.get(0).key());
        } else {
            callback.onOpenBundle();
        }
    }

    private void rebuildCard() {
        cardScroll.removeAllViews();
        cardScroll.scrollTo(0, 0);
        List<BoopNotificationEnvelope> cards = cards();
        if (cards.isEmpty()) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
        String packageName = cards.get(0).packageName();
        boolean branded = cards.size() == 1
                && BoopNotificationSignIdentity.brandedStyle(packageName) >= 0;
        signView.setNotificationLabel(branded ? null : identityLabel(cards));

        LinearLayout card = new LinearLayout(getContext());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        int horizontal = dp(24);
        int vertical = dp(20);
        card.setPadding(horizontal, vertical, horizontal, vertical);
        card.setBackground(cardBackground());
        card.setOnClickListener(v -> openCurrentPresentation());

        LinearLayout identityRow = new LinearLayout(getContext());
        identityRow.setOrientation(LinearLayout.HORIZONTAL);
        identityRow.setGravity(Gravity.CENTER);

        ImageView icon = new ImageView(getContext());
        icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        icon.setImageDrawable(loadAppIcon(cards.get(0).packageName()));
        int iconSize = dp(42);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(iconSize, iconSize);
        iconParams.setMargins(0, 0, dp(12), 0);
        identityRow.addView(icon, iconParams);

        TextView app = text(identityLabel(cards), 21f, true);
        identityRow.addView(app, new LinearLayout.LayoutParams(
                0, LayoutParams.WRAP_CONTENT, 1f));

        if (cards.size() > 1) {
            TextView count = text(String.valueOf(cards.size()), 14f, true);
            count.setGravity(Gravity.CENTER);
            count.setMinWidth(dp(30));
            count.setMinHeight(dp(30));
            count.setPadding(dp(7), dp(3), dp(7), dp(3));
            count.setBackground(badgeBackground());
            LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            countParams.setMargins(dp(12), 0, 0, 0);
            identityRow.addView(count, countParams);
        }
        card.addView(identityRow, wrapBottom(10));

        if (presentation != null && !presentation.locked()) {
            BoopNotificationEnvelope first = cards.get(0);
            if (!empty(first.title())) {
                TextView title = text(first.title(), 19f, true);
                card.addView(title, wrapBottom(6));
            }
            if (!empty(first.text())) {
                TextView body = text(first.text(), 17f, false);
                card.addView(body, wrapBottom(0));
            }
        }

        cardScroll.addView(card, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.CENTER_HORIZONTAL));
        setContentDescription(contentDescription(cards));
    }

    private void startEntrance() {
        signStartMs = signClock.now(SystemClock.uptimeMillis());
        renderCanonical();
    }

    private final Runnable frame = new Runnable() {
        @Override public void run() {
            frameScheduled = false;
            if (!isShown()) return;
            renderCanonical();
        }
    };

    private void renderCanonical() {
        double elapsed = powerManager != null && powerManager.isPowerSaveMode()
                ? 10000.0 : Math.max(0L, signClock.now(SystemClock.uptimeMillis()) - signStartMs);
        int style = notificationStyle();
        com.boop.eyes.SignMotion.Pose pose = com.boop.eyes.SignMotion.sample(elapsed, style);
        eyeRenderer.pose = pose.eyes;
        signView.show(pose, style);
        eyeSurface.requestRender();
        scheduleFrame();
    }

    private int notificationStyle() {
        List<BoopNotificationEnvelope> list = cards();
        if (list.isEmpty()) return 0;
        return Math.max(0, BoopNotificationSignIdentity.brandedStyle(list.get(0).packageName()));
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        int width = MeasureSpec.getSize(widthSpec);
        int height = MeasureSpec.getSize(heightSpec);
        int margin = dp(28);
        float portraitScale = Math.min(width / 1000f, height / 680f);
        // The original sign is centered at canonical y=510. Its rotated
        // 800x400 board and bob reach at most ~494 units below the canvas
        // midpoint (340), so reserve 500 without changing the artwork/motion.
        // The entrance still rises from below, as in the original renderer.
        int portraitCardHeight = (int) Math.floor(height / 2f - 500f * portraitScale)
                - margin - dp(8);
        boolean sideBySide = (width > height && height > 0) || portraitCardHeight < dp(112);
        int artworkWidth = sideBySide ? width / 2 : width;

        // Only the host bounds change. Both panes retain the same canonical art,
        // motion and text sizes; the card cannot cover the landscape art pane.
        FrameLayout.LayoutParams eyes = (FrameLayout.LayoutParams) eyeSurface.getLayoutParams();
        eyes.width = artworkWidth;
        eyes.height = Math.max(1, Math.round(height * 0.64f));
        eyes.gravity = Gravity.TOP | Gravity.LEFT;
        FrameLayout.LayoutParams sign = (FrameLayout.LayoutParams) signView.getLayoutParams();
        sign.width = artworkWidth;
        sign.height = LayoutParams.MATCH_PARENT;
        sign.gravity = Gravity.TOP | Gravity.LEFT;
        FrameLayout.LayoutParams card = (FrameLayout.LayoutParams) cardHost.getLayoutParams();
        card.width = Math.max(1, (sideBySide ? width - artworkWidth : width) - margin * 2);
        card.height = LayoutParams.WRAP_CONTENT;
        card.gravity = sideBySide ? Gravity.RIGHT | Gravity.CENTER_VERTICAL : Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        card.setMargins(margin, margin, margin, margin);
        cardScroll.maxHeight = Math.max(1, sideBySide ? height - margin * 2 : portraitCardHeight);
        super.onMeasure(widthSpec, heightSpec);
    }

    private final class BoundedCardScroll extends ScrollView {
        int maxHeight = Integer.MAX_VALUE;
        float startX, startY;
        boolean tracking;

        BoundedCardScroll(Context context) { super(context); }

        @Override public boolean dispatchTouchEvent(MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                startX = event.getX(); startY = event.getY(); tracking = true;
            } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                tracking = false;
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP && tracking) {
                tracking = false;
                float dx = Math.abs(event.getX() - startX);
                float dy = Math.abs(event.getY() - startY);
                if (dx > dy && BoopNotificationSwipeGesture.isDismiss(startX, startY,
                        event.getX(), event.getY(), getResources().getDisplayMetrics().density)) {
                    MotionEvent cancel = MotionEvent.obtain(event);
                    cancel.setAction(MotionEvent.ACTION_CANCEL);
                    super.dispatchTouchEvent(cancel);
                    cancel.recycle();
                    callback.onDismiss();
                    return true;
                }
            }
            return super.dispatchTouchEvent(event);
        }

        @Override protected void onMeasure(int widthSpec, int heightSpec) {
            int limit = MeasureSpec.getMode(heightSpec) == MeasureSpec.UNSPECIFIED ? maxHeight
                    : Math.min(maxHeight, MeasureSpec.getSize(heightSpec));
            super.onMeasure(widthSpec, MeasureSpec.makeMeasureSpec(limit, MeasureSpec.AT_MOST));
        }
    }

    private void scheduleFrame() {
        if (frameScheduled || !isShown()) return;
        frameScheduled = true;
        handler.postDelayed(frame, FRAME_MS);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        eyeSurface.onResume();
        scheduleFrame();
    }

    @Override protected void onDetachedFromWindow() {
        handler.removeCallbacks(frame);
        frameScheduled = false;
        eyeSurface.onPause();
        super.onDetachedFromWindow();
    }

    private Drawable loadAppIcon(String packageName) {
        BoopDevNotificationIdentity.Spec devIdentity =
                BoopDevNotificationIdentity.forPackage(packageName);
        if (devIdentity != null) {
            return new BoopDevNotificationIconDrawable(devIdentity);
        }
        if (!empty(packageName)) {
            try {
                return getContext().getPackageManager().getApplicationIcon(packageName);
            } catch (RuntimeException ignored) {
                // Fall through to a neutral Android notification icon.
            } catch (android.content.pm.PackageManager.NameNotFoundException ignored) {
                // Fall through to a neutral Android notification icon.
            }
        }
        return getContext().getDrawable(android.R.drawable.ic_dialog_info);
    }

    private String identityLabel(List<BoopNotificationEnvelope> cards) {
        Set<String> labels = new LinkedHashSet<>();
        for (BoopNotificationEnvelope item : cards) {
            if (item == null || empty(item.appLabel())) continue;
            labels.add(item.appLabel().trim());
        }
        if (labels.isEmpty()) return "Notification";
        if (labels.size() == 1) return labels.iterator().next();
        String first = labels.iterator().next();
        return first + " + " + (labels.size() - 1) + " apps";
    }

    private String contentDescription(List<BoopNotificationEnvelope> cards) {
        String identity = identityLabel(cards);
        if (cards.size() == 1) return "BOOP notification from " + identity;
        return "BOOP notifications, " + cards.size() + ", from " + identity;
    }

    private List<BoopNotificationEnvelope> cards() {
        return presentation == null
                ? java.util.Collections.emptyList()
                : presentation.cards();
    }

    private GradientDrawable cardBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(28, 28, 28));
        background.setCornerRadius(dp(22));
        background.setStroke(dp(1), Color.rgb(74, 74, 74));
        return background;
    }

    private GradientDrawable badgeBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(245, 196, 0));
        background.setShape(GradientDrawable.RECTANGLE);
        background.setCornerRadius(dp(20));
        return background;
    }

    private TextView text(String value, float sizeSp, boolean bold) {
        TextView view = new TextView(getContext());
        view.setText(value);
        view.setTextColor(Color.WHITE);
        view.setTextSize(sizeSp);
        view.setGravity(Gravity.CENTER);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private LinearLayout.LayoutParams wrapBottom(int bottomDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(bottomDp));
        return params;
    }

    private FrameLayout.LayoutParams match() {
        return new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private static boolean empty(String value) {
        return value == null || value.trim().isEmpty();
    }
}
