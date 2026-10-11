package com.boop.launcher;

import android.content.*;
import org.json.*;
import java.util.*;

public final class WorkspaceStore {
 private static final String PREF="alpha2_workspace",KEY="items";
 private final Context context;
 public WorkspaceStore(Context c){context=c.getApplicationContext();}
 public ArrayList<WorkspaceItem> load(){
  ArrayList<WorkspaceItem> out=new ArrayList<>(); String raw=context.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(KEY,"[]");
  try{JSONArray a=new JSONArray(raw);for(int n=0;n<a.length();n++){JSONObject o=a.getJSONObject(n);WorkspaceItem i=new WorkspaceItem();i.component=o.optString("component","");i.label=o.optString("label","");i.x=(float)o.optDouble("x",.08);i.y=(float)o.optDouble("y",.08);i.w=(float)o.optDouble("w",.22);i.h=(float)o.optDouble("h",.13);i.page=o.optInt("page",0);i.widgetId=o.optInt("widgetId",-1);out.add(i);}}catch(JSONException ignored){}
  return out;
 }
 public void save(List<WorkspaceItem> items){
  JSONArray a=new JSONArray();for(WorkspaceItem i:items){JSONObject o=new JSONObject();try{o.put("component",i.component);o.put("label",i.label);o.put("x",i.x);o.put("y",i.y);o.put("w",i.w);o.put("h",i.h);o.put("page",i.page);o.put("widgetId",i.widgetId);a.put(o);}catch(JSONException ignored){}}
  context.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(KEY,a.toString()).apply();
 }
 public void clearLegacy(){context.getSharedPreferences("home",Context.MODE_PRIVATE).edit().clear().apply();}
}
