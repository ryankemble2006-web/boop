package com.boop.launcher;

import java.util.*;

public final class AppSearch {
 private AppSearch(){}
 public static List<AppEntry> filter(List<AppEntry> apps,String query){
  String q=query==null?"":query.trim().toLowerCase(Locale.ROOT);
  if(q.isEmpty())return new ArrayList<>(apps);
  ArrayList<AppEntry> prefix=new ArrayList<>(),contains=new ArrayList<>();
  for(AppEntry app:apps){String n=app.label.toLowerCase(Locale.ROOT);if(n.startsWith(q))prefix.add(app);else if(n.contains(q))contains.add(app);}
  prefix.addAll(contains);return prefix;
 }
}
