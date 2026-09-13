package com.boop.alpha1;

import java.util.HashSet;
import java.util.Set;

/** Owns visibility, not artwork or animation. Independent modal owners cannot revive each other. */
final class BoopFacePresentationState {
    private final Set<String> occluders=new HashSet<>();
    private int requested=4;

    void request(int visibility){
        if(visibility!=0&&visibility!=4&&visibility!=8)throw new IllegalArgumentException("Unknown visibility");
        requested=visibility;
    }

    void occlude(String owner,boolean hidden){
        if(owner==null||owner.isEmpty())throw new IllegalArgumentException("Owner required");
        if(hidden)occluders.add(owner);else occluders.remove(owner);
    }

    int effective(){return occluders.isEmpty()?requested:8;}
}
