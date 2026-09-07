package com.boop.launcher;

import android.content.*;
import android.content.pm.*;
import java.util.*;

public final class AppRepository {
 public List<AppEntry> loadLaunchableApps(PackageManager pm){
  Intent i=new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
  List<ResolveInfo> resolved=pm.queryIntentActivities(i,0);
  ArrayList<AppEntry> out=new ArrayList<>();
  for(ResolveInfo r:resolved){
   if(r.activityInfo==null)continue;
   ComponentName c=new ComponentName(r.activityInfo.packageName,r.activityInfo.name);
   out.add(new AppEntry(c,r.loadLabel(pm).toString(),r.loadIcon(pm)));
  }
  out.sort((a,b)->a.label.compareToIgnoreCase(b.label));
  return out;
 }
}
