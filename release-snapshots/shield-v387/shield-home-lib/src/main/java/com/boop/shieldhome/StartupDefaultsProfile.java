package com.boop.shieldhome;

import java.util.List;

/** Frozen choices only. No source-device preferences, broad prefixes or hidden extras. */
public final class StartupDefaultsProfile {
    public static final String ID="boop-shield-defaults-v1";
    public record Entry(String packageName,String label,boolean disable,boolean boot,
                        boolean background,String consequence) {
        public String actions() {
            return (disable ? "Disable app" : "Keep current enabled state")
                    + (boot ? " / Close after boot" : "")
                    + (background ? " / Limit background start" : "");
        }
    }
    private static final List<Entry> ENTRIES=List.of(
        new Entry("com.amazon.amazonvideo.livingroom.nvidia","Prime Video",true,true,true,"Prime Video will be unavailable until re-enabled."),
        new Entry("com.android.printspooler","Print service",true,true,false,"Android printing will be unavailable."),
        new Entry("com.android.providers.contacts","Contacts storage",true,true,false,"Apps that use Android contacts may lose access to them."),
        new Entry("com.google.android.tv","Live Channels",true,true,false,"The Live Channels app will be unavailable."),
        new Entry("com.google.android.tvrecommendations","TV recommendations",true,true,true,"Stock TV recommendations will be unavailable."),
        new Entry("com.nvidia.factory","NVIDIA factory component",true,false,false,"A vendor component is disabled; firmware behavior can differ."),
        new Entry("com.nvidia.nvgamecast","NVIDIA nvgamecast",true,false,true,"Features using this vendor component may stop working."),
        new Entry("com.nvidia.shield.nvcustomize","NVIDIA customization",true,true,false,"Vendor customization features may be unavailable."),
        new Entry("com.lonelycatgames.Xplore","X-plore",false,true,false,"Closed once after boot; manual opening remains available."),
        new Entry("org.xbmc.kodi","Kodi",false,true,false,"Closed once after boot. Existing background restrictions are left alone."),
        new Entry("com.nvidia.ControllerMapper","NVIDIA Controller Mapper",false,false,true,"Controller mapping may be affected by background limits."),
        new Entry("flar2.homebutton","Button Mapper",false,false,true,"Remote shortcuts may be affected. Active input services are skipped."),
        new Entry("uk.local.eastenders","EastEnders shortcut",false,false,true,"Its background behavior is limited; it is not disabled."),
        // The actual HOME change is deliberately the final package in every Apply.
        new Entry("com.google.android.tvlauncher","Google TV launcher",true,true,false,"Stock Home is removed. BOOP Home and recovery must be ready first.")
    );
    private StartupDefaultsProfile() { }
    public static List<Entry> entries() { return ENTRIES; }
    public static Entry find(String pkg) {
        for(Entry entry:ENTRIES) if(entry.packageName().equals(pkg))return entry;
        return null;
    }
}
