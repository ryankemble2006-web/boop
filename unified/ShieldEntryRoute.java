package com.boop.alpha1;

final class ShieldEntryRoute {
    enum Target {
        SHIELD_PUPPET("com.boop.shieldoverlay.MainActivity", true),
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
        if (mode == BoopDeviceProfile.Mode.SHIELD) {
            return Target.SHIELD_PUPPET;
        }
        return mode == BoopDeviceProfile.Mode.WALL
                ? Target.WALL
                : Target.HANDHELD_LAUNCHER;
    }
}
