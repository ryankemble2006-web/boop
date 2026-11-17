import re
import sys
import unittest
from pathlib import Path

smali = Path(sys.argv.pop(1)).read_text(encoding='utf-8')
source = Path(sys.argv.pop(1)).read_text(encoding='utf-8')

class BackgroundRecovery(unittest.TestCase):
    def test_background_receiver_cannot_launch_capture_ui(self):
        method = re.search(r'(?ms)^\.method private static reviveSpectrumIfWanted\(Landroid/content/Context;\)V\n.*?^\.end method', smali).group()
        self.assertNotIn('->startActivity', method, 'Background recovery steals foreground playback focus')
        self.assertNotIn('BassCaptureActivity;', method)

    def test_source_receiver_cannot_launch_capture_ui(self):
        self.assertNotIn('context.startActivity(', source)

    def test_permission_and_cleanup_recovery_remain(self):
        self.assertIn('->tryDirectSecureSettingsGrant(', smali)
        self.assertIn('->schedule(', smali)

if __name__ == '__main__':
    unittest.main()
