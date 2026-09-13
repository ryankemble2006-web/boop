import com.boop.lab.LabPacket;

public final class LabPacketTest {
    private static void expect(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        LabPacket d=LabPacket.discover("shield-1","Shield Living Room");
        LabPacket d2=LabPacket.parse(d.encode());
        expect("DISCOVER".equals(d2.kind),"discover kind");
        expect("Shield Living Room".equals(d2.deviceName),"discover name");
        LabPacket n=LabPacket.notify("req-7","MESSAGE","Hello | BOOP","Line 1\nLine 2");
        LabPacket n2=LabPacket.parse(n.encode());
        expect("NOTIFY".equals(n2.kind),"notify kind");
        expect("MESSAGE".equals(n2.testType),"notify type");
        expect("Hello | BOOP".equals(n2.title),"title round trip");
        expect("Line 1\nLine 2".equals(n2.text),"text round trip");
        boolean rejected=false;try{LabPacket.parse("junk");}catch(IllegalArgumentException e){rejected=true;}
        expect(rejected,"bad packet rejected");
        System.out.println("LabPacketTest passed");
    }
}
