package com.boop.eyes;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
/** Independent UI owners cannot accidentally uncover each other's puppet. */
public final class PuppetCoverState {
    private final Set<Object> owners=Collections.newSetFromMap(new IdentityHashMap<Object,Boolean>());
    public void set(Object owner,boolean covered){
        if(owner==null)throw new IllegalArgumentException("Cover owner required");
        if(covered)owners.add(owner);else owners.remove(owner);
    }
    public boolean covered(){return !owners.isEmpty();}
}
