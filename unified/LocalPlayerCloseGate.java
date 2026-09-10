package com.boop.alpha1;

/** One explicitly selected local player. Never accepts arbitrary shell/package input. */
final class LocalPlayerCloseGate {
    final long sessionId;
    final String packageName, nonce;
    private volatile boolean cancelled;
    LocalPlayerCloseGate(long sessionId, String packageName, String nonce) {
        if (sessionId <= 0 || !("deezer.android.app".equals(packageName)
                || "com.google.android.youtube.tv".equals(packageName))
                || nonce == null || !nonce.matches("[a-f0-9]{32}")) {
            throw new IllegalArgumentException("Unsupported local player");
        }
        this.sessionId=sessionId; this.packageName=packageName; this.nonce=nonce;
    }
    boolean matches(long id, String name) {
        return !cancelled && id==sessionId && packageName.equals(name);
    }
    void cancel() { cancelled=true; }
    String filename() { return "boop-close-"+nonce; }
    String identityCommand() {
        return "run-as com.boop.alpha1 cat files/"+filename()+" 2>/dev/null";
    }
    String closeCommand() {
        if(cancelled) throw new IllegalStateException("Close cancelled");
        return "if [ \"$("+identityCommand()+")\" = '"+nonce+"' ]; then "
                +"am force-stop "+packageName+" && if [ -z \"$(pidof "+packageName
                +")\" ]; then echo CLOSED_"+nonce+"; fi; fi";
    }
}
