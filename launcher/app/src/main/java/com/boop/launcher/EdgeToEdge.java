package com.boop.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;

public final class EdgeToEdge {
 private EdgeToEdge(){}
 public static void apply(Activity activity){
  Window w=activity.getWindow();
  w.setStatusBarColor(Color.TRANSPARENT);
  w.setNavigationBarColor(Color.TRANSPARENT);
  if(Build.VERSION.SDK_INT>=30){
   w.setDecorFitsSystemWindows(false);
   View decor=w.getDecorView();
   WindowInsetsController c=decor.getWindowInsetsController();
   if(c!=null)c.setSystemBarsAppearance(0,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
  }else{
   w.getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE|android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
  }
 }
}
