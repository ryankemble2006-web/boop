package com.boop.alpha1;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class BoopDevMenuActivity extends Activity {
    private static final long THINKING_PREVIEW_MS = 3_500L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FrameLayout root;
    private BoopFaceView face;
    private Runnable thinkingStop;
    private int berryVariant;
    private boolean previewShowing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);
        showMenu();
    }

    @Override
    protected void onDestroy() {
        cancelThinking();
        handler.removeCallbacksAndMessages(null);
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
        cancelThinking();
        previewShowing = false;
        root.removeAllViews();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
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
        title.setText("BOOP Dev");
        title.setTextColor(Color.WHITE);
        title.setTextSize(28f);
        title.setGravity(Gravity.CENTER);
        column.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView subtitle = new TextView(this);
        subtitle.setText("Local previews only");
        subtitle.setTextColor(Color.LTGRAY);
        subtitle.setTextSize(15f);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.setMargins(0, dp(2), 0, dp(14));
        column.addView(subtitle, subtitleParams);

        BoopFaceView menuFace = new BoopFaceView(this);
        face = menuFace;
        LinearLayout.LayoutParams faceParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(260));
        faceParams.setMargins(0, 0, 0, dp(18));
        column.addView(menuFace, faceParams);
        menuFace.post(() -> {
            if (face != menuFace || previewShowing) {
                return;
            }
            menuFace.showIdleBlackImmediately();
            menuFace.wakeFromIdle();
        });

        for (BoopDevMenuModel.Shelf shelf : BoopDevMenuModel.shelves()) {
            addShelf(column, shelf);
        }

        Button done = new Button(this);
        done.setAllCaps(false);
        done.setText("Done");
        done.setTextSize(18f);
        done.setTextColor(Color.WHITE);
        done.setBackgroundColor(Color.rgb(42, 42, 42));
        done.setOnClickListener(v -> finish());
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58));
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
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        scroller.addView(row, new HorizontalScrollView.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        for (BoopDevMenuModel.Item item : shelf.items()) {
            Button button = new Button(this);
            button.setAllCaps(false);
            button.setText(item.label());
            button.setTextSize(17f);
            button.setTextColor(Color.WHITE);
            button.setBackgroundColor(Color.rgb(42, 42, 42));
            button.setContentDescription(item.label() + " demo");
            button.setOnClickListener(v -> runAction(item.action()));
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                    dp(146), dp(62));
            buttonParams.setMargins(0, 0, dp(10), 0);
            row.addView(button, buttonParams);
        }

        LinearLayout.LayoutParams scrollerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        scrollerParams.setMargins(0, 0, 0, dp(14));
        column.addView(scroller, scrollerParams);
    }

    private void runAction(BoopDevMenuModel.Action action) {
        switch (action) {
            case WAKE:
                cancelThinking();
                if (face != null) {
                    face.showIdleBlackImmediately();
                    face.wakeFromIdle();
                }
                return;
            case THINK:
                playThinkingPreview();
                return;
            case BERRY:
                cancelThinking();
                if (face != null) {
                    face.playMemberBerry(berryVariant++);
                }
                return;
            case SHAKE:
                cancelThinking();
                if (face != null) {
                    face.playShakeMuppet(0.85f);
                }
                return;
            case SLEEP:
                cancelThinking();
                if (face != null) {
                    face.goIdleBlack();
                }
                return;
            case NOTIFICATION_UNLOCKED:
            case NOTIFICATION_LOCKED:
            case NOTIFICATION_BUNDLE:
                showNotificationPreview(action);
                return;
            default:
                throw new IllegalArgumentException("Unknown dev action: " + action);
        }
    }

    private void playThinkingPreview() {
        cancelThinking();
        BoopFaceView activeFace = face;
        if (activeFace == null) {
            return;
        }
        activeFace.startThinking();
        thinkingStop = () -> {
            if (face == activeFace) {
                activeFace.stopThinking();
            }
            thinkingStop = null;
        };
        handler.postDelayed(thinkingStop, THINKING_PREVIEW_MS);
    }

    private void showNotificationPreview(BoopDevMenuModel.Action action) {
        cancelThinking();
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
    }

    private void cancelThinking() {
        if (thinkingStop != null) {
            handler.removeCallbacks(thinkingStop);
            thinkingStop = null;
        }
        if (face != null) {
            face.stopThinking();
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
