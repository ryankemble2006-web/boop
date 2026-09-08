package com.boop.shieldhome;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Remote-first app card with only local focus/selection animation. */
public final class TvAppCardView extends FrameLayout {
    public static final float FOCUSED_SCALE = 1.08f;
    public static final long FOCUS_DURATION_MS = 120L;

    private final ImageView iconView;
    private final TextView labelView;
    private final TextView favouriteBadge;

    public TvAppCardView(Context context) {
        this(context, null);
    }

    public TvAppCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setClickable(true);
        setClipChildren(false);
        setClipToPadding(false);
        setPadding(dp(14), dp(14), dp(14), dp(12));
        setBackground(cardBackground());

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        LayoutParams contentParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(content, contentParams);

        iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(76), dp(76));
        iconParams.bottomMargin = dp(10);
        content.addView(iconView, iconParams);

        labelView = new TextView(context);
        labelView.setTextColor(Color.WHITE);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        labelView.setGravity(Gravity.CENTER);
        labelView.setMaxLines(1);
        labelView.setEllipsize(android.text.TextUtils.TruncateAt.END);
        content.addView(labelView, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        favouriteBadge = new TextView(context);
        favouriteBadge.setText("★");
        favouriteBadge.setTextColor(Color.WHITE);
        favouriteBadge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        favouriteBadge.setGravity(Gravity.CENTER);
        favouriteBadge.setVisibility(View.GONE);
        LayoutParams badgeParams = new LayoutParams(dp(30), dp(30), Gravity.TOP | Gravity.END);
        addView(favouriteBadge, badgeParams);

        setOnFocusChangeListener((view, focused) -> animateScale(focused || isSelected()));
    }

    public void bind(TvAppEntry entry) {
        bind(entry, false);
    }

    public void bind(TvAppEntry entry, boolean favourite) {
        if (entry == null) {
            labelView.setText("");
            iconView.setImageDrawable(null);
            favouriteBadge.setVisibility(View.GONE);
            setContentDescription("");
            return;
        }

        labelView.setText(entry.label());
        setContentDescription(entry.label());
        favouriteBadge.setVisibility(favourite ? View.VISIBLE : View.GONE);

        Drawable icon = null;
        try {
            icon = getContext().getPackageManager().getApplicationIcon(entry.packageName());
        } catch (PackageManager.NameNotFoundException ignored) {
            // A package change can race rendering. The activity will reconcile the catalogue.
        }
        iconView.setImageDrawable(icon);
    }

    @Override public void setSelected(boolean selected) {
        super.setSelected(selected);
        animateScale(selected || hasFocus());
    }

    private void animateScale(boolean emphasized) {
        animate()
                .scaleX(emphasized ? FOCUSED_SCALE : 1f)
                .scaleY(emphasized ? FOCUSED_SCALE : 1f)
                .setDuration(FOCUS_DURATION_MS)
                .start();
    }

    private GradientDrawable cardBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.rgb(38, 38, 38));
        background.setCornerRadius(dp(12));
        return background;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
