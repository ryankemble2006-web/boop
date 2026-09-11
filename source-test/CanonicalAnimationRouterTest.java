import com.boop.alpha1.BoopCanonicalAnimationRouter;

public final class CanonicalAnimationRouterTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        check("wake".equals(BoopCanonicalAnimationRouter.wake()), "wake clip");
        check("sleep".equals(BoopCanonicalAnimationRouter.sleep()), "sleep clip");
        check("listening".equals(BoopCanonicalAnimationRouter.listening()), "listening clip");
        check("thinking".equals(BoopCanonicalAnimationRouter.thinking()), "thinking clip");
        check("berry_remember".equals(BoopCanonicalAnimationRouter.berry(0)), "berry remember");
        check("berry_curious".equals(BoopCanonicalAnimationRouter.berry(1)), "berry curious");
        check("berry_cheeky".equals(BoopCanonicalAnimationRouter.berry(2)), "berry cheeky");
        check("shake_reaction".equals(BoopCanonicalAnimationRouter.shake()), "shake clip");
        check("notification".equals(BoopCanonicalAnimationRouter.notification()), "notification clip");
        System.out.println("CanonicalAnimationRouterTest PASS");
    }
}
