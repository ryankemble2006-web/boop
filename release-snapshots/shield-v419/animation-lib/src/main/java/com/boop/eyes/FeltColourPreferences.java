package com.boop.eyes;
import android.content.Context;
/** Independent cloth colour; opening settings never writes or enables sharing. */
public final class FeltColourPreferences {
 public static final String KEY="felt_colour";
 private FeltColourPreferences(){}
 public static int load(Context context){
  try{return FeltPalette.clamp(context.getSharedPreferences("boop_eyes",Context.MODE_PRIVATE).getInt(KEY,0));}
  catch(ClassCastException malformed){return 0;}
 }
 public static void save(Context context,int value){
  context.getSharedPreferences("boop_eyes",Context.MODE_PRIVATE).edit().putInt(KEY,FeltPalette.clamp(value)).apply();
 }
}
