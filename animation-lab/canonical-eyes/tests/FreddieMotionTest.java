import com.boop.eyes.FreddieMotion;
import com.boop.eyes.SignMotion;
public final class FreddieMotionTest {
    private static int checks;
    private static void check(boolean value,String message){checks++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args){
        check(FreddieMotion.sample(0).lift==0,"Starts before entrance");
        check(FreddieMotion.sample(1200).lift==1,"Entrance completed");
        check(Math.abs(FreddieMotion.sample(2700).angle)>1,"Performance has motion");
        for(int t=0;t<=14000;t+=7){
            SignMotion.Pose p=FreddieMotion.sample(t);
            check(Float.isFinite(p.angle)&&Float.isFinite(p.bob)&&Float.isFinite(p.sway),"Finite state");
            check(p.lift>=0&&p.lift<=1,"Entrance state bounded");
        }
        SignMotion.Pose held=FreddieMotion.sample(10000);
        check(held.lift==1&&held.angle==0&&held.bob==0&&held.sway==0,"Settles without looping");
        check(FreddieMotion.sample(Double.NaN).lift==0,"Invalid clock is safe");
        check(FreddieMotion.sample(-100).lift==0,"Negative clock is safe");
        System.out.println(checks+" Freddie state checks passed; no visual assertions.");
    }
}
