package com.boop.eyes;
import android.content.Context;
import android.content.SharedPreferences;
/** Independent local hand hue; zero restores the approved original yellow pixels. */
public final class HandColourPreferences {
 public static final String KEY="hand_colour";
 private HandColourPreferences(){}
 public static int load(Context context){
  SharedPreferences prefs=context.getSharedPreferences("boop_eyes",Context.MODE_PRIVATE);
  int value=prefs.getInt(KEY,0);
  return value>=0&&value<=359?value:0;
 }
 public static void save(Context context,int hue){
  if(hue<0||hue>359)throw new IllegalArgumentException("Hand hue");
  context.getSharedPreferences("boop_eyes",Context.MODE_PRIVATE).edit().putInt(KEY,hue).apply();
 }
}
