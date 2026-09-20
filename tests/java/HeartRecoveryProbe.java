package com.boop.alpha1;
import java.util.*;
public final class HeartRecoveryProbe {
    static int checks,calls;
    static void yes(boolean value,String why){checks++;if(!value)throw new AssertionError(why);}
    static Map<String,String> receipt(String op,String status,int saved,int target){
        Map<String,String> r=new HashMap<>();r.put("operation",op);r.put("status",status);
        r.put("saved",""+saved);r.put("target_saved",""+target);r.put("nonce","original");return r;
    }
    public static void main(String[] args)throws Exception {
        Map<String,String> first=receipt("toggle","UNCONFIRMED",-1,1);
        Map<String,String> result=DeezerHeartReceiptRecovery.resolve(first,()->{calls++;return receipt("read","OK",1,-1);});
        yes(calls==1,"Exactly one follow-up");yes("OK".equals(result.get("status")),"Applied save reconciles");
        yes("1".equals(result.get("saved")),"Uses observed saved state");yes("toggle".equals(result.get("operation")),"Keeps original owner");
        yes("UNCONFIRMED".equals(first.get("status")),"Original receipt not mutated");
        result=DeezerHeartReceiptRecovery.resolve(first,()->receipt("read","OK",0,-1));
        yes("STALE_STATE".equals(result.get("status")),"Opposite observed state is not reported as a successful save");
        yes("0".equals(result.get("saved")),"Still refreshes actual state");
        result=DeezerHeartReceiptRecovery.resolve(receipt("toggle","UNCONFIRMED",-1,0),()->receipt("read","OK",0,-1));
        yes("OK".equals(result.get("status")),"Applied removal reconciles");
        for(Map<String,String> r:Arrays.asList(receipt("read","UNCONFIRMED",-1,1),receipt("dislike","UNCONFIRMED",-1,1),receipt("toggle","OK",1,1),receipt("toggle","UNAVAILABLE",-1,1),receipt("toggle","UNCONFIRMED",-1,-1)))
            yes(DeezerHeartReceiptRecovery.resolve(r,()->{throw new AssertionError("No replay or recursive reads");})==r,"Only uncertain toggles qualify");
        result=DeezerHeartReceiptRecovery.resolve(first,()->receipt("read","UNAVAILABLE",-1,-1));
        yes(result==first,"Unconfirmed read cannot manufacture a filled heart");
        result=DeezerHeartReceiptRecovery.resolve(first,()->null);yes(result==first,"Missing receipt cannot manufacture success");
        System.out.println("Heart readback: "+checks+" behavioural checks passed");
    }
}
