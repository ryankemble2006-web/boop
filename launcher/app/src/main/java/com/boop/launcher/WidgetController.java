package com.boop.launcher;

import android.app.Activity;
import android.appwidget.*;
import android.content.*;

public final class WidgetController {
 public static final int PICK=201,BIND=202,CONFIGURE=203;private static final int HOST_ID=7062;
 private final Activity activity;private final AppWidgetManager manager;private final AppWidgetHost host;private int pending=-1;
 public WidgetController(Activity a){activity=a;manager=AppWidgetManager.getInstance(a);host=new AppWidgetHost(a,HOST_ID);}
 public void start(){host.startListening();}public void stop(){host.stopListening();}
 public void pick(){pending=host.allocateAppWidgetId();Intent i=new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,pending);activity.startActivityForResult(i,PICK);}
 public int handle(int request,int result,Intent data){if(request==PICK){if(result!=Activity.RESULT_OK){release();return -1;}int id=data==null?pending:data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,pending);AppWidgetProviderInfo info=manager.getAppWidgetInfo(id);if(info==null){release();return -1;}pending=id;if(info.configure!=null){Intent c=new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).setComponent(info.configure).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);activity.startActivityForResult(c,CONFIGURE);return -2;}return finish();}if(request==CONFIGURE){if(result!=Activity.RESULT_OK){release();return -1;}return finish();}return -1;}
 private int finish(){int id=pending;pending=-1;return id;}private void release(){if(pending>=0)host.deleteAppWidgetId(pending);pending=-1;}
}
