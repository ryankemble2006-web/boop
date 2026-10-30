package com.boop.alpha1;

final class ShieldEntryRoute {
    enum Target {
        SHIELD_PUPPET("com.boop.shieldhome.ShieldLauncherActivity", true),
        WALL("com.boop.alpha1.MainActivity", true),
        HANDHELD_LAUNCHER("com.boop.launcher.MainActivity", true);

        private final String className;
        private final boolean suppressEntryTransition;

        Target(String className, boolean suppressEntryTransition) {
            this.className = className;
            this.suppressEntryTransition = suppressEntryTransition;
        }

        String className() {
            return className;
        }

        boolean suppressEntryTransition() {
            return suppressEntryTransition;
        }
    }

    private ShieldEntryRoute() {}

    static Target resolve(BoopDeviceProfile.Mode mode, boolean homeIntent) {
        return resolve(mode, homeIntent, true);
    }

    static Target resolve(BoopDeviceProfile.Mode mode, boolean homeIntent, boolean builtinLauncherEnabled) {
        if (mode == BoopDeviceProfile.Mode.SHIELD) {
            return Target.SHIELD_PUPPET;
        }
        if (mode == BoopDeviceProfile.Mode.WALL || !builtinLauncherEnabled) {
            return Target.WALL;
        }
        return Target.HANDHELD_LAUNCHER;
    }
}
