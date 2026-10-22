package com.boop.shieldhome;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import static com.boop.shieldhome.StartupDefaultsJournal.*;

/** Orchestrates the existing verified actions, with a separate pre-preset Undo boundary. */
public final class StartupDefaultsCoordinator {
    private static final ReentrantLock MUTATION=new ReentrantLock();
    public interface Environment {
        List<StartupPackageState> inventory() throws Exception;
        StartupPackageController.Bridge bridge();
        StartupPackageController.BootStore bootStore();
        StartupRestoreStore.Backend individualRecords();
        StartupPackageController controller();
        void checkSafety(String pkg,boolean undo) throws Exception;
        boolean autoEnabled();
        Set<String> cleanupTargets();
        void setAuto(boolean enabled) throws Exception;
    }
    public record Row(StartupDefaultsProfile.Entry profile,State state,String ledger,String reason) {
        public boolean available() { return state!=null&&reason.isEmpty(); }
    }
    public record Preview(List<Row> rows,boolean auto,Set<String> targets) {
        public Preview {rows=List.copyOf(rows);targets=Set.copyOf(targets);}
    }
    public record UndoPlan(Batch batch,List<Row> rows,boolean auto,Set<String> targets) {
        public UndoPlan {rows=List.copyOf(rows);targets=Set.copyOf(targets);}
    }
    public record Result(int applied,int skipped,List<String> details,boolean pending) {
        public Result {details=List.copyOf(details);}
        public String summary() { return applied+" package(s) completed; "+skipped+" left unchanged."
                +(pending?" Saved Undo is available.":""); }
    }
    private final Environment env;
    private final StartupDefaultsJournal journal;
    public StartupDefaultsCoordinator(Environment env,StartupDefaultsJournal journal) {this.env=env;this.journal=journal;}
    public Preview preview() throws Exception {
        Set<String> installed=new HashSet<>();for(var s:env.inventory())installed.add(s.packageName());
        List<Row> rows=new ArrayList<>();
        for(var profile:StartupDefaultsProfile.entries()) {
            try {
                if(!installed.contains(profile.packageName()))throw new IllegalStateException("Not installed on this Shield.");
                env.checkSafety(profile.packageName(),false);
                rows.add(new Row(profile,observe(profile.packageName()),ledger(profile.packageName()),""));
            } catch(Exception failure) {rows.add(new Row(profile,null,null,message(failure)));}
        }
        return new Preview(rows,env.autoEnabled(),env.cleanupTargets());
    }
    public Result apply(Preview review,Set<String> selection,BooleanSupplier cancelled) throws Exception {
        if(!MUTATION.tryLock())throw new IllegalStateException("Another BOOP defaults operation is still finishing.");
        try {
            if(journal.load()!=null)return new Result(0,selection.size(),List.of("BOOP defaults already has a saved transaction. Undo it before applying a different selection."),true);
            for(String pkg:selection)if(StartupDefaultsProfile.find(pkg)==null)throw new IllegalArgumentException("Unknown BOOP defaults package.");
            if(selection.isEmpty())return new Result(0,0,List.of("No packages selected."),false);
            if(env.autoEnabled()!=review.auto()||!env.cleanupTargets().equals(review.targets()))
                return new Result(0,selection.size(),List.of("Boot cleanup changed since the review. Open BOOP defaults again."),false);
            Map<String,Row> reviewed=new HashMap<>();for(Row row:review.rows())reviewed.put(row.profile().packageName(),row);
            List<String> fixed=new ArrayList<>();for(var profile:StartupDefaultsProfile.entries())if(selection.contains(profile.packageName()))fixed.add(profile.packageName());
            Batch batch=new Batch(fixed,List.of(),env.autoEnabled(),false,env.cleanupTargets());
            List<String> details=new ArrayList<>();int done=0,skipped=0;boolean useAuto=false;
            for(String pkg:fixed) {
                if(stopped(cancelled)){details.add("Cancelled. Remaining packages were not touched.");break;}
                Row row=reviewed.get(pkg);Entry receipt=null;
                try {
                    if(row==null||!row.available())throw new IllegalStateException(row==null?"Not reviewed.":row.reason());
                    env.checkSafety(pkg,false);State current=observe(pkg);String raw=ledger(pkg);
                    if(!current.equals(row.state())||!Objects.equals(raw,row.ledger()))throw new IllegalStateException("Changed since review; open the review again.");
                    State target=current.desired(StartupDefaultsProfile.find(pkg));int mask=current.difference(target);
                    if(mask==0){useAuto|=StartupDefaultsProfile.find(pkg).boot();details.add(row.profile().label()+": already matches; left unchanged.");skipped++;continue;}
                    receipt=new Entry(pkg,current,current,raw,raw,mask,true);
                    batch=batch.put(receipt);journal.save(batch);
                    // Limit background and select boot cleanup before disabling the package.
                    int[] actions={(mask&(RUN|ANY)),mask&BOOT,mask&ENABLED};
                    for(int action:actions) {
                        if(action==0)continue;
                        requireRunning(cancelled);env.checkSafety(pkg,false);
                        State beforeAction=observe(pkg);String beforeRecord=ledger(pkg);
                        if(!beforeAction.equals(receipt.after())||!Objects.equals(beforeRecord,receipt.afterLedger()))
                            throw new IllegalStateException("Another setting changed between actions; remaining actions were stopped.");
                        State expected=(action&(RUN|ANY))!=0?new State(beforeAction.enabled(),"ignore","ignore",beforeAction.boot())
                                :(action&BOOT)!=0?new State(beforeAction.enabled(),beforeAction.run(),beforeAction.any(),true)
                                :new State("disabled-user",beforeAction.run(),beforeAction.any(),beforeAction.boot());
                        receipt=receipt.observed(beforeAction,beforeRecord,true);batch=batch.put(receipt);journal.save(batch);
                        StartupPackageController c=env.controller();
                        var result=(action&(RUN|ANY))!=0?c.setBackgroundBlock(pkg,true)
                                :(action&BOOT)!=0?c.setBootClean(pkg,true):c.disable(pkg);
                        State afterAction=observe(pkg);
                        receipt=receipt.observed(afterAction,ledger(pkg),!result.success()||!expected.equals(afterAction));batch=batch.put(receipt);journal.save(batch);
                        if(!result.success())throw new IllegalStateException(result.summary());
                        if(!expected.equals(afterAction))throw new IllegalStateException("Another package setting changed during the action; remaining actions were stopped.");
                    }
                    if(!target.matches(observe(pkg),mask))throw new IllegalStateException("Android did not confirm every change.");
                    useAuto|=StartupDefaultsProfile.find(pkg).boot();done++;details.add(row.profile().label()+": applied.");
                } catch(Exception failure) {
                    skipped++;details.add((row==null?pkg:row.profile().label())+": "+message(failure));
                    if(receipt!=null) {
                        try {receipt=receipt.observed(observe(pkg),ledger(pkg),true);batch=batch.put(receipt);journal.save(batch);}
                        catch(Exception unavailable) {details.add("Undo data retained. No further packages were changed.");break;}
                    }
                    if(stopped(cancelled))break;
                }
            }
            if(useAuto&&!batch.originalAuto()&&!stopped(cancelled)) {
                try {
                    if(env.autoEnabled())details.add("Automatic cleanup was enabled elsewhere; BOOP defaults did not take ownership.");
                    else {batch=batch.auto(true);journal.save(batch);requireRunning(cancelled);env.setAuto(true);
                        if(!env.autoEnabled())throw new IllegalStateException("Automatic cleanup was not confirmed.");
                        details.add("Automatic boot cleanup enabled. Existing selections were kept.");}
                } catch(Exception failure) {details.add(message(failure));}
            }
            if(batch.entries().isEmpty()&&!batch.autoArmed()&&journal.load()!=null)journal.clear();
            return new Result(done,skipped,details,journal.load()!=null);
        } finally {MUTATION.unlock();}
    }
    public UndoPlan previewUndo() throws Exception {
        Batch batch=journal.load();List<Row> rows=new ArrayList<>();
        if(batch!=null)for(Entry entry:batch.entries()) {
            try {
                env.checkSafety(entry.pkg(),true);State current=observe(entry.pkg());String raw=ledger(entry.pkg());
                rows.add(new Row(StartupDefaultsProfile.find(entry.pkg()),current,raw,undoConflict(entry,current,raw)));
            } catch(Exception failure) {rows.add(new Row(StartupDefaultsProfile.find(entry.pkg()),null,null,message(failure)));}
        }
        return new UndoPlan(batch,rows,env.autoEnabled(),env.cleanupTargets());
    }
    public Result undo(UndoPlan review,BooleanSupplier cancelled) throws Exception {
        if(!MUTATION.tryLock())throw new IllegalStateException("Another BOOP defaults operation is still finishing.");
        try {
            Batch batch=journal.load();if(batch==null)return new Result(0,0,List.of("No BOOP defaults changes to undo."),false);
            if(!batch.equals(review.batch()))throw new IllegalStateException("The Undo receipt changed. Review it again.");
            Map<String,Row> reviewed=new HashMap<>();for(Row row:review.rows())reviewed.put(row.profile().packageName(),row);
            List<Entry> reversed=new ArrayList<>(batch.entries());Collections.reverse(reversed);
            List<String> details=new ArrayList<>();int done=0,skipped=0;
            for(Entry entry:reversed) {
                if(stopped(cancelled)){details.add("Undo cancelled. Remaining saved originals are kept.");break;}
                String pkg=entry.pkg();Row row=reviewed.get(pkg);
                try {
                    if(row==null||!row.available())throw new IllegalStateException(row==null?"Not reviewed.":row.reason());
                    env.checkSafety(pkg,true);State current=observe(pkg);String raw=ledger(pkg);
                    if(!current.equals(row.state())||!Objects.equals(raw,row.ledger()))throw new IllegalStateException("Changed after the Undo review; left alone.");
                    String conflict=undoConflict(entry,current,raw);if(!conflict.isEmpty())throw new IllegalStateException(conflict);
                    // Make partial Undo itself recoverable before the first setter.
                    batch=batch.put(entry.observed(current,raw,true));journal.save(batch);
                    if((entry.mask()&ENABLED)!=0&&!current.enabled().equals(entry.before().enabled())) {
                        requireRunning(cancelled);env.checkSafety(pkg,true);env.bridge().setEnabledState(pkg,entry.before().enabled());
                    }
                    current=observe(pkg);
                    if((entry.mask()&(RUN|ANY))!=0) {
                        String run=(entry.mask()&RUN)!=0?entry.before().run():current.run();
                        String any=(entry.mask()&ANY)!=0?entry.before().any():current.any();
                        if(!run.equals(current.run())||!any.equals(current.any())) {
                            requireRunning(cancelled);env.checkSafety(pkg,true);env.bridge().setBackgroundModes(pkg,run,any);
                        }
                    }
                    if((entry.mask()&BOOT)!=0&&env.bootStore().contains(pkg)!=entry.before().boot()) {
                        requireRunning(cancelled);env.checkSafety(pkg,true);
                        if(!env.bootStore().setTarget(pkg,entry.before().boot()))throw new IllegalStateException("Boot selection could not be restored.");
                    }
                    if(!entry.before().matches(observe(pkg),entry.mask()))throw new IllegalStateException("Undo is incomplete; the original is still saved.");
                    if(!Objects.equals(ledger(pkg),raw))throw new IllegalStateException("Individual settings changed during Undo; their record was kept.");
                    requireRunning(cancelled);restoreLedger(pkg,entry.beforeLedger());
                    batch=batch.remove(pkg);journal.save(batch);done++;details.add(row.profile().label()+": pre-defaults state restored.");
                } catch(Exception failure) {skipped++;details.add((row==null?pkg:row.profile().label())+": "+message(failure));}
            }
            if(batch.autoArmed()&&!stopped(cancelled)) {
                try {
                    if(!env.autoEnabled()) {batch=batch.auto(false);journal.save(batch);}
                    else if(env.autoEnabled()!=review.auto()||!env.cleanupTargets().equals(batch.originalTargets()))
                        details.add("Automatic cleanup kept on: other boot selections changed. Review them before retrying Undo.");
                    else {requireRunning(cancelled);env.setAuto(batch.originalAuto());
                        if(env.autoEnabled()!=batch.originalAuto())throw new IllegalStateException("Automatic cleanup could not be restored.");
                        batch=batch.auto(false);journal.save(batch);details.add("Previous automatic cleanup switch restored.");}
                } catch(Exception failure) {details.add(message(failure));}
            }
            if(batch.entries().isEmpty()&&!batch.autoArmed())journal.clear();
            return new Result(done,skipped,details,journal.load()!=null);
        } finally {MUTATION.unlock();}
    }
    /** Explicitly discard group ownership only. Never changes any package or individual ledger. */
    public void keepCurrent(Batch reviewed) {
        if(!MUTATION.tryLock())throw new IllegalStateException("Another defaults operation is still finishing.");
        try {if(!Objects.equals(journal.load(),reviewed))throw new IllegalStateException("Undo data changed. Review again.");journal.clear();}
        finally {MUTATION.unlock();}
    }
    private String undoConflict(Entry entry,State now,String raw) {
        if(entry.before().matches(now,entry.mask())&&Objects.equals(raw,entry.beforeLedger()))return "";
        if(!entry.interrupted())return entry.after().matches(now,entry.mask())&&Objects.equals(raw,entry.afterLedger())?"":"Changed after BOOP defaults; newer settings will be kept.";
        State intended=entry.before().desired(StartupDefaultsProfile.find(entry.pkg()));
        for(int bit:new int[]{ENABLED,RUN,ANY,BOOT})if((entry.mask()&bit)!=0&&!entry.before().matches(now,bit)&&!intended.matches(now,bit))
            return "Changed outside the interrupted action; left unchanged.";
        if(Objects.equals(raw,entry.beforeLedger())||Objects.equals(raw,entry.afterLedger()))return "";
        // A crash can occur between an individual controller save and the batch save.
        StartupRestoreRecord record=raw==null?null:StartupRestoreRecord.decode(raw);
        if(record==null)return "Individual Restore data changed; left unchanged.";
        StartupRestoreRecord old=entry.beforeLedger()==null?null:StartupRestoreRecord.decode(entry.beforeLedger());
        if(old!=null) {
            if(!record.originalEnabledState().equals(old.originalEnabledState())||!record.originalRunInBackground().equals(old.originalRunInBackground())
                    ||!record.originalRunAnyInBackground().equals(old.originalRunAnyInBackground())||record.originalBootClean()!=old.originalBootClean()
                    ||record.firstChangedAtMillis()!=old.firstChangedAtMillis())return "Individual Restore baseline changed; left unchanged.";
        } else if(!record.originalEnabledState().equals(entry.before().enabled())||!record.originalRunInBackground().equals(entry.before().run())
                ||!record.originalRunAnyInBackground().equals(entry.before().any())||record.originalBootClean()!=entry.before().boot())
            return "Individual Restore baseline changed; left unchanged.";
        Set<StartupRecoveryPolicy.ManagedAction> permitted=new HashSet<>();if(old!=null)permitted.addAll(old.managedActions());
        var p=StartupDefaultsProfile.find(entry.pkg());
        if(p.disable())permitted.add(StartupRecoveryPolicy.ManagedAction.DISABLED);
        if(p.background())permitted.add(StartupRecoveryPolicy.ManagedAction.BACKGROUND_BLOCK);
        if(p.boot())permitted.add(StartupRecoveryPolicy.ManagedAction.BOOT_CLEAN);
        return permitted.containsAll(record.managedActions())?"":"Newer individual rules are kept.";
    }
    private State observe(String pkg) throws Exception {
        StartupPackageState value=env.bridge().probe(pkg);
        if(value==null||!pkg.equals(value.packageName()))throw new IllegalStateException("Package identity changed.");
        return State.of(value,env.bootStore().contains(pkg));
    }
    private String ledger(String pkg) {String raw=env.individualRecords().get("entry."+pkg);StartupDefaultsJournal.validateLedger(pkg,raw);return raw;}
    private void restoreLedger(String pkg,String original) {
        boolean saved=original==null?env.individualRecords().remove("entry."+pkg):env.individualRecords().put("entry."+pkg,original);
        if(!saved||!Objects.equals(original,ledger(pkg)))throw new IllegalStateException("Could not restore the previous individual record. Group Undo is retained.");
    }
    private static boolean stopped(BooleanSupplier cancelled) {return Thread.currentThread().isInterrupted()||cancelled.getAsBoolean();}
    private static void requireRunning(BooleanSupplier cancelled)throws InterruptedException {if(stopped(cancelled))throw new InterruptedException("Cancelled. Saved originals are kept.");}
    private static String message(Exception failure) {String text=failure.getMessage();return text==null||text.trim().isEmpty()?"The action did not complete.":text;}
}
