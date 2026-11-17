package com.boop.shieldhome;
import android.content.Context;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/** Android wiring reuses existing authenticated local control and original per-app recovery. */
final class AndroidStartupDefaultsEnvironment implements StartupDefaultsCoordinator.Environment {
    private final Context context;
    private final StartupLocalBridge.PackageSession session;
    private final StartupCleanupStore cleanup;
    private final StartupRestoreStore.Backend records;
    private final StartupPackageController.BootStore boot;
    AndroidStartupDefaultsEnvironment(Context context,StartupLocalBridge.PackageSession session) {
        this.context=context.getApplicationContext();this.session=session;
        cleanup=new StartupCleanupStore(this.context);records=new AndroidStartupRestoreBackend(this.context);
        boot=new StartupPackageController.BootStore(){
            public boolean setTarget(String pkg,boolean enabled){return cleanup.setTarget(pkg,enabled);}
            public boolean contains(String pkg){return cleanup.targets().contains(pkg);}
        };
    }
    public List<StartupPackageState> inventory()throws Exception{return session.inventory();}
    public StartupPackageController.Bridge bridge(){return session;}
    public StartupPackageController.BootStore bootStore(){return boot;}
    public StartupRestoreStore.Backend individualRecords(){return records;}
    public StartupPackageController controller(){return new StartupPackageController(session,boot,new StartupRestoreStore(records,System::currentTimeMillis),AndroidRecoveryCapabilities.resolve(context));}
    public void checkSafety(String pkg,boolean undo)throws Exception {session.verifyDefaultsSafety(pkg);}
    public boolean autoEnabled(){return cleanup.autoEnabled();}
    public Set<String> cleanupTargets(){return cleanup.targets();}
    public void setAuto(boolean enabled)throws Exception {
        session.requireDefaultsSession();
        if(!cleanup.setAutoEnabled(enabled)||cleanup.autoEnabled()!=enabled)throw new IOException("Automatic cleanup did not confirm the change.");
    }
}
