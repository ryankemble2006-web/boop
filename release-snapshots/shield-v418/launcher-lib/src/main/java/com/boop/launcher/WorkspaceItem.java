package com.boop.launcher;

import android.content.ComponentName;

public final class WorkspaceItem {
 public String component,label; public float x,y,w=.22f,h=.13f; public int page=0; public int widgetId=-1;
 public ComponentName componentName(){return ComponentName.unflattenFromString(component);}
}
