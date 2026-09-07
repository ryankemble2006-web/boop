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

 @Test public void launcherOpenTransitionMirrorsWallReturnSwipe() throws Exception {
  String main=read(
   Paths.get("src/main/java/com/boop/launcher/MainActivity.java"),
   Paths.get("app/src/main/java/com/boop/launcher/MainActivity.java"),
   Paths.get("launcher/app/src/main/java/com/boop/launcher/MainActivity.java"));
  String enter=read(
   Paths.get("src/main/res/anim/boop_enter_from_right.xml"),
   Paths.get("app/src/main/res/anim/boop_enter_from_right.xml"),
   Paths.get("launcher/app/src/main/res/anim/boop_enter_from_right.xml"));
  String exit=read(
   Paths.get("src/main/res/anim/boop_exit_to_left.xml"),
   Paths.get("app/src/main/res/anim/boop_exit_to_left.xml"),
   Paths.get("launcher/app/src/main/res/anim/boop_exit_to_left.xml"));
  assertTrue(main.contains("overrideActivityTransition"));
  assertTrue(main.contains("R.anim.boop_enter_from_right"));
  assertTrue(main.contains("R.anim.boop_exit_to_left"));
  assertTrue(enter.contains("fromXDelta=\"100%p\""));
  assertTrue(enter.contains("toXDelta=\"0\""));
  assertTrue(exit.contains("fromXDelta=\"0\""));
  assertTrue(exit.contains("toXDelta=\"-100%p\""));
 }
}
