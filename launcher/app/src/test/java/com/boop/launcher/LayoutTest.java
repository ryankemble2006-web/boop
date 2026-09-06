package com.boop.launcher;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
public class LayoutTest {
 @Test public void touchingEdgesDoNotCollide() { assertFalse(new Layout.Box(0,0,.2f,.2f).overlaps(new Layout.Box(.2f,0,.2f,.2f))); }
 @Test public void overlapRejected() { assertFalse(Layout.free(new Layout.Box(.1f,.1f,.2f,.2f), Arrays.asList(new Layout.Box(0,0,.2f,.2f)))); }
 @Test public void clampStaysInside() { Layout.Box b=Layout.clamp(new Layout.Box(-1,2,.3f,.4f)); assertEquals(0,b.x,0); assertEquals(.6f,b.y,.001f); }
 @Test public void reflowPreservesNonOverlap() { List<Layout.Box> out=new ArrayList<>(); for(int i=0;i<8;i++){ Layout.Box b=Layout.place(new Layout.Box(0,0,.2f,.2f),out); assertNotNull(b); assertTrue(Layout.free(b,out)); out.add(b); } }
 @Test public void fullPageFailsSafely() { assertNull(Layout.place(new Layout.Box(0,0,.2f,.2f),Arrays.asList(new Layout.Box(0,0,1,1)))); }
 @Test public void backClosesTransientUiFirst() { assertEquals("edit",Navigation.back(true,true,2)); assertEquals("drawer",Navigation.back(false,true,2)); assertEquals("page",Navigation.back(false,false,2)); assertEquals("stay",Navigation.back(false,false,0)); }
}
