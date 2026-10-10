package local.boop.precision;
import android.app.*;import android.content.*;import android.content.res.*;import android.os.*;import android.view.*;import android.widget.*;import java.lang.reflect.*;import java.util.*;
public final class PrecisionLayoutProbe extends Activity {
 String result="not run";int checks;
 void check(boolean pass,String message){checks++;if(!pass)throw new AssertionError(message);}
 Object make(Class<?> c,Class<?>[] types,Object...args)throws Exception{Constructor<?> ctor=c.getDeclaredConstructor(types);ctor.setAccessible(true);return ctor.newInstance(args);}
 Method method(Class<?> c,String n,Class<?>... types)throws Exception{Method m=c.getDeclaredMethod(n,types);m.setAccessible(true);return m;}
 Context isolated;ClassLoader loader;
 Class<?> type(String name)throws Exception{return loader.loadClass("com.boop.shieldhome."+name);}
 public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894);try{
  Context installed=createPackageContext("com.boop.shieldoverlay",CONTEXT_INCLUDE_CODE|CONTEXT_IGNORE_SECURITY);
  Configuration cfg=new Configuration(installed.getResources().getConfiguration());cfg.fontScale=1.3f;cfg.densityDpi=256;
  Context layout=installed.createConfigurationContext(cfg);loader=installed.getClassLoader();
  isolated=new ContextWrapper(this){public Resources getResources(){return layout.getResources();}public AssetManager getAssets(){return layout.getAssets();}public ClassLoader getClassLoader(){return loader;}};
  Object store=make(type("ShieldHomeStore"),new Class[]{Context.class},isolated);method(type("ShieldHomeStore"),"setAccentHue",int.class).invoke(store,20);
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(30,15,30,15);root.setBackgroundColor(0xff000000);setContentView(root);
  Object home=make(type("ShieldHomeView"),new Class[]{Context.class},isolated);Class<?> callbacks=type("ShieldHomeView$Callbacks");Object fake=Proxy.newProxyInstance(loader,new Class[]{callbacks},(p,m,a)->null);
  LinearLayout nav=(LinearLayout)method(type("ShieldHomeView"),"navRow",callbacks).invoke(home,fake);root.addView(nav);method(type("BoopTvChrome"),"install",Activity.class).invoke(null,this);
  for(int i=0;i<nav.getChildCount();i++)if(nav.getChildAt(i) instanceof TextView){TextView v=(TextView)nav.getChildAt(i);check(v.getBackground()==null,"top button background");check(v.length()>0,"missing top caption");check(v.getCompoundDrawables()[1]!=null,"missing top vector");}
  Class<?> weather=type("ShieldWeatherView"),snapshot=type("WeatherSnapshot"),hour=type("WeatherSnapshot$Hour"),day=type("WeatherSnapshot$Day");
  LinearLayout widget=(LinearLayout)make(weather,new Class[]{Context.class},isolated);
  int[] codes={0,1,2,3,45,48,51,53,55,56,57,61,63,65,66,67,71,73,75,77,80,81,82,85,86,95,96,99,-1,999};
  Method bind=method(weather,"bind",snapshot,long.class),map=method(type("WeatherCode"),"icon",int.class);
  List<Object> hours=new ArrayList<>(),days=new ArrayList<>();
  for(int i=0;i<4;i++)hours.add(make(hour,new Class[]{String.class,int.class,double.class,int.class},"2026-10-08T"+(20+i)+":00",codes[i+4],13.0,63));
  for(int i=0;i<3;i++)days.add(make(day,new Class[]{String.class,int.class,double.class,double.class,int.class},String.format("2026-10-%02d",8+i),codes[i+19],15.0,8.0,85));
  for(int code:codes){
   int resource=(Integer)map.invoke(null,code);check(isolated.getDrawable(resource)!=null,"missing condition "+code);
   Object s=make(snapshot,new Class[]{String.class,String.class,int.class,double.class,double.class,double.class,int.class,String.class,String.class,long.class,List.class,List.class},"Cardiff","2026-10-08T20:00",code,13.0,11.0,10.0,270,"2026-10-08T07:24","2026-10-08T18:34",System.currentTimeMillis(),hours,days);
   bind.invoke(widget,s,System.currentTimeMillis());widget.measure(View.MeasureSpec.makeMeasureSpec(1800,1073741824),View.MeasureSpec.makeMeasureSpec(300,1073741824));widget.layout(0,0,1800,300);inspect(widget);
  }
  ViewGroup weatherTop=(ViewGroup)widget.getChildAt(0);
  View currentGroup=weatherTop.getChildAt(0),hoursGroup=weatherTop.getChildAt(1),daysGroup=weatherTop.getChildAt(2);
  check(currentGroup.getLeft()+currentGroup.getWidth()/2==Math.round(115*isolated.getResources().getDisplayMetrics().density),"Cardiff group centered over Kodi");
  check(Math.abs(hoursGroup.getLeft()+hoursGroup.getWidth()/2.0-widget.getWidth()/2.0)<=1,"hours exactly screen centered");
  TextView lastTemp=findText(daysGroup,"15C / 8C",true);check(lastTemp!=null,"daily uses C units");
  android.graphics.Rect tempInk=visibleInk(lastTemp);
  check(Math.abs(relativeLeft(lastTemp,widget)+tempInk.right-widget.getWidth())<=1,"final C visible pixel matches right widget edge");
  check(findText(widget,"13C",false)!=null,"current/hour C units");
  check(widget.getBackground()==null,"weather background removed");check(((ViewGroup)widget.getChildAt(0)).getChildCount()==3,"weather has three columns without dividers");
  method(type("ShieldHomeStore"),"setAccentHue",int.class).invoke(store,220);
  inspect(widget);
  method(type("ShieldHomeStore"),"setAccentHue",int.class).invoke(store,20);
  root.addView(widget,new LinearLayout.LayoutParams(-1,364));
  Object player=make(type("ShieldNowPlayingView"),new Class[]{Context.class},isolated);View pv=(View)player;pv.setVisibility(View.VISIBLE);root.addView(pv,new LinearLayout.LayoutParams(-1,364));
  Object track=make(type("NowPlayingSnapshot"),new Class[]{long.class,String.class,String.class,String.class,int.class,long.class,long.class,long.class,float.class,long.class,android.graphics.Bitmap.class},1L,"deezer.android.app","Heart Of Gold (Hixxy Remix) - Extended Clubland Classics Edition","Above & Beyond",2,Long.MAX_VALUE,2000L,180000L,1f,SystemClock.elapsedRealtime(),null);
  method(type("ShieldNowPlayingView"),"bind",type("NowPlayingSnapshot"),callbacks).invoke(player,track,fake);
  for(String name:new String[]{"previousButton","playPauseButton","nextButton","lyricsButton","sourceButton","queueButton"}){
   Field f=type("ShieldNowPlayingView").getDeclaredField(name);f.setAccessible(true);View v=(View)f.get(player);
   check(!(v instanceof TextView),"visible media text "+name);check(v.getClass().getName().endsWith("PrecisionIcons$Control"),"missing media vector "+name);check(v.getBackground()==null,"media background "+name);check(v.getContentDescription()!=null,"missing accessibility "+name);
   v.post(()->android.util.Log.i("PrecisionProbe",name+" skip="+v.willNotDraw()+" alpha="+v.getAlpha()+" shown="+v.isShown()+" size="+v.getWidth()+"x"+v.getHeight()));
   v.setWillNotDraw(false);
  }
  Field cf=type("ShieldNowPlayingView").getDeclaredField("controls");cf.setAccessible(true);LinearLayout row=(LinearLayout)cf.get(player);
  check(row.getChildAt(0).getClass().getSimpleName().equals("DeezerFavouriteButton"),"positive first");
  check(row.getChildAt(1).getClass().getSimpleName().equals("DeezerFavouriteButton"),"negative second");
  check("Prev".contentEquals(row.getChildAt(2).getContentDescription()),"negative before previous");
  pv.measure(View.MeasureSpec.makeMeasureSpec(1800,1073741824),View.MeasureSpec.makeMeasureSpec(364,1073741824));pv.layout(0,0,1800,364);
  nav.measure(View.MeasureSpec.makeMeasureSpec(1800,1073741824),View.MeasureSpec.makeMeasureSpec(300,-2147483648));nav.layout(0,0,1800,nav.getMeasuredHeight());
  Method captionInk=method(nav.getClass(),"captionInk",TextView.class);int[] navgaps=new int[4];int captionEnd=0;
  for(int ni=0;ni<4;ni++){TextView action=(TextView)nav.getChildAt(ni);android.graphics.Rect ink=(android.graphics.Rect)captionInk.invoke(nav,action);int start=action.getLeft()+ink.left;
   if(ni==0)check(start==0,"Apps A aligns favourites left edge");else navgaps[ni-1]=start-captionEnd;
   captionEnd=action.getLeft()+ink.right;
  }
  View boop=nav.getChildAt(nav.getChildCount()-1);navgaps[3]=(Integer)method(nav.getClass(),"boopVisibleLeft",View.class).invoke(nav,boop)-captionEnd;
  double[] centers=new double[4];
  for(int ni=0;ni<4;ni++){TextView action=(TextView)nav.getChildAt(ni);android.graphics.Rect ink=(android.graphics.Rect)captionInk.invoke(nav,action);centers[ni]=action.getLeft()+(ink.left+ink.right)/2.0;}
  check(Math.abs(centers[2]-nav.getWidth()/2.0)<=1,"Close media screen centered");
  check(Math.abs(centers[1]-(centers[0]+centers[2])/2)<=1,"settings rebalanced between Apps and Close media");
  double eyesCenter=((Integer)method(nav.getClass(),"boopVisibleLeft",View.class).invoke(nav,boop)+nav.getWidth())/2.0;
  check(Math.abs(centers[3]-(centers[2]+eyesCenter)/2)<=1,"sync rebalanced between Close media and BOOP");
  check(boop.getRight()==nav.getWidth(),"BOOP anchored right unchanged");check(boop.getWidth()==Math.round(144*isolated.getResources().getDisplayMetrics().density),"BOOP width unchanged");
  check(pv.getBackground()==null,"now playing pure black no charcoal panel");
  Field tf=type("ShieldNowPlayingView").getDeclaredField("title");tf.setAccessible(true);TextView heading=(TextView)tf.get(player);
  check(heading.getClass().getSimpleName().equals("SerenMarqueeTextView"),"track uses stable Seren marquee");
  check(heading.isSelected(),"track marquee automatically selected");
  check(Math.abs(heading.getTextSize()-android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP,24,isolated.getResources().getDisplayMetrics()))<0.01f,"track font reduced by 2sp");
  ViewGroup stack=(ViewGroup)heading.getParent();int[] gaps=new int[4];int previousBottom=0;
  for(int n=0;n<stack.getChildCount();n++) { View element=stack.getChildAt(n);android.graphics.Rect ink=visibleInk(element);
    if(n==0)check(Math.abs(element.getTop()+ink.top)<=1,"visible title aligns album top top="+element.getTop()+" ink="+ink);
    else gaps[n-1]=element.getTop()+ink.top-previousBottom;
    previousBottom=element.getTop()+ink.bottom;
  }
  check(Math.abs(previousBottom-stack.getHeight())<=1,"visible icon bottoms align album bottom");
  for(int n=1;n<gaps.length;n++)check(Math.abs(gaps[n]-gaps[0])<=2,"visually equal gaps "+java.util.Arrays.toString(gaps));
  Field eqf=type("ShieldNowPlayingView").getDeclaredField("puppetView");eqf.setAccessible(true);View eq=(View)eqf.get(player);
  Field artf=type("ShieldNowPlayingView").getDeclaredField("artwork");artf.setAccessible(true);View art=(View)artf.get(player);
  check(relativeLeft(art,pv)==0,"album art aligns favourites outer left edge");
  check(pv.getWidth()-(relativeLeft(eq,pv)+eq.getWidth())==0,"EQ aligns favourites outer right edge");

  Field barsf=type("ShieldSpectrumView").getDeclaredField("bars");barsf.setAccessible(true);View bars=(View)barsf.get(eq);Field peaks=bars.getClass().getDeclaredField("peak");peaks.setAccessible(true);java.util.Arrays.fill((float[])peaks.get(bars),0.5f);Field holds=bars.getClass().getDeclaredField("hold");holds.setAccessible(true);java.util.Arrays.fill((long[])holds.get(bars),Long.MAX_VALUE);
  check(visibleInk(bars).right==bars.getWidth(),"last EQ bar reaches right edge");
  Field pf=type("ShieldNowPlayingView").getDeclaredField("progress");pf.setAccessible(true);View bar=(View)pf.get(player);
  Field playf=type("ShieldNowPlayingView").getDeclaredField("playPauseButton");playf.setAccessible(true);View play=(View)playf.get(player);
  check(Math.abs((play.getLeft()+play.getRight())-row.getWidth())<=2,"play centered in row");
  check(Math.abs(relativeLeft(bar,pv)+bar.getWidth()-pv.getWidth()/2.0)<=1,"progress ends on Close media center line");
  View artistArea=(View)field(player,"subtitle");check(relativeLeft(artistArea,pv)+artistArea.getWidth()==relativeLeft(bar,pv)+bar.getWidth(),"artist area ends at center");
  int albumArtistGap=relativeLeft(artistArea,pv)+visibleInk(artistArea).left-(relativeLeft(art,pv)+art.getWidth());
  int eqGap=relativeLeft(eq,pv)-(relativeLeft(bar,pv)+bar.getWidth());
  check(Math.abs(eqGap-albumArtistGap)<=1,"EQ gap matches visible album to artist gap "+eqGap+" vs "+albumArtistGap);
  check(row.getWidth()==bar.getWidth(),"row matches progress width");
  check(row.getChildAt(0).getLeft()+visibleInk(row.getChildAt(0)).left==0,"heart visible pixels align left edge");
  View flow=row.getChildAt(row.getChildCount()-1);check(flow.getLeft()+visibleInk(flow).right==row.getWidth(),"flow arrow tip pixels align right edge");
  check(row.getChildAt(1).getLeft()-row.getChildAt(0).getRight()==Math.round(6*isolated.getResources().getDisplayMetrics().density),"heart spacing tightened");
  check(row.getChildAt(3).getLeft()-row.getChildAt(2).getRight()==Math.round(6*isolated.getResources().getDisplayMetrics().density),"transport spacing tightened");
  Field af=type("ShieldNowPlayingView").getDeclaredField("artwork");af.setAccessible(true);View album=(View)af.get(player);
  album.requestFocus();album.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_RIGHT));check(row.getChildAt(0).hasFocus(),"album right to favourite");
  row.getChildAt(0).dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP));check(bar.hasFocus(),"favourite up to progress");
  bar.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_UP));Field sf=type("ShieldNowPlayingView").getDeclaredField("subtitle");sf.setAccessible(true);check(((View)sf.get(player)).hasFocus(),"progress up to artist");

  bar.requestFocus();bar.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_DOWN));check(row.getChildAt(0).hasFocus(),"progress down to favourite");
  View fav=row.getChildAt(0);View artist=(View)sf.get(player);
  art.requestFocus();check(art.getForeground()!=null,"album focused border present");check(art.getScaleX()==1f&&art.getScaleY()==1f,"album focus remains inside bounds");

  Method drawHeart=method(type("DeezerFavouriteButton"),"drawStandaloneHeart",android.graphics.Canvas.class,int.class,boolean.class,boolean.class,boolean.class,boolean.class);
  android.graphics.Bitmap hb=android.graphics.Bitmap.createBitmap(fav.getWidth(),fav.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);android.graphics.Canvas hc=new android.graphics.Canvas(hb);int orange=0xffff8855;
  artist.requestFocus();drawHeart.invoke(fav,hc,orange,true,true,false,true);check(hb.getPixel(hb.getWidth()/2,hb.getHeight()/2)==orange,"saved heart orange filled");
  hb.eraseColor(0);drawHeart.invoke(fav,hc,orange,true,false,false,true);check(android.graphics.Color.alpha(hb.getPixel(hb.getWidth()/2,hb.getHeight()/2))==0,"unsaved heart hollow");check(countColor(hb,android.graphics.Color.WHITE)>10,"unsaved heart white outline");
  fav.requestFocus();hb.eraseColor(0);drawHeart.invoke(fav,hc,orange,true,false,false,true);check(android.graphics.Color.alpha(hb.getPixel(hb.getWidth()/2,hb.getHeight()/2))==0,"focused unsaved heart hollow");check(countColor(hb,orange)>10,"focused unsaved heart orange outline");
  hb.eraseColor(0);drawHeart.invoke(fav,hc,orange,true,true,false,true);check(hb.getPixel(hb.getWidth()/2,hb.getHeight()/2)==orange,"focused saved keeps orange fill");check(countColor(hb,android.graphics.Color.WHITE)>10,"focused saved white outline");
  Class<?> pi=type("PrecisionIcons");int rid=isolated.getResources().getIdentifier("boop_icon_play","drawable","com.boop.shieldoverlay");
  android.graphics.drawable.Drawable state=(android.graphics.drawable.Drawable)method(pi,"drawable",Context.class,int.class,float.class).invoke(null,isolated,rid,34.32f);
  android.graphics.Bitmap bm=android.graphics.Bitmap.createBitmap(100,100,android.graphics.Bitmap.Config.ARGB_8888);android.graphics.Canvas cv=new android.graphics.Canvas(bm);state.setBounds(0,0,100,100);state.setState(new int[0]);state.draw(cv);check(bm.getPixel(45,50)==0,"normal play hollow");bm.eraseColor(0);state.setState(new int[]{android.R.attr.state_focused});state.draw(cv);check(android.graphics.Color.alpha(bm.getPixel(45,50))>200,"focused play filled");check(android.graphics.Color.red(bm.getPixel(45,50))>android.graphics.Color.blue(bm.getPixel(45,50)),"focused orange");
  Field timelinef=heading.getClass().getDeclaredField("progress");timelinef.setAccessible(true);Object timeline=timelinef.get(heading);Field elapsedf=type("SerenMarqueeState").getDeclaredField("elapsedMs");elapsedf.setAccessible(true);elapsedf.setLong(timeline,10000);
  method(type("ShieldNowPlayingView"),"bind",type("NowPlayingSnapshot"),callbacks).invoke(player,track,fake);check(elapsedf.getLong(timeline)==10000,"same track refresh preserves marquee position");check(heading.getText().length()>50,"full long title retained");
  android.graphics.Rect flowNormal=visibleInk(flow);check(flow.requestFocus(),"Flow highlight can focus");android.graphics.Rect flowActive=visibleInk(flow);
  check(Math.abs(flowNormal.left-flowActive.left)<=1&&Math.abs(flowNormal.right-flowActive.right)<=1&&Math.abs(flowNormal.top-flowActive.top)<=1&&Math.abs(flowNormal.bottom-flowActive.bottom)<=1,"filled Flow arrows retain original silhouette bounds");
  View queue=(View)field(player,"queueButton");queue.setVisibility(View.VISIBLE);queue.setFocusable(true);method(type("ShieldNowPlayingView"),"updateControlSpacing").invoke(player);
  for(int screenWidth:new int[]{1786,1920}) {
    pv.measure(View.MeasureSpec.makeMeasureSpec(screenWidth,1073741824),View.MeasureSpec.makeMeasureSpec(364,1073741824));pv.layout(0,0,screenWidth,364);
    check(Math.abs(relativeLeft(bar,pv)+bar.getWidth()-screenWidth/2.0)<=1,"responsive center "+screenWidth);
    check(relativeLeft(eq,pv)+eq.getWidth()==screenWidth,"responsive EQ right edge "+screenWidth);
    int lastRight=-999;
    for(int i=0;i<row.getChildCount();i++){View button=row.getChildAt(i);if(button.getVisibility()!=View.VISIBLE)continue;android.graphics.Rect ink=visibleInk(button);int left=button.getLeft()+ink.left,right=button.getLeft()+ink.right;check(left>=lastRight,"queue glyphs do not overlap at "+screenWidth+" child "+i);check(left>=0&&right<=row.getWidth(),"queue glyph remains inside bar "+i);lastRight=right;}
  }
  queue.setVisibility(View.GONE);method(type("ShieldNowPlayingView"),"updateControlSpacing").invoke(player);
  method(type("ShieldNowPlayingView"),"bind",type("NowPlayingSnapshot"),callbacks).invoke(player,null,fake);
  pv.measure(View.MeasureSpec.makeMeasureSpec(1800,1073741824),View.MeasureSpec.makeMeasureSpec(364,1073741824));pv.layout(0,0,1800,364);check(true,"closed Deezer empty player layout does not crash");
  check(((android.graphics.Rect)method(pi,"inkBounds",Context.class,int.class,float.class,int.class,int.class).invoke(null,isolated,rid,34.32f,0,0)).isEmpty(),"zero size glyph bounds safe");
  Class<?> lyricsType=type("ShieldLyricsView"),lyricsCallbacks=type("ShieldLyricsView$Controls");java.util.Map<String,Integer> lyricActions=new java.util.HashMap<>();
  Object lyricControls=Proxy.newProxyInstance(loader,new Class[]{lyricsCallbacks},(p,m,args)->{lyricActions.put(m.getName(),lyricActions.getOrDefault(m.getName(),0)+1);return null;});
  View lyricsScreen=(View)make(lyricsType,new Class[]{Context.class,int.class,lyricsCallbacks},isolated,orange,lyricControls);
  root.addView(lyricsScreen,new LinearLayout.LayoutParams(1920,1080));
  method(lyricsType,"setSnapshot",type("NowPlayingSnapshot"),boolean.class).invoke(lyricsScreen,track,true);method(lyricsType,"setQueueAvailable",boolean.class).invoke(lyricsScreen,true);
  lyricsScreen.measure(View.MeasureSpec.makeMeasureSpec(1920,1073741824),View.MeasureSpec.makeMeasureSpec(1080,1073741824));lyricsScreen.layout(0,0,1920,1080);
  Object[] lyricButtons=(Object[])field(lyricsScreen,"buttons");
  int lyricAccent=(Integer)method(type("FocusChrome"),"accentColor",Context.class).invoke(null,isolated);
  for(Object control:lyricButtons){View v=(View)control;check(v.getClass().getName().endsWith("PrecisionIcons$Control"),"lyrics transports share home Precision glyphs");check(v.getBackground()==null,"lyrics transport floats");check(v.requestFocus(),"lyrics control focuses");android.graphics.Rect ink=visibleInk(v);check(ink.left>0&&ink.top>0&&ink.right<v.getWidth()&&ink.bottom<v.getHeight(),"lyrics focused glyph contained");}
  for(String name:new String[]{"queueButton","lookupButton"}){View v=(View)field(lyricsScreen,name);check(!(v instanceof TextView),"lyrics icon replaces label "+name);check(v.getBackground()==null,"lyrics action background absent");check(v.getContentDescription()!=null,"lyrics action accessibility");check(v.requestFocus(),"lyrics action focuses");android.graphics.Rect ink=visibleInk(v);check(ink.left>=0&&ink.top>=0&&ink.right<=v.getWidth()&&ink.bottom<=v.getHeight(),"lyrics action glyph not clipped");v.performClick();}
  ((View)lyricButtons[0]).performClick();((View)lyricButtons[1]).performClick();((View)lyricButtons[2]).performClick();
  check(lyricActions.getOrDefault("previous",0)==1&&lyricActions.getOrDefault("playPause",0)==1&&lyricActions.getOrDefault("next",0)==1,"lyrics transport callbacks preserved");
  check(lyricActions.getOrDefault("queue",0)==1&&lyricActions.getOrDefault("lookup",0)==1,"lyrics queue and lookup callbacks preserved");
  for(String name:new String[]{"addHeart","removeHeart"}){Object heart=field(lyricsScreen,name);Field standalone=heart.getClass().getDeclaredField("standaloneHeart");standalone.setAccessible(true);check(standalone.getBoolean(heart),"lyrics shares standalone home heart states");}
  View lookup=(View)field(lyricsScreen,"lookupButton");lookup.requestFocus();android.graphics.Bitmap searchBitmap=android.graphics.Bitmap.createBitmap(lookup.getWidth(),lookup.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);lookup.draw(new android.graphics.Canvas(searchBitmap));check(countColor(searchBitmap,lyricAccent)>20,"focused lyrics Lookup fills accent");searchBitmap.recycle();
  View lyricPlay=(View)lyricButtons[1];check("Play".contentEquals(lyricPlay.getContentDescription()),"lyrics paused play glyph accessibility");
  method(lyricsType,"setSnapshot",type("NowPlayingSnapshot"),boolean.class).invoke(lyricsScreen,null,false);check(!lyricPlay.isFocusable(),"empty lyrics disables transport");check(((View)field(lyricsScreen,"lookupButton")).getVisibility()==View.GONE,"lookup visibility preserved");
  root.removeView(lyricsScreen);
  if(getIntent().getBooleanExtra("lyrics",false)){method(lyricsType,"setSnapshot",type("NowPlayingSnapshot"),boolean.class).invoke(lyricsScreen,track,true);method(lyricsType,"setQueueAvailable",boolean.class).invoke(lyricsScreen,true);setContentView(lyricsScreen);lyricsScreen.post(()->{try{((View)field(lyricsScreen,"lookupButton")).requestFocus();}catch(Exception e){throw new RuntimeException(e);}});}
  TextView gallery=new TextView(this);gallery.setText("Precision · all 30 condition/fallback checks · font scale 1.3");gallery.setTextColor(-1);root.addView(gallery);
  LinearLayout icons=new LinearLayout(this);icons.setGravity(Gravity.CENTER);root.addView(icons);
  for(int code:new int[]{0,2,3,45,51,61,71,80,85,95}){ImageView i=new ImageView(this);android.graphics.drawable.Drawable d=isolated.getDrawable((Integer)map.invoke(null,code)).mutate();d.setTint(0xffff8855);i.setImageDrawable(d);icons.addView(i,new LinearLayout.LayoutParams(90,90));}
  drawerChecks(root,store);
  result="PASS "+checks+" Precision controls, accessibility, all weather conditions and centred forecast cells";
 }catch(Throwable e){result="FAIL "+e;}}
 void drawerChecks(LinearLayout root,Object store)throws Exception {
  Class<?> cardType=type("TvAppCardView"),entryType=type("TvAppEntry");View card=(View)make(cardType,new Class[]{Context.class},isolated);
  Object entry=make(entryType,new Class[]{String.class,String.class,String.class},"missing/.Main","missing","Long application label that must fit safely");
  Method bind=method(cardType,"bind",entryType,boolean.class);bind.invoke(card,entry,true);
  root.addView(card,new LinearLayout.LayoutParams(304,256));card.measure(View.MeasureSpec.makeMeasureSpec(304,1073741824),View.MeasureSpec.makeMeasureSpec(256,1073741824));card.layout(0,0,304,256);
  ImageView image=(ImageView)field(card,"iconView");TextView label=(TextView)field(card,"labelView");
  check(image.getWidth()==Math.round(103*isolated.getResources().getDisplayMetrics().density),"drawer artwork is 35 percent larger");
  check(image.getBottom()+((LinearLayout.LayoutParams)image.getLayoutParams()).bottomMargin<=label.getTop(),"drawer label below artwork");
  check(label.getBottom()<=((View)label.getParent()).getHeight(),"drawer label fits content");
  check(((View)field(card,"favouriteBadge")).getVisibility()==View.VISIBLE,"drawer favourite badge retained");
  for(int hue:new int[]{20,220}){method(type("ShieldHomeStore"),"setAccentHue",int.class).invoke(store,hue);card.setSelected(true);card.animate().cancel();
   int accent=(Integer)method(type("FocusChrome"),"accentColor",Context.class).invoke(null,isolated);
   check(label.getCurrentTextColor()==accent,"drawer selected label follows accent");
   check(card.getForeground()==null,"drawer focus box belongs to artwork");check(image.getForeground()!=null,"drawer artwork focus outline present");
   check(card.getBackground() instanceof android.graphics.drawable.ColorDrawable&&((android.graphics.drawable.ColorDrawable)card.getBackground()).getColor()==0,"drawer no charcoal focus plate");
   check(card.getScaleX()==1f&&image.getScaleX()==1f,"drawer no focus scaling");
   android.graphics.drawable.Drawable outline=image.getForeground();android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(image.getWidth(),image.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);outline.setBounds(0,0,image.getWidth(),image.getHeight());outline.draw(new android.graphics.Canvas(bitmap));check(countColor(bitmap,accent)>20,"drawer outline selected accent pixels");check(bitmap.getPixel(image.getWidth()/2,image.getHeight()/2)==0,"drawer outline has transparent fill");bitmap.recycle();card.setSelected(false);check(label.getCurrentTextColor()==android.graphics.Color.WHITE,"drawer unfocused label white");check(image.getForeground()==null,"drawer loses outline off selection");
  }
  bind.invoke(card,entry,false);check(((View)field(card,"favouriteBadge")).getVisibility()==View.GONE,"recycled drawer unfavourite removes badge");
  method(cardType,"bindFavourite",entryType).invoke(card,entry);card.setSelected(true);check(image.getLayoutParams().width==Math.round(76*isolated.getResources().getDisplayMetrics().density),"Home fallback artwork size preserved");check(label.getCurrentTextColor()==android.graphics.Color.WHITE,"Home favourite label preserved");check(card.getForeground()==null&&image.getForeground()!=null,"Home artwork focus preserved");root.removeView(card);
  method(type("ShieldHomeStore"),"setAccentHue",int.class).invoke(store,20);
  LinearLayout drawer=(LinearLayout)make(type("ShieldAppsView"),new Class[]{Context.class},isolated);List<Object> apps=new ArrayList<>();for(int i=0;i<25;i++)apps.add(make(entryType,new Class[]{String.class,String.class,String.class},"missing"+i+"/.Main","missing", "App "+i));Class<?> cb=type("ShieldAppsView$Callbacks");Object fake=Proxy.newProxyInstance(loader,new Class[]{cb},(o,m,a)->null);method(type("ShieldAppsView"),"render",List.class,Set.class,cb).invoke(drawer,apps,Set.of(),fake);root.addView(drawer,new LinearLayout.LayoutParams(1920,1080));drawer.measure(View.MeasureSpec.makeMeasureSpec(1920,1073741824),View.MeasureSpec.makeMeasureSpec(1080,1073741824));drawer.layout(0,0,1920,1080);GridView grid=(GridView)drawer.getChildAt(1);check(grid.getClipChildren()&&grid.getClipToPadding(),"scrolled drawer stays inside grid viewport");check(grid.getNumColumns()==5,"drawer keeps five columns");check(grid.getAdapter().getCount()==25,"drawer all apps preserved");check(grid.getChildCount()>=15,"drawer at least three full rows");for(int i=0;i<Math.min(15,grid.getChildCount());i++){View tile=grid.getChildAt(i);ImageView artwork=(ImageView)field(tile,"iconView");TextView name=(TextView)field(tile,"labelView");check(relativeLeft(artwork,tile)>=0&&relativeLeft(artwork,tile)+artwork.getWidth()<=tile.getWidth(),"drawer artwork horizontally contained");check(name.getBottom()<=((View)name.getParent()).getHeight(),"grid enlarged artwork and label contained");}root.removeView(drawer);
 }
 TextView findText(View v,String text,boolean last){TextView found=null;if(v instanceof TextView&&text.contentEquals(((TextView)v).getText()))found=(TextView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){TextView next=findText(g.getChildAt(i),text,last);if(next!=null){found=next;if(!last)return found;}}}return found;}
 Object field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(object);}
 int relativeLeft(View v,View root){int x=0;while(v!=root){x+=v.getLeft();v=(View)v.getParent();}return x;}
 android.graphics.Rect visibleInk(View v){android.graphics.Bitmap b=android.graphics.Bitmap.createBitmap(v.getWidth(),v.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);v.draw(new android.graphics.Canvas(b));int l=b.getWidth(),t=b.getHeight(),r=0,bot=0;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(android.graphics.Color.alpha(b.getPixel(x,y))>64){l=Math.min(l,x);t=Math.min(t,y);r=Math.max(r,x+1);bot=Math.max(bot,y+1);}b.recycle();return new android.graphics.Rect(l,t,r,bot);}
 int countColor(android.graphics.Bitmap b,int color){int n=0;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(b.getPixel(x,y)==color)n++;return n;}
 void inspect(View v){if(v instanceof ImageView){android.graphics.Rect ink=visibleInk(v);check(Math.abs(ink.left+ink.right-v.getWidth())<=1,"weather horizontal ink center "+ink);check(Math.abs(ink.top+ink.bottom-v.getHeight())<=1,"weather vertical ink center "+ink);android.graphics.Bitmap b=android.graphics.Bitmap.createBitmap(v.getWidth(),v.getHeight(),android.graphics.Bitmap.Config.ARGB_8888);v.draw(new android.graphics.Canvas(b));check(countColor(b,android.graphics.Color.WHITE)==0,"weather glyph must use accent outline");try{int accent=(Integer)method(type("FocusChrome"),"accentColor",Context.class).invoke(null,isolated);check(countColor(b,accent)>0,"weather glyph follows selected accent");}catch(Exception e){throw new RuntimeException(e);}b.recycle();}check(v.getTranslationX()==0,"weather offset");if(v instanceof ImageView && v.getLayoutParams().width>0 && v.getParent() instanceof LinearLayout && ((LinearLayout)v.getParent()).getOrientation()==1){ViewGroup parent=(ViewGroup)v.getParent();check(Math.abs((v.getLeft()+v.getRight())-parent.getWidth())<=1,"forecast icon not centred");}if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++)inspect(g.getChildAt(i));}}
 public static final class ProbeInstrumentation extends Instrumentation{public void onCreate(Bundle b){super.onCreate(b);start();}public void onStart(){PrecisionLayoutProbe a=(PrecisionLayoutProbe)startActivitySync(new Intent(getTargetContext(),PrecisionLayoutProbe.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));Bundle b=new Bundle();b.putString("result",a.result);runOnMainSync(a::finish);finish(a.result.startsWith("PASS")?-1:0,b);}}
}


