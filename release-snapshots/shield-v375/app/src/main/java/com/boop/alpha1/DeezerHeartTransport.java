package com.boop.alpha1;

import java.io.IOException;
import java.util.function.BooleanSupplier;

/** A fallback is safe only before any native operation has been dispatched. */
final class DeezerHeartTransport {
    static java.util.List<String> upload(String file,String encoded) {
        if(!file.matches("/data/local/tmp/boop_[a-z0-9_.]+")||!encoded.matches("[A-Za-z0-9_+/=-]+"))
            throw new IllegalArgumentException("Invalid staged heart data");
        java.util.List<String> commands=new java.util.ArrayList<>();
        commands.add("umask 077; : > "+file);
        for(int i=0;i<encoded.length();i+=1500)
            commands.add("printf '%s' '"+encoded.substring(i,Math.min(encoded.length(),i+1500))+"' >> "+file);
        return commands;
    }
    interface Connection extends AutoCloseable {
        String execute(String command)throws Exception;
        void close()throws Exception;
    }
    interface Local { Connection open()throws Exception; }
    interface Fallback { String execute()throws Exception; }
    static String execute(Local local,Fallback fallback,String command,BooleanSupplier current)throws Exception {
        check(current);
        Connection connection;
        try { connection=local.open(); }
        catch(Exception unavailable){check(current);return fallback.execute();}
        try(Connection active=connection){
            check(current);
            String result=active.execute(command);
            check(current);
            return result;
        }
    }
    private static void check(BooleanSupplier current)throws IOException {
        if(Thread.currentThread().isInterrupted()||!current.getAsBoolean())throw new IOException("Heart request cancelled");
    }
}
