"""Standalone packaging/dependency checks, never visual acceptance."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET
ROOT=Path(__file__).resolve().parents[1]
A="{http://schemas.android.com/apk/res/android}"
class LauncherSplitTest(unittest.TestCase):
    def test_standalone_manifest_exists(self):
        self.assertTrue((ROOT/"shield-launcher/app/src/main/AndroidManifest.xml").is_file(), "Standalone launcher manifest is not implemented")
    def test_independent_identity(self):
        f=ROOT/"shield-launcher/app/build.gradle"
        self.assertTrue(f.is_file(), "Independent build is not implemented")
        self.assertIn("applicationId 'com.boop.shieldlauncher'", f.read_text())
    def test_only_launcher_components_and_permissions(self):
        f=ROOT/"shield-launcher/app/src/main/AndroidManifest.xml"
        self.assertTrue(f.is_file(), "Standalone manifest is not implemented")
        root=ET.parse(f).getroot()
        names={e.get(A+"name", "") for e in root.iter()}
        self.assertIn("com.boop.shieldhome.ShieldLauncherActivity",names)
        self.assertIn("com.boop.shieldhome.ShieldStartupManagerActivity",names)
        self.assertIn("android.intent.category.HOME", names)
        self.assertIn("android.intent.category.LEANBACK_LAUNCHER", names)
        for denied in ("RECORD_AUDIO", "SYSTEM_ALERT_WINDOW", "BIND_VOICE_INTERACTION", "com.boop.alpha1", "com.boop.launcher", "com.boop.shieldoverlay"):
            self.assertFalse(any(denied in name for name in names), denied)
        self.assertEqual(root.find("application").get(A+"allowBackup"), "false")
    def test_shield_settings_fallback_is_private_and_declared(self):
        root=ET.parse(ROOT/"shield-launcher/app/src/main/AndroidManifest.xml").getroot()
        activities={e.get(A+"name"):e for e in root.findall("application/activity")}
        name="com.boop.shieldhome.ShieldAccessibilityRouteActivity"
        self.assertIn(name,activities,"The Shield firmware settings router must travel with the standalone app")
        self.assertEqual(activities[name].get(A+"exported"),"false")
        self.assertEqual(activities[name].get(A+"noHistory"),"true")
    def test_materialized_dependencies_have_no_unified_or_ha(self):
        base=ROOT/"work/shield-launcher-src/java"
        self.assertTrue(base.is_dir(), "Standalone sources have not been materialized")
        files=list(base.rglob("*.java")); self.assertGreater(len(files),40)
        for f in files:
            text=f.read_text(encoding="utf-8")
            for forbidden in ("import com.boop.alpha1", "com.boop.alpha1.Boop", "import com.boop.shieldoverlay", "HomeAssistantSession", "HomeAssistantAuthClient", "SecureCredentialStore", "sherpa.onnx"):
                self.assertNotIn(forbidden,text,str(f))
if __name__ == "__main__": unittest.main()
