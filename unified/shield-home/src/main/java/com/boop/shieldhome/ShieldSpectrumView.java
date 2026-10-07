package com.boop.shieldhome;
import android.content.*;import android.graphics.*;import android.os.SystemClock;import android.view.*;import android.widget.*;
/** Home spectrum: user accent for bars, white peak hold markers, live PCM only. */
public final class ShieldSpectrumView extends FrameLayout {
 private boolean playing,attached;private final Bars bars;private final Button enable;
 public ShieldSpectrumView(Context context){super(context);setFocusable(false);setClipChildren(true);setContentDescription("Music spectrum settings");setOnClickListener(item->context.startActivity(new Intent(context,BassCaptureActivity.class)));bars=new Bars(context);addView(bars,new LayoutParams(-1,-1));enable=new Button(context);enable.setText("Enable spectrum");enable.setTextSize(14);enable.setTextColor(Color.WHITE);enable.setOnClickListener(v->context.startActivity(new Intent(context,BassCaptureActivity.class)));LayoutParams p=new LayoutParams(-2,-2,Gravity.CENTER);addView(enable,p);}
 public void setSnapshot(NowPlayingSnapshot s){playing=s!=null&&s.isPlaying();bars.invalidate();}
 public boolean focusControl(){if(enable.getVisibility()==VISIBLE){enable.setFocusable(true);return enable.requestFocus();}return requestFocus();}
 public void setLeftNavigation(View target){View.OnKeyListener left=(v,key,event)->event.getAction()==KeyEvent.ACTION_DOWN&&key==KeyEvent.KEYCODE_DPAD_LEFT&&target.requestFocus();enable.setOnKeyListener(left);setOnKeyListener(left);}
 private final Runnable tick=new Runnable(){public void run(){if(!attached)return;enable.setVisibility(SpectrumState.running?GONE:VISIBLE);setFocusable(SpectrumState.running);setClickable(SpectrumState.running);bars.invalidate();postDelayed(this,33);}};
 @Override protected void onAttachedToWindow(){super.onAttachedToWindow();attached=true;post(tick);}
 @Override protected void onDetachedFromWindow(){attached=false;removeCallbacks(tick);super.onDetachedFromWindow();}
 private final class Bars extends View {
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final float[] shown=new float[PcmSpectrum.BANDS],peak=new float[PcmSpectrum.BANDS];private final long[] hold=new long[PcmSpectrum.BANDS];private long previous;
  Bars(Context c){super(c);setFocusable(false);setClickable(false);}
  @Override protected void onDraw(Canvas canvas){long now=SystemClock.uptimeMillis();float dt=previous==0?0.033f:Math.min(0.1f,(now-previous)/1000f);previous=now;float[] levels=playing?SpectrumState.levels(now):new float[PcmSpectrum.BANDS];float density=getResources().getDisplayMetrics().density,base=getHeight(),available=Math.max(0,getHeight()-6*density),step=getWidth()/(float)PcmSpectrum.BANDS;
   for(int i=0;i<shown.length;i++){shown[i]=levels[i]>=shown[i]?levels[i]:Math.max(levels[i],shown[i]-dt*2);if(shown[i]>peak[i]){peak[i]=shown[i];hold[i]=now+450;}else if(now>hold[i])peak[i]=Math.max(shown[i],peak[i]-dt*0.65f);float x=i*step,w=step*0.72f;paint.setColor(FocusChrome.accentColor(getContext()));if(shown[i]>0)canvas.drawRect(x,base-shown[i]*available,x+w,base,paint);paint.setColor(Color.WHITE);if(peak[i]>0)canvas.drawRect(x,base-peak[i]*available-3*density,x+w,base-peak[i]*available-2*density,paint);}
  }
 }
}
