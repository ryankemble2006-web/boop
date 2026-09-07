package com.boop.launcher;

import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import static org.junit.Assert.*;

public class ImmersiveModeSourceTest {
 private static String source(String file) throws Exception {
  Path[] candidates={
   Paths.get("src/main/java/com/boop/launcher",file),
   Paths.get("app/src/main/java/com/boop/launcher",file),
   Paths.get("launcher/app/src/main/java/com/boop/launcher",file)
  };
  for(Path p:candidates)if(Files.exists(p))return new String(Files.readAllBytes(p),StandardCharsets.UTF_8);
  throw new AssertionError("Could not locate launcher source: "+file);
 }

 @Test public void launcherHidesSystemBarsWithSwipeRecovery() throws Exception {
  String s=source("EdgeToEdge.java");
  assertTrue("system bars must be hidden",s.contains("WindowInsets.Type.systemBars()"));
  assertTrue("edge swipe must temporarily reveal system bars",s.contains("BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE"));
 }

 @Test public void drawerKeepsScrollableContentOutOfItsPadding() throws Exception {
  String s=source("AllAppsView.java");
  assertTrue("drawer must scroll behind its own safe bottom padding",s.contains("grid.setClipToPadding(false)"));
 }
}
