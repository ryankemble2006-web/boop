package com.boop.shieldhome;

public final class TvAppEntry {
    private final String component;
    private final String packageName;
    private final String label;

    public TvAppEntry(String component, String packageName, String label) {
        this.component = component == null ? "" : component;
        this.packageName = packageName == null ? "" : packageName;
        this.label = label == null ? "" : label;
    }

    public String component() { return component; }
    public String packageName() { return packageName; }
    public String label() { return label; }
}
