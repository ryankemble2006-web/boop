package com.boop.shieldhome;
import android.content.*;import android.graphics.*;import android.os.SystemClock;import android.view.*;import android.widget.*;
/** Shared mirrored spectrum with a gently flowing palette relative to the Home accent. */
public final class ShieldSpectrumView extends FrameLayout {
 private boolean playing,attached,active=true;private final Bars bars;private final Button enable;
 public ShieldSpectrumView(Context context){super(context);setFocusable(false);setClipChildren(true);setContentDescription("Music spectrum settings");setOnClickListener(item->context.startActivity(new Intent(context,BassCaptureActivity.class)));bars=new Bars(context);addView(bars,new LayoutParams(-1,-1));enable=new Button(context);enable.setText("Enable spectrum");enable.setTextSize(14);enable.setTextColor(Color.WHITE);enable.setAllCaps(false);BoopTvChrome.useTextOnlyFocus(enable);enable.setBackground(null);enable.setCompoundDrawables(PrecisionIcons.drawable(context,R.drawable.boop_icon_audio_lines,26),null,null,null);enable.setCompoundDrawablePadding(PrecisionIcons.dp(context,8));enable.setOnFocusChangeListener((v,focused)->v.setBackground(null));enable.setOnClickListener(v->context.startActivity(new Intent(context,BassCaptureActivity.class)));LayoutParams p=new LayoutParams(-2,-2,Gravity.CENTER);addView(enable,p);}
 public void setSnapshot(NowPlayingSnapshot s){playing=s!=null&&s.isPlaying();bars.invalidate();}
 public void setActive(boolean value){active=value;removeCallbacks(tick);if(attached&&active)post(tick);bars.invalidate();}
 public boolean focusControl(){if(enable.getVisibility()==VISIBLE){enable.setFocusable(true);return enable.requestFocus();}return requestFocus();}
 public void setLeftNavigation(View target){View.OnKeyListener left=(v,key,event)->event.getAction()==KeyEvent.ACTION_DOWN&&key==KeyEvent.KEYCODE_DPAD_LEFT&&target.requestFocus();enable.setOnKeyListener(left);setOnKeyListener(left);}
 private final Runnable tick=new Runnable(){public void run(){if(!attached||!active)return;boolean running=SpectrumState.running;enable.setVisibility(running?GONE:VISIBLE);setFocusable(running);setClickable(running);if(active&&isShown()&&getWindowVisibility()==VISIBLE){bars.invalidate();if(running){postOnAnimation(this);return;}}postDelayed(this,100);}};
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;if(active)post(tick);}
 @Override protected void onDetachedFromWindow(){attached=false;removeCallbacks(tick);super.onDetachedFromWindow();}
 private final class Bars extends View {
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
  private final float[] levels=new float[PcmSpectrum.BANDS],shown=new float[PcmSpectrum.BANDS],hsv=new float[3];
  private long previous;
  Bars(Context c){super(c);setFocusable(false);setClickable(false);}
  @Override protected void onDraw(Canvas canvas){
   long now=SystemClock.uptimeMillis();float dt=previous==0?0.016f:Math.min(0.1f,(now-previous)/1000f);previous=now;
   boolean fresh=active&&playing&&SpectrumState.copyLevels(now,levels)>=0;
   if(!fresh){java.util.Arrays.fill(levels,0);java.util.Arrays.fill(shown,0);return;}
   float density=getResources().getDisplayMetrics().density,centre=getHeight()/2f,gap=density;
   float step=getWidth()/(PcmSpectrum.BANDS-0.28f);
   Color.colorToHSV(FocusChrome.accentColor(getContext()),hsv);
   float accentHue=hsv[0],saturation=hsv[1],brightness=hsv[2];
   float retained=(float)Math.exp(-dt*Math.log(10)/0.050);
   for(int i=0;i<levels.length;i++){
    float level=levels[i]+(shown[i]-levels[i])*retained;shown[i]=level;
    float half=SpectrumPresentation.halfHeight(getHeight(),4*density,level);
    if(half<=gap)continue;
    float position=i/(float)(levels.length-1),x=i*step,w=step*.72f;
    float palettePosition=1f-position;
    hsv[0]=SpectrumPresentation.hue(accentHue,palettePosition,now/1000f);
    hsv[1]=saturation*(1f-.28f*palettePosition);
    hsv[2]=SpectrumPresentation.brightness(brightness,position);
    paint.setColor(Color.HSVToColor(hsv));
    canvas.drawRect(x,centre-half,x+w,centre-gap,paint);
    canvas.drawRect(x,centre+gap,x+w,centre+half,paint);
   }
  }
 }
}
