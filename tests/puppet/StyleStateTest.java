import com.boop.eyes.StyleState;
public final class StyleStateTest {
    static void check(boolean b,String m){if(!b)throw new AssertionError(m);}
    public static void main(String[] args){
        StyleState[] group=new StyleState[5];
        for(int i=0;i<5;i++)group[i]=new StyleState("device-"+i);
        group[3].edit("hue",280);
        for(StyleState device:group)device.merge(group[3].encode());
        for(StyleState device:group)check(device.value("hue")==280,"device 4 reaches all five");
        group[0].edit("pitch",2);group[4].edit("pitch",.5);
        String a=group[0].encode(), b=group[4].encode();
        group[0].merge(b);group[4].merge(a);
        check(group[0].encode().equals(group[4].encode()),"simultaneous edits converge regardless of arrival order");
        for(StyleState device:group){device.merge(group[4].encode());device.merge(group[0].encode());}
        StyleState reconnect=new StyleState("offline-device");reconnect.merge(group[1].encode());
        check(reconnect.value("pitch")==.5,"offline device catches up");
        String before=group[0].encode();
        for(String invalid:new String[]{"junk",before.replace("hue","token"),before.replace("280.0","NaN"),before.replace("280.0","900")}) {
            boolean rejected=false;try{group[0].merge(invalid);}catch(IllegalArgumentException expected){rejected=true;}
            check(rejected,"malformed/out-of-range state rejected atomically");
            check(before.equals(group[0].encode()),"invalid packet cannot partially mutate state");
        }
        check(!group[0].merge(before),"duplicates do not echo as new edits");
        System.out.println("StyleStateTest PASS: five devices, conflict, reconnect, validation");
    }
}
