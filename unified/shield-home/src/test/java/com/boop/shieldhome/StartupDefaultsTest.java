package com.boop.shieldhome;
import java.util.*;
public final class StartupDefaultsTest {
    static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        List<StartupDefaultsProfile.Entry> entries=StartupDefaultsProfile.entries();
        check(entries.size()==14,"14 frozen entries");
        check(entries.stream().filter(StartupDefaultsProfile.Entry::disable).count()==9,"nine disables");
        check(entries.stream().filter(StartupDefaultsProfile.Entry::boot).count()==9,"nine boot selections");
        check(entries.stream().filter(StartupDefaultsProfile.Entry::background).count()==6,"six paired restrictions");
        Set<String> ids=new HashSet<>(); for(var e:entries)check(ids.add(e.packageName()),"unique IDs");
        check(!ids.contains("com.nvidia.tegrazone3")&&!ids.contains("com.nvidia.benchmarkblocker")&&!ids.contains("com.google.android.tungsten.setupwraith"),"old disables excluded");
        check(!StartupDefaultsProfile.find("org.xbmc.kodi").disable()&&!StartupDefaultsProfile.find("org.xbmc.kodi").background(),"Kodi stays enabled and keeps old app-op");
        check(entries.get(entries.size()-1).packageName().equals(StartupRecoveryPolicy.STOCK_LAUNCHER),"launcher last");
        check(StartupDefaultsProfile.find("com.google.android.tvlauncher.extra")==null,"no prefix rules");
        var memory=new LinkedHashMap<String,String>(); var journal=new StartupDefaultsJournal(StartupRestoreStore.mapBackend(memory));
        check(journal.load()==null,"no receipt initially");
        var before=new StartupDefaultsJournal.State("default","allow","foreground",false);
        var after=new StartupDefaultsJournal.State("disabled-user","allow","foreground",true);
        var entry=new StartupDefaultsJournal.Entry("com.google.android.tv",before,after,null,null,9,false);
        var batch=new StartupDefaultsJournal.Batch(List.of("com.google.android.tv"),List.of(entry),false,false,Set.of("com.other.app"));
        journal.save(batch);check(journal.load().equals(batch),"durable exact round trip");
        String good=memory.get("batch"); memory.put("batch","not-a-receipt");boolean rejected=false;
        try{journal.load();}catch(IllegalStateException expected){rejected=true;}
        check(rejected&&memory.get("batch").equals("not-a-receipt"),"corruption is not an empty receipt");
        memory.put("batch",good);journal.clear();check(journal.load()==null,"verified removal");
        boolean invalidMask=false;
        try{new StartupDefaultsJournal.Entry("com.google.android.tv",before,after,null,null,15,false);}
        catch(IllegalArgumentException expected){invalidMask=true;}
        check(invalidMask,"receipt cannot own actions outside frozen profile");
        System.out.println("StartupDefaultsTest PASS");
    }
}
