package com.boop.launcher;

import android.appwidget.AppWidgetHostView;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public final class WorkspaceView extends FrameLayout {
 public interface Listener{void launch(WorkspaceItem item);void changed();void emptyLongPress(float x,float y);void openDrawer();void openWall();}
 private final ArrayList<WorkspaceItem> items;private final Listener listener;private final WidgetController widgets;private final Handler handler=new Handler(Looper.getMainLooper());
 private final int slop,removeBand,drawerThreshold,pageThreshold;private int currentPage=0;
 private WorkspaceItem editingItem;private WidgetFrame activeWidget;private LinearLayout widgetTools;
 private static final int EDIT_COLOUR=0xff00c8e8;
 public WorkspaceView(Context c,ArrayList<WorkspaceItem> items,WidgetController widgets,Listener listener){super(c);this.items=items;this.widgets=widgets;this.listener=listener;setBackgroundColor(Color.BLACK);setContentDescription("Home canvas");setMotionEventSplittingEnabled(false);slop=ViewConfiguration.get(c).getScaledTouchSlop();removeBand=dp(112);drawerThreshold=dp(72);pageThreshold=dp(64);setOnTouchListener(new EmptyTouch());}
 private int dp(float n){return Math.round(n*getResources().getDisplayMetrics().density);}
 public int currentPage(){return currentPage;}
 private final Runnable redraw=this::drawItems;
 public void refresh(){removeCallbacks(redraw);removeAllViews();activeWidget=null;widgetTools=null;post(redraw);}
 public boolean finishWidgetEdit(){if(editingItem==null)return false;editingItem=null;if(activeWidget!=null)activeWidget.showEditing(false);activeWidget=null;if(widgetTools!=null){removeView(widgetTools);widgetTools=null;}return true;}
 private void blocked(){Toast.makeText(getContext(),"That space is occupied. Move the icons first.",Toast.LENGTH_SHORT).show();}
 private void beginWidgetEdit(WidgetFrame frame){
  if(activeWidget!=null&&activeWidget!=frame)activeWidget.showEditing(false);
  editingItem=frame.item;activeWidget=frame;frame.showEditing(true);frame.bringToFront();showWidgetTools();
 }
 private void showWidgetTools(){
  if(widgetTools!=null)removeView(widgetTools);
  widgetTools=new LinearLayout(getContext());widgetTools.setOrientation(LinearLayout.HORIZONTAL);widgetTools.setPadding(dp(8),dp(6),dp(8),dp(6));widgetTools.setBackgroundColor(0xff202627);widgetTools.setElevation(dp(12));
  addWidgetAction("Centre",()->{WorkspacePlacement.Bounds b=WorkspacePlacement.centre(editingItem,items);if(b==null){blocked();return;}b.apply(editingItem);listener.changed();refresh();});
  addWidgetAction("Remove",()->{WorkspaceItem item=editingItem;finishWidgetEdit();removeItem(item);});
  addWidgetAction("Done",this::finishWidgetEdit);
  FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,-2,editingItem.y+editingItem.h>.75f?Gravity.TOP:Gravity.BOTTOM);p.leftMargin=p.rightMargin=dp(12);
  android.graphics.Insets safe=android.graphics.Insets.NONE;WindowInsets insets=getRootWindowInsets();if(insets!=null){if(Build.VERSION.SDK_INT>=30)safe=insets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());else safe=android.graphics.Insets.of(insets.getStableInsetLeft(),insets.getStableInsetTop(),insets.getStableInsetRight(),insets.getStableInsetBottom());}
  p.topMargin=safe.top+dp(8);p.bottomMargin=safe.bottom+dp(8);addView(widgetTools,p);
 }
 private void addWidgetAction(String title,Runnable action){TextView button=new TextView(getContext());button.setText(title);button.setTextColor(EDIT_COLOUR);button.setTextSize(16);button.setGravity(Gravity.CENTER);button.setMinHeight(dp(48));button.setPadding(dp(4),dp(8),dp(4),dp(8));button.setBackgroundResource(android.R.drawable.list_selector_background);button.setOnClickListener(v->{if(editingItem!=null)action.run();});widgetTools.addView(button,new LinearLayout.LayoutParams(0,-2,1));}
 public void addApp(AppEntry app){WorkspaceItem i=new WorkspaceItem();i.component=app.component.flattenToString();i.label=app.label;i.page=currentPage;if(!findOpenSlot(i,currentPage)){i.page=maxPage()+1;currentPage=i.page;findOpenSlot(i,currentPage);}items.add(i);listener.changed();refresh();}
 public void addWidget(int id){WorkspaceItem i=new WorkspaceItem();i.widgetId=id;i.label="Widget";i.w=.84f;i.h=.24f;i.page=currentPage;if(!findOpenSlot(i,currentPage)){i.page=maxPage()+1;currentPage=i.page;findOpenSlot(i,currentPage);}items.add(i);listener.changed();refresh();}
 private boolean findOpenSlot(WorkspaceItem item,int page){item.page=page;for(float y=.05f;y+item.h<.96f;y+=.06f)for(float x=.04f;x+item.w<.97f;x+=.05f){item.x=x;item.y=y;if(!overlaps(item,null))return true;}item.x=.05f;item.y=.08f;return false;}
 private boolean overlaps(WorkspaceItem candidate,WorkspaceItem except){for(WorkspaceItem i:items){if(i==except||i.page!=candidate.page)continue;if(candidate.x<i.x+i.w&&candidate.x+candidate.w>i.x&&candidate.y<i.y+i.h&&candidate.y+candidate.h>i.y)return true;}return false;}
 private int maxPage(){int m=0;for(WorkspaceItem i:items)m=Math.max(m,i.page);return m;}
 private boolean pageExists(int page){if(page==0)return true;for(WorkspaceItem i:items)if(i.page==page)return true;return false;}
 private void goPage(int page){if(page<0||!pageExists(page))return;currentPage=page;refresh();}
 private void compactPages(){TreeSet<Integer> used=new TreeSet<>();for(WorkspaceItem i:items)used.add(i.page);if(used.isEmpty()){currentPage=0;return;}HashMap<Integer,Integer> map=new HashMap<>();int n=0;for(int old:used)map.put(old,n++);Integer mapped=map.get(currentPage);for(WorkspaceItem i:items)i.page=map.get(i.page);currentPage=mapped==null?Math.min(currentPage,n-1):mapped;}
 private void removeItem(WorkspaceItem item){items.remove(item);if(item.widgetId>=0)widgets.deleteId(item.widgetId);compactPages();listener.changed();refresh();}
 private void drawItems(){int W=getWidth(),H=getHeight();if(W<=0||H<=0)return;ArrayList<WorkspaceItem> stale=new ArrayList<>();for(WorkspaceItem item:new ArrayList<>(items)){if(item.page!=currentPage)continue;if(item.widgetId>=0){AppWidgetHostView host=widgets.createView(getContext(),item.widgetId);if(host==null){stale.add(item);continue;}WidgetFrame frame=new WidgetFrame(item,host);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(Math.max(1,Math.round(item.w*W)),Math.max(1,Math.round(item.h*H)));p.leftMargin=(int)(item.x*W);p.topMargin=(int)(item.y*H);addView(frame,p);if(item==editingItem){activeWidget=frame;frame.showEditing(true);}frame.post(frame::notifySize);continue;}LinearLayout tile=new LinearLayout(getContext());tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);tile.setBackgroundColor(Color.TRANSPARENT);ImageView icon=new ImageView(getContext());try{icon.setImageDrawable(getContext().getPackageManager().getActivityIcon(item.componentName()));}catch(Exception e){icon.setImageResource(android.R.drawable.sym_def_app_icon);}tile.addView(icon,new LinearLayout.LayoutParams(-1,0,1));TextView label=new TextView(getContext());label.setText(item.label);label.setTextColor(Color.WHITE);label.setTextSize(13);label.setGravity(Gravity.CENTER);label.setMaxLines(2);tile.addView(label,new LinearLayout.LayoutParams(-1,dp(38)));tile.setContentDescription(item.label);FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(Math.max(dp(64),(int)(item.w*W)),Math.max(dp(84),(int)(item.h*H)));p.leftMargin=(int)(item.x*W);p.topMargin=(int)(item.y*H);tile.setOnTouchListener(new ItemTouch(item,tile));addView(tile,p);}if(activeWidget!=null){activeWidget.bringToFront();showWidgetTools();}else editingItem=null;if(!stale.isEmpty()){for(WorkspaceItem i:stale){items.remove(i);widgets.deleteId(i.widgetId);}compactPages();listener.changed();post(this::refresh);}}
 private final class ItemTouch implements OnTouchListener{final WorkspaceItem item;final View view;float downX,downY,startX,startY;boolean dragging,moved;Runnable hold;ItemTouch(WorkspaceItem i,View v){item=i;view=v;}
  public boolean onTouch(View v,MotionEvent e){switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:finishWidgetEdit();downX=e.getRawX();downY=e.getRawY();startX=item.x;startY=item.y;moved=false;dragging=false;hold=()->{dragging=true;v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);};handler.postDelayed(hold,ViewConfiguration.getLongPressTimeout());return true;case MotionEvent.ACTION_MOVE:float dx=e.getRawX()-downX,dy=e.getRawY()-downY;if(Math.abs(dx)>slop||Math.abs(dy)>slop){moved=true;if(!dragging){handler.removeCallbacks(hold);return true;}v.setTranslationX(dx);v.setTranslationY(dy);}return true;case MotionEvent.ACTION_UP:handler.removeCallbacks(hold);if(dragging){if(e.getRawY()<removeBand){removeItem(item);return true;}float nx=startX+(e.getRawX()-downX)/Math.max(1f,getWidth());float ny=startY+(e.getRawY()-downY)/Math.max(1f,getHeight());WorkspacePlacement.Bounds b=WorkspacePlacement.snapApp(item,nx,ny,dp(20)/(float)Math.max(1,getWidth()),dp(20)/(float)Math.max(1,getHeight()),items);if(b!=null){b.apply(item);listener.changed();}else blocked();refresh();return true;}if(!moved)listener.launch(item);return true;case MotionEvent.ACTION_CANCEL:handler.removeCallbacks(hold);view.setTranslationX(0);view.setTranslationY(0);return true;default:return true;}}
 }
 private final class WidgetFrame extends FrameLayout {
  final WorkspaceItem item;final AppWidgetHostView host;final TextView grip;
  float downRawX,downRawY,startX,startY,startW,startH;boolean editingGesture,resizing,moved,invalid;
  Runnable hold;WorkspacePlacement.Bounds preview;
  WidgetFrame(WorkspaceItem item,AppWidgetHostView host){
   super(WorkspaceView.this.getContext());this.item=item;this.host=host;setBackgroundColor(Color.BLACK);
   addView(host,new FrameLayout.LayoutParams(-1,-1));
   grip=new TextView(getContext());grip.setText("\u2198");grip.setContentDescription("Resize widget");grip.setTextColor(Color.BLACK);grip.setTextSize(24);grip.setGravity(Gravity.CENTER);grip.setBackgroundColor(EDIT_COLOUR);grip.setVisibility(GONE);
   addView(grip,new FrameLayout.LayoutParams(dp(56),dp(56),Gravity.END|Gravity.BOTTOM));
  }
  void showEditing(boolean enabled){
   grip.setVisibility(enabled?VISIBLE:GONE);
   if(enabled){GradientDrawable border=new GradientDrawable();border.setColor(Color.TRANSPARENT);border.setStroke(dp(2),EDIT_COLOUR);setForeground(border);}else{setForeground(null);cancelHold();}
  }
  void cancelHold(){if(hold!=null){handler.removeCallbacks(hold);hold=null;}}
  @Override protected void onDetachedFromWindow(){cancelHold();super.onDetachedFromWindow();}
  @Override public boolean onInterceptTouchEvent(MotionEvent e){
   switch(e.getActionMasked()){
    case MotionEvent.ACTION_DOWN:
     cancelHold();downRawX=e.getRawX();downRawY=e.getRawY();startX=item.x;startY=item.y;startW=getWidth();startH=getHeight();moved=false;invalid=false;preview=null;
     editingGesture=editingItem==item;resizing=editingGesture&&e.getX()>getWidth()-dp(72)&&e.getY()>getHeight()-dp(72);
     if(editingGesture)return true;
     hold=()->{editingGesture=true;beginWidgetEdit(this);performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);};
     handler.postDelayed(hold,ViewConfiguration.getLongPressTimeout());return false;
    case MotionEvent.ACTION_MOVE:
     if(editingGesture)return true;
     if(Math.abs(e.getRawX()-downRawX)>slop||Math.abs(e.getRawY()-downRawY)>slop)cancelHold();return false;
    case MotionEvent.ACTION_POINTER_DOWN:cancelHold();return editingGesture;
    case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:cancelHold();return editingGesture;
    default:return editingGesture;
   }
  }
  @Override public boolean onTouchEvent(MotionEvent e){
   if(e.getActionMasked()==MotionEvent.ACTION_DOWN)return true;
   if(!editingGesture){
    int action=e.getActionMasked();
    if(action==MotionEvent.ACTION_UP||action==MotionEvent.ACTION_CANCEL||action==MotionEvent.ACTION_POINTER_DOWN
      ||(action==MotionEvent.ACTION_MOVE&&(Math.abs(e.getRawX()-downRawX)>slop||Math.abs(e.getRawY()-downRawY)>slop)))cancelHold();
    return true;
   }
   switch(e.getActionMasked()){
    case MotionEvent.ACTION_MOVE:updatePreview(e);return true;
    case MotionEvent.ACTION_UP:
     cancelHold();if(moved){updatePreview(e);if(preview!=null){preview.apply(item);listener.changed();}if(invalid)blocked();refresh();}return true;
    case MotionEvent.ACTION_CANCEL:cancelHold();refresh();return true;
    default:return true;
   }
  }
  private void updatePreview(MotionEvent e){
   float dx=e.getRawX()-downRawX,dy=e.getRawY()-downRawY;
   if(!moved&&Math.abs(dx)<=slop&&Math.abs(dy)<=slop)return;moved=true;
   float W=Math.max(1,WorkspaceView.this.getWidth()),H=Math.max(1,WorkspaceView.this.getHeight());
   WorkspacePlacement.Bounds next;
   if(resizing){
    android.appwidget.AppWidgetProviderInfo info=host.getAppWidgetInfo();
    float minW=info!=null&&info.minResizeWidth>0?info.minResizeWidth:dp(120);
    float minH=info!=null&&info.minResizeHeight>0?info.minResizeHeight:dp(96);
    next=WorkspacePlacement.resize(item,(startW+dx)/W,(startH+dy)/H,Math.max(dp(48),minW)/W,Math.max(dp(48),minH)/H,items);
   }else next=WorkspacePlacement.move(item,startX+dx/W,startY+dy/H,items);
   invalid=next==null;
   if(next==null)return;
   preview=next;FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)getLayoutParams();
   lp.leftMargin=Math.round(next.x*W);lp.topMargin=Math.round(next.y*H);lp.width=Math.max(1,Math.round(next.w*W));lp.height=Math.max(1,Math.round(next.h*H));setLayoutParams(lp);
  }
  void notifySize(){int wdp=Math.max(1,Math.round(getWidth()/getResources().getDisplayMetrics().density));int hdp=Math.max(1,Math.round(getHeight()/getResources().getDisplayMetrics().density));try{host.updateAppWidgetSize(null,wdp,hdp,wdp,hdp);}catch(Exception ignored){}}
 }
 private final class EmptyTouch implements OnTouchListener{float x,y;boolean moved,drawerGesture,pageGesture,wallGesture,dismissedEdit;int pageDirection;Runnable hold;public boolean onTouch(View v,MotionEvent e){switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:dismissedEdit=finishWidgetEdit();if(dismissedEdit)return true;x=e.getX();y=e.getY();moved=false;drawerGesture=false;pageGesture=false;wallGesture=false;pageDirection=0;hold=()->{if(!moved){v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);listener.emptyLongPress(x,y);}};handler.postDelayed(hold,ViewConfiguration.getLongPressTimeout());return true;case MotionEvent.ACTION_MOVE:if(dismissedEdit)return true;float dx=e.getX()-x,dy=e.getY()-y;if(Math.abs(dx)>slop||Math.abs(dy)>slop){moved=true;handler.removeCallbacks(hold);if(dy<-drawerThreshold&&Math.abs(dy)>Math.abs(dx)){drawerGesture=true;return true;}if(Math.abs(dx)>pageThreshold&&Math.abs(dx)>Math.abs(dy)){if(dx>0&&currentPage==0){wallGesture=true;pageGesture=false;}else{pageGesture=true;pageDirection=dx<0?1:-1;}return true;}}return true;case MotionEvent.ACTION_UP:if(dismissedEdit)return true;handler.removeCallbacks(hold);if(drawerGesture){listener.openDrawer();return true;}if(wallGesture){listener.openWall();return true;}if(pageGesture){goPage(currentPage+pageDirection);return true;}return true;case MotionEvent.ACTION_CANCEL:if(hold!=null)handler.removeCallbacks(hold);return true;default:return true;}}}
}
