from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class PairingGateCiContractTest(unittest.TestCase):
    def read(self, relative):
        return (ROOT / relative).read_text(encoding="utf-8")

    def test_shield_apk_inspection_locks_package_sdk_and_permission_boundary(self):
        workflow = self.read(".github/workflows/build-shield-overlay-poc.yml")
        for required in (
            "package: name='com.boop.shieldoverlay'",
            "targetSdkVersion:'36'",
            "android.permission.SYSTEM_ALERT_WINDOW",
            "android.permission.FOREGROUND_SERVICE",
            "android.permission.FOREGROUND_SERVICE_SPECIAL_USE",
            "android.permission.INTERNET",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.permission.RECORD_AUDIO",
            "android.permission.RECEIVE_BOOT_COMPLETED",
            "android.permission.BIND_ACCESSIBILITY_SERVICE",
        ):
            self.assertIn(required, workflow)

    def test_wall_apk_inspection_locks_package_sdk_and_pairing_activity(self):
        workflow = self.read(".github/workflows/build-apk.yml")
        for required in (
            "package: name='com.boop.alpha1'",
            "targetSdkVersion:'36'",
            "ShieldPairingActivity",
            "dump xmltree",
        ):
            self.assertIn(required, workflow)


if __name__ == "__main__":
    unittest.main()
