package com.boop.launcher;
public final class Navigation { public static String back(boolean edit,boolean drawer,int page){return edit?"edit":drawer?"drawer":page>0?"page":"stay";} }
