package com.boop.rally;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.zip.*;

/** Validates without extracting guest paths onto the host filesystem. */
public final class GameBundle {
    public static final long MAX_BYTES=300L*1024*1024;
    public static void validate(File file, GameSpec game) throws IOException {
        if(game==null || !file.isFile() || file.length()>MAX_BYTES) throw new IOException("Game files are missing or too large.");
        try(ZipFile zip=new ZipFile(file)) {
            long total=0; int count=0; boolean executable=false;
            Set<String> names=new HashSet<>();
            ZipEntry boot=null, config=null;
            Enumeration<? extends ZipEntry> entries=zip.entries();
            while(entries.hasMoreElements()) {
                ZipEntry entry=entries.nextElement(); String name=entry.getName();
                if(++count>10000 || name.length()>512 || name.startsWith("/") || name.indexOf('\\')>=0 || name.indexOf(':')>=0 || name.indexOf('\0')>=0)
                    throw new IOException("This ZIP has an unsafe path.");
                for(String part:name.split("/")) if(part.equals("..") || part.equals(".")) throw new IOException("This ZIP has an unsafe path.");
                String key=name.toUpperCase(Locale.ROOT);
                if(!names.add(key)) throw new IOException("This ZIP has duplicate paths.");
                long size=entry.getSize();
                if(size<0 || size>1024L*1024*1024 || (total+=size)>1024L*1024*1024) throw new IOException("This ZIP is too large.");
                if(key.equals(game.executable) && !entry.isDirectory() && size>2) executable=true;
                if(key.equals("DOSBOX.BAT")) boot=entry;
                if(key.equals("DOSBOX.CONF")) config=entry;
            }
            if(!executable || boot==null || config==null) throw new IOException("Choose the prepared "+game.id+".zip game pack.");
            if(!readSmall(zip,boot).equals(game.boot) || !readSmall(zip,config).equals(game.config))
                throw new IOException("This game pack needs preparing with the supplied desktop tool.");
        }
    }
    private static String readSmall(ZipFile zip, ZipEntry entry) throws IOException {
        if(entry.getSize()>8192) throw new IOException("Unexpected game setup file.");
        try(InputStream input=zip.getInputStream(entry);ByteArrayOutputStream output=new ByteArrayOutputStream()) {
            byte[] bytes=new byte[1024];int count;
            while((count=input.read(bytes))!=-1) { if(output.size()+count>8192) throw new IOException("Unexpected game setup file."); output.write(bytes,0,count); }
            return new String(output.toByteArray(),StandardCharsets.US_ASCII);
        }
    }
    public static void importPack(InputStream input, File destination, GameSpec game) throws IOException {
        File parent=destination.getParentFile();
        if(parent==null || (!parent.isDirectory() && !parent.mkdirs())) throw new IOException("Game storage is not available.");
        File temporary=File.createTempFile("rally-import-",".part",parent);
        try {
            try(OutputStream out=new FileOutputStream(temporary)) {
                byte[] buffer=new byte[65536];int n;long total=0;
                while((n=input.read(buffer))!=-1) { if((total+=n)>MAX_BYTES) throw new IOException("This ZIP is too large.");out.write(buffer,0,n); }
            }
            validate(temporary,game);
            Files.move(temporary.toPath(),destination.toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
        } finally { if(temporary.exists()) temporary.delete(); }
    }
}
