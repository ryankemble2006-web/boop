package com.boop.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

public final class EdgeToEdge {
 private EdgeToEdge(){}
 public static void apply(Activity activity){
  Window w=activity.getWindow();
  w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
  w.setStatusBarColor(Color.TRANSPARENT);
  w.setNavigationBarColor(Color.TRANSPARENT);
  if(Build.VERSION.SDK_INT>=30)w.setDecorFitsSystemWindows(false);
  hideBars(activity);
 }
 public static void hideBars(Activity activity){
  Window w=activity.getWindow();
  w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
  View decor=w.getDecorView();
  if(Build.VERSION.SDK_INT>=30){
   WindowInsetsController c=decor.getWindowInsetsController();
   if(c!=null){
    c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    c.hide(WindowInsets.Type.systemBars());
    c.setSystemBarsAppearance(0,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
   }
  }else{
   decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
  }
 }
}
