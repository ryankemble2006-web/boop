package uk.local.casualty;

public final class CleanupSequenceTest {
    static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
    public static void main(String[] args) {
        CleanupSequence sequence=new CleanupSequence();
        sequence.reset();
        check(sequence.next(true,true,false,false)==CleanupSequence.CLICK_FORCE_STOP,"App info emits one Force stop click");
        check(sequence.next(true,true,false,false)==CleanupSequence.NONE,"Repeated stale App info cannot emit a second Force stop click");
        sequence.actionResult(CleanupSequence.CLICK_FORCE_STOP,true);
        check(sequence.next(true,true,false,false)==CleanupSequence.NONE,"Accepted Force stop waits for confirmation instead of clicking again");
        check(sequence.next(false,false,true,true)==CleanupSequence.CLICK_CONFIRM,"Confirmation emits one OK click");
        check(sequence.next(false,false,true,true)==CleanupSequence.NONE,"Repeated stale confirmation cannot emit a second OK click");
        sequence.actionResult(CleanupSequence.CLICK_CONFIRM,true);
        check(sequence.next(false,false,true,true)==CleanupSequence.NONE,"Accepted OK ignores stale confirmation frames");
        check(sequence.next(true,false,false,false)==CleanupSequence.NONE,"First stopped observation is not enough");
        check(sequence.next(true,false,false,false)==CleanupSequence.COMPLETE,"Two stopped observations complete cleanup");

        sequence.reset();
        check(sequence.next(true,false,false,false)==CleanupSequence.NONE,"Already-stopped app needs stable verification");
        check(sequence.next(true,false,false,false)==CleanupSequence.COMPLETE,"Already-stopped app completes without opening confirmation");

        sequence.reset();
        check(sequence.next(true,true,false,false)==CleanupSequence.CLICK_FORCE_STOP,"Fresh sequence can request Force stop");
        sequence.actionResult(CleanupSequence.CLICK_FORCE_STOP,false);
        check(sequence.next(true,true,false,false)==CleanupSequence.CLICK_FORCE_STOP,"Rejected accessibility click may retry without advancing");
        System.out.println("PASS: Force stop and OK are each one-shot across stale event/poll frames");
    }
}
