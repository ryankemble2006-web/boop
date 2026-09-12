package com.boop.rally;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;

final class TvUi {
    static final int CYAN=0xff55e5ff, INK=0xff090e17, PANEL=0xff152333;
    static int dp(Activity a,int n){return Math.round(n*a.getResources().getDisplayMetrics().density);}
    static void immersive(Activity a){a.getWindow().getDecorView().setSystemUiVisibility(5894);}
    static TextView text(Activity a,String s,int size,int color){
        TextView t=new TextView(a);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setGravity(Gravity.CENTER);return t;
    }
    static GradientDrawable background(int fill,int stroke){
        GradientDrawable d=new GradientDrawable();d.setColor(fill);d.setCornerRadius(18);d.setStroke(3,stroke);return d;
    }
    static Button button(Activity a,String label,View.OnClickListener click){
        Button b=new Button(a);b.setText(label);b.setAllCaps(false);b.setTextSize(20);b.setTextColor(Color.WHITE);b.setTypeface(null,Typeface.BOLD);
        b.setPadding(dp(a,22),dp(a,16),dp(a,22),dp(a,16));b.setMinHeight(dp(a,60));b.setFocusable(true);
        b.setBackground(background(PANEL,0xff31546a));
        b.setOnFocusChangeListener((v,focused)->{b.setBackground(background(focused?0xff1d3b50:PANEL,focused?CYAN:0xff31546a));b.setTextColor(focused?CYAN:Color.WHITE);});
        b.setOnClickListener(click);return b;
    }
}
