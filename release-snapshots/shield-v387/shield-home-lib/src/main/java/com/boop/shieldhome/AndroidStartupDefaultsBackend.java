package com.boop.shieldhome;

import android.content.Context;
import android.util.AtomicFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/** App-private, no-backup receipt: never transferred with the public preset or cloud backup. */
final class AndroidStartupDefaultsBackend implements StartupRestoreStore.Backend {
    private static final Object LOCK=new Object();
    private final AtomicFile file;
    AndroidStartupDefaultsBackend(Context context) {
        file=new AtomicFile(new File(context.getApplicationContext().getNoBackupFilesDir(),"boop-shield-defaults-v1.receipt"));
    }
    public String get(String key) {
        requireKey(key);
        synchronized(LOCK) {
            try(InputStream in=file.openRead();ByteArrayOutputStream out=new ByteArrayOutputStream()) {
                byte[] chunk=new byte[4096];int count;
                while((count=in.read(chunk))!=-1){out.write(chunk,0,count);if(out.size()>262144)throw new IOException("Receipt too large");}
                return new String(out.toByteArray(),StandardCharsets.UTF_8);
            } catch(FileNotFoundException missing) {
                if(file.getBaseFile().exists()||new File(file.getBaseFile()+".bak").exists())throw new IllegalStateException("BOOP defaults Undo could not be read.",missing);
                return null;
            } catch(IOException failed) {throw new IllegalStateException("BOOP defaults Undo could not be read.",failed);}
        }
    }
    public boolean put(String key,String value) {
        requireKey(key);if(value==null||value.length()>262144)return false;
        synchronized(LOCK) {
            FileOutputStream out=null;
            try {out=file.startWrite();out.write(value.getBytes(StandardCharsets.UTF_8));file.finishWrite(out);return value.equals(get(key));}
            catch(IOException failed){file.failWrite(out);return false;}
        }
    }
    public boolean remove(String key) {requireKey(key);synchronized(LOCK){file.delete();return get(key)==null;}}
    public Set<String> keys() {return get("batch")==null?Set.of():Set.of("batch");}
    private static void requireKey(String key) {if(!"batch".equals(key))throw new IllegalArgumentException("Unknown defaults key");}
}
