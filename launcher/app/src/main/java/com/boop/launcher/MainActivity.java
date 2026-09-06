package com.boop.launcher;

import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.provider.Settings;
import android.text.*;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public final class MainActivity extends Activity {
 private static final int HOST=706, BIND=71, CONFIGURE=72;
 private final ArrayList<Item> items=new ArrayList<>();
 private final ArrayList<ResolveInfo> apps=new ArrayList<>();
 private final ArrayDeque<Integer> history=new ArrayDeque<>();
 private AppWidgetHost host; private AppWidgetManager widgets;
 private AlertDialog activeDialog;
 private ItemFrame touchingItem;
 private LinearLayout root; private FrameLayout canvas; private boolean editing,drawer,secondPage; private int page=0;
 private int pendingId=-1,pendingPage=1; private String pendingProvider="";
 private boolean waitingNotification, resumeBoop;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private static final class Item {String component="",label="";int widget=-1,page;Layout.Box box;}
 private android.content.SharedPreferences prefs(){return getSharedPreferences("home",MODE_PRIVATE);}
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 public void onCreate(Bundle b){super.onCreate(b);if(Build.VERSION.SDK_INT>=33)getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,this::handleBack);immersive();getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);widgets=AppWidgetManager.getInstance(this);host=new AppWidgetHost(this,HOST);load();pendingId=prefs().getInt("pendingId",-1);pendingPage=prefs().getInt("pendingPage",1);pendingProvider=prefs().getString("pendingProvider","");for(Item saved:items)if(saved.widget>=0&&saved.widget==pendingId){pendingId=-1;persistPending();break;}if(b!=null){page=b.getInt("page");editing=b.getBoolean("edit");drawer=b.getBoolean("drawer");int[] previous=b.getIntArray("history");if(previous!=null)for(int entry:previous)history.addLast(entry);}render();if(!prefs().getBoolean("welcome",false))handler.post(this::welcome);}
 protected void onStart(){super.onStart();host.startListening();}
 protected void onStop(){host.stopListening();super.onStop();}
 protected void onResume(){super.onResume();stopService(new Intent(this,ReturnService.class));}
 protected void onSaveInstanceState(Bundle b){b.putInt("page",page);b.putBoolean("edit",editing);b.putBoolean("drawer",drawer);int[] previous=new int[history.size()];int index=0;for(int entry:history)previous[index++]=entry;b.putIntArray("history",previous);super.onSaveInstanceState(b);}
 protected void onNewIntent(Intent i){super.onNewIntent(i);if(activeDialog!=null){activeDialog.dismiss();activeDialog=null;}setIntent(i);page=0;history.clear();editing=false;drawer=false;render();}
 @android.annotation.SuppressLint("GestureBackNavigation")
 @Override public void onBackPressed(){handleBack();}
 private void handleBack(){String action=Navigation.back(editing,drawer,page);if("edit".equals(action)){editing=false;render();}else if("drawer".equals(action)){drawer=false;render();}else if(!history.isEmpty()){page=history.pop();render();}else if("page".equals(action)){page=0;render();}}
 public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused)immersive();else if(touchingItem!=null)touchingItem.cancelTouch(true);}
 private void immersive(){if(Build.VERSION.SDK_INT>=30){getWindow().setDecorFitsSystemWindows(false);WindowInsetsController c=getWindow().getDecorView().getWindowInsetsController();if(c!=null){c.hide(WindowInsets.Type.systemBars());c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);}}else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);}
 private void welcome(){prefs().edit().putBoolean("welcome",true).apply();activeDialog=new AlertDialog.Builder(this).setTitle("Your BOOP home").setMessage("Swipe up for apps. Long press an app to add it. Long press empty home to edit. Swipe left for widgets. You can always change your default Home app in Android settings.").setPositiveButton("Start",(d,w)->{}).setNeutralButton("Open BOOP",(d,w)->openBoop()).setNegativeButton("Home settings",(d,w)->bailout()).show();}
 private void bailout(){try{startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));}catch(ActivityNotFoundException e){startActivity(new Intent(Settings.ACTION_SETTINGS));}}
 private Button button(String text,Runnable action){Button b=new Button(this);b.setText(text);b.setTextSize(18);b.setMinHeight(dp(56));b.setOnClickListener(v->action.run());return b;}
 private TextView label(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(20);t.setTextColor(Color.WHITE);t.setGravity(Gravity.CENTER);t.setPadding(dp(12),dp(12),dp(12),dp(12));return t;}
 private void render(){
  // Removing the old root sends CANCEL before detach. Preserve the navigation
  // state just chosen by Back/HOME instead of restoring the drag's old mode.
  if(touchingItem!=null)touchingItem.cancelTouch(false);
  if(!drawer&&getCurrentFocus()!=null)getSystemService(android.view.inputmethod.InputMethodManager.class).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
  root=new LinearLayout(this);root.setMotionEventSplittingEnabled(false);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(Color.BLACK);root.setFitsSystemWindows(true);setContentView(root);
  root.setOnApplyWindowInsetsListener((v,in)->{if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=in.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}else{int left=in.getSystemWindowInsetLeft(),top=in.getSystemWindowInsetTop(),right=in.getSystemWindowInsetRight(),bottom=in.getSystemWindowInsetBottom();DisplayCutout cut=in.getDisplayCutout();if(cut!=null){left=Math.max(left,cut.getSafeInsetLeft());top=Math.max(top,cut.getSafeInsetTop());right=Math.max(right,cut.getSafeInsetRight());bottom=Math.max(bottom,cut.getSafeInsetBottom());}v.setPadding(left,top,right,bottom);}return in;});
  if(drawer){renderDrawer();return;}
  if(editing){HorizontalScrollView bar=new HorizontalScrollView(this);LinearLayout row=new LinearLayout(this);row.addView(button("Done",()->{editing=false;render();}));row.addView(button("Bail out",this::bailout));row.addView(button("Apps",()->{drawer=true;render();}));row.addView(button("Add widget",this::chooseWidget));row.addView(button("Home",()->changePage(0)));row.addView(button("Widgets 1",()->changePage(1)));row.addView(button(secondPage?"Widgets 2":"Enable page 2",()->{secondPage=true;prefs().edit().putBoolean("second",true).apply();changePage(2);}));row.addView(button("Open BOOP",this::openBoop));bar.addView(row);root.addView(bar);TextView help=label("Hold and drag up to remove • tap an item for options • swipe down to finish");help.setTextSize(14);root.addView(help);}
  canvas=new FrameLayout(this);canvas.setMotionEventSplittingEnabled(false);canvas.setBackgroundColor(Color.BLACK);canvas.setContentDescription(page==0?"Home canvas":"Widget page "+page);root.addView(canvas,new LinearLayout.LayoutParams(-1,0,1));
  installCanvasGestures();canvas.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(r-l!=or-ol||b-t!=ob-ot)canvas.post(()->{if(canvas.getWidth()>0&&canvas.getHeight()>0)drawItems();});});
  canvas.post(()->{if(canvas.getWidth()>0 && canvas.getHeight()>0)drawItems();});
 }
 private void navigatePage(int dest){if(dest!=page){history.push(page);page=dest;}}
 private void changePage(int dest){navigatePage(dest);drawer=false;render();}
 private void installCanvasGestures(){
  canvas.setOnTouchListener(new View.OnTouchListener(){
   float startX,startY;boolean active,longPressed;Runnable hold;
   public boolean onTouch(View view,MotionEvent event){
    switch(event.getActionMasked()){
     case MotionEvent.ACTION_DOWN:
      startX=event.getX();startY=event.getY();active=true;longPressed=false;
      hold=()->{if(active){longPressed=true;editing=true;view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);render();}};
      handler.postDelayed(hold,ViewConfiguration.getLongPressTimeout());return true;
     case MotionEvent.ACTION_MOVE:
      if(Math.abs(event.getX()-startX)>ViewConfiguration.get(MainActivity.this).getScaledTouchSlop()||Math.abs(event.getY()-startY)>ViewConfiguration.get(MainActivity.this).getScaledTouchSlop())handler.removeCallbacks(hold);
      return true;
     case MotionEvent.ACTION_UP:
      handler.removeCallbacks(hold);boolean navigate=active&&!longPressed;active=false;
      if(navigate){String direction=Swipe.classify(event.getX()-startX,event.getY()-startY,dp(60));
       if("left".equals(direction))changePage(Math.min(secondPage?2:1,page+1));
       else if("right".equals(direction))changePage(Math.max(0,page-1));
       else if("up".equals(direction)){drawer=true;render();}
       else if("down".equals(direction)&&editing){editing=false;render();}
      }return true;
     case MotionEvent.ACTION_CANCEL:handler.removeCallbacks(hold);active=false;return true;
     default:return true;
    }
   }
  });
 }

 private void renderDrawer(){
  EditText search=new EditText(this);search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(Color.LTGRAY);search.setTextSize(22);search.setHint("Search apps");search.setContentDescription("Search apps");root.addView(search);ScrollView scroll=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));root.addView(button("Back to home",()->{drawer=false;editing=false;render();}));
  apps.clear();apps.addAll(getPackageManager().queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0));apps.sort((a,b)->a.loadLabel(getPackageManager()).toString().compareToIgnoreCase(b.loadLabel(getPackageManager()).toString()));
  Runnable fill=()->{list.removeAllViews();String q=search.getText().toString().toLowerCase(Locale.ROOT);for(ResolveInfo a:apps){String name=a.loadLabel(getPackageManager()).toString();if(!name.toLowerCase(Locale.ROOT).contains(q))continue;ComponentName cn=new ComponentName(a.activityInfo.packageName,a.activityInfo.name);Button b=button(name,()->launch(cn.flattenToString()));Drawable icon=a.loadIcon(getPackageManager());icon.setBounds(0,0,dp(48),dp(48));b.setCompoundDrawables(icon,null,null,null);b.setCompoundDrawablePadding(dp(16));b.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);b.setContentDescription(name+". Long press to add to home");b.setOnLongClickListener(v->{Item it=new Item();it.component=cn.flattenToString();it.label=name;it.page=page;it.box=new Layout.Box(.04f,.04f,.22f,.18f);if(add(it)){drawer=false;editing=true;render();}return true;});list.addView(b);}if(list.getChildCount()==0)list.addView(label("No matching apps"));};search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){fill.run();}public void afterTextChanged(Editable e){}});fill.run();
 }
 private ArrayList<Layout.Box> occupied(int p,Item except){ArrayList<Layout.Box> out=new ArrayList<>();for(Item i:items)if(i.page==p&&i!=except)out.add(i.box);return out;}
 private boolean add(Item i){Layout.Box b=Layout.place(i.box,occupied(i.page,null));if(b==null){toast("This page is full. Move or remove something first.");return false;}i.box=b;items.add(i);save();return true;}
 private void drawItems(){
  canvas.removeAllViews();int w=canvas.getWidth(),h=canvas.getHeight();for(Item i:items){if(i.page!=page)continue;View content;
   if(i.widget>=0){AppWidgetProviderInfo info=widgets.getAppWidgetInfo(i.widget);if(info==null){Button b=button("Widget unavailable — hold to remove",()->toast("The widget provider may have been removed or disabled."));content=b;}else{AppWidgetHostView v=host.createView(this,i.widget,info);v.setAppWidget(i.widget,info);int width=Math.max(1,(int)(i.box.w*w/getResources().getDisplayMetrics().density)),height=Math.max(1,(int)(i.box.h*h/getResources().getDisplayMetrics().density));v.updateAppWidgetSize(null,width,height,width,height);content=v;}}
   else {LinearLayout icon=new LinearLayout(this);icon.setOrientation(LinearLayout.VERTICAL);icon.setGravity(Gravity.CENTER);ImageView image=new ImageView(this);image.setScaleType(ImageView.ScaleType.FIT_CENTER);try{image.setImageDrawable(getPackageManager().getActivityIcon(ComponentName.unflattenFromString(i.component)));}catch(Exception e){image.setImageResource(android.R.drawable.sym_def_app_icon);}icon.addView(image,new LinearLayout.LayoutParams(-1,0,1));TextView name=label(i.label);name.setTextSize(14);name.setMaxLines(2);name.setPadding(0,0,0,0);icon.addView(name);icon.setContentDescription(i.label);icon.setOnClickListener(v->{if(editing)itemMenu(i);else launch(i.component);});content=icon;}
   ItemFrame frame=new ItemFrame(i);frame.addView(content,new FrameLayout.LayoutParams(-1,-1));frame.setContentDescription(i.widget>=0?"Widget. Long press to edit":i.label);frame.setBackgroundColor(editing?0x33222222:Color.TRANSPARENT);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(Math.max(1,(int)(i.box.w*w)),Math.max(1,(int)(i.box.h*h)));p.leftMargin=Math.round(i.box.x*w);p.topMargin=Math.round(i.box.y*h);canvas.addView(frame,p);
  }
 }
 private final class ItemFrame extends FrameLayout {
  final Item item;
  final FrameLayout surface=canvas;
  final LinearLayout container=root;
  float x,y,localX,localY,startX,startY;
  long downTime;
  boolean tracking,ownsTouch,wasEditing,moved,dragging,cancelled;
  boolean surfaceClipped,containerClipped;
  Runnable hold;
  TextView removeTarget;

  ItemFrame(Item i){super(MainActivity.this);item=i;}

  // Keep terminal events here: intercepting a widget for the first time on UP
  // would cancel its child without delivering that same UP to onTouchEvent.
  @Override public boolean dispatchTouchEvent(MotionEvent e){
   int action=e.getActionMasked();
   if(action==MotionEvent.ACTION_DOWN){
    x=e.getRawX();y=e.getRawY();localX=e.getX();localY=e.getY();downTime=e.getDownTime();
    startX=item.box.x;startY=item.box.y;wasEditing=editing;
    tracking=true;ownsTouch=wasEditing||item.widget<0;moved=false;dragging=false;cancelled=false;
    touchingItem=this;
    hold=()->{if(tracking&&!cancelled&&isAttachedToWindow())beginDrag();};
    handler.postDelayed(hold,ViewConfiguration.getLongPressTimeout());
   }
   if(!tracking)return super.dispatchTouchEvent(e);
   if(action==MotionEvent.ACTION_POINTER_DOWN){cancelTouch(true);return true;}
   if(cancelled){if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL)finishTracking();return true;}
   if(action==MotionEvent.ACTION_MOVE){
    float dx=e.getRawX()-x,dy=e.getRawY()-y;
    if(Math.abs(dx)>ViewConfiguration.get(MainActivity.this).getScaledTouchSlop()||Math.abs(dy)>ViewConfiguration.get(MainActivity.this).getScaledTouchSlop()){
     moved=true;handler.removeCallbacks(hold);
     if(wasEditing&&!dragging)beginDrag();
    }
    if(dragging){setTranslationX(dx);setTranslationY(dy);boolean removing=overRemove(e);removeTarget.setText(removing?"Release to remove":"Drag here to remove");removeTarget.setBackgroundColor(removing?0xff602828:0xff202020);}
   }
   if(action==MotionEvent.ACTION_CANCEL){
    if(!ownsTouch)super.dispatchTouchEvent(e);
    cancelTouch(true);finishTracking();return true;
   }
   if(action==MotionEvent.ACTION_UP){
    if(!ownsTouch){boolean handled=super.dispatchTouchEvent(e);finishTracking();return handled;}
    boolean removeOnDrop=dragging&&moved&&overRemove(e),wasDragging=dragging;
    float dx=e.getRawX()-x,dy=e.getRawY()-y;
    endDragVisuals();finishTracking();
    if(removeOnDrop){remove(item);return true;}
    if(wasDragging){
     if(moved&&surface.getWidth()>0&&surface.getHeight()>0){
      Layout.Box box=Layout.clamp(new Layout.Box(startX+dx/surface.getWidth(),startY+dy/surface.getHeight(),item.box.w,item.box.h));
      if(Layout.free(box,occupied(item.page,item))){item.box=box;save();}else toast("That spot overlaps another item.");
     }
     render();
    }else if(!moved){if(wasEditing)itemMenu(item);else if(item.widget<0)launch(item.component);}
    return true;
   }
   if(ownsTouch)return true;
   // A non-clickable widget must still retain its pending long press.
   super.dispatchTouchEvent(e);return true;
  }

  private void beginDrag(){
   if(dragging||cancelled||!tracking)return;
   if(!ownsTouch)cancelWidgetTouch();
   ownsTouch=true;dragging=true;editing=true;
   getParent().requestDisallowInterceptTouchEvent(true);
   performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
   surfaceClipped=surface.getClipChildren();containerClipped=container.getClipChildren();
   surface.setClipChildren(false);container.setClipChildren(false);setAlpha(.7f);
   // Overlay the existing layout: no resize, dialog or new touch target while held.
   removeTarget=label("Drag here to remove");removeTarget.setBackgroundColor(0xff202020);
   removeTarget.setPadding(dp(12),container.getPaddingTop(),dp(12),dp(12));
   int height=container.getPaddingTop()+dp(72);
   removeTarget.measure(MeasureSpec.makeMeasureSpec(container.getWidth(),MeasureSpec.EXACTLY),MeasureSpec.makeMeasureSpec(height,MeasureSpec.EXACTLY));
   removeTarget.layout(0,0,container.getWidth(),height);
   container.getOverlay().add(removeTarget);
  }

  private boolean overRemove(MotionEvent e){
   if(removeTarget==null)return false;
   int[] pos=new int[2];removeTarget.getLocationOnScreen(pos);
   return e.getRawX()>=pos[0]&&e.getRawX()<pos[0]+removeTarget.getWidth()&&e.getRawY()>=pos[1]&&e.getRawY()<pos[1]+removeTarget.getHeight();
  }

  private void cancelWidgetTouch(){
   MotionEvent cancel=MotionEvent.obtain(downTime,SystemClock.uptimeMillis(),MotionEvent.ACTION_CANCEL,localX,localY,0);
   cancel.setSource(InputDevice.SOURCE_TOUCHSCREEN);
   super.dispatchTouchEvent(cancel);cancel.recycle();
  }

  private void endDragVisuals(){
   if(removeTarget!=null){container.getOverlay().remove(removeTarget);removeTarget=null;}
   if(dragging){surface.setClipChildren(surfaceClipped);container.setClipChildren(containerClipped);}
   setTranslationX(0);setTranslationY(0);setAlpha(1);dragging=false;
   if(getParent()!=null)getParent().requestDisallowInterceptTouchEvent(false);
  }

  private void cancelTouch(boolean restoreEditing){
   if(!tracking)return;
   if(!ownsTouch)cancelWidgetTouch();
   ownsTouch=true;cancelled=true;handler.removeCallbacks(hold);
   if(dragging&&restoreEditing)editing=wasEditing;
   endDragVisuals();
   if(touchingItem==this)touchingItem=null;
  }

  private void finishTracking(){if(hold!=null)handler.removeCallbacks(hold);tracking=false;if(touchingItem==this)touchingItem=null;}
  @Override protected void onDetachedFromWindow(){cancelTouch(false);finishTracking();super.onDetachedFromWindow();}
 }
 private void itemMenu(Item i){editing=true;String[] choices={"Small","Medium","Large","Move to Home","Move to Widgets 1","Move to Widgets 2","Remove","Done editing"};activeDialog=new AlertDialog.Builder(this).setTitle(i.widget>=0?"Edit widget":i.label).setItems(choices,(d,n)->{if(n>=0&&n<=2){float factor=n==0?.8f:n==1?1f:1.25f;float bw=i.widget>=0?.7f:.22f,bh=i.widget>=0?.32f:.18f;Layout.Box box=Layout.place(new Layout.Box(i.box.x,i.box.y,bw*factor,bh*factor),occupied(i.page,i));if(box!=null){i.box=box;save();}else toast("Not enough free space for that size.");}else if(n>=3&&n<=5){int target=n-3;Layout.Box box=Layout.place(i.box,occupied(target,i));if(box!=null){i.page=target;i.box=box;if(target==2){secondPage=true;prefs().edit().putBoolean("second",true).apply();}save();navigatePage(target);}else toast("That page is full.");}else if(n==6){remove(i);return;}else if(n==7)editing=false;render();}).setOnCancelListener(d->render()).show();}
 private void remove(Item i){items.remove(i);if(i.widget>=0)host.deleteAppWidgetId(i.widget);save();render();}
 private void launch(String component){try{startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setComponent(ComponentName.unflattenFromString(component)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(Exception e){toast("This app is no longer available. Long press its icon to remove it.");}}
 private void chooseWidget(){if(pendingId>=0){activeDialog=new AlertDialog.Builder(this).setTitle("Unfinished widget").setMessage("Discard the unfinished widget setup and choose again?").setPositiveButton("Discard",(d,w)->{cancelPending();chooseWidget();}).setNegativeButton("Keep",null).show();return;}List<AppWidgetProviderInfo> providers=widgets.getInstalledProviders();providers.sort((a,b)->a.loadLabel(getPackageManager()).compareToIgnoreCase(b.loadLabel(getPackageManager())));if(providers.isEmpty()){toast("Install an app with Android widgets, such as Home Assistant, then try again.");return;}String[] names=new String[providers.size()];for(int j=0;j<names.length;j++)names[j]=providers.get(j).loadLabel(getPackageManager())+" — "+providers.get(j).provider.getPackageName();activeDialog=new AlertDialog.Builder(this).setTitle("Choose Android widget").setItems(names,(d,n)->{AppWidgetProviderInfo provider=providers.get(n);pendingId=host.allocateAppWidgetId();pendingPage=page==0?1:page;pendingProvider=provider.provider.flattenToString();persistPending();try{if(widgets.bindAppWidgetIdIfAllowed(pendingId,provider.provider))configureWidget();else startActivityForResult(new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,pendingId).putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER,provider.provider),BIND);}catch(RuntimeException e){cancelPending();toast("Android could not start widget binding.");}}).setNegativeButton("Cancel",null).show();}
 private void configureWidget(){AppWidgetProviderInfo info=widgets.getAppWidgetInfo(pendingId);if(info==null){cancelPending();toast("Widget binding was not completed.");return;}if(info.configure!=null){try{host.startAppWidgetConfigureActivityForResult(this,pendingId,0,CONFIGURE,null);}catch(RuntimeException e){cancelPending();toast("The widget configuration could not open.");}}else finishWidget();}
 protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=BIND&&request!=CONFIGURE)return;if(pendingId<0)return;if(result!=RESULT_OK){cancelPending();render();return;}if(request==BIND)configureWidget();else finishWidget();}
 private void finishWidget(){Item i=new Item();i.widget=pendingId;i.page=pendingPage;i.box=new Layout.Box(.04f,.04f,.7f,.32f);if(add(i)){pendingId=-1;persistPending();navigatePage(i.page);editing=true;drawer=false;render();}else cancelPending();}
 private void cancelPending(){if(pendingId>=0)host.deleteAppWidgetId(pendingId);pendingId=-1;persistPending();}
 private void persistPending(){prefs().edit().putInt("pendingId",pendingId).putInt("pendingPage",pendingPage).putString("pendingProvider",pendingProvider).apply();}
 private void openBoop(){Intent launch=getPackageManager().getLaunchIntentForPackage("com.boop.alpha1");if(launch==null){toast("Install BOOP (com.boop.alpha1) first, then return here to open it.");return;}if(prefs().getBoolean("edge",false)&&Settings.canDrawOverlays(this)){launchBoop(launch,true);return;}activeDialog=new AlertDialog.Builder(this).setTitle("Open BOOP").setMessage("Optional: enable a narrow right-edge strip, then swipe left to return home. It stays active if BOOP opens another app, until you return, stop it in the notification, or turn off the screen. BOOP taps and voice remain inside BOOP. Android will ask for permission to display over other apps.").setPositiveButton("Enable return strip",(d,w)->{prefs().edit().putBoolean("edge",true).apply();if(!Settings.canDrawOverlays(this)){startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,android.net.Uri.parse("package:"+getPackageName())));toast("After granting permission, return here and tap Open BOOP again.");}else launchBoop(launch,true);}).setNeutralButton("Open without strip",(d,w)->{prefs().edit().putBoolean("edge",false).apply();launchBoop(launch,false);}).setNegativeButton("Cancel",null).show();}
 private void launchBoop(Intent launch,boolean edge){if(edge){if(Build.VERSION.SDK_INT>=33&&!prefs().getBoolean("notificationAsked",false)&&checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED){waitingNotification=true;prefs().edit().putBoolean("notificationAsked",true).apply();requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},73);return;}try{startForegroundService(new Intent(this,ReturnService.class));}catch(RuntimeException e){toast("Return strip could not start. Use Android Home to return.");}}try{startActivity(launch);}catch(RuntimeException e){stopService(new Intent(this,ReturnService.class));toast("BOOP could not open.");}}
 public void onRequestPermissionsResult(int code,String[] permissions,int[] results){super.onRequestPermissionsResult(code,permissions,results);if(code==73&&waitingNotification){waitingNotification=false;resumeBoop=true;}}
 protected void onPostResume(){super.onPostResume();if(resumeBoop){resumeBoop=false;handler.post(()->{Intent i=getPackageManager().getLaunchIntentForPackage("com.boop.alpha1");if(i!=null)launchBoop(i,true);});}}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 private void save(){JSONArray a=new JSONArray();try{for(Item i:items){JSONObject o=new JSONObject();o.put("component",i.component);o.put("label",i.label);o.put("widget",i.widget);o.put("page",i.page);o.put("x",i.box.x);o.put("y",i.box.y);o.put("w",i.box.w);o.put("h",i.box.h);a.put(o);}prefs().edit().putString("items",a.toString()).apply();}catch(JSONException e){toast("Could not save layout.");}}
 private void load(){secondPage=prefs().getBoolean("second",false);try{JSONArray a=new JSONArray(prefs().getString("items","[]"));for(int n=0;n<a.length();n++){JSONObject o=a.getJSONObject(n);Item i=new Item();i.component=o.optString("component");i.label=o.optString("label");i.widget=o.optInt("widget",-1);i.page=Math.max(0,Math.min(2,o.optInt("page")));i.box=Layout.place(new Layout.Box((float)o.optDouble("x",0),(float)o.optDouble("y",0),(float)o.optDouble("w",.22),(float)o.optDouble("h",.18)),occupied(i.page,null));if(i.box!=null)items.add(i);}}catch(JSONException e){toast("The saved layout could not be read.");}}
}
