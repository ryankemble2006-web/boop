"""Nonvisual boot CLEAN START security contract."""
from pathlib import Path
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "app/src/main/java/com/boop/shieldturbo/cleanstart"
MANIFEST = ROOT / "app/src/main/AndroidManifest.xml"
ANDROID = "{http://schemas.android.com/apk/res/android}"


class CleanStartBootContractTest(unittest.TestCase):
    def test_boot_components_exist_and_are_private(self):
        receiver_source = SOURCE / "CleanStartBootReceiver.kt"
        service_source = SOURCE / "CleanStartJobService.kt"
        scheduler_source = SOURCE / "CleanStartScheduler.kt"
        self.assertTrue(receiver_source.exists(), "missing CLEAN START boot receiver")
        self.assertTrue(service_source.exists(), "missing CLEAN START one-shot job")
        self.assertTrue(scheduler_source.exists(), "missing CLEAN START bounded scheduler")

        manifest = ET.parse(MANIFEST).getroot()
        permissions = {p.get(ANDROID + "name") for p in manifest.findall("uses-permission")}
        self.assertIn("android.permission.RECEIVE_BOOT_COMPLETED", permissions)
        self.assertNotIn("android.permission.FOREGROUND_SERVICE", permissions)
        app = manifest.find("application")
        receiver = next(r for r in app.findall("receiver") if r.get(ANDROID + "name") == ".cleanstart.CleanStartBootReceiver")
        self.assertEqual("false", receiver.get(ANDROID + "exported"))
        service = next(s for s in app.findall("service") if s.get(ANDROID + "name") == ".cleanstart.CleanStartJobService")
        self.assertEqual("false", service.get(ANDROID + "exported"))
        self.assertEqual("android.permission.BIND_JOB_SERVICE", service.get(ANDROID + "permission"))

    def test_boot_job_is_bounded_and_never_requests_new_adb_approval(self):
        scheduler = (SOURCE / "CleanStartScheduler.kt").read_text()
        job = (SOURCE / "CleanStartJobService.kt").read_text()
        self.assertIn("MAX_ATTEMPTS = 3", scheduler)
        self.assertNotIn("setPeriodic", scheduler)
        self.assertNotIn("setPersisted(true)", scheduler)
        self.assertIn("withTrustedAdb", job)
        self.assertNotIn("withAdb(::approval", job)
        self.assertNotIn("startForeground", job)


if __name__ == "__main__":
    unittest.main(verbosity=2)
