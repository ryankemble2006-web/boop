"""Focused Wall/Shield split contracts, added before the implementation."""
from pathlib import Path
import subprocess
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
BASE = "9d57019d9370dbe3f47061b6e8b0ce8ed5134715"
A = "{http://schemas.android.com/apk/res/android}"


def text(path):
    p = ROOT / path
    assert p.is_file(), f"Missing split implementation: {path}"
    return p.read_text(encoding="utf-8")


def test_two_application_shells_share_one_assistant_library():
    shared = text("split/assistant-lib.gradle")
    assert "com.android.library" in shared
    assert "../app/src/main/java" in shared
    assert "../app/src/main/assets" in shared
    assert "applicationId " not in shared
    for body, package in (("wall", "com.boop.alpha1"), ("shield", "com.boop.shieldoverlay")):
        shell = text(f"split/{body}/build.gradle")
        assert "com.android.application" in shell
        assert package in shell
        assert "project(':assistant-lib')" in shell
        assert "boopDev" in shell
        manifest = ET.fromstring(text(f"split/{body}/AndroidManifest.xml"))
        assert manifest.find("application").get(A + "allowBackup") == "false"


def test_profile_picker_is_not_a_setup_requirement():
    entry = text("unified/UnifiedEntryActivity.java")
    profile = text("unified/BoopProfileActivity.java")
    assert "profile_choice_seen" not in entry
    assert "profile_choice_seen" not in profile
    for label in ("Choose how BOOP opens", "Automatic for this device", "Wall / tablet", "Phone launcher", "Shield / TV Home"):
        assert label not in profile
    assert "Voice settings" in profile
    assert "room" in profile
    assert "ACTION_HOME_SETTINGS" in profile or "ROLE_HOME" in profile


def test_real_identity_policy_rejects_cross_body_or_unknown_packages():
    source = text("source/BoopAppIdentity.java")
    harness = '''package com.boop.alpha1;
public class SplitIdentityTest {
  public static void main(String[] args) {
    if (BoopAppIdentity.isShield("com.boop.alpha1")) throw new AssertionError("Wall became TV");
    if (!BoopAppIdentity.isShield("com.boop.shieldoverlay")) throw new AssertionError("Shield became Wall");
    for (String invalid : new String[]{null,"", "com.boop.unified.wall", "com.boop.unified.shield", "example.other"}) {
      try { BoopAppIdentity.isShield(invalid); throw new AssertionError("Unknown identity accepted: "+invalid); }
      catch (IllegalArgumentException expected) { }
    }
    System.out.println("7 installed-identity cases passed");
  }
}'''
    with tempfile.TemporaryDirectory() as d:
        p = Path(d)
        (p / "BoopAppIdentity.java").write_text(source)
        (p / "SplitIdentityTest.java").write_text(harness)
        subprocess.run(["javac", "-d", d, str(p / "BoopAppIdentity.java"), str(p / "SplitIdentityTest.java")], check=True)
        subprocess.run(["java", "-cp", d, "com.boop.alpha1.SplitIdentityTest"], check=True)


def test_native_checkpoint_does_not_depend_on_expiring_ci_artifacts():
    import json
    import re
    baseline = json.loads(text("split/v206-native-baseline.json"))
    assert baseline["source"] == BASE
    assert baseline["apkSha256"] == "b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca"
    assert baseline["signerSha256"] == text("shield-overlay/signing/boop-dev-cert-sha256.txt").strip()
    assert len(baseline["nativeSha256"]) == 16
    assert all(re.fullmatch(r"[0-9a-f]{64}", value) for value in baseline["nativeSha256"].values())
    assert "run-id: 35099151524" not in text(".github/workflows/build-wall-shield-split.yml")
