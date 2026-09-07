package com.boop.launcher;

import android.app.Activity;
import android.appwidget.*;
import android.content.*;
import java.util.*;

public final class WidgetController {
 public static final int PICK=201,BIND=202,CONFIGURE=203;private static final int HOST_ID=7062;private static final String PREF="alpha2_widget_flow",KEY_PENDING="pending";
 private final Activity activity;private final AppWidgetManager manager;private final LauncherAppWidgetHost host;private int pending;private Runnable refresh;
 public WidgetController(Activity a){activity=a;manager=AppWidgetManager.getInstance(a);pending=a.getSharedPreferences(PREF,Context.MODE_PRIVATE).getInt(KEY_PENDING,-1);host=new LauncherAppWidgetHost(a,HOST_ID,()->{if(refresh!=null)refresh.run();});}
 public void setRefreshCallback(Runnable r){refresh=r;}
 public void start(){host.startListening();}
 public void stop(){host.stopListening();}
 public void pick(){releasePending();setPending(host.allocateAppWidgetId());Intent i=new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,pending);activity.startActivityForResult(i,PICK);}
 public int handle(int request,int result,Intent data){
  if(request==PICK){if(result!=Activity.RESULT_OK){releasePending();return -1;}int id=data==null?pending:data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,pending);setPending(id);AppWidgetProviderInfo info=manager.getAppWidgetInfo(id);if(info!=null)return configureOrFinish(info);android.content.ComponentName provider=data==null?null:(android.content.ComponentName)data.getParcelableExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER);if(provider==null){releasePending();return -1;}if(manager.bindAppWidgetIdIfAllowed(id,provider)){info=manager.getAppWidgetInfo(id);return info==null?fail():configureOrFinish(info);}Intent bind=new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);bind.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);bind.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,provider);activity.startActivityForResult(bind,BIND);return -2;}
  if(request==BIND){if(result!=Activity.RESULT_OK){releasePending();return -1;}AppWidgetProviderInfo info=manager.getAppWidgetInfo(pending);return info==null?fail():configureOrFinish(info);}
  if(request==CONFIGURE){if(result!=Activity.RESULT_OK){releasePending();return -1;}return finish();}
  return -1;
 }
 private int configureOrFinish(AppWidgetProviderInfo info){if(info.configure!=null){Intent c=new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).setComponent(info.configure).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,pending);activity.startActivityForResult(c,CONFIGURE);return -2;}return finish();}
 public AppWidgetHostView createView(Context c,int id){AppWidgetProviderInfo info=manager.getAppWidgetInfo(id);if(info==null)return null;AppWidgetHostView v=host.createView(c,id,info);v.setAppWidget(id,info);return v;}
 public boolean prune(ArrayList<WorkspaceItem> items){boolean changed=false;HashSet<Integer> keep=new HashSet<>();Iterator<WorkspaceItem> it=items.iterator();while(it.hasNext()){WorkspaceItem item=it.next();if(item.widgetId<0)continue;if(manager.getAppWidgetInfo(item.widgetId)==null){deleteId(item.widgetId);it.remove();changed=true;}else keep.add(item.widgetId);}for(int id:host.getAppWidgetIds())if(!keep.contains(id)&&id!=pending)deleteId(id);return changed;}
 public void deleteId(int id){if(id<0)return;try{host.deleteAppWidgetId(id);}catch(Exception ignored){}}
 private int finish(){int id=pending;clearPending();return id;}
 private int fail(){releasePending();return -1;}
 private void setPending(int id){pending=id;activity.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putInt(KEY_PENDING,id).apply();}
 private void clearPending(){pending=-1;activity.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().remove(KEY_PENDING).apply();}
 private void releasePending(){if(pending>=0)deleteId(pending);clearPending();}
}
