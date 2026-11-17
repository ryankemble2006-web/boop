import com.boop.alpha1.BoopCanonicalAnimationRouter;

public final class CanonicalAnimationRouterTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check("idle".equals(BoopCanonicalAnimationRouter.idle()), "idle clip");
        check("blink".equals(BoopCanonicalAnimationRouter.blink()), "blink clip");
        check("double_blink".equals(BoopCanonicalAnimationRouter.doubleBlink()), "double blink clip");
        check("wake".equals(BoopCanonicalAnimationRouter.wake()), "wake clip");
        check("sleep".equals(BoopCanonicalAnimationRouter.sleep()), "sleep clip");
        check("listening".equals(BoopCanonicalAnimationRouter.listening()), "listening clip");
        check("reading".equals(BoopCanonicalAnimationRouter.reading()), "reading clip");
        check("thinking".equals(BoopCanonicalAnimationRouter.thinking()), "thinking clip");
        check("berry_remember".equals(BoopCanonicalAnimationRouter.berry(0)), "berry remember");
        check("berry_curious".equals(BoopCanonicalAnimationRouter.berry(1)), "berry curious");
        check("berry_cheeky".equals(BoopCanonicalAnimationRouter.berry(2)), "berry cheeky");
        check("music".equals(BoopCanonicalAnimationRouter.music()), "music clip");
        check("media_pause".equals(BoopCanonicalAnimationRouter.mediaPause()), "pause clip");
        check("track_change".equals(BoopCanonicalAnimationRouter.trackChange()), "track clip");
        check("cinema".equals(BoopCanonicalAnimationRouter.cinema()), "cinema clip");
        check("shake_reaction".equals(BoopCanonicalAnimationRouter.shake()), "shake clip");
        check("curious".equals(BoopCanonicalAnimationRouter.curious()), "curious clip");
        check("skeptical".equals(BoopCanonicalAnimationRouter.skeptical()), "skeptical clip");
        check("success".equals(BoopCanonicalAnimationRouter.success()), "success clip");
        check("confused".equals(BoopCanonicalAnimationRouter.confused()), "confused clip");
        check("wink_left".equals(BoopCanonicalAnimationRouter.winkLeft()), "left wink clip");
        check("wink_right".equals(BoopCanonicalAnimationRouter.winkRight()), "right wink clip");
        check("attention_left".equals(BoopCanonicalAnimationRouter.attentionLeft()), "left attention clip");
        check("attention_right".equals(BoopCanonicalAnimationRouter.attentionRight()), "right attention clip");
        check("notification".equals(BoopCanonicalAnimationRouter.notification()), "notification clip");
        check("reset".equals(BoopCanonicalAnimationRouter.reset()), "reset clip");
        System.out.println("CanonicalAnimationRouterTest PASS");
    }
}
