package com.boop.alpha1;

/** One explicitly selected local player. Never accepts arbitrary shell/package input. */
final class LocalPlayerCloseGate {
    final long sessionId;
    final String packageName, nonce;
    private final String applicationPackageName;
    private final boolean all;
    private volatile boolean cancelled;
    /** Legacy unified caller. Split application callers must supply their Context package. */
    LocalPlayerCloseGate(long sessionId, String packageName, String nonce) {
        this(sessionId,packageName,nonce,"com.boop.alpha1");
    }
    LocalPlayerCloseGate(long sessionId, String packageName, String nonce, String applicationPackageName) {
        this(sessionId,packageName,nonce,applicationPackageName,false);
    }
    static LocalPlayerCloseGate allMediaApps(String nonce) {
        return allMediaApps(nonce,"com.boop.alpha1");
    }
    static LocalPlayerCloseGate allMediaApps(String nonce, String applicationPackageName) {
        return new LocalPlayerCloseGate(1,"deezer.android.app",nonce,applicationPackageName,true);
    }
    private LocalPlayerCloseGate(long sessionId, String packageName, String nonce,
            String applicationPackageName, boolean all) {
        if (sessionId <= 0 || !("deezer.android.app".equals(packageName)
                || "com.google.android.youtube.tv".equals(packageName))
                || nonce == null || !nonce.matches("[a-f0-9]{32}")) {
            throw new IllegalArgumentException("Unsupported local player");
        }
        if (!("com.boop.alpha1".equals(applicationPackageName)
                || "com.boop.shieldoverlay".equals(applicationPackageName))) {
            throw new IllegalArgumentException("Unsupported BOOP application");
        }
        this.sessionId=sessionId; this.packageName=packageName; this.nonce=nonce;
        this.applicationPackageName=applicationPackageName;
        this.all=all;
    }
    boolean matches(long id, String name) {
        return !all && !cancelled && id==sessionId && packageName.equals(name);
    }
    void cancel() { cancelled=true; }
    String filename() { return "boop-close-"+nonce; }
    String identityCommand() {
        return "run-as "+applicationPackageName+" cat files/"+filename()+" 2>/dev/null";
    }
    String closeCommand() {
        if(cancelled) throw new IllegalStateException("Close cancelled");
        if(all) {
            String identity="[ \"$("+identityCommand()+")\" = '"+nonce+"' ]";
            return "if "+identity+"; then am force-stop deezer.android.app && "+identity
                    +" && am force-stop com.google.android.youtube.tv && "+identity
                    +" && if [ -z \"$(pidof deezer.android.app)\" ] && "
                    +"[ -z \"$(pidof com.google.android.youtube.tv)\" ]; then echo CLOSED_"
                    +nonce+"; fi; fi";
        }
        return "if [ \"$("+identityCommand()+")\" = '"+nonce+"' ]; then "
                +"am force-stop "+packageName+" && if [ -z \"$(pidof "+packageName
                +")\" ]; then echo CLOSED_"+nonce+"; fi; fi";
    }
}
