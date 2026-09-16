package uk.local.eastenders;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.security.MessageDigest;
final class BridgeTrust {
    static boolean packageTrusted(Context context,String pkg) {
        try {
            PackageInfo info=context.getPackageManager().getPackageInfo(pkg,PackageManager.GET_SIGNATURES);
            if(info.signatures==null || info.signatures.length!=1) return false;
            byte[] hash=MessageDigest.getInstance("SHA-256").digest(info.signatures[0].toByteArray());
            StringBuilder hex=new StringBuilder();
            for(byte b:hash) hex.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
            return BridgePolicy.accepts(pkg,hex.toString());
        } catch(Exception unavailable) { return false; }
    }
    static boolean callerTrusted(Context context,int uid) {
        String[] packages=context.getPackageManager().getPackagesForUid(uid);
        if(packages==null) return false;
        for(String pkg:packages) if(packageTrusted(context,pkg)) return true;
        return false;
    }
}
