import com.boop.eyes.EyeMotion;
import com.boop.eyes.ProductionAnimationController;

public final class ProductionAnimationControllerTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    private static void close(float actual, float expected, String message) {
        if (Math.abs(actual - expected) > 0.0001f) {
            throw new AssertionError(message + ": " + actual + " != " + expected);
        }
    }

    private static void same(EyeMotion.Pose a, EyeMotion.Pose b, String message) {
        close(a.left, b.left, message + " left");
        close(a.right, b.right, message + " right");
        close(a.x, b.x, message + " x");
        close(a.y, b.y, message + " y");
    }

    public static void main(String[] args) {
        String[] ids = ProductionAnimationController.clipIds();
        check(ids.length == 26, "all 26 canonical clips are production reachable");
        for (String id : ids) check(ProductionAnimationController.supports(id), "supports " + id);
        check(!ProductionAnimationController.supports("not-a-clip"), "reject unknown clip");

        ProductionAnimationController media =
                new ProductionAnimationController("music", 0L, 7L);
        check("music".equals(media.activeClipId()), "music starts active");
        check("music".equals(media.steadyClipId()), "music starts steady");

        media.trigger("track_change", 100L, 0f);
        check("track_change".equals(media.activeClipId()), "track change becomes active");
        check("music".equals(media.steadyClipId()), "track change preserves music steady state");
        media.sample(799L);
        check("track_change".equals(media.activeClipId()), "one-shot remains active before duration");
        media.sample(800L);
        check("music".equals(media.activeClipId()), "one-shot returns to steady state at duration");

        media.setState("media_pause", 900L, 0f);
        media.sample(5000L);
        check("media_pause".equals(media.activeClipId()), "paused clip can remain a steady state");
        check("media_pause".equals(media.steadyClipId()), "paused clip is steady state");

        ProductionAnimationController clock =
                new ProductionAnimationController("music", 0L, 11L);
        EyeMotion.Pose beforePause = clock.sample(450L);
        clock.pause(450L);
        same(beforePause, clock.sample(2000L), "pause freezes canonical time");
        clock.resume(2000L);
        EyeMotion.Pose afterResume = clock.sample(2450L);
        ProductionAnimationController reference =
                new ProductionAnimationController("music", 0L, 11L);
        same(reference.sample(900L), afterResume, "resume continues without catch-up");

        System.out.println("ProductionAnimationControllerTest PASS");
    }
}
