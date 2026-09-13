package com.boop.alpha1;
import android.view.View;
import android.view.ViewGroup;
/** Cover existing owners before adding a notification, then release this owner only. */
final class BoopPuppetVisibility {
    private BoopPuppetVisibility(){}
    static void cover(View view,Object owner,boolean covered){
        if(view instanceof BoopCanonicalFaceView)((BoopCanonicalFaceView)view).setCovered(owner,covered);
        else if(view instanceof ViewGroup){
            ViewGroup group=(ViewGroup)view;
            for(int i=0;i<group.getChildCount();i++)cover(group.getChildAt(i),owner,covered);
        }
    }
}
