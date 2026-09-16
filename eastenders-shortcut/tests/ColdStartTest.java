package uk.local.eastenders;
import java.util.*;
import java.nio.file.*;
public final class ColdStartTest {
    static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        String service=Files.readString(Paths.get("src/uk/local/eastenders/WatchNowService.java"));
        check(service.contains("PlayerBridgeClient"),"Cold start must use the app-local silent cleanup bridge");
        check(!service.contains("APPLICATION_DETAILS_SETTINGS"),"Cold start must not open Android App info");
        List<String> commands=new ArrayList<>();
        PlayerReset.reset(command -> { commands.add(command); return ""; });
        check(commands.size()==2,"Stop and verify before permitting launch");
        check(commands.get(0).equals("am force-stop --user current com.nvidia.bbciplayer"),"Only official Shield iPlayer is stopped");
        check(commands.get(1).contains("ps -A -o NAME"),"Verify main and colon-suffixed background processes");
        boolean failed=false;
        try { PlayerReset.reset(command -> command.startsWith("ps ") ? "com.nvidia.bbciplayer:background" : ""); }
        catch(Exception expected) { failed=true; }
        check(failed,"Never launch warm when any iPlayer process remains");
        commands.clear(); failed=false;
        try { PlayerReset.reset(command -> { commands.add(command); throw new Exception("denied"); }); }
        catch(Exception expected) { failed=true; }
        check(failed && commands.size()==1,"Failed force stop cannot proceed");
        BridgeSession sessions=new BridgeSession();
        long first=sessions.begin(123);
        check(sessions.current(123,first),"Initial owner may use session");
        check(!sessions.current(456,first),"Another caller cannot stop this session");
        long second=sessions.begin(456);
        check(!sessions.current(123,first),"A newer cleanup invalidates old pending stop");
        check(sessions.current(456,second),"New cleanup owns session");
        sessions.cancel(123,first);
        check(sessions.current(456,second),"Stale cancellation cannot affect current session");
        sessions.cancel(456,second);
        check(!sessions.current(456,second),"Completed session cannot act again");
        System.out.println("PASS: app-local cold reset, process verification, failure closed, stale-session safety");
    }
}
