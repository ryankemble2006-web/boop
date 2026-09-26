"""Exercise notification scope against the production immutable settings and policy."""
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parents[1]


def test_all_apps_remains_opt_in_and_honours_exclusions(tmp_path):
    probe = tmp_path / 'AllAppsProbe.java'
    probe.write_text(r'''
package com.boop.alpha1;
import java.util.Set;
public class AllAppsProbe {
    static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
    static boolean allows(BoopNotificationSettingsState state, String app, String channel) {
        return BoopNotificationPolicy.allows(state, app, channel);
    }
    public static void main(String[] args) {
        BoopNotificationSettingsState old = new BoopNotificationSettingsState(
            true, 8000, Set.of("chat"), Set.of(BoopNotificationSettingsCodec.channelKey("chat", "messages")));
        check(allows(old, "chat", "messages"), "existing selected category preserved");
        check(!allows(old, "chat", "new") && !allows(old, "new-app", "messages"), "upgrade does not widen consent");
        BoopNotificationSettingsState all = old.withAllAppsEnabled(true);
        check(allows(all, "new-app", "new-category"), "explicit all-app mode accepts future categories");
        check(!allows(all.withMasterEnabled(false), "new-app", "new-category"), "master off overrides all apps");
        all = all.withAppEnabled("chat", false).withChannelEnabled("mail", "marketing", false);
        check(!allows(all, "chat", "new"), "excluded app blocks new categories too");
        check(!allows(all, "mail", "marketing") && allows(all, "mail", "inbox"), "category exclusion scoped to its app");
        check(allows(all, "elsewhere", "marketing"), "same category id in another app is not excluded");
        check(!allows(all.withTimeoutMs(20000), "chat", "new"), "ordinary setting preserves exclusions");
        check(all.equals(BoopNotificationSettingsCodec.decodeState(BoopNotificationSettingsCodec.encodeState(all))),
                "serialized scope and exclusions survive round trip");
        check(!BoopNotificationSettingsCodec.decodeState("1|8000||").allAppsEnabled(), "legacy settings do not broaden consent");
        BoopNotificationSettingsState selected = all.withAllAppsEnabled(false);
        check(!allows(selected, "chat", "messages"), "switching to selected mode cannot re-enable an excluded app");
        check(allows(old.withAllAppsEnabled(true).withAllAppsEnabled(false), "chat", "messages"), "unchanged selected-mode choices survive switch");
        check(allows(selected.withAppEnabled("chat", true), "chat", "messages"), "explicit selected-mode enable clears exclusion");
        check(!allows(selected, "new-app", "new-category"), "turning off all apps restores narrow scope");
        check(!allows(selected.withAllAppsEnabled(true), "chat", "new"), "exclusions survive round trip");
        all = all.withAppEnabled("chat", true).withChannelEnabled("mail", "marketing", true);
        check(allows(all, "chat", "new") && allows(all, "mail", "marketing"), "individual exclusions reversible");
        check(!allows(all, null, "x") && !allows(all, "", "x") && !allows(all, "x", null), "invalid metadata denied");
        check(!BoopNotificationSettingsState.defaults().allAppsEnabled(), "fresh installs remain opt in");
        check(!all.equals(old), "scope participates in state equality");
        System.out.println("PASS existing consent, new categories, app/category exclusions, round trip, master and metadata gates");
    }
}
''', encoding='utf-8')
    names = ['BoopNotificationSettingsState', 'BoopNotificationSettingsCodec', 'BoopNotificationPolicy']
    subprocess.run(['javac', '-encoding', 'UTF-8', '-d', str(tmp_path),
                    *[str(ROOT / 'source' / (name + '.java')) for name in names], str(probe)], check=True)
    subprocess.run(['java', '-cp', str(tmp_path), 'com.boop.alpha1.AllAppsProbe'], check=True)
