package com.boop.launcher;

import java.util.*;

public class WorkspacePlacementProbe {
    static WorkspaceItem item(float x,float y,float w,float h) {
        WorkspaceItem i=new WorkspaceItem(); i.x=x;i.y=y;i.w=w;i.h=h; return i;
    }
    static void near(float expected,float actual) {
        if(Math.abs(expected-actual)>.00001f)throw new AssertionError(expected+" != "+actual);
    }
    static void require(boolean ok,String message) {if(!ok)throw new AssertionError(message);}
    public static void main(String[] args) {
        WorkspaceItem clock=item(.04f,.05f,.84f,.24f);clock.widgetId=11;
        List<WorkspaceItem> home=new ArrayList<>(Arrays.asList(clock));
        WorkspacePlacement.Bounds centred=WorkspacePlacement.centre(clock,home);
        near(.08f,centred.x);near(.05f,centred.y);near(.84f,centred.w);
        near(.04f,clock.x); // A preview must not silently mutate saved layout.
        WorkspacePlacement.Bounds moved=WorkspacePlacement.move(clock,-.2f,.95f,home);
        near(0,moved.x);near(.76f,moved.y);
        WorkspacePlacement.Bounds small=WorkspacePlacement.resize(clock,.01f,.01f,.3f,.12f,home);
        near(.3f,small.w);near(.12f,small.h);
        WorkspacePlacement.Bounds big=WorkspacePlacement.resize(clock,2,2,.3f,.12f,home);
        near(.96f,big.w);near(.95f,big.h);
        WorkspaceItem neighbour=item(.034f,.315f,.175f,.106f);home.add(neighbour);
        require(WorkspacePlacement.resize(clock,.84f,.4f,.3f,.12f,home)==null,"Resize must not cover apps");
        require(WorkspacePlacement.move(clock,.04f,.3f,home)==null,"Move must not cover apps");
        neighbour.page=1;
        require(WorkspacePlacement.move(clock,.04f,.3f,home)!=null,"Other pages must not block");
        neighbour.page=0;
        WorkspaceItem bike=item(.412f,.414f,.175f,.106f);
        WorkspaceItem row=item(.223f,.43f,.175f,.106f);
        WorkspaceItem column=item(.412f,.66f,.175f,.106f);
        home.addAll(Arrays.asList(bike,row,column));
        WorkspacePlacement.Bounds aligned=WorkspacePlacement.snapApp(bike,.405f,.421f,.05f,.023f,home);
        near(.412f,aligned.x);near(.43f,aligned.y);
        // A crowded drop may not displace another app or alter the original.
        require(WorkspacePlacement.snapApp(bike,.225f,.431f,.05f,.023f,home)==null,"Occupied slot must reject");
        near(.414f,bike.y);near(.43f,row.y);
        WorkspacePlacement.Bounds free=WorkspacePlacement.snapApp(bike,.70f,.13f,.02f,.01f,home);
        require(free==null,"Clock's occupied area must remain protected");
        WorkspaceItem edge=item(.825f,.6f,.175f,.106f);
        home.add(edge);
        WorkspacePlacement.Bounds end=WorkspacePlacement.snapApp(edge,1,.81f,.01f,.01f,home);
        near(.825f,end.x);near(.81f,end.y);
        System.out.println("Widget centring, bounds, collisions and icon row/column snapping passed");
    }
}
