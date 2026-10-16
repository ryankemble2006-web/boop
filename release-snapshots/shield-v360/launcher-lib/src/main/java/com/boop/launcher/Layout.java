package com.boop.launcher;
import java.util.List;
public final class Layout {
 public static final class Box {
  public float x,y,w,h;
  public Box(float x,float y,float w,float h){this.x=x;this.y=y;this.w=w;this.h=h;}
  public boolean overlaps(Box b){return x < b.x+b.w-.0001f && x+w > b.x+.0001f && y < b.y+b.h-.0001f && y+h > b.y+.0001f;}
 }
 public static Box clamp(Box b){float w=Math.max(.05f,Math.min(1,b.w)),h=Math.max(.05f,Math.min(1,b.h));return new Box(Math.max(0,Math.min(1-w,b.x)),Math.max(0,Math.min(1-h,b.y)),w,h);}
 public static boolean free(Box b,List<Box> used){for(Box u:used)if(b.overlaps(u))return false;return true;}
 public static Box place(Box desired,List<Box> used){Box b=clamp(desired);if(free(b,used))return b;for(int y=0;y<=50;y++)for(int x=0;x<=50;x++){Box p=clamp(new Box(x/50f,y/50f,b.w,b.h));if(free(p,used))return p;}return null;}
}
