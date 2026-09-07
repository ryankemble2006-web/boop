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
 @Override public void onCreate(Bundle b){super.onCreate(b);configureOpenTransition();EdgeToEdge.apply(this);getWindow().getDecorView().setBackgroundColor(Color.BLACK);store=new WorkspaceStore(this);if(!getPreferences(MODE_PRIVATE).getBoolean("alpha2_started",false)){store.clearLegacy();getPreferences(MODE_PRIVATE).edit().putBoolean("alpha2_started",true).apply();}items=store.load();widgets=new WidgetController(this);if(widgets.prune(items))store.save(items);if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::handleBack);build();}
 private void configureOpenTransition(){if(Build.VERSION.SDK_INT>=34)overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,R.anim.boop_enter_from_left,R.anim.boop_exit_to_right);}
 @Override public void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus)EdgeToEdge.hideBars(this);}
 @Override protected void onStart(){super.onStart();widgets.start();}
 @Override protected void onResume(){super.onResume();if(widgets.prune(items)){store.save(items);if(workspace!=null)workspace.refresh();}}
 @Override protected void onStop(){widgets.stop();super.onStop();}
 @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);state=LauncherState.HOME;showState(false);}
 @SuppressLint("GestureBackNavigation")
 @Override public void onBackPressed(){handleBack();}
 private void handleBack(){if(state==LauncherState.SEARCH&&drawer!=null&&drawer.isSearching()){drawer.closeSearch();state=LauncherState.ALL_APPS;return;}if(state!=LauncherState.HOME){state=state.back();showState(true);}}
 private void build(){root=new FrameLayout(this);root.setBackgroundColor(Color.BLACK);setContentView(root);workspace=new WorkspaceView(this,items,widgets,new WorkspaceView.Listener(){public void launch(WorkspaceItem i){MainActivity.this.launch(i.componentName());}public void changed(){store.save(items);}public void emptyLongPress(float x,float y){showHomeMenu(x,y);}public void openDrawer(){state=LauncherState.ALL_APPS;showState(true);}public void openWall(){MainActivity.this.openWall();}});widgets.setRefreshCallback(()->runOnUiThread(()->{if(workspace!=null)workspace.refresh();}));drawer=new AllAppsView(this,new AllAppsView.Listener(){public void launch(AppEntry a){MainActivity.this.launch(a.component);}public void pin(AppEntry a){workspace.addApp(a);state=LauncherState.HOME;showState(true);Toast.makeText(MainActivity.this,"Added to home",Toast.LENGTH_SHORT).show();}public void searchOpened(){state=LauncherState.SEARCH;}public void searchClosed(){state=LauncherState.ALL_APPS;}public void closeDrawer(){state=LauncherState.HOME;showState(true);}});drawer.submit(appRepository.loadLaunchableApps(getPackageManager()));root.addView(workspace,new FrameLayout.LayoutParams(-1,-1));root.addView(drawer,new FrameLayout.LayoutParams(-1,-1));workspace.refresh();showState(false);}
 private void showState(boolean animate){if(state==LauncherState.HOME){workspace.setVisibility(View.VISIBLE);if(drawer.isSearching())drawer.closeSearch();if(animate&&drawer.getVisibility()==View.VISIBLE)drawer.animate().translationY(root.getHeight()).setDuration(220).withEndAction(()->drawer.setVisibility(View.GONE)).start();else drawer.setVisibility(View.GONE);return;}drawer.submit(appRepository.loadLaunchableApps(getPackageManager()));drawer.setVisibility(View.VISIBLE);drawer.setTranslationY(animate?root.getHeight():0);workspace.setVisibility(View.VISIBLE);drawer.animate().translationY(0).setDuration(240).start();}
 private void launch(android.content.ComponentName c){if(c==null)return;try{Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(c).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);}catch(Exception e){Toast.makeText(this,"That app is no longer available",Toast.LENGTH_SHORT).show();items.removeIf(it->c.flattenToString().equals(it.component));store.save(items);workspace.refresh();}}
 private void showHomeMenu(float x,float y){android.util.Log.i("BOOPLauncher","HOME_LONG_PRESS");View anchor=new View(this);anchor.setAlpha(0f);FrameLayout.LayoutParams ap=new FrameLayout.LayoutParams(2,2);ap.leftMargin=Math.max(0,Math.min(root.getWidth()-2,Math.round(x)));ap.topMargin=Math.max(0,Math.min(root.getHeight()-2,Math.round(y)));root.addView(anchor,ap);PopupMenu menu=new PopupMenu(this,anchor);menu.getMenu().add("Add widget");menu.getMenu().add("Home settings");menu.setOnMenuItemClickListener(item->{if("Add widget".contentEquals(item.getTitle()))widgets.pick();else openHomeSettings();return true;});menu.setOnDismissListener(m->{root.removeView(anchor);});menu.show();}
 private void openWall(){android.util.Log.i("BOOPLauncher","BOOP_WALL_SWIPE");Intent i=getPackageManager().getLaunchIntentForPackage("com.boop.alpha1");if(i==null){Toast.makeText(this,"BOOP Wall is not installed",Toast.LENGTH_SHORT).show();return;}i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_NEW_TASK);try{startActivity(i);}catch(Exception e){Toast.makeText(this,"BOOP Wall could not open",Toast.LENGTH_SHORT).show();}}
 private void openHomeSettings(){try{startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(ActivityNotFoundException e){startActivity(new Intent(Settings.ACTION_SETTINGS));}}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);int id=widgets.handle(request,result,data);if(id>=0){workspace.addWidget(id);Toast.makeText(this,"Widget added",Toast.LENGTH_SHORT).show();}}
}
