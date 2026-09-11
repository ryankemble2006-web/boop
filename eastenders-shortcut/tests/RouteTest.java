package uk.local.eastenders;
import java.util.*;
public class RouteTest {
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        List<String> calls = new ArrayList<>();
        boolean ok = Routes.open((pkg, url) -> {calls.add(pkg); return pkg.equals("com.nvidia.bbciplayer");}, () -> {throw new AssertionError("Unneeded browser");});
        check(ok && calls.size()==1,"Prefer Shield iPlayer");
        calls.clear();
        boolean[] browser = {false};
        ok = Routes.open((pkg,url) -> {calls.add(pkg); return false;}, () -> {browser[0]=true;return true;});
        check(ok && browser[0],"Rejected links fall back to browser");
        check(!Routes.open((pkg,url)->false,()->false),"No handler must report failure");
        check(Routes.TV_URL.contains("tv%2Fprogrammes%2Fb006m86d"),"Use EastEnders programme, not a dated episode");
        System.out.println("PASS: Shield preference, rejection fallback, missing handlers, stable programme link");
    }
}
