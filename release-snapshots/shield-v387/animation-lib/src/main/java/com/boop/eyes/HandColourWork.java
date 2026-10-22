package com.boop.eyes;
import java.util.concurrent.Executor;
import java.util.function.IntFunction;
import java.util.function.Consumer;
/** One coalesced worker; only the current attached request may publish. */
public final class HandColourWork<T> {
 private final T originals;
 private final Executor worker,ui;
 private final IntFunction<T> render;
 private final Consumer<T> apply;
 private boolean active,running;
 private long revision;
 private int latest;
 public HandColourWork(T originals,Executor worker,Executor ui,IntFunction<T> render,Consumer<T> apply){
  this.originals=originals;this.worker=worker;this.ui=ui;this.render=render;this.apply=apply;
 }
 public synchronized void start(int hue){active=true;request(hue);}
 public synchronized void stop(){active=false;revision++;}
 // Called on the UI executor by the view and preference listener.
 public synchronized void request(int hue){
  if(hue<0||hue>359)throw new IllegalArgumentException("Hand hue");
  if(!active)return;
  latest=hue;revision++;
  if(hue==0){apply.accept(originals);return;}
  if(!running){running=true;worker.execute(this::drain);}
 }
 private void drain(){
  while(true){
   final int hue;final long ticket;
   synchronized(this){
    if(!active||latest==0){running=false;return;}
    hue=latest;ticket=revision;
   }
   final T result;
   try{result=render.apply(hue);}
   catch(RuntimeException failure){
    synchronized(this){running=false;}
    return; // Keep the last complete artwork if decoding is unavailable.
   }
   ui.execute(()->{
    synchronized(this){if(active&&revision==ticket)apply.accept(result);}
   });
   synchronized(this){if(revision==ticket){running=false;return;}}
  }
 }
}
