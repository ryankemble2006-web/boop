import com.boop.eyes.SignMotion;

public final class SignMotionTest {
    static int checks;
    static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
    public static void main(String[] args){
        check(SignMotion.sample(0,0).lift==0,"starts below the stage");
        check(SignMotion.sample(1200,0).lift==1,"sign must finish lifting");
        check(Math.abs(SignMotion.sample(3000,0).angle)>0.1,"performance must move the sign");
        check(SignMotion.sample(1277,0).eyes.left==1,"shared blink reaches closed hold");
        for(int style=-1;style<5;style++)for(int t=-100;t<14000;t+=7){
            SignMotion.Pose p=SignMotion.sample(t,style);
            check(Float.isFinite(p.lift)&&p.lift>=0&&p.lift<=1,"bounded lift");
            check(Float.isFinite(p.angle)&&Math.abs(p.angle)<=20,"bounded angle");
            check(Float.isFinite(p.sway)&&Float.isFinite(p.bob)&&Float.isFinite(p.wrist),"finite prop state");
            check(p.eyes.left>=0&&p.eyes.left<=1&&p.eyes.right>=0&&p.eyes.right<=1,"bounded lids");
            if(t>=8000)check(p.lift==1&&p.angle==0&&p.sway==0&&p.bob==0&&p.wrist==0,"settles instead of endless jiggling");
        }
        check(SignMotion.sample(Double.NaN,0).lift==0,"invalid clock resets safely");
        check(SignMotion.sample(Double.POSITIVE_INFINITY,0).lift==0,"infinite clock resets safely");
        System.out.println(checks+" sign timing/state checks passed; no visual assertions.");
    }
}
