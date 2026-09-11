import com.boop.alpha1.BoopCanonicalAnimationMenu;

public final class CanonicalAnimationMenuTest {
    private static void check(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        String[] clips = BoopCanonicalAnimationMenu.clipIds();
        check(clips.length == 26, "menu exposes all 26 eye clips");
        check("idle".equals(clips[0]), "idle first");
        check("reset".equals(clips[25]), "reset last");
        check(BoopCanonicalAnimationMenu.signStyles() == 4, "four sign fixtures");
        check(BoopCanonicalAnimationMenu.hasFreddie(), "Freddie lab routine included");
        System.out.println("CanonicalAnimationMenuTest PASS");
    }
}
