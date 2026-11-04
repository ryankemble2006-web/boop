package com.boop.shieldoverlay;

public final class DeezerPuppetSettingsModel {
    public interface Actions { void setEnabled(boolean enabled); boolean openAccessSettings(); }
    private final Actions actions;
    private boolean awaitingConfirmation;
    private boolean settingsUnavailable;

    public DeezerPuppetSettingsModel(Actions actions) { this.actions = actions; }

    /** True asks the view to present the explicit consent dialog. */
    public boolean toggle(MediaPuppetState.Snapshot snapshot) {
        if (snapshot.enabled) {
            awaitingConfirmation = false;
            actions.setEnabled(false);
            return false;
        }
        awaitingConfirmation = true;
        return true;
    }

    public void confirmEnable() {
        if (awaitingConfirmation) {
            awaitingConfirmation = false;
            actions.setEnabled(true);
        }
    }

    public void cancelEnable() { awaitingConfirmation = false; }

    public void manageAccess() { settingsUnavailable = !actions.openAccessSettings(); }

    public String explanation(MediaPuppetState.Snapshot snapshot) {
        String grant = "Android notification access: " + (snapshot.granted ? "Granted." : "Not granted.");
        if (settingsUnavailable) {
            return grant + " This Shield needs one-time computer setup for access. Ordinary eyes still work.";
        }
        return grant + " Manage access to allow or remove Android's separate grant. "
                + "Switching Off keeps that grant. BOOP ignores notification contents.";
    }
}
