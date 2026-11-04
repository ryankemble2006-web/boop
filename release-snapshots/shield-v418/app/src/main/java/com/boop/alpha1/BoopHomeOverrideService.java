package com.boop.alpha1;

/** Optional Shield HOME workaround, inert whenever another BOOP profile is selected. */
public final class BoopHomeOverrideService extends com.boop.shieldhome.ShieldHomeOverrideService {
    @Override protected boolean homeProfileEnabled() {
        return BoopDeviceProfile.resolve(this) == BoopDeviceProfile.Mode.SHIELD;
    }
    @Override protected android.content.Intent homeIntent() {
        return new android.content.Intent(this, UnifiedEntryActivity.class)
            .setAction(android.content.Intent.ACTION_MAIN)
            .addCategory(android.content.Intent.CATEGORY_HOME);
    }
}
