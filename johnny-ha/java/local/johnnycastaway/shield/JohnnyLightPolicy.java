package local.johnnycastaway.shield;

/** Gives a real night frame time to render before the OI reaction takes actor ownership. */
final class JohnnyLightPolicy {
 private JohnnyLightPolicy() {}
 static long oiDelayMs(){ return 150L; }
}
