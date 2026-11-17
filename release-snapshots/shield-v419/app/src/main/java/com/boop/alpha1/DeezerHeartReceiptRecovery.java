package com.boop.alpha1;
import java.util.HashMap;
import java.util.Map;

/** One read-only reconciliation after an uncertain toggle. Never retries a mutation. */
final class DeezerHeartReceiptRecovery {
    interface ReadOnly { Map<String,String> read()throws Exception; }
    private DeezerHeartReceiptRecovery() { }
    static Map<String,String> resolve(Map<String,String> original,ReadOnly read)throws Exception {
        if(!"toggle".equals(original.get("operation"))||!"UNCONFIRMED".equals(original.get("status")))return original;
        String target=original.get("target_saved");
        if(!"0".equals(target)&&!"1".equals(target))return original;
        Map<String,String> observed=read.read();
        if(observed==null||!"read".equals(observed.get("operation"))||!"OK".equals(observed.get("status")))return original;
        String saved=observed.get("saved");
        if(!"0".equals(saved)&&!"1".equals(saved))return original;
        Map<String,String> result=new HashMap<>(original);
        result.put("saved",saved);
        result.put("status",target.equals(saved)?"OK":"STALE_STATE");
        return result;
    }
}
