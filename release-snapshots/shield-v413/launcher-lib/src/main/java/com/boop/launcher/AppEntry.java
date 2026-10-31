package com.boop.launcher;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;

public final class AppEntry {
 public final ComponentName component;
 public final String label;
 public final Drawable icon;
 public AppEntry(ComponentName component,String label,Drawable icon){this.component=component;this.label=label;this.icon=icon;}
}
