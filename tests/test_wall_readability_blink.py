"""Regression tests for the approved larger notice and awake-only idle blink.

Mutation targets: fixed/out-of-range blink delays, failure to reopen the eyes,
resetting the existing idle timer, or dropping the notice's accessible text spans.
No Android framework mock is used; rendering is checked by the emulator suite.
"""
import importlib.util
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]


def load_module(path, name):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


HARNESS = r'''
package com.boop.alpha1;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
public final class BlinkHarness {
    private static void check(boolean ok, String reason) {
        if (!ok) throw new AssertionError(reason);
    }
    public static void main(String[] args) {
        Random random = new Random(71510L);
        Set<Long> delays = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            long delay = BoopIdleBlink.nextDelayMillis(random);
            check(delay >= 3000 && delay <= 7000, "blink delay outside 3-7 seconds");
            delays.add(delay);
        }
        check(delays.size() > 20, "blink timing must not be mechanical");
        check(BoopIdleBlink.openness(0f) == 1f, "blink must start open");
        check(BoopIdleBlink.openness(1f) == 1f, "blink must end open");
        check(BoopIdleBlink.openness(0.4f) <= 0.08f, "eyelids must actually close");
        check(BoopIdleBlink.openness(-1f) == 1f, "negative progress must stay open");
        check(BoopIdleBlink.openness(2f) == 1f, "finished progress must stay open");
        check(BoopIdleBlink.openness(Float.NaN) == 1f, "invalid progress must stay open");
        for (int i = 0; i <= 100; i++) {
            float value = BoopIdleBlink.openness(i / 100f);
            check(value >= 0.05f && value <= 1f, "blink geometry out of bounds");
        }
        System.out.println("BOOP_IDLE_BLINK_POLICY_PASS");
    }
}
'''


class WallReadabilityBlinkTest(unittest.TestCase):
    def test_irregular_timing_and_complete_blink(self):
        source = ROOT / 'source/BoopIdleBlink.java'
        self.assertTrue(source.is_file(), 'awake idle blink policy is not implemented')
        with tempfile.TemporaryDirectory() as temp:
            harness = Path(temp) / 'BlinkHarness.java'
            harness.write_text(HARNESS)
            subprocess.run(['javac', '-d', temp, str(source), str(harness)], check=True,
                           capture_output=True, text=True, timeout=30)
            result = subprocess.run(['java', '-cp', temp, 'com.boop.alpha1.BlinkHarness'],
                                    check=True, capture_output=True, text=True, timeout=30)
            self.assertIn('BOOP_IDLE_BLINK_POLICY_PASS', result.stdout)

    def test_notice_uses_text_spans_not_a_custom_background_toast(self):
        path = ROOT / 'source/BoopFreeChatNotice.java'
        self.assertTrue(path.is_file(), 'larger paste instructions are not implemented')
        text = path.read_text()
        self.assertIn('RelativeSizeSpan(1.5f)', text)
        self.assertIn('StyleSpan(Typeface.BOLD)', text)
        self.assertIn('Question copied.\\nPaste into Free Chat.', text)
        self.assertIn('Toast.makeText', text)
        self.assertNotIn('setView(', text)
        self.assertNotIn('SYSTEM_ALERT_WINDOW', text)
        self.assertNotIn('startActivity', text)

    def test_effective_patch_is_idempotent_and_does_not_change_sleep_timer(self):
        path = ROOT / 'scripts/patch-wall-idle-blink.py'
        self.assertTrue(path.is_file(), 'idle blink is not connected to the actual face')
        blink = load_module(path, 'wall_idle_blink')
        chat = load_module(ROOT / 'scripts/patch-wall-chat-mode.py', 'wall_chat_mode')
        before = chat.patch_text((ROOT / 'source/MainActivity.java').read_text())
        after = blink.patch_main(before)
        self.assertEqual(after, blink.patch_main(after))
        start = '    private void scheduleFaceIdle() {'
        stop = '    private void startAssistantThinking() {'
        self.assertEqual(before.split(start)[1].split(stop)[0],
                         after.split(start)[1].split(stop)[0])
        for state in ('activityInForeground', 'presenceState.isIdleBlack()', 'listening',
                      'thinking', 'tts.isSpeaking()', 'voiceSettingsOpen', 'chatModeOpen',
                      'faceTouchActive'):
            self.assertIn(state, after)
        face = (ROOT / 'source/BoopFaceView.java').read_text()
        patched = blink.patch_face(face)
        self.assertEqual(patched, blink.patch_face(patched))
        self.assertIn('onDetachedFromWindow', patched)
        self.assertIn('onWindowVisibilityChanged', patched)
        self.assertIn('onWindowFocusChanged', patched)
        self.assertNotIn('scheduleFaceIdle()', patched)
        self.assertNotIn('wakeFaceForInteraction()', patched)
        with self.assertRaises(ValueError):
            blink.patch_face('not the BOOP face')
        with self.assertRaises(ValueError):
            blink.patch_main('not the BOOP activity')


if __name__ == '__main__':
    unittest.main()
