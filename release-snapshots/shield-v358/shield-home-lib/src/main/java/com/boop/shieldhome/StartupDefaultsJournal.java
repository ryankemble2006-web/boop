package com.boop.shieldhome;

import java.io.*;
import java.util.*;

/** One bounded, versioned transaction, durably saved BEFORE each external write. */
public final class StartupDefaultsJournal {
    public static final int ENABLED=1, RUN=2, ANY=4, BOOT=8;
    private static final String PREFIX="BOOP_DEFAULTS_V1:";
    private final StartupRestoreStore.Backend disk;
    public StartupDefaultsJournal(StartupRestoreStore.Backend disk) { this.disk=Objects.requireNonNull(disk); }
    public record State(String enabled,String run,String any,boolean boot) {
        public State {
            if(!StartupRestoreRecord.validEnabled(enabled)||!StartupRestoreRecord.validMode(run)||!StartupRestoreRecord.validMode(any))
                throw new IllegalArgumentException("The exact package state is unavailable.");
        }
        public static State of(StartupPackageState state,boolean boot) {
            return new State(state.enabledState(),state.runInBackgroundMode(),state.runAnyInBackgroundMode(),boot);
        }
        public boolean matches(State other,int mask) {
            return ((mask&ENABLED)==0||enabled.equals(other.enabled)) && ((mask&RUN)==0||run.equals(other.run))
                    && ((mask&ANY)==0||any.equals(other.any)) && ((mask&BOOT)==0||boot==other.boot);
        }
        public State desired(StartupDefaultsProfile.Entry p) {
            return new State(p.disable()&&!disabled(enabled)?"disabled-user":enabled,
                    p.background()?"ignore":run,p.background()?"ignore":any,p.boot()||boot);
        }
        public int difference(State other) {
            return (enabled.equals(other.enabled)?0:ENABLED)|(run.equals(other.run)?0:RUN)
                    |(any.equals(other.any)?0:ANY)|(boot==other.boot?0:BOOT);
        }
        public static boolean disabled(String value) { return value.startsWith("disabled")||value.equals("manifest-disabled"); }
    }
    public record Entry(String pkg,State before,State after,String beforeLedger,String afterLedger,int mask,boolean interrupted) {
        public Entry {
            if(StartupDefaultsProfile.find(pkg)==null||before==null||after==null||mask<1||mask>15)
                throw new IllegalArgumentException("Invalid BOOP defaults receipt.");
            if(mask!=before.difference(before.desired(StartupDefaultsProfile.find(pkg))))
                throw new IllegalArgumentException("Receipt actions do not match the frozen profile.");
            validateLedger(pkg,beforeLedger);validateLedger(pkg,afterLedger);
        }
        public Entry observed(State current,String ledger,boolean uncertain) { return new Entry(pkg,before,current,beforeLedger,ledger,mask,uncertain); }
    }
    public record Batch(List<String> selection,List<Entry> entries,boolean originalAuto,boolean autoArmed,Set<String> originalTargets) {
        public Batch {
            selection=List.copyOf(selection);entries=List.copyOf(entries);originalTargets=Set.copyOf(originalTargets);
            if(selection.isEmpty()||selection.size()>14||new HashSet<>(selection).size()!=selection.size()||entries.size()>14||originalTargets.size()>4096)
                throw new IllegalArgumentException("Invalid defaults selection.");
            for(String pkg:selection)if(StartupDefaultsProfile.find(pkg)==null)throw new IllegalArgumentException("Unknown preset entry.");
            Set<String> unique=new HashSet<>();
            for(Entry entry:entries)if(!selection.contains(entry.pkg())||!unique.add(entry.pkg()))throw new IllegalArgumentException("Duplicate receipt.");
            for(String pkg:originalTargets)if(!StartupPackageController.validPackageName(pkg))throw new IllegalArgumentException("Invalid cleanup target.");
            if(originalAuto&&autoArmed)throw new IllegalArgumentException("Preset did not own that switch.");
        }
        public Batch put(Entry entry) {
            List<Entry> next=new ArrayList<>(entries);next.removeIf(e->e.pkg().equals(entry.pkg()));next.add(entry);
            return new Batch(selection,next,originalAuto,autoArmed,originalTargets);
        }
        public Batch remove(String pkg) { return new Batch(selection,entries.stream().filter(e->!e.pkg().equals(pkg)).collect(java.util.stream.Collectors.toList()),originalAuto,autoArmed,originalTargets); }
        public Batch auto(boolean armed) { return new Batch(selection,entries,originalAuto,armed,originalTargets); }
    }
    public Batch load() {
        String raw=disk.get("batch");if(raw==null)return null;
        try {
            if(!raw.startsWith(PREFIX)||raw.length()>262144)throw new IOException();
            byte[] bytes=Base64.getDecoder().decode(raw.substring(PREFIX.length()));
            DataInputStream in=new DataInputStream(new ByteArrayInputStream(bytes));
            if(in.readInt()!=1)throw new IOException();
            List<String> selected=readStrings(in,14);boolean originalAuto=in.readBoolean(),armed=in.readBoolean();
            Set<String> targets=new LinkedHashSet<>(readStrings(in,4096));int n=count(in,14);
            List<Entry> entries=new ArrayList<>();
            for(int i=0;i<n;i++)entries.add(new Entry(in.readUTF(),readState(in),readState(in),nullable(in.readUTF()),nullable(in.readUTF()),in.readInt(),in.readBoolean()));
            if(in.read()!=-1)throw new IOException();
            Batch batch=new Batch(selected,entries,originalAuto,armed,targets);
            if(!encode(batch).equals(raw))throw new IOException();
            return batch;
        } catch(Exception corrupt) { throw new IllegalStateException("BOOP defaults Undo data needs attention. It has been kept unchanged.",corrupt); }
    }
    public void save(Batch batch) {
        Batch old=load();
        if(old!=null) {
            if(!old.selection().equals(batch.selection())||old.originalAuto()!=batch.originalAuto()||!old.originalTargets().equals(batch.originalTargets()))
                throw new IllegalStateException("The first defaults baseline must not be replaced.");
            for(Entry previous:old.entries())for(Entry entry:batch.entries())if(previous.pkg().equals(entry.pkg())
                    && (!previous.before().equals(entry.before())||!Objects.equals(previous.beforeLedger(),entry.beforeLedger())||previous.mask()!=entry.mask()))
                throw new IllegalStateException("The package baseline must not be replaced.");
        }
        String raw=encode(batch);
        if(!disk.put("batch",raw)||!raw.equals(disk.get("batch")))throw new IllegalStateException("Could not save BOOP defaults Undo. No further changes were made.");
    }
    public void clear() {
        load();
        if(!disk.remove("batch")||disk.get("batch")!=null)throw new IllegalStateException("Could not clear the completed defaults receipt.");
    }
    public static void validateLedger(String pkg,String raw) {
        if(raw==null)return;
        StartupRestoreRecord record=StartupRestoreRecord.decode(raw);
        if(record==null||!pkg.equals(record.packageName()))throw new IllegalArgumentException("The individual Restore record needs attention.");
    }
    private static String nullable(String raw) { return raw.isEmpty()?null:raw; }
    private static int count(DataInputStream in,int max)throws IOException { int count=in.readInt();if(count<0||count>max)throw new IOException();return count; }
    private static List<String> readStrings(DataInputStream in,int max)throws IOException {int n=count(in,max);List<String> list=new ArrayList<>();for(int i=0;i<n;i++)list.add(in.readUTF());return list;}
    private static void strings(DataOutputStream out,Collection<String> values)throws IOException {out.writeInt(values.size());for(String value:values)out.writeUTF(value);}
    private static State readState(DataInputStream in)throws IOException {return new State(in.readUTF(),in.readUTF(),in.readUTF(),in.readBoolean());}
    private static void state(DataOutputStream out,State state)throws IOException {out.writeUTF(state.enabled());out.writeUTF(state.run());out.writeUTF(state.any());out.writeBoolean(state.boot());}
    private static String encode(Batch batch) {
        try {
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
            out.writeInt(1);strings(out,batch.selection());out.writeBoolean(batch.originalAuto());out.writeBoolean(batch.autoArmed());
            strings(out,new TreeSet<>(batch.originalTargets()));out.writeInt(batch.entries().size());
            for(Entry e:batch.entries()) {out.writeUTF(e.pkg());state(out,e.before());state(out,e.after());out.writeUTF(e.beforeLedger()==null?"":e.beforeLedger());out.writeUTF(e.afterLedger()==null?"":e.afterLedger());out.writeInt(e.mask());out.writeBoolean(e.interrupted());}
            out.flush();return PREFIX+Base64.getEncoder().encodeToString(bytes.toByteArray());
        } catch(IOException invalid) {throw new IllegalStateException("Could not encode BOOP defaults Undo.",invalid);}
    }
}
