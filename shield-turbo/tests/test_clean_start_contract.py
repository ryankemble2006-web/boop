"""Functional CLEAN START source contract. No visual/layout assertions."""
from pathlib import Path
import unittest

ROOT = Path(__file__).resolve().parents[1]
ACTIVITY = ROOT / "app/src/main/java/com/boop/shieldturbo/startup/StartupManagerActivity.kt"


class CleanStartManagerContractTest(unittest.TestCase):
    def test_manager_uses_real_clean_start_flow_not_old_background_toggle(self):
        text = ACTIVITY.read_text()
        self.assertIn("CleanStartStore.android(applicationContext)", text)
        self.assertIn("cleanStore.setTarget(app.packageName", text)
        self.assertIn("bridge.stopAndVerify(adb, app.packageName)", text)
        self.assertIn("runCleanStartGroup", text)
        self.assertIn("bridge.resumedPackage(adb)", text)
        self.assertNotIn('actions += "BLOCK STARTUP / KEEP LAUNCHABLE"', text)

    def test_old_ledger_undo_and_manual_launch_are_preserved(self):
        text = ACTIVITY.read_text()
        self.assertIn("ledger.record(app.packageName)", text)
        self.assertIn("bridge.restoreStartup(adb, record.original)", text)
        self.assertIn('"LAUNCH APP NOW"', text)
        self.assertIn('"HARD BLOCK / DISABLE APP"', text)

    def test_runtime_rechecks_safe_user_package_before_stopping(self):
        text = ACTIVITY.read_text()
        self.assertIn("PowerPolicy.safeUserPackage", text)
        self.assertIn("safeTarget(app.packageName)", text)


if __name__ == "__main__":
    unittest.main(verbosity=2)
