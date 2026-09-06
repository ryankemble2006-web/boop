package com.boop.launcher;
import org.junit.Test;
import static org.junit.Assert.*;
public class SwipeTest {
 @Test public void upUsesDisplacementWithoutVelocity(){assertEquals("up",Swipe.classify(0,-150,60));}
 @Test public void leftAndRightUseDominantAxis(){assertEquals("left",Swipe.classify(-120,20,60));assertEquals("right",Swipe.classify(120,-20,60));}
 @Test public void downFinishesEditing(){assertEquals("down",Swipe.classify(10,120,60));}
 @Test public void smallMotionIsNotNavigation(){assertEquals("none",Swipe.classify(40,-30,60));}
}
