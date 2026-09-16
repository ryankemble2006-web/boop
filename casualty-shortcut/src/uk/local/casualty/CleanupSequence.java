package uk.local.casualty;

/** One-shot state machine for Android TV's visible Force stop flow. */
final class CleanupSequence {
    static final int NONE=0;
    static final int CLICK_FORCE_STOP=1;
    static final int CLICK_CONFIRM=2;
    static final int COMPLETE=3;

    private static final int WAIT_APP_INFO=0;
    private static final int WAIT_CONFIRM=1;
    private static final int VERIFY_STOPPED=2;

    private int phase;
    private boolean actionPending;
    private int stoppedObservations;

    void reset() {
        phase=WAIT_APP_INFO;
        actionPending=false;
        stoppedObservations=0;
    }

    int next(boolean appInfo,boolean forceStopVisible,boolean confirmation,boolean okVisible) {
        if(actionPending) return NONE;
        if(phase==WAIT_APP_INFO) {
            if(appInfo && forceStopVisible) {
                stoppedObservations=0;
                actionPending=true;
                return CLICK_FORCE_STOP;
            }
            if(appInfo && !forceStopVisible) {
                if(++stoppedObservations>=2) return COMPLETE;
            } else stoppedObservations=0;
            return NONE;
        }
        if(phase==WAIT_CONFIRM) {
            if(confirmation && okVisible) {
                actionPending=true;
                return CLICK_CONFIRM;
            }
            return NONE;
        }
        if(phase==VERIFY_STOPPED) {
            if(appInfo && !forceStopVisible) {
                if(++stoppedObservations>=2) return COMPLETE;
            } else if(appInfo) stoppedObservations=0;
        }
        return NONE;
    }

    void actionResult(int action,boolean accepted) {
        if(!actionPending) return;
        actionPending=false;
        if(!accepted) return;
        stoppedObservations=0;
        if(action==CLICK_FORCE_STOP && phase==WAIT_APP_INFO) phase=WAIT_CONFIRM;
        else if(action==CLICK_CONFIRM && phase==WAIT_CONFIRM) phase=VERIFY_STOPPED;
    }
}
