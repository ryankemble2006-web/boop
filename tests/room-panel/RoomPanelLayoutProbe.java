package com.boop.shieldhome;
public final class RoomPanelLayoutProbe {
    static void check(boolean ok) { if (!ok) throw new AssertionError("Room panel bounds"); }
    public static void main(String[] args) {
        RoomPanelLayout.Bounds a = RoomPanelLayout.calculate(1116,552,355,16,336,110);
        check(a.top==371 && a.height==181 && a.width==780);
        RoomPanelLayout.Bounds b = RoomPanelLayout.calculate(1116,552,355,16,0,110);
        check(b.top==a.top && b.height==a.height && b.width==1116);
        check(RoomPanelLayout.calculate(1116,552,600,16,0,110).height==0);
        check(RoomPanelLayout.tileWidth(800,3,12,150)==258);
        check(RoomPanelLayout.tileWidth(800,1,12,150)==800);
        check(RoomPanelLayout.tileWidth(800,20,12,150)==191);
        for(int w=0;w<2400;w+=13)for(int bottom=0;bottom<800;bottom+=11){
            RoomPanelLayout.Bounds r=RoomPanelLayout.calculate(w,675,bottom,16,336,110);
            check(r.top>=0 && r.top<=675 && r.height>=0 && r.top+r.height<=675 && r.width>=0 && r.width<=w);
        }
    }
}
