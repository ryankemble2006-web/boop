package com.boop.shieldturbo.power;

import java.util.Locale;

/** Fixed command construction; package names never come from a free-form shell field. */
public final class PowerPolicy {
    private PowerPolicy() { }
    public static boolean safeUserPackage(String pkg,boolean system) {
        return !system && pkg!=null && pkg.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+")
            && pkg.length()<180 && !pkg.startsWith("com.boop.") && !pkg.startsWith("com.android.")
            && !pkg.startsWith("com.nvidia.") && !pkg.startsWith("com.google.android.gms")
            && !pkg.startsWith("com.google.android.gsf");
    }
    public static boolean validComponent(String value) {
        return value!=null && value.length()<350 && value.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+/[A-Za-z0-9_.$]+");
    }
    public static String restart(String pkg,String component,boolean system) {
        if(!safeUserPackage(pkg,system) || !validComponent(component) || !component.startsWith(pkg+"/"))
            throw new IllegalArgumentException("Only the selected non-system app may be restarted");
        return "am force-stop --user current '"+pkg+"' && am start -W --user current -n '"+component+"'";
    }
    public static int pageScore(String page,String name,String label) {
        String simple=name.substring(name.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);
        String title=label.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
        if(simple.equals("mainsettings") || simple.equals("settings") || simple.contains("shortcut")) return 0;
        if(page.equals("accessibility")) {
            if(simple.equals("accessibilityactivity")) return 100;
            return simple.contains("accessibility") && !simple.contains("service") ? 60 : 0;
        }
        if(simple.contains("display") && simple.contains("sound")) return simple.contains("nv")?110:100;
        if(title.equals("displaysound") || title.equals("displayandsound")) return 90;
        return 0;
    }
}
