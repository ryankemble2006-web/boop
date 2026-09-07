package com.boop.launcher;
import android.content.ComponentName;import java.util.*;import org.junit.Test;import static org.junit.Assert.*;
public class AppSearchTest {private AppEntry e(String s){return new AppEntry(new ComponentName("x",s),s,null);}@Test public void prefixBeforeContains(){List<AppEntry> r=AppSearch.filter(Arrays.asList(e("My Settings"),e("Settings"),e("Settle")),"set");assertEquals("Settings",r.get(0).label);assertEquals("Settle",r.get(1).label);}@Test public void emptyReturnsEverything(){assertEquals(2,AppSearch.filter(Arrays.asList(e("A"),e("B")),"").size());}}
