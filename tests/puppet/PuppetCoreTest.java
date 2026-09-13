import com.boop.eyes.PuppetClock;
import com.boop.eyes.PuppetCoverState;
public final class PuppetCoreTest {
    static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        PuppetClock c=new PuppetClock(1000);
        check(c.now(1100)==1100,"1x must preserve existing timestamps");
        c.setSpeed(2,1100);check(c.now(1150)==1200,"2x delta only");
        c.setSpeed(.5,1150);check(c.now(1250)==1250,"half speed, no jump");
        c.pause(1250);check(c.now(9250)==1250,"paused clock freezes");
        c.resume(9250);check(c.now(9350)==1300,"resume must not catch up");
        c.setSpeed(Double.NaN,9350);check(c.speed()==1,"invalid speed safely normal");
        c.setSpeed(0,9350);check(c.now(9450)==1300,"explicit BOOP motion off");
        PuppetCoverState state=new PuppetCoverState();Object voice=new Object(),notice=new Object();
        state.set(voice,true);state.set(notice,true);state.set(voice,false);
        check(state.covered(),"closing settings must not reveal face under notification");
        state.set(notice,false);check(!state.covered(),"last owner release restores primary");
        state.set(voice,true);state.set(voice,true);state.set(voice,false);
        check(!state.covered(),"covering twice must be idempotent");
        System.out.println("PuppetCoreTest PASS");
    }
}
