package com.boop.eyes;
import java.util.Map;
import java.util.TreeMap;

/** Bounded appearance-only registers; deterministic merge without relying on wall clocks. */
public final class StyleState {
    private static final String HEADER="BOOPSTYLE1";
    private final String localId;
    private final Map<String,Entry> fields=new TreeMap<>();
    private long counter;
    private static final class Entry {
        final double value; final long version; final String origin;
        Entry(double v,long n,String id){value=v;version=n;origin=id;}
    }
    public StyleState(String id) {
        if(!validId(id))throw new IllegalArgumentException("Invalid device identity");
        localId=id; seed("hue",190);seed("pitch",1);seed("rate",1);seed("speed",1);
    }
    public synchronized void seed(String key,double value){fields.put(key,new Entry(normalize(key,value),0,localId));}
    public synchronized double value(String key){return fields.get(key).value;}
    public synchronized boolean edit(String key,double value) {
        value=normalize(key,value);
        if(Double.compare(fields.get(key).value,value)==0)return false;
        fields.put(key,new Entry(value,++counter,localId));return true;
    }
    public synchronized String encode() {
        StringBuilder out=new StringBuilder(HEADER).append('\n');
        for(Map.Entry<String,Entry> item:fields.entrySet()) {
            Entry v=item.getValue();out.append(item.getKey()).append('\t').append(v.value).append('\t').append(v.version).append('\t').append(v.origin).append('\n');
        }
        return out.toString();
    }
    public synchronized boolean merge(String payload) {
        if(payload==null || payload.length()>2048)throw new IllegalArgumentException("Oversized style state");
        String[] lines=payload.split("\n");
        if(lines.length!=5 || !HEADER.equals(lines[0]))throw new IllegalArgumentException("Unknown style protocol");
        Map<String,Entry> incoming=new TreeMap<>();
        try {
            for(int i=1;i<lines.length;i++) {
                String[] parts=lines[i].split("\t",-1);
                if(parts.length!=4 || !validId(parts[3]))throw new IllegalArgumentException("Invalid style record");
                double value=normalize(parts[0],Double.parseDouble(parts[1]));
                long version=Long.parseLong(parts[2]);
                if(version<0 || version>1000000000000L)throw new IllegalArgumentException("Invalid style version");
                if(incoming.put(parts[0],new Entry(value,version,parts[3]))!=null)throw new IllegalArgumentException("Duplicate style key");
            }
        } catch(NumberFormatException bad) {throw new IllegalArgumentException("Invalid style number",bad);}
        if(incoming.size()!=4)throw new IllegalArgumentException("Incomplete style state");
        boolean changed=false;
        for(Map.Entry<String,Entry> item:incoming.entrySet()) {
            Entry next=item.getValue(),old=fields.get(item.getKey());
            counter=Math.max(counter,next.version);
            if(next.version>old.version || (next.version==old.version && next.origin.compareTo(old.origin)>0)) {
                fields.put(item.getKey(),next);changed=true;
            }
        }
        return changed;
    }
    private static boolean validId(String id){return id!=null&&id.matches("[A-Za-z0-9-]{1,48}");}
    private static double normalize(String key,double value) {
        double min,max;
        switch(key) {
            case "hue": min=0;max=360;break;
            case "pitch": case "rate": min=.5;max=2;break;
            case "speed": min=0;max=2;break;
            default: throw new IllegalArgumentException("Unknown style key");
        }
        if(!Double.isFinite(value)||value<min||value>max)throw new IllegalArgumentException("Invalid style value");
        return key.equals("hue")?Math.round(value):Math.round(value*1000)/1000d;
    }
}
