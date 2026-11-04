"""Regression gate for the shipped display writer, including permission recovery."""
import re
import sys
import unittest
from pathlib import Path

smali = Path(sys.argv.pop(1)).read_text(encoding='utf-8')
source = Path(sys.argv.pop(1)).read_text(encoding='utf-8')

class DisplayIsolation(unittest.TestCase):
    def test_shipped_launcher_cannot_change_global_display(self):
        self.assertNotRegex(smali, r'wm (?:size|density)|display_(?:size|density)_forced')
        method = re.search(r'(?ms)^\.method public static ensureNative4kDisplayAsync\(.*?^\.end method', smali).group()
        self.assertNotIn('AdbWire', method)
        self.assertNotIn('EXECUTOR', method)

    def test_future_source_cannot_reintroduce_global_display_writes(self):
        self.assertNotRegex(source, r'wm (?:size|density)|display_(?:size|density)_forced')

if __name__ == '__main__':
    unittest.main()
