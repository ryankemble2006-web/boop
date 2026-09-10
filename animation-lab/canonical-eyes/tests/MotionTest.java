import com.boop.eyes.*;
public final class MotionTest {
    static int checks;
    static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
    static boolean near(float a,float b){return Math.abs(a-b)<0.0001f;}
    static boolean same(EyeMotion.Pose a,EyeMotion.Pose b){return near(a.left,b.left)&&near(a.right,b.right)&&near(a.x,b.x)&&near(a.y,b.y);}
    public static void main(String[] args){
        check(EyeCatalogue.ALL.length==26,"complete catalogue");
        check(EyeMotion.blink(0)==0&&EyeMotion.blink(183)==0,"blink endpoints");
        check(EyeMotion.blink(73.2)==1&&EyeMotion.blink(81.2)==1,"closed hold");
        check(EyeMotion.blink(Double.NaN)==0,"invalid time");
        for(EyeMotion.Clip c:EyeCatalogue.ALL){
            for(int t=0;t<20000;t+=17){EyeMotion.Pose p=c.sample(t);check(Float.isFinite(p.x)&&p.left>=0&&p.left<=1&&p.right>=0&&p.right<=1,"finite/clamped "+c.id);}
            if(c.loop)check(same(c.sample(0),c.sample(c.duration)),"loop seam "+c.id);
            else check(same(c.sample(c.duration),c.sample(c.duration+5000)),"terminal hold "+c.id);
        }
        EyeMotion.Controller controller=new EyeMotion.Controller(EyeCatalogue.find("thinking"),0,8);
        EyeMotion.Pose before=controller.sample(300);
        controller.select(EyeCatalogue.find("reading"),300,160);
        check(same(before,controller.sample(300)),"interruption continuity");
        EyeMotion.Pose frozen=controller.sample(380);controller.pause(380);
        check(same(frozen,controller.sample(80000)),"pause freezes time");
        controller.resume(80000);check(same(frozen,controller.sample(80000)),"resume no jump");
        controller.select(EyeCatalogue.find("reset"),80100,160);
        check(same(controller.sample(80500),EyeMotion.OPEN),"reset returns exact neutral state");
        System.out.println(checks+" timing/state checks passed; no visual assertions.");
    }
}
