package com.boop.launcher;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class MainActivity extends Activity {
 private final AppRepository appRepository=new AppRepository();private ArrayList<WorkspaceItem> items;private WorkspaceStore store;private WorkspaceView workspace;private AllAppsView drawer;private FrameLayout root;private LauncherState state=LauncherState.HOME;private WidgetController widgets;
 @Override public void onCreate(Bundle b){super.onCreate(b);EdgeToEdge.apply(this);getWindow().getDecorView().setBackgroundColor(Color.BLACK);store=new WorkspaceStore(this);if(!getPreferences(MODE_PRIVATE).getBoolean("alpha2_started",false)){store.clearLegacy();getPreferences(MODE_PRIVATE).edit().putBoolean("alpha2_started",true).apply();}items=store.load();widgets=new WidgetController(this);if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::handleBack);build();}
 @Override protected void onStart(){super.onStart();widgets.start();}
 @Override protected void onStop(){widgets.stop();super.onStop();}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);state=LauncherState.HOME;showState(false);}
 @SuppressLint("GestureBackNavigation")
 @Override public void onBackPressed(){handleBack();}
 private void handleBack(){if(state==LauncherState.SEARCH&&drawer!=null&&drawer.isSearching()){drawer.closeSearch();state=LauncherState.ALL_APPS;return;}if(state!=LauncherState.HOME){state=state.back();showState(true);}}
 private void build(){root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);setContentView(root);workspace=new WorkspaceView(this,items,new WorkspaceView.Listener(){public void launch(WorkspaceItem i){MainActivity.this.launch(i.componentName());}public void changed(){store.save(items);}public void emptyLongPress(float x,float y){showHomeMenu();}public void openDrawer(){state=LauncherState.ALL_APPS;showState(true);}});drawer=new AllAppsView(this,new AllAppsView.Listener(){public void launch(AppEntry a){MainActivity.this.launch(a.component);}public void pin(AppEntry a){workspace.addApp(a);state=LauncherState.HOME;showState(true);Toast.makeText(MainActivity.this,"Added to home",Toast.LENGTH_SHORT).show();}public void searchOpened(){state=LauncherState.SEARCH;}public void searchClosed(){state=LauncherState.ALL_APPS;}});drawer.submit(appRepository.loadLaunchableApps(getPackageManager()));root.addView(workspace,new FrameLayout.LayoutParams(-1,-1));root.addView(drawer,new FrameLayout.LayoutParams(-1,-1));workspace.refresh();showState(false);}
 private void showState(boolean animate){if(state==LauncherState.HOME){workspace.setVisibility(View.VISIBLE);if(drawer.isSearching())drawer.closeSearch();if(animate&&drawer.getVisibility()==View.VISIBLE)drawer.animate().translationY(root.getHeight()).setDuration(220).withEndAction(()->drawer.setVisibility(View.GONE)).start();else drawer.setVisibility(View.GONE);return;}drawer.submit(appRepository.loadLaunchableApps(getPackageManager()));drawer.setVisibility(View.VISIBLE);drawer.setTranslationY(animate?root.getHeight():0);workspace.setVisibility(View.VISIBLE);drawer.animate().translationY(0).setDuration(240).start();}
 private void launch(android.content.ComponentName c){if(c==null)return;try{Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(c).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception e){Toast.makeText(this,"That app is no longer available",Toast.LENGTH_SHORT).show();items.removeIf(it->c.flattenToString().equals(it.component));store.save(items);workspace.refresh();}}
 private void showHomeMenu(){PopupMenu menu=new PopupMenu(this,workspace);menu.getMenu().add("Add widget");menu.getMenu().add("Home settings");menu.setOnMenuItemClickListener(item->{if("Add widget".contentEquals(item.getTitle()))widgets.pick();else openHomeSettings();return true;});menu.show();}
 private void openHomeSettings(){try{startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(ActivityNotFoundException e){startActivity(new Intent(Settings.ACTION_SETTINGS));}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);int id=widgets.handle(request,result,data);if(id>=0){WorkspaceItem i=new WorkspaceItem();i.widgetId=id;i.label="Widget";i.x=.08f;i.y=.12f;i.w=.84f;i.h=.24f;items.add(i);store.save(items);Toast.makeText(this,"Widget added. Placement preview comes next.",Toast.LENGTH_SHORT).show();workspace.refresh();}}
}
