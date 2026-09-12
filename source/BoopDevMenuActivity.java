package com.boop.alpha1;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class BoopDevMenuActivity extends Activity {
    private FrameLayout root;
    private BoopCanonicalFaceView face;
    private boolean previewShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyImmersiveUi();
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);
        showMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersiveUi();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applyImmersiveUi();
    }

    @Override
    protected void onDestroy() {
        cancelActiveAnimation();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (previewShowing) {
            showMenu();
            return;
        }
        super.onBackPressed();
    }

    private void showMenu() {
        cancelActiveAnimation();
        previewShowing = false;
        root.removeAllViews();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setFocusable(true);
        LinearLayout column = new LinearLayout(this);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.setPadding(dp(24), dp(20), dp(24), dp(28));
        scroll.addView(column, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        TextView title = new TextView(this);
        title.setText("BOOP Dev Lab");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28f);
        title.setGravity(Gravity.CENTER);
        column.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView subtitle = new TextView(this);
        subtitle.setText("Local previews only • no Android shade posts");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(15f);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.setMargins(0, dp(2), 0, dp(14));
        column.addView(subtitle, subtitleParams);

        BoopCanonicalFaceView menuFace = new BoopCanonicalFaceView(this);
        face = menuFace;
        LinearLayout.LayoutParams faceParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(250));
        faceParams.setMargins(0, 0, 0, dp(18));
        column.addView(menuFace, faceParams);
        menuFace.post(() -> {
            if (face != menuFace || previewShowing) return;
            menuFace.showIdleBlackImmediately();
            menuFace.wakeFromIdle();
        });

        addCanonicalAnimationShelf(column);

        for (BoopDevMenuModel.Shelf shelf : BoopDevMenuModel.shelves()) {
            addShelf(column, shelf);
        }

        Button done = new Button(this);
        styleButton(done, "Done");
        done.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(62));
        doneParams.setMargins(0, dp(10), 0, 0);
        column.addView(done, doneParams);
    }

    private void addShelf(LinearLayout column, BoopDevMenuModel.Shelf shelf) {
        TextView shelfTitle = new TextView(this);
        shelfTitle.setText(shelf.title());
        shelfTitle.setTextColor(Color.WHITE);
        shelfTitle.setTextSize(20f);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(0, dp(6), 0, dp(8));
        column.addView(shelfTitle, titleParams);

        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        scroller.setFocusable(true);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        scroller.addView(row, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        for (BoopDevMenuModel.Item item : shelf.items()) {
            Button button = new Button(this);
            styleButton(button, item.label());
            button.setContentDescription(item.label() + " demo");
            button.setOnClickListener(v -> runAction(item.action()));
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    dp(164), dp(66));
            buttonParams.setMargins(0, 0, dp(10), 0);
            row.addView(button, buttonParams);
        }

        LinearLayout.LayoutParams scrollerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollerParams.setMargins(0, 0, 0, dp(14));
        column.addView(scroller, scrollerParams);
    }

    private void addCanonicalAnimationShelf(LinearLayout column) {
        TextView title = new TextView(this);
        title.setText("Canonical animations");
        title.setTextColor(Color.WHITE); title.setTextSize(20f);
        column.addView(title);
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        for (com.boop.eyes.EyeMotion.Clip clip : com.boop.eyes.EyeCatalogue.ALL) {
            Button button = new Button(this); styleButton(button, clip.label);
            button.setContentDescription(clip.label + " canonical animation");
            button.setOnClickListener(v -> { if (face != null) face.playCanonicalClip(clip.id); });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(180), dp(66));
            params.setMargins(0, 0, dp(10), 0); row.addView(button, params);
        }
        scroller.addView(row);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(6), 0, dp(14)); column.addView(scroller, params);
    }

    private void runAction(BoopDevMenuModel.Action action) {
        switch (action) {
            case NOTIFICATION_FACEBOOK:
            case NOTIFICATION_WHATSAPP:
            case NOTIFICATION_GMAIL:
            case NOTIFICATION_X:
            case NOTIFICATION_YOUTUBE:
            case NOTIFICATION_MESSENGER:
            case NOTIFICATION_INSTAGRAM:
            case NOTIFICATION_DISCORD:
            case NOTIFICATION_SPOTIFY:
            case NOTIFICATION_REDDIT:
            case NOTIFICATION_LOCKED:
            case NOTIFICATION_BUNDLE:
                showNotificationPreview(action); return;
            default:
                throw new IllegalArgumentException("Unknown dev action: " + action);
        }
    }

    private void showNotificationPreview(BoopDevMenuModel.Action action) {
        cancelActiveAnimation();
        previewShowing = true;
        face = null;
        root.removeAllViews();

        BoopNotificationPresentation presentation =
                BoopDevNotificationPreview.presentation(action, System.currentTimeMillis());
        BoopNotificationPuppetView puppet = new BoopNotificationPuppetView(
                this,
                presentation,
                new BoopNotificationPuppetView.Callback() {
                    @Override
                    public void onOpen(String notificationKey) {
                        showMenu();
                    }

                    @Override
                    public void onOpenBundle() {
                        showMenu();
                    }

                    @Override
                    public void onDismiss() {
                        showMenu();
                    }
                });
        root.addView(puppet, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        Button back = new Button(this);
        styleButton(back, "Back to dev menu");
        back.setOnClickListener(v -> showMenu());
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                dp(240),
                dp(62),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        backParams.bottomMargin = dp(22);
        root.addView(back, backParams);
    }

    private void cancelActiveAnimation() {
        if (face != null) face.playCanonicalClip("idle");
    }

    private void styleButton(Button button, String label) {
        button.setAllCaps(false);
        button.setText(label);
        button.setTextSize(18f);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(Color.rgb(42, 42, 42));
        button.setFocusable(true);
    }

    private void applyImmersiveUi() {
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
            return;
        }
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
