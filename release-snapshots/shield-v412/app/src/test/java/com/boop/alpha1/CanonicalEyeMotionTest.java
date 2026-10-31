import com.boop.eyes.EyeCatalogue;
import com.boop.eyes.EyeMotion;

public final class CanonicalEyeMotionTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check(EyeCatalogue.ALL.length == 26, "all 26 clips imported");
        check(EyeCatalogue.find("idle") != null, "idle exists");
        check(EyeCatalogue.find("notification") != null, "notification exists");
        EyeMotion.Controller controller = new EyeMotion.Controller(EyeCatalogue.find("idle"), 0, 3);
        controller.setAmbientBlinkEnabled(false);
        EyeMotion.Pose disabled = controller.sample(10_000);
        EyeMotion.Pose base = EyeCatalogue.find("idle").sample(10_000);
        check(Math.abs(disabled.left - base.left) < 0.0001f, "ambient blink can be disabled");
        controller.setAmbientBlinkEnabled(true);
        check(controller.ambientBlinkEnabled(), "ambient blink re-enabled");
        System.out.println("CanonicalEyeMotionTest PASS");
    }
}
