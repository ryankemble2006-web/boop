package com.boop.shieldhome;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static com.boop.shieldhome.StartupDefaultsJournal.*;

public final class StartupDefaultsCoordinatorTest {
    static int count;
    interface Scenario { void run() throws Exception; }
    static void check(boolean v,String message) { if(!v)throw new AssertionError(message); }
    static void test(String name,Scenario scenario)throws Exception {scenario.run();count++;System.out.println("PASS "+name);}
    static final String VIDEO="com.amazon.amazonvideo.livingroom.nvidia", TV="com.google.android.tv", KODI="org.xbmc.kodi", HOME=StartupRecoveryPolicy.STOCK_LAUNCHER;
    static final StartupRecoveryPolicy.RecoveryCapabilities CAPS=new StartupRecoveryPolicy.RecoveryCapabilities("com.boop.alpha1","com.android.tv.settings","com.google.android.packageinstaller","com.google.android.inputmethod.latin","com.boop.alpha1");
    static StartupPackageState state(String pkg,String enabled,String run,String any) {return new StartupPackageState(pkg,pkg,true,pkg.equals(HOME),enabled,run,any,Set.of());}
    static final class World implements StartupDefaultsCoordinator.Environment,StartupPackageController.Bridge,StartupPackageController.BootStore {
        Map<String,StartupPackageState> live=new LinkedHashMap<>();
        Map<String,String> ledgers=new LinkedHashMap<>(),journalDisk=new LinkedHashMap<>();
        Set<String> targets=new LinkedHashSet<>(),blocked=new HashSet<>();
        List<String> writes=new ArrayList<>();boolean auto,ignoreEnabled,partialBackground,failJournal;int safetyChecks;
        Runnable afterWrite=()->{};
        StartupRestoreStore.Backend backend=StartupRestoreStore.mapBackend(ledgers);
        StartupDefaultsJournal journal=new StartupDefaultsJournal(new StartupRestoreStore.Backend(){
            public String get(String k){return journalDisk.get(k);}public boolean put(String k,String v){if(failJournal)return false;journalDisk.put(k,v);return true;}
            public boolean remove(String k){journalDisk.remove(k);return true;}public Set<String> keys(){return Set.copyOf(journalDisk.keySet());}
        });
        World(String... packages){for(String pkg:packages)live.put(pkg,state(pkg,"default","default","default"));}
        StartupDefaultsCoordinator coordinator(){return new StartupDefaultsCoordinator(this,journal);}
        public List<StartupPackageState> inventory(){return List.copyOf(live.values());}
        public StartupPackageState probe(String pkg){if(!live.containsKey(pkg))throw new IllegalStateException("Not installed");return live.get(pkg);}
        public StartupPackageController.Bridge bridge(){return this;}
        public StartupPackageController.BootStore bootStore(){return this;}
        public StartupRestoreStore.Backend individualRecords(){return backend;}
        public StartupPackageController controller(){return new StartupPackageController(this,this,new StartupRestoreStore(backend,()->123L),CAPS);}
        public void checkSafety(String pkg,boolean undo){safetyChecks++;if(blocked.contains(pkg))throw new IllegalStateException("Recovery or current foreground must stay available");}
        public boolean autoEnabled(){return auto;}
        public Set<String> cleanupTargets(){return Set.copyOf(targets);}
        public void setAuto(boolean enabled){writes.add("AUTO:"+enabled);auto=enabled;afterWrite.run();}
        public boolean setTarget(String pkg,boolean enabled){writes.add("BOOT:"+pkg+":"+enabled);if(enabled)targets.add(pkg);else targets.remove(pkg);afterWrite.run();return true;}
        public boolean contains(String pkg){return targets.contains(pkg);}
        public void setEnabledState(String pkg,String enabled){writes.add("ENABLED:"+pkg);var old=probe(pkg);if(!ignoreEnabled)live.put(pkg,state(pkg,enabled,old.runInBackgroundMode(),old.runAnyInBackgroundMode()));afterWrite.run();}
        public void setBackgroundModes(String pkg,String run,String any){writes.add("BG:"+pkg);var old=probe(pkg);live.put(pkg,state(pkg,old.enabledState(),run,partialBackground?old.runAnyInBackgroundMode():any));afterWrite.run();if(partialBackground){partialBackground=false;throw new IllegalStateException("second app-op failed");}}
        public void forceStop(String pkg){throw new AssertionError("preset must not force-stop manually");}
    }
    static StartupDefaultsCoordinator.Result apply(World w,Set<String> ids)throws Exception {var c=w.coordinator();return c.apply(c.preview(),ids,()->false);}
    public static void main(String[] args)throws Exception {
        test("preview makes no persistent changes",()->{World w=new World(VIDEO);var p=w.coordinator().preview();check(p.rows().size()==14,"all profile rows visible");check(w.writes.isEmpty()&&w.ledgers.isEmpty()&&w.journalDisk.isEmpty(),"read-only preview");});
        test("only selected exact IDs apply",()->{World w=new World(VIDEO,TV,"com.example.other");apply(w,Set.of(TV));check(w.live.get(TV).enabledState().equals("disabled-user"),"TV disabled");check(w.live.get(VIDEO).enabledState().equals("default"),"excluded package unchanged");check(!w.ledgers.containsKey("entry.com.example.other"),"no prefix expansion");});
        test("missing and protected packages are skipped",()->{World w=new World(TV);w.blocked.add(TV);var r=apply(w,Set.of(TV,VIDEO));check(w.writes.isEmpty(),"no unsafe writes");check(r.skipped()>=2,"reported skipped");});
        test("launcher is the last package",()->{World w=new World(HOME,TV,VIDEO);apply(w,Set.of(HOME,TV,VIDEO));int home=-1,last=-1;for(int i=0;i<w.writes.size();i++)if(!w.writes.get(i).startsWith("AUTO")){last=i;if(w.writes.get(i).endsWith(HOME))home=i;}check(home==last,"Home disable last");});
        test("receiving device and previous customization restored exactly",()->{World w=new World(VIDEO);w.live.put(VIDEO,state(VIDEO,"enabled","ignore","allow"));w.targets.add("com.example.keep");var original=new StartupRestoreRecord(VIDEO,"default","default","allow",Set.of(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK),"enabled","ignore","allow",44L);w.ledgers.put("entry."+VIDEO,original.encode());String old=w.ledgers.get("entry."+VIDEO);apply(w,Set.of(VIDEO));var c=w.coordinator();c.undo(c.previewUndo(),()->false);var restored=w.live.get(VIDEO);check(restored.enabledState().equals("enabled")&&restored.runInBackgroundMode().equals("ignore")&&restored.runAnyInBackgroundMode().equals("allow"),"pre-preset not original global baseline");check(w.ledgers.get("entry."+VIDEO).equals(old),"individual ledger exact");check(w.targets.equals(Set.of("com.example.keep"))&&!w.auto,"global and unrelated targets restored");check(w.journal.load()==null,"completed journal cleared");});
        test("Kodi restrictions and enabled state left alone",()->{World w=new World(KODI);w.live.put(KODI,state(KODI,"enabled","deny","ignore"));apply(w,Set.of(KODI));check(w.live.get(KODI).enabledState().equals("enabled")&&w.live.get(KODI).runInBackgroundMode().equals("deny"),"only boot changes");check(w.writes.stream().noneMatch(x->x.startsWith("BG")||x.startsWith("ENABLED")),"no Kodi disable/app-op command");});
        test("already applied actions are no-ops",()->{World w=new World(VIDEO);w.live.put(VIDEO,state(VIDEO,"disabled-user","ignore","ignore"));w.targets.add(VIDEO);w.auto=true;apply(w,Set.of(VIDEO));check(w.writes.isEmpty()&&w.ledgers.isEmpty()&&w.journal.load()==null,"no accidental ownership of pre-existing defaults");});
        test("repeat apply cannot broaden or overwrite",()->{World w=new World(TV,VIDEO);apply(w,Set.of(TV));String original=w.journalDisk.get("batch");int writes=w.writes.size();apply(w,Set.of(VIDEO));check(w.writes.size()==writes&&original.equals(w.journalDisk.get("batch")),"repeat untouched");});
        test("preview drift blocks package change",()->{World w=new World(TV);var c=w.coordinator();var preview=c.preview();w.live.put(TV,state(TV,"enabled","default","default"));c.apply(preview,Set.of(TV),()->false);check(w.writes.isEmpty(),"changed after review");});
        test("storage refusal happens before any device write",()->{World w=new World(TV);w.failJournal=true;try{apply(w,Set.of(TV));}catch(IllegalStateException expected){}check(w.writes.isEmpty(),"durable undo required first");});
        test("partial background write remains undoable after recreation",()->{World w=new World(VIDEO);w.partialBackground=true;apply(w,Set.of(VIDEO));check(w.journal.load()!=null,"partial journal persists");var c=w.coordinator();c.undo(c.previewUndo(),()->false);check(w.live.get(VIDEO).runInBackgroundMode().equals("default")&&w.journal.load()==null,"partial first write restored");});
        test("ignored Android action does not report completed package",()->{World w=new World(TV);w.ignoreEnabled=true;var r=apply(w,Set.of(TV));check(r.applied()==0&&r.details().stream().anyMatch(s->s.contains("confirm")||s.contains("incomplete")),"failure visible");});
        test("cancellation keeps recovery and stops following packages",()->{World w=new World(VIDEO,TV);AtomicBoolean cancel=new AtomicBoolean();w.afterWrite=()->cancel.set(true);var c=w.coordinator();c.apply(c.preview(),Set.of(VIDEO,TV),cancel::get);check(w.live.get(TV).enabledState().equals("default")&&w.journal.load()!=null,"remaining untouched and original retained");w.afterWrite=()->{};c=w.coordinator();c.undo(c.previewUndo(),()->false);check(w.live.get(VIDEO).runInBackgroundMode().equals("default"),"cancelled prefix undoable");});
        test("newer manual changes are not overwritten",()->{World w=new World(VIDEO);apply(w,Set.of(VIDEO));w.live.put(VIDEO,state(VIDEO,"enabled","ignore","ignore"));int writes=w.writes.size();var c=w.coordinator();var plan=c.previewUndo();c.undo(plan,()->false);check(w.writes.size()==writes&&w.journal.load()!=null,"drift retained for review");});
        test("untouched dimension is preserved during Undo",()->{World w=new World(TV);apply(w,Set.of(TV));w.live.put(TV,state(TV,"disabled-user","deny","foreground"));var c=w.coordinator();c.undo(c.previewUndo(),()->false);check(w.live.get(TV).runInBackgroundMode().equals("deny")&&w.live.get(TV).runAnyInBackgroundMode().equals("foreground"),"do not restore unowned app-ops");});
        test("Undo leaves newer global cleanup choices on",()->{World w=new World(TV);apply(w,Set.of(TV));w.targets.add("com.new.selection");var c=w.coordinator();c.undo(c.previewUndo(),()->false);check(w.auto&&w.targets.contains("com.new.selection")&&w.journal.load()!=null,"new cleanup preserved");});
        test("safety is rechecked during application",()->{World w=new World(VIDEO,HOME);var c=w.coordinator();var preview=c.preview();w.afterWrite=()->w.blocked.add(HOME);c.apply(preview,Set.of(VIDEO,HOME),()->false);check(w.live.get(HOME).enabledState().equals("default"),"recovery changed before launcher");});
        test("unknown package and malformed state cannot be applied",()->{World w=new World(TV);w.live.put(TV,state(TV,"default","unknown","default"));apply(w,Set.of(TV));check(w.writes.isEmpty(),"unknown state fails closed");try{apply(w,Set.of("com.example.inject"));}catch(IllegalArgumentException expected){}check(w.writes.isEmpty(),"unknown IDs rejected");});
        test("undo preview becoming stale prevents writes",()->{World w=new World(TV);apply(w,Set.of(TV));var c=w.coordinator();var plan=c.previewUndo();w.live.put(TV,state(TV,"disabled-user","ignore","default"));int writes=w.writes.size();c.undo(plan,()->false);check(w.writes.size()==writes,"recheck exact reviewed state");});
        test("individual Restore record changed later is not erased",()->{World w=new World(VIDEO);apply(w,Set.of(VIDEO));var raw=new StartupRestoreRecord(VIDEO,"enabled","allow","default",Set.of(),"disabled-user","ignore","ignore",999L);w.ledgers.put("entry."+VIDEO,raw.encode());var c=w.coordinator();int writes=w.writes.size();c.undo(c.previewUndo(),()->false);check(w.writes.size()==writes&&w.ledgers.get("entry."+VIDEO).equals(raw.encode()),"new ledger preserved");});
        test("process death after Android write keeps original recoverable",()->{
            World w=new World(VIDEO);w.afterWrite=()->{throw new AssertionError("simulated process death");};
            try{apply(w,Set.of(VIDEO));}catch(AssertionError expected){check(expected.getMessage().contains("simulated"),"expected crash point");}
            check(w.journal.load()!=null&&w.journal.load().entries().get(0).interrupted(),"write-ahead receipt survived");
            w.afterWrite=()->{};var c=w.coordinator();c.undo(c.previewUndo(),()->false);
            check(w.live.get(VIDEO).runInBackgroundMode().equals("default")&&w.journal.load()==null,"fresh process recovered original");
        });
        test("process death halfway through Undo is retryable",()->{
            World w=new World(VIDEO);apply(w,Set.of(VIDEO));w.afterWrite=()->{throw new AssertionError("simulated undo death");};
            var c=w.coordinator();try{c.undo(c.previewUndo(),()->false);}catch(AssertionError expected){}
            w.afterWrite=()->{};c=w.coordinator();c.undo(c.previewUndo(),()->false);
            check(w.live.get(VIDEO).enabledState().equals("default")&&w.live.get(VIDEO).runInBackgroundMode().equals("default")&&!w.auto&&w.journal.load()==null,"resumed Undo is exact");
        });
        test("pre-existing automatic cleanup stays enabled",()->{World w=new World(TV);w.auto=true;w.targets.add("com.old.target");apply(w,Set.of(TV));var c=w.coordinator();c.undo(c.previewUndo(),()->false);check(w.auto&&w.targets.equals(Set.of("com.old.target")),"previous on switch preserved");});
        test("damaged individual original cannot be overwritten",()->{World w=new World(TV);w.ledgers.put("entry."+TV,"damaged");apply(w,Set.of(TV));check(w.writes.isEmpty()&&w.ledgers.get("entry."+TV).equals("damaged"),"corrupt baseline fails closed");});
        test("keep-current clears only group ownership",()->{World w=new World(VIDEO);apply(w,Set.of(VIDEO));var current=w.live.get(VIDEO);String ledger=w.ledgers.get("entry."+VIDEO);int countWrites=w.writes.size();w.coordinator().keepCurrent(w.journal.load());check(w.journal.load()==null&&w.writes.size()==countWrites&&w.live.get(VIDEO).equals(current)&&w.ledgers.get("entry."+VIDEO).equals(ledger),"no package/individual changes");});
        test("mid-package external drift is rejected before next write",()->{
            World w=new World(VIDEO);w.afterWrite=()->{if(w.writes.size()==1)w.live.put(VIDEO,state(VIDEO,"enabled","ignore","ignore"));};
            var result=apply(w,Set.of(VIDEO));check(w.live.get(VIDEO).enabledState().equals("enabled"),"new external enabled choice must not be disabled");
        });
        test("review cannot broaden the frozen profile",()->{
            World w=new World(KODI);var c=w.coordinator();var p=c.preview();
            var fake=new StartupDefaultsProfile.Entry(KODI,"Kodi",true,true,true,"forged flags");
            var row=new StartupDefaultsCoordinator.Row(fake,new State("default","default","default",false),null,"");
            c.apply(new StartupDefaultsCoordinator.Preview(List.of(row),p.auto(),p.targets()),Set.of(KODI),()->false);
            check(w.live.get(KODI).enabledState().equals("default")&&w.live.get(KODI).runInBackgroundMode().equals("default"),"only frozen actions authoritative");
        });
        System.out.println("StartupDefaultsCoordinatorTest PASS: "+count+" scenarios");
    }
}
