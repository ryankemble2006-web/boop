package com.boop.launcher;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static org.junit.Assert.*;

public class TransitionDirectionSourceTest {
 private static String read(Path... candidates) throws Exception {
  for(Path p:candidates) if(Files.exists(p)) return new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
  throw new AssertionError("Could not locate expected launcher file");
 }

 @Test public void crossAppHandoffUsesBlackSafeFadeWithoutSystemSlide() throws Exception {
  String main=read(
   Paths.get("src/main/java/com/boop/launcher/MainActivity.java"),
   Paths.get("app/src/main/java/com/boop/launcher/MainActivity.java"),
   Paths.get("launcher/app/src/main/java/com/boop/launcher/MainActivity.java"));
  String manifest=read(
   Paths.get("src/main/AndroidManifest.xml"),
   Paths.get("app/src/main/AndroidManifest.xml"),
   Paths.get("launcher/app/src/main/AndroidManifest.xml"));
  String styles=read(
   Paths.get("src/main/res/values/styles.xml"),
   Paths.get("app/src/main/res/values/styles.xml"),
   Paths.get("launcher/app/src/main/res/values/styles.xml"));
  String styles31=read(
   Paths.get("src/main/res/values-v31/styles.xml"),
   Paths.get("app/src/main/res/values-v31/styles.xml"),
   Paths.get("launcher/app/src/main/res/values-v31/styles.xml"));

  assertTrue(main.contains("overrideActivityTransition(OVERRIDE_TRANSITION_OPEN,0,0)"));
  assertTrue(main.contains("overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE,0,0)"));
  assertFalse(main.contains("Intent.FLAG_ACTIVITY_NO_ANIMATION"));
  assertFalse(main.contains("overridePendingTransition(0,0)"));
  assertTrue(main.contains("ActivityOptions.makeCustomAnimation(this,android.R.anim.fade_in,0)"));
  assertTrue(main.contains("root.animate().alpha(0f).setDuration(120)"));
  assertTrue(main.contains("root.animate().alpha(1f).setDuration(180)"));
  assertFalse(main.contains("R.anim.boop_enter_from_"));
  assertFalse(main.contains("R.anim.boop_exit_to_"));

  assertTrue(manifest.contains("android:theme=\"@style/Theme.BoopLauncher\""));
  assertTrue(styles.contains("<item name=\"android:windowBackground\">@android:color/black</item>"));
  assertTrue(styles.contains("<item name=\"android:windowDisablePreview\">true</item>"));
  assertTrue(styles.contains("<item name=\"android:windowAnimationStyle\">@style/BoopNoWindowAnimation</item>"));
  assertTrue(styles.contains("<item name=\"android:activityOpenEnterAnimation\">@null</item>"));
  assertTrue(styles.contains("<item name=\"android:activityCloseExitAnimation\">@null</item>"));
  assertTrue(styles31.contains("<item name=\"android:windowSplashScreenBackground\">@android:color/black</item>"));
  assertTrue(styles31.contains("<item name=\"android:windowSplashScreenAnimationDuration\">0</item>"));
 }
}
