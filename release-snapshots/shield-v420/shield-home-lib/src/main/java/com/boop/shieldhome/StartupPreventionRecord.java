package com.boop.shieldhome;

import java.util.List;

public record StartupPreventionRecord(
        String packageName,
        String originalRunInBackground,
        String originalRunAnyInBackground,
        boolean managed) {
    private static final List<String> MODES = List.of("allow", "ignore", "deny", "default");

    public String encode() {
        if (!valid()) throw new IllegalStateException("Invalid startup prevention record");
        return String.join("\t", "v1", packageName, originalRunInBackground,
                originalRunAnyInBackground, Boolean.toString(managed));
    }

    public static StartupPreventionRecord decode(String raw) {
        if (raw == null) return null;
        String[] fields = raw.split("\\t", -1);
        if (fields.length != 5 || !fields[0].equals("v1")) return null;
        boolean managed;
        if (fields[4].equals("true")) managed = true;
        else if (fields[4].equals("false")) managed = false;
        else return null;
        StartupPreventionRecord record = new StartupPreventionRecord(fields[1], fields[2], fields[3], managed);
        return record.valid() ? record : null;
    }
    private boolean valid() {
        return StartupCleanupPolicy.validPackage(packageName)
                && MODES.contains(originalRunInBackground)
                && MODES.contains(originalRunAnyInBackground);
    }
}
