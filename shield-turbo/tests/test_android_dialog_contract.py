"""Android API misuse regression, not a visual or layout test.

AlertDialog message and item-list content are mutually exclusive. Test builder
calls only; do not launch, render, inspect hierarchy, screenshot or judge focus.
Reference: https://developer.android.com/develop/ui/views/components/dialogs
"""
from pathlib import Path
import re
import unittest

SOURCE = Path(__file__).resolve().parents[1] / 'app/src/main/java/com/boop/shieldturbo'


def conflicting_builders(source):
    return [chain for chain in re.findall(
        r'AlertDialog\.Builder\(.*?(?:\.show\(\)|\.create\(\))', source, re.S
    ) if re.search(r'\.setMessage\s*\(', chain)
        and re.search(r'\.(?:setItems|setAdapter|setSingleChoiceItems|setMultiChoiceItems)\s*\(', chain)]


class AndroidDialogContractTest(unittest.TestCase):
    def test_checker_detects_both_call_orders(self):
        for calls in [
            '.setMessage(message).setItems(items, listener)',
            '.setItems(items, listener).setMessage(message)',
        ]:
            self.assertEqual(1, len(conflicting_builders('AlertDialog.Builder(context)' + calls + '.show()')))

    def test_checker_allows_separate_message_and_choice_dialogs(self):
        source = ('AlertDialog.Builder(context).setMessage(message).show()\n'
                  'AlertDialog.Builder(context).setItems(items, listener).show()')
        self.assertEqual([], conflicting_builders(source))

    def test_startup_actions_do_not_compete_with_message_content(self):
        source = (SOURCE / 'startup/StartupManagerActivity.kt').read_text()
        self.assertEqual([], conflicting_builders(source),
                         'A message replaces the startup action list; use list content alone.')


if __name__ == '__main__':
    unittest.main(verbosity=2)
