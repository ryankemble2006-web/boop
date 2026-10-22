package com.boop.shieldhome;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StartupPackageInventory {
    private static final Pattern COMPONENT = Pattern.compile(
            "(?m)^\\s*([A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+)/(?:[^\\s]+)\\s*$");
    private StartupPackageInventory() { }

    public static Set<String> parsePackageList(String raw) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (raw == null) return out;
        for (String line : raw.split("\\r\\n|\\r|\\n")) {
            String value = line.trim();
            if (!value.startsWith("package:")) continue;
            String pkg = value.substring("package:".length()).trim();
            if (StartupPackageController.validPackageName(pkg)) out.add(pkg);
        }
        return Set.copyOf(out);
    }

    public static Set<String> parseActivityPackages(String raw) {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        if (raw == null) return out;
        Matcher matcher = COMPONENT.matcher(raw);
        while (matcher.find()) out.add(matcher.group(1));
        return Set.copyOf(out);
    }

    public static StartupPackageState detail(String pkg, String userState, boolean system,
            boolean home, String label, String run, String any,
            Set<StartupRecoveryPolicy.ManagedAction> actions) {
        String enabled = StartupPackageCommands.parseEnabled(userState);
        if(!StartupPackageController.validPackageName(pkg) || enabled == null || userState == null
                || !Pattern.compile("\\binstalled=true\\b").matcher(userState).find()
                || !StartupRestoreRecord.validMode(run) || !StartupRestoreRecord.validMode(any))
            throw new IllegalArgumentException("Installed package state could not be verified.");
        return new StartupPackageState(pkg,label,system,home,enabled,run,any,actions);
    }

    public static List<StartupPackageState> merge(String allRaw, String systemRaw,
                                                   String disabledRaw, String launcherRaw,
                                                   Function<String,String> labelLookup) {
        Set<String> all = parsePackageList(allRaw);
        Set<String> system = parsePackageList(systemRaw);
        Set<String> disabled = parsePackageList(disabledRaw);
        Set<String> launchers = parseActivityPackages(launcherRaw);
        ArrayList<StartupPackageState> out = new ArrayList<>();
        for (String pkg : all) {
            String label = labelLookup == null ? null : labelLookup.apply(pkg);
            if (label == null || label.isBlank()) label = pkg;
            out.add(new StartupPackageState(pkg, label, system.contains(pkg), launchers.contains(pkg),
                    disabled.contains(pkg) ? "disabled-user" : "default",
                    "unknown", "unknown", Set.of()));
        }
        out.sort(Comparator.comparing(StartupPackageState::label, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(StartupPackageState::packageName, String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(out);
    }
}
