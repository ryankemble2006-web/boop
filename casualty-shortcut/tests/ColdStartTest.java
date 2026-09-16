package uk.local.casualty;
import java.util.*;
import java.nio.file.*;
public final class ColdStartTest {
    static void check(boolean value,String message) { if(!value) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        String service=Files.readString(Paths.get("src/uk/local/casualty/WatchNowService.java"));
        check(service.contains("CleanupSequence cleanupSequence"),"Cleanup actions need an explicit one-shot sequence gate");
        check(!service.contains("cleanupLastAction"),"A time delay alone must not guard duplicate force-stop actions");
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
        check(!sessions.current(123,first),"Switching programme invalidates old pending stop");
        check(sessions.current(456,second),"New programme owns cleanup");
        sessions.cancel(123,first);
        check(sessions.current(456,second),"Stale cancellation cannot affect current programme");
        sessions.cancel(456,second);
        check(!sessions.current(456,second),"Completed session cannot act again");
        check(!BridgePolicy.accepts("unknown",BridgePolicy.EASTENDERS_SIGNER),"Unknown app refused");
        check(!BridgePolicy.accepts("uk.local.casualty","wrong"),"Wrong signer refused");
        check(BridgePolicy.accepts("uk.local.casualty",BridgePolicy.CASUALTY_SIGNER),"Original Casualty accepted");
        check(BridgePolicy.accepts("uk.local.eastenders",BridgePolicy.EASTENDERS_SIGNER),"Original EastEnders accepted");
        System.out.println("PASS: one-shot cleanup gate required, clean reset, background verification, failure closed, stale-session safety, caller trust");
    }
}
