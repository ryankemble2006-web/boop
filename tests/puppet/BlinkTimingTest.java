import com.boop.eyes.EyeMotion;
import com.boop.eyes.EyeCatalogue;
public final class BlinkTimingTest {
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
    public static void main(String[] args){
        check(EyeMotion.blink(160)==0,"ordinary blink must reopen a little sooner");
        check(EyeCatalogue.find("blink").sample(160).left==0,"explicit blink shares quicker timing");
        check(EyeCatalogue.find("double_blink").sample(453).left==0,"second blink also quicker");
        check(EyeCatalogue.find("success").sample(710).left==0,"embedded success blink quicker");
        check(EyeCatalogue.find("notification").sample(1510).left==0,"notification blink quicker");
        check(EyeCatalogue.find("sleep").sample(1200).left==1,"sleep is not a blink");
        check(EyeCatalogue.find("wink_left").duration==420,"wink choreography preserved");
        check(EyeCatalogue.ALL.length==26,"all 26 routines preserved");
        check(EyeCatalogue.find("success").duration==733,"no change to event sequence duration");
        System.out.println("BlinkTimingTest PASS");
    }
}
