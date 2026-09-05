package com.boop.shieldhdrdebug;

final class DisplaySnapshotFailureFactory {
    private DisplaySnapshotFailureFactory() { }

    static DisplaySnapshot create() {
        return DisplaySnapshot.failure();
    }
}
