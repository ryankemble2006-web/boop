package com.boop.launcher;

import java.util.List;

/** Normalized Home rectangles. Previews never change the saved items. */
final class WorkspacePlacement {
    static final class Bounds {
        final float x,y,w,h;
        Bounds(float x,float y,float w,float h){this.x=x;this.y=y;this.w=w;this.h=h;}
        void apply(WorkspaceItem item){item.x=x;item.y=y;item.w=w;item.h=h;}
    }
    private static float clamp(float value,float min,float max){return Math.max(min,Math.min(max,value));}
    private static Bounds available(WorkspaceItem item,Bounds b,List<WorkspaceItem> items){
        for(WorkspaceItem other:items){
            if(other==item||other.page!=item.page)continue;
            // Shared edges are legal, including tiny float rounding at a snapped edge.
            if(b.x<other.x+other.w-.00001f&&b.x+b.w>other.x+.00001f
                    &&b.y<other.y+other.h-.00001f&&b.y+b.h>other.y+.00001f)return null;
        }
        return b;
    }
    static Bounds move(WorkspaceItem item,float x,float y,List<WorkspaceItem> items){
        return available(item,new Bounds(clamp(x,0,1-item.w),clamp(y,0,1-item.h),item.w,item.h),items);
    }
    static Bounds centre(WorkspaceItem item,List<WorkspaceItem> items){return move(item,(1-item.w)/2,item.y,items);}
    static Bounds resize(WorkspaceItem item,float w,float h,float minW,float minH,List<WorkspaceItem> items){
        float maxW=1-item.x,maxH=1-item.y;
        return available(item,new Bounds(item.x,item.y,clamp(w,Math.min(minW,maxW),maxW),
                clamp(h,Math.min(minH,maxH),maxH)),items);
    }
    static Bounds snapApp(WorkspaceItem item,float x,float y,float thresholdX,float thresholdY,List<WorkspaceItem> items){
        x=clamp(x,0,1-item.w);y=clamp(y,0,1-item.h);
        float snappedX=x,snappedY=y,bestX=thresholdX,bestY=thresholdY;
        for(WorkspaceItem other:items){
            if(other==item||other.page!=item.page||other.widgetId>=0)continue;
            float column=other.x+(other.w-item.w)/2;
            float dx=Math.abs(column-x),dy=Math.abs(other.y-y);
            if(dx<bestX){bestX=dx;snappedX=column;}
            if(dy<bestY){bestY=dy;snappedY=other.y;}
        }
        Bounds result=move(item,snappedX,snappedY,items);
        if(result==null)result=move(item,snappedX,y,items);
        if(result==null)result=move(item,x,snappedY,items);
        return result!=null?result:move(item,x,y,items);
    }
}
