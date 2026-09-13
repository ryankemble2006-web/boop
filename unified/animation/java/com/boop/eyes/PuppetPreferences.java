package com.boop.eyes;
import android.content.Context;
import android.content.SharedPreferences;
/** Same saved appearance values for all renderers and voice backends. */
public final class PuppetPreferences {
    private PuppetPreferences(){}
    public static SharedPreferences prefs(Context c,String name){return c.getSharedPreferences(name,Context.MODE_PRIVATE);}
    public static double speed(Context c){return bounded(prefs(c,"boop_puppet").getFloat("animation_speed",1f),0,2,1);}
    public static int hue(Context c){return Math.max(0,Math.min(360,prefs(c,"boop_eyes").getInt("hue_degrees",190)));}
    public static float pitch(Context c){return bounded(prefs(c,"boop_voice").getFloat("pitch",1f),.5f,2f,1f);}
    public static float rate(Context c){return bounded(prefs(c,"boop_voice").getFloat("speech_rate",1f),.5f,2f,1f);}
    public static boolean syncEnabled(Context c){return prefs(c,"boop_puppet").getBoolean("sync_enabled",true);}
    public static void speed(Context c,float v){prefs(c,"boop_puppet").edit().putFloat("animation_speed",bounded(v,0,2,1)).apply();}
    public static void hue(Context c,int v){prefs(c,"boop_eyes").edit().putInt("hue_degrees",Math.max(0,Math.min(360,v))).apply();}
    public static void pitch(Context c,float v){prefs(c,"boop_voice").edit().putFloat("pitch",bounded(v,.5f,2f,1f)).apply();}
    public static void rate(Context c,float v){prefs(c,"boop_voice").edit().putFloat("speech_rate",bounded(v,.5f,2f,1f)).apply();}
    public static void syncEnabled(Context c,boolean v){prefs(c,"boop_puppet").edit().putBoolean("sync_enabled",v).apply();}
    public static Runnable watch(Context c,Runnable update){
        SharedPreferences[] stores={prefs(c,"boop_puppet"),prefs(c,"boop_eyes"),prefs(c,"boop_voice")};
        SharedPreferences.OnSharedPreferenceChangeListener listener=(s,k)->update.run();
        for(SharedPreferences store:stores)store.registerOnSharedPreferenceChangeListener(listener);
        return ()->{for(SharedPreferences store:stores)store.unregisterOnSharedPreferenceChangeListener(listener);};
    }
    private static float bounded(float v,float lo,float hi,float fallback){return Float.isFinite(v)?Math.max(lo,Math.min(hi,v)):fallback;}
}
