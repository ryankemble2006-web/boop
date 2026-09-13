package com.boop.alpha1;
import java.util.Set;
public final class NotificationAppDefaultTest {
    private static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
    public static void main(String[] args){
        String app="com.boop.animationlab";
        BoopNotificationSettingsState all=new BoopNotificationSettingsState(true,8000,Set.of(app),Set.of(BoopNotificationSettingsCodec.channelKey(app,"*")));
        check(BoopNotificationPolicy.allows(all,app,"boop_lab_tests"),"selected app must accept newly discovered categories");
        check(!BoopNotificationPolicy.allows(all,"unselected.app","test"),"unselected app remains private");
        check(!BoopNotificationPolicy.allows(all.withMasterEnabled(false),app,"test"),"master off wins");
        BoopNotificationSettingsState selective=new BoopNotificationSettingsState(true,8000,Set.of(app),Set.of(BoopNotificationSettingsCodec.channelKey(app,"one")));
        check(!BoopNotificationPolicy.allows(selective,app,"two"),"explicit category selection remains selective");
        check(BoopNotificationPolicy.allows(selective,app,"one"),"selected category works");
        System.out.println("NotificationAppDefaultTest PASS");
    }
}
