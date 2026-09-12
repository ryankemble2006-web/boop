package com.boop.shieldhome;
import java.util.*;
public final class LocalMediaClosePolicyTest {
    static final class Fake implements LocalMediaClosePolicy.Bridge {
        final List<String> stopped=new ArrayList<>(); boolean valid=true; boolean missing=false;
        public boolean installed(String name) { return !missing; }
        public void stop(String name) { stopped.add(name); }
    }
    static void check(boolean condition) { if(!condition) throw new AssertionError(); }
    static void rejected(boolean all,long id,String pkg) throws Exception {
        Fake b=new Fake(); boolean failed=false;
        try { LocalMediaClosePolicy.execute(all,id,pkg,()->true,b); }
        catch(IllegalArgumentException expected) { failed=true; }
        check(failed && b.stopped.isEmpty());
    }
    public static void main(String[] args) throws Exception {
        Fake one=new Fake();
        check(LocalMediaClosePolicy.execute(false,42,"deezer.android.app",()->true,one)==1);
        check(one.stopped.equals(List.of("deezer.android.app")));
        Fake all=new Fake();
        check(LocalMediaClosePolicy.execute(true,0,null,()->true,all)==2);
        check(all.stopped.equals(List.of("deezer.android.app","com.google.android.youtube.tv")));
        rejected(false,1,"com.google.android.apps.mediashell");
        rejected(false,1,"com.android.systemui"); rejected(false,1,"deezer.android.app; reboot");
        rejected(false,0,"deezer.android.app"); rejected(false,1,null);
        Fake cancelled=new Fake(); boolean failed=false;
        try { LocalMediaClosePolicy.execute(true,0,null,()->false,cancelled); }
        catch(java.io.IOException expected) { failed=true; }
        check(failed && cancelled.stopped.isEmpty());
        Fake missing=new Fake();missing.missing=true;
        check(LocalMediaClosePolicy.execute(true,0,null,()->true,missing)==0);
        Fake between=new Fake(); failed=false;
        try { LocalMediaClosePolicy.execute(true,0,null,()->between.stopped.isEmpty(),between); }
        catch(java.io.IOException expected) { failed=true; }
        check(failed && between.stopped.equals(List.of("deezer.android.app")));
        Fake throwsOnSecond=new Fake();
        System.out.println("LocalMediaClosePolicyTest PASS: exact targets, Cast/system protection, injection, missing apps, stale/cancelled action");
    }
}
