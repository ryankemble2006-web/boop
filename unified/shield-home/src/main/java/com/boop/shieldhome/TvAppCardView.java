package com.boop.shieldhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
    public static final float GRABBED_SCALE = 1.14f;
    private static final float HOME_ARTWORK_FOCUSED_SCALE = 1.05f;
    private static final float HOME_ARTWORK_GRABBED_SCALE = 1.03f;
    public static final long FOCUS_DURATION_MS = 120L;

    private final ImageView iconView;
    private final TextView labelView;
    private final TextView favouriteBadge;
    private boolean grabbed;
    private boolean favourite;
    private boolean homeFavourite;

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
        setBackground(new ColorDrawable(Color.TRANSPARENT));

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

        setOnFocusChangeListener((view, focused) -> refreshEmphasis());
    }

    public void bind(TvAppEntry entry) {
        bind(entry, false);
    }

    public void bind(TvAppEntry entry, boolean favourite) {
        bindInternal(entry, favourite, false);
    }

    /** HOME favourites prefer the Android TV banner, falling back to the app icon. */
    public void bindFavourite(TvAppEntry entry) {
        bindInternal(entry, true, true);
    }

    private void bindInternal(TvAppEntry entry, boolean favourite, boolean preferBanner) {
        grabbed = false;
        this.favourite = favourite;
        homeFavourite = preferBanner;
        configureCardPadding(preferBanner);
        setScaleX(1f);
        setScaleY(1f);
        iconView.setScaleX(1f);
        iconView.setScaleY(1f);

        if (entry == null) {
            labelView.setText("");
            iconView.setImageDrawable(null);
            setContentDescription("");
            configureArtworkSize(false);
            refreshBadge();
            refreshEmphasis();
            return;
        }

        labelView.setText(entry.label());
        setContentDescription(entry.label());

        Drawable artwork = null;
        boolean banner = false;
        PackageManager pm = getContext().getPackageManager();
        if (preferBanner) {
            ComponentName component = ComponentName.unflattenFromString(entry.component());
            if (component != null) {
                try {
                    ActivityInfo info = pm.getActivityInfo(component, 0);
                    artwork = info.loadBanner(pm);
                } catch (PackageManager.NameNotFoundException ignored) {
                    // Package changes can race rendering. Fall through to package banner/icon.
                }
            }
            if (artwork == null) {
                try {
                    artwork = pm.getApplicationBanner(entry.packageName());
                } catch (PackageManager.NameNotFoundException ignored) {
                    // Fall through to normal icon.
                }
            }
            banner = artwork != null;
        }

        if (artwork == null) {
            try {
                artwork = pm.getApplicationIcon(entry.packageName());
            } catch (PackageManager.NameNotFoundException ignored) {
                // The activity will reconcile a package that vanished mid-render.
            }
        }

        configureArtworkSize(banner);
        iconView.setScaleType(banner ? ImageView.ScaleType.CENTER_CROP : ImageView.ScaleType.FIT_CENTER);
        iconView.setImageDrawable(artwork);
        refreshBadge();
        refreshEmphasis();
    }

    public void setGrabbed(boolean grabbed) {
        this.grabbed = grabbed;
        refreshBadge();
        refreshEmphasis();
    }

    @Override public void setSelected(boolean selected) {
        super.setSelected(selected);
        refreshEmphasis();
    }

    private void refreshBadge() {
        favouriteBadge.setText(grabbed ? "↔" : "★");
        favouriteBadge.setVisibility(
                AppCardChromePolicy.showBadge(homeFavourite, favourite, grabbed)
                        ? View.VISIBLE
                        : View.GONE);
    }

    private void configureCardPadding(boolean homeFavourite) {
        int horizontal = homeFavourite ? 0 : dp(14);
        setPadding(horizontal, dp(14), horizontal, dp(12));
    }

    private void configureArtworkSize(boolean banner) {
        LinearLayout.LayoutParams params = banner
                ? new LinearLayout.LayoutParams(dp(230), dp(129))
                : new LinearLayout.LayoutParams(dp(76), dp(76));
        params.bottomMargin = banner ? dp(6) : dp(10);
        iconView.setLayoutParams(params);
    }

    private void refreshEmphasis() {
        boolean focused = hasFocus();
        boolean selected = isSelected();
        boolean emphasized = focused || selected;
        boolean showPlate = AppCardChromePolicy.showPlate(
                homeFavourite, focused, selected, grabbed);
        setBackground(showPlate ? cardBackground() : new ColorDrawable(Color.TRANSPARENT));

        if (homeFavourite) {
            setForeground(null);
            iconView.setForeground(emphasized ? FocusChrome.outline(getContext(), 2) : null);
        } else {
            iconView.setForeground(null);
            setForeground(emphasized ? FocusChrome.outline(getContext(), 12) : null);
        }
        animateEmphasis(emphasized);
    }

    private void animateEmphasis(boolean emphasized) {
        if (AppCardChromePolicy.emphasizeArtworkOnly(homeFavourite)) {
            animate().cancel();
            setScaleX(1f);
            setScaleY(1f);
            float artworkTarget = grabbed
                    ? HOME_ARTWORK_GRABBED_SCALE
                    : (emphasized ? HOME_ARTWORK_FOCUSED_SCALE : 1f);
            iconView.animate()
                    .scaleX(artworkTarget)
                    .scaleY(artworkTarget)
                    .setDuration(FOCUS_DURATION_MS)
                    .start();
            return;
        }

        iconView.animate().cancel();
        iconView.setScaleX(1f);
        iconView.setScaleY(1f);
        float target = grabbed ? GRABBED_SCALE : (emphasized ? FOCUSED_SCALE : 1f);
        animate()
                .scaleX(target)
                .scaleY(target)
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
