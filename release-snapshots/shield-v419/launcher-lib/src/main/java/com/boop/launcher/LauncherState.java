package com.boop.launcher;

public enum LauncherState {
 HOME, ALL_APPS, SEARCH;
 public LauncherState back(){ return this==SEARCH?ALL_APPS:this==ALL_APPS?HOME:HOME; }
}
