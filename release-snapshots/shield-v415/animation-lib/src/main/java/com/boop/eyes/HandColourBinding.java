package com.boop.eyes;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
/** Applies a complete colour bundle outside drawing; no geometry or source pixel edits. */
final class HandColourBinding implements View.OnAttachStateChangeListener {
 private static final Executor WORKER=Executors.newSingleThreadExecutor(r->{
  Thread t=new Thread(r,"boop-hand-colour");t.setDaemon(true);return t;
 });
 private final View view;
 private final Bitmap[] originals;
 private final int signIndex;
 private final SharedPreferences prefs;
 private final HandColourWork<Bitmap[]> work;
 private final SharedPreferences.OnSharedPreferenceChangeListener listener;
 HandColourBinding(View view,Bitmap[] originals,int signIndex,Consumer<Bitmap[]> apply){
  this.view=view;this.originals=originals.clone();this.signIndex=signIndex;
  prefs=view.getContext().getSharedPreferences("boop_eyes",0);
  Handler main=new Handler(Looper.getMainLooper());
  work=new HandColourWork<>(this.originals,WORKER,command->main.post(command),this::recolour,apply);
  listener=(store,key)->{if(key==null||HandColourPreferences.KEY.equals(key))
   work.request(HandColourPreferences.load(view.getContext()));};
  view.addOnAttachStateChangeListener(this);
  if(view.isAttachedToWindow())onViewAttachedToWindow(view);
 }
 private Bitmap[] recolour(int hue){
  Bitmap[] result=new Bitmap[originals.length];
  for(int i=0;i<originals.length;i++){
   Bitmap source=originals[i];int w=source.getWidth(),h=source.getHeight();
   int[] pixels=new int[w*h];source.getPixels(pixels,0,w,0,0,w,h);
   for(int n=0;n<pixels.length;n++)pixels[n]=i==signIndex
    ?HandColourPixels.applySign(pixels[n],hue,n%w,w):HandColourPixels.apply(pixels[n],hue);
   result[i]=Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);
  }
  return result;
 }
 @Override public void onViewAttachedToWindow(View attached){
  prefs.registerOnSharedPreferenceChangeListener(listener);
  work.start(HandColourPreferences.load(view.getContext()));
 }
 @Override public void onViewDetachedFromWindow(View detached){
  prefs.unregisterOnSharedPreferenceChangeListener(listener);work.stop();
 }
}
