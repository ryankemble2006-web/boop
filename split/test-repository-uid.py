#!/usr/bin/env python3
"""Execute the generated app repository with two shell UIDs and foreign apps."""
from pathlib import Path
import subprocess
import tempfile

source = Path('boop-build/BOOP-Alpha1/launcher-lib/src/main/java/com/boop/launcher/AppRepository.java').read_text()
files = {
 'com/boop/launcher/AppRepository.java': source,
 'android/content/Intent.java': 'package android.content; public class Intent { public static final String ACTION_MAIN="MAIN",CATEGORY_LAUNCHER="LAUNCHER"; public Intent(String s){} public Intent addCategory(String s){return this;} }',
 'android/content/ComponentName.java': 'package android.content; public class ComponentName {public final String pkg; public ComponentName(String p,String n){pkg=p;}}',
 'android/content/pm/ApplicationInfo.java': 'package android.content.pm; public class ApplicationInfo {public int uid;}',
 'android/content/pm/ActivityInfo.java': 'package android.content.pm; public class ActivityInfo {public String packageName,name; public ApplicationInfo applicationInfo;}',
 'android/content/pm/ResolveInfo.java': 'package android.content.pm; public class ResolveInfo {public ActivityInfo activityInfo; public String label; public CharSequence loadLabel(PackageManager p){return label;} public Object loadIcon(PackageManager p){return new Object();}}',
 'android/content/pm/PackageManager.java': 'package android.content.pm; import java.util.*; import android.content.Intent; public class PackageManager {public final List<ResolveInfo> apps=new ArrayList<>(); public List<ResolveInfo> queryIntentActivities(Intent i,int f){return apps;}}',
 'android/os/Process.java': 'package android.os; public class Process {public static int uid; public static int myUid(){return uid;}}',
 'com/boop/launcher/AppEntry.java': 'package com.boop.launcher; import android.content.ComponentName; public class AppEntry { public final ComponentName component; public final String label; public AppEntry(ComponentName c,String l,Object icon){component=c;label=l;} }',
 'com/boop/launcher/RepositoryHarness.java': '''package com.boop.launcher;
import android.content.pm.*;
import java.util.*;
public class RepositoryHarness {
 static ResolveInfo item(String pkg,String label,int uid){
  ResolveInfo r=new ResolveInfo();r.label=label;r.activityInfo=new ActivityInfo();
  r.activityInfo.packageName=pkg;r.activityInfo.name="Activity";r.activityInfo.applicationInfo=new ApplicationInfo();r.activityInfo.applicationInfo.uid=uid;return r;
 }
 public static void main(String[] args){
  int checks=0;
  for(String self:new String[]{"com.boop.alpha1","com.boop.shieldoverlay"}){
   android.os.Process.uid=12000;
   PackageManager pm=new PackageManager();pm.apps.add(item(self,"BOOP",12000));
   pm.apps.add(item("external.z","Zulu",12001));pm.apps.add(item("external.a","Alpha",12002));
   pm.apps.add(new ResolveInfo());
   List<AppEntry> result=new AppRepository().loadLaunchableApps(pm);
   if(result.size()!=2)throw new AssertionError("Self filter changed external apps");checks++;
   if(!result.get(0).label.equals("Alpha")||!result.get(1).label.equals("Zulu"))throw new AssertionError("Sorting changed");checks++;
   android.os.Process.uid=13000;
   if(new AppRepository().loadLaunchableApps(pm).size()!=3)throw new AssertionError("Foreign UID falsely treated as self");checks++;
  }
  System.out.println(checks+" actual generated repository/self-UID assertions passed");
 }
}'''
}
with tempfile.TemporaryDirectory() as folder:
    root=Path(folder)
    inputs=[]
    for path,text in files.items():
        p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(text);inputs.append(str(p))
    subprocess.run(['javac','-d',folder,*inputs],check=True)
    subprocess.run(['java','-cp',folder,'com.boop.launcher.RepositoryHarness'],check=True)
