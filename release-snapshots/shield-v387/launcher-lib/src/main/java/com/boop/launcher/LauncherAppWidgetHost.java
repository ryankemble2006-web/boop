package com.boop.launcher;

import android.appwidget.*;
import android.content.Context;

public final class LauncherAppWidgetHost extends AppWidgetHost {
 private final Runnable changed;
 public LauncherAppWidgetHost(Context context,int hostId,Runnable changed){super(context,hostId);this.changed=changed;}
 @Override protected void onProviderChanged(int appWidgetId,AppWidgetProviderInfo info){super.onProviderChanged(appWidgetId,info);if(changed!=null)changed.run();}
 @Override protected void onProvidersChanged(){super.onProvidersChanged();if(changed!=null)changed.run();}
}
