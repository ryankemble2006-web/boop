import com.boop.eyes.MotionPolicy;

public final class MotionPolicyTest {
    private static void expect(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        expect(!MotionPolicy.shouldReduce(false, 0f),
                "Android animator scale 0 must not freeze BOOP");
        expect(MotionPolicy.shouldReduce(true, 1f),
                "Power saver may still reduce BOOP motion");
        expect(MotionPolicy.scaledDelta(16.0, 1.0) == 16.0,
                "1x must preserve existing animation timing exactly");
        expect(MotionPolicy.scaledDelta(16.0, 0.5) == 8.0,
                "0.5x must halve BOOP clock progress");
        expect(MotionPolicy.scaledDelta(16.0, 2.0) == 32.0,
                "2x must double BOOP clock progress");
        System.out.println("MotionPolicyTest passed");
    }
}
