package com.boop.shieldhome;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

/** Scalable Precision glyphs; colour comes from the launcher's current accent. */
final class PrecisionIcons {
    static final float SCALE = 0.9f;
    private PrecisionIcons() { }
    static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
    static Drawable drawable(Context context, int resource, float sizeDp) {
        Drawable normal = context.getDrawable(resource).mutate();
        normal.setTint(android.graphics.Color.WHITE);
        String name=context.getResources().getResourceEntryName(resource)+"_focus";
        int filled=context.getResources().getIdentifier(name,"drawable",context.getResources().getResourcePackageName(resource));
        if(filled==0) filled=context.getResources().getIdentifier(name,"drawable","com.boop.shieldhome");
        android.graphics.drawable.StateListDrawable icon=new android.graphics.drawable.StateListDrawable();
        Drawable active=context.getDrawable(filled==0?resource:filled).mutate();
        active.setTint(FocusChrome.accentColor(context));
        icon.addState(new int[]{android.R.attr.state_focused},active);
        icon.addState(new int[0],normal);
        int size = dp(context, sizeDp * SCALE);
        icon.setBounds(0, 0, size, size);
        return icon;
    }
    private static final java.util.Map<Long, android.graphics.Rect> INK = new java.util.HashMap<>();
    /** Rasterise each vector once to measure visible ink rather than its transparent viewport. */
    static android.graphics.Rect inkBounds(Context context, int resource, float sizeDp, int width, int height) {
        int size = Math.min(dp(context,sizeDp*SCALE),Math.min(width,height));
        if(size<=0)return new android.graphics.Rect();
        long key=((long)resource<<32)|(size&0xffffffffL);
        android.graphics.Rect bounds=INK.get(key);
        if(bounds==null) {
            android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(size,size,android.graphics.Bitmap.Config.ARGB_8888);
            Drawable shape=context.getDrawable(resource).mutate();shape.setTint(android.graphics.Color.WHITE);
            shape.setBounds(0,0,size,size);shape.draw(new android.graphics.Canvas(bitmap));
            int left=size,top=size,right=0,bottom=0;
            for(int y=0;y<size;y++) for(int x=0;x<size;x++) if(android.graphics.Color.alpha(bitmap.getPixel(x,y))>64) {
                left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x+1);bottom=Math.max(bottom,y+1);
            }
            bitmap.recycle();bounds=new android.graphics.Rect(left,top,right,bottom);INK.put(key,bounds);
        }
        android.graphics.Rect result=new android.graphics.Rect(bounds);result.offset((width-size)/2,(height-size)/2);return result;
    }
    /** Size each vector by its visible pixels, rather than its transparent square. */
    static float sizeForInkHeight(Context context, int resource, int height, float fallback) {
        if (height <= 0 || resource == 0) return fallback;
        float pixelDp = 1f / context.getResources().getDisplayMetrics().density / SCALE;
        int referenceHeight = inkBounds(context, resource, 100 * pixelDp, 100, 100).height();
        if (referenceHeight <= 0) return fallback;
        int estimate = Math.round(height * 100f / referenceHeight);
        int best = estimate, error = Integer.MAX_VALUE;
        for (int size = Math.max(1, estimate - 4); size <= estimate + 4; size++) {
            int actual = inkBounds(context, resource, size * pixelDp, size, size).height();
            int difference = Math.abs(actual - height);
            if (difference < error) { best = size; error = difference; }
        }
        return best * pixelDp;
    }
    static void bindControl(Control view, String label) {
        int resource;
        switch (label) {
            case "Prev": resource=R.drawable.boop_icon_skip_back; break;
            case "Next": resource=R.drawable.boop_icon_skip_forward; break;
            case "Pause": resource=R.drawable.boop_icon_pause; break;
            case "Play": resource=R.drawable.boop_icon_play; break;
            case "Lyrics": resource=R.drawable.boop_icon_mic_vocal; break;
            case "Queue": resource=R.drawable.boop_icon_list_music; break;
            case "Lookup": resource=R.drawable.boop_icon_search; break;
            case "Spectrum": resource=R.drawable.boop_icon_spectrum; break;
            case "Flow": resource=R.drawable.boop_icon_shuffle; break;
            default: throw new IllegalArgumentException("Unknown control: " + label);
        }
        view.setContentDescription(label);
        view.setTooltipText(label);
        view.setIcon(resource);
    }
    static final class Control extends View {
        private Drawable icon;
        private int resource;
        private int visibleHeight;
        private float glyphSizeDp = 34.32f;
        Control(Context context) { super(context);setWillNotDraw(false); }
        @Override protected void onMeasure(int widthSpec,int heightSpec) {
            setMeasuredDimension(resolveSize(dp(getContext(),44),widthSpec),
                    resolveSize(dp(getContext(),44),heightSpec));
        }
        void setIcon(int next) {
            if(resource!=next || icon==null) {
                resource=next;icon=drawable(getContext(),resource,34.32f);
                glyphSizeDp = sizeForInkHeight(getContext(), resource, visibleHeight, 34.32f);
            }
            invalidate();
        }
        void setVisibleHeight(int pixels) {
            if (visibleHeight == pixels) return;
            visibleHeight = pixels;
            glyphSizeDp = sizeForInkHeight(getContext(), resource, pixels, 34.32f);
            invalidate();
        }
        android.graphics.Rect inkBounds() {
            return PrecisionIcons.inkBounds(getContext(),resource,glyphSizeDp,getMeasuredWidth(),getMeasuredHeight());
        }
        @Override protected void onFocusChanged(boolean gain,int direction,android.graphics.Rect previous) {
            super.onFocusChanged(gain,direction,previous);invalidate();
        }
        @Override protected void onDraw(android.graphics.Canvas canvas) {
            if(icon==null) return;
            icon.setState(hasFocus()?new int[]{android.R.attr.state_focused}:new int[0]);
            int size=Math.min(dp(getContext(),glyphSizeDp*SCALE),Math.min(getWidth(),getHeight()));
            int x=(getWidth()-size)/2,y=(getHeight()-size)/2;
            icon.setBounds(x,y,x+size,y+size);icon.draw(canvas);
        }
    }
}
