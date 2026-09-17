"""Non-visual regression for the split app's private close-marker ownership.

Compile and exercise the real production gate. The reflection-only compatibility
path lets the same harness reproduce the pre-fix command, not a compile error.
"""
from pathlib import Path
import re
import subprocess
import pytest

ROOT = Path(__file__).resolve().parents[1]
NONCE = "0123456789abcdef0123456789abcdef"
OWNERS = ["com.boop.alpha1", "com.boop.shieldoverlay"]
GENERATED = ROOT / "boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1"
SOURCES = [ROOT / "unified"] + ([GENERATED] if GENERATED.is_dir() else [])

PROBE = r'''
package com.boop.alpha1;
import java.lang.reflect.*;
public final class CloseIdentityProbe {
    private static LocalPlayerCloseGate gate(String owner, boolean all, String player, String nonce) throws Exception {
        try {
            if (all) {
                Method factory = LocalPlayerCloseGate.class.getDeclaredMethod("allMediaApps", String.class, String.class);
                return (LocalPlayerCloseGate) factory.invoke(null, nonce, owner);
            }
            Constructor<LocalPlayerCloseGate> ctor = LocalPlayerCloseGate.class.getDeclaredConstructor(long.class, String.class, String.class, String.class);
            return ctor.newInstance(7L, player, nonce, owner);
        } catch (NoSuchMethodException oldUnifiedApi) {
            if (all) return (LocalPlayerCloseGate) LocalPlayerCloseGate.class.getDeclaredMethod("allMediaApps", String.class).invoke(null, nonce);
            return LocalPlayerCloseGate.class.getDeclaredConstructor(long.class, String.class, String.class).newInstance(7L, player, nonce);
        }
    }
    public static void main(String[] args) throws Exception {
        try {
            String owner = "<null>".equals(args[0]) ? null : args[0];
            LocalPlayerCloseGate g = gate(owner, "all".equals(args[1]), args[2], args[4]);
            if ("identity".equals(args[3])) System.out.print(g.identityCommand());
            else if ("close".equals(args[3])) System.out.print(g.closeCommand());
            else if ("matches".equals(args[3])) System.out.print(g.matches(7, args[2]) + "," + g.matches(8, args[2]));
            else if ("cancel".equals(args[3])) {
                g.cancel();
                if (g.matches(7, args[2])) throw new AssertionError("Cancelled gate still matches");
                try { g.closeCommand(); throw new AssertionError("Cancelled gate issued a command"); }
                catch (IllegalStateException expected) { System.out.print("CANCELLED"); }
            } else throw new AssertionError("Unknown probe operation");
        } catch (InvocationTargetException failure) {
            if (!(failure.getCause() instanceof IllegalArgumentException)) throw failure;
            System.out.print("REJECTED");
        }
    }
}
'''

@pytest.fixture(scope="module", params=SOURCES, ids=lambda p: "materialized" if p == GENERATED else "source")
def probe(request, tmp_path_factory):
    source = request.param
    out = tmp_path_factory.mktemp("close-identity")
    harness = out / "CloseIdentityProbe.java"
    harness.write_text(PROBE)
    subprocess.run(["javac", "-d", str(out), str(source / "LocalPlayerCloseGate.java"), str(harness)], check=True, capture_output=True, text=True)
    def run(owner, mode="native", player="deezer.android.app", operation="identity", nonce=NONCE):
        result = subprocess.run(["java", "-cp", str(out), "com.boop.alpha1.CloseIdentityProbe", owner, mode, player, operation, nonce], check=True, capture_output=True, text=True)
        return result.stdout
    return run

@pytest.mark.parametrize("owner", OWNERS)
@pytest.mark.parametrize("mode", ["native", "all"])
def test_marker_is_read_from_the_running_application(probe, owner, mode):
    assert probe(owner, mode) == f"run-as {owner} cat files/boop-close-{NONCE} 2>/dev/null"

@pytest.mark.parametrize("owner", OWNERS)
@pytest.mark.parametrize("player", ["deezer.android.app", "com.google.android.youtube.tv"])
def test_selected_native_close_uses_same_owner_and_only_selected_target(probe, owner, player):
    command = probe(owner, player=player, operation="close")
    identity = f"run-as {owner} cat files/boop-close-{NONCE} 2>/dev/null"
    assert command.count(identity) == 1
    assert re.findall(r"am force-stop ([a-z0-9_.]+)", command) == [player]
    assert f"pidof {player}" in command and f"echo CLOSED_{NONCE}" in command
    assert "com.google.android.apps.mediashell" not in command

@pytest.mark.parametrize("owner", OWNERS)
def test_close_media_rechecks_same_owner_between_native_targets(probe, owner):
    command = probe(owner, "all", operation="close")
    identity = f"run-as {owner} cat files/boop-close-{NONCE} 2>/dev/null"
    assert command.count(identity) == 3
    assert re.findall(r"am force-stop ([a-z0-9_.]+)", command) == ["deezer.android.app", "com.google.android.youtube.tv"]
    assert "pidof deezer.android.app" in command and "pidof com.google.android.youtube.tv" in command
    assert f"echo CLOSED_{NONCE}" in command
    assert "com.google.android.apps.mediashell" not in command

@pytest.mark.parametrize("owner", ["<null>", "", "com.boop", "com.boop.shieldoverlay.extra", "com.boop.alpha1;id", "com.boop.shieldoverlay\n", "com.example.other", "$(id)"])
@pytest.mark.parametrize("mode", ["native", "all"])
def test_untrusted_application_package_is_rejected(probe, owner, mode):
    assert probe(owner, mode) == "REJECTED"

@pytest.mark.parametrize("owner", OWNERS)
@pytest.mark.parametrize("mode", ["native", "all"])
def test_back_cancellation_still_blocks_commands(probe, owner, mode):
    assert probe(owner, mode, operation="cancel") == "CANCELLED"

@pytest.mark.parametrize("owner", OWNERS)
def test_selected_session_identity_still_matters(probe, owner):
    assert probe(owner, operation="matches") == "true,false"
    assert probe(owner, "all", operation="matches") == "false,false"

@pytest.mark.parametrize("owner", OWNERS)
@pytest.mark.parametrize("nonce", ["", "0123", NONCE + ";id"])
def test_invalid_marker_nonce_is_rejected(probe, owner, nonce):
    assert probe(owner, nonce=nonce) == "REJECTED"

@pytest.mark.parametrize("source", SOURCES, ids=lambda p: "materialized" if p == GENERATED else "source")
def test_both_activity_routes_supply_the_context_package(source):
    activity = (source / "BoopClosePlayerActivity.java").read_text()
    assert "String applicationPackageName=getPackageName();" in activity
    assert "LocalPlayerCloseGate.allMediaApps(nonce,applicationPackageName)" in activity
    assert "new LocalPlayerCloseGate(sessionId,player,nonce,applicationPackageName)" in activity
    assert "marker=new File(getFilesDir(),gate.filename());" in activity
