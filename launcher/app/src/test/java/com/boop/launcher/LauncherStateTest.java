package com.boop.launcher;
import org.junit.Test;import static org.junit.Assert.*;
public class LauncherStateTest { @Test public void backCollapsesSearchThenDrawer(){assertEquals(LauncherState.ALL_APPS,LauncherState.SEARCH.back());assertEquals(LauncherState.HOME,LauncherState.ALL_APPS.back());assertEquals(LauncherState.HOME,LauncherState.HOME.back());}}
