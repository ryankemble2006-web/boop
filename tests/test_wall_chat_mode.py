"""Native relay configuration and wiring contracts; the real protocol runs on JVM.

Mutations caught: accepting cleartext/credential-bearing URLs, missing setup guard,
or omitting protocol tests from the materialized Android unit suite.
"""
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]

class RelayConfigurationTest(unittest.TestCase):
    def test_config_is_https_only_and_missing_values_are_safe(self):
        source = ROOT / 'source/OpenAiRelayConfig.java'
        self.assertTrue(source.is_file(), 'native relay configuration is not implemented')
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root/'BuildConfig.java').write_text('package com.boop.alpha1; final class BuildConfig { '
                'static final String BOOP_RELAY_URL=""; static final String BOOP_RELAY_TOKEN=""; }')
            (root/'ConfigHarness.java').write_text(r'''
package com.boop.alpha1;
public class ConfigHarness {
  static void check(boolean value) { if (!value) throw new AssertionError("configuration boundary"); }
  public static void main(String[] args) {
    check(!OpenAiRelayConfig.fromBuildConfig().configured());
    check(!new OpenAiRelayConfig(null, null).configured());
    check(!new OpenAiRelayConfig("https://example.invalid/chat", "").configured());
    for (String url : new String[]{"http://example.invalid/chat", "file:///tmp/a", "broken",
        "https://u:p@example.invalid/chat", "https://example.invalid/?token=secret",
        "https://example.invalid/#f", "https://api.openai.com/v1/responses"}) {
      check(!new OpenAiRelayConfig(url, "test-only-token").configured());
    }
    check(!new OpenAiRelayConfig("https://example.invalid/chat", "a\nb").configured());
    check(!new OpenAiRelayConfig("https://example.invalid/chat", "sk-" + "x".repeat(40)).configured());
    OpenAiRelayConfig c = new OpenAiRelayConfig(" https://example.invalid/chat ", " test-only-token ");
    check(c.configured()); check(c.url().equals("https://example.invalid/chat"));
    check(c.token().equals("test-only-token"));
    System.out.println("BOOP_RELAY_CONFIG_PASS");
  }
}''')
            compile = subprocess.run(['javac', '--release', '17', '-d', tmp, str(source),
                         str(root/'BuildConfig.java'), str(root/'ConfigHarness.java')], capture_output=True, text=True)
            self.assertEqual(compile.returncode, 0, compile.stderr)
            run = subprocess.run(['java', '-cp', tmp, 'com.boop.alpha1.ConfigHarness'], capture_output=True, text=True)
            self.assertEqual(run.returncode, 0, run.stderr)
            self.assertIn('BOOP_RELAY_CONFIG_PASS', run.stdout)

    def test_real_protocol_harness_exists(self):
        self.assertTrue((ROOT/'source/OpenAiRelayAssistantClient.java').is_file(), 'native relay client missing')
        self.assertTrue((ROOT/'tests/java/OpenAiRelayAssistantClientHarness.java').is_file())

class RelayRoutingTest(unittest.TestCase):
    def test_all_local_outcomes_and_all_modes(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            (root/'HomeAssistantClient.java').write_text('package com.boop.alpha1; class HomeAssistantClient { CommandOutcome process(String s){throw new AssertionError();}}')
            (root/'HomeAssistantGeneralAssistantClient.java').write_text('package com.boop.alpha1; class HomeAssistantGeneralAssistantClient { CommandOutcome ask(String s){throw new AssertionError();}}')
            args = ['javac','--release','17','-d',tmp]
            args += [str(ROOT/'source'/n) for n in ('CommandOutcome.java','BoopCommandRouter.java','BoopChatMode.java')]
            args += [str(root/'HomeAssistantClient.java'),str(root/'HomeAssistantGeneralAssistantClient.java'),str(ROOT/'tests/java/BoopNativeRoutingHarness.java')]
            compiled = subprocess.run(args,capture_output=True,text=True)
            self.assertEqual(0,compiled.returncode,compiled.stderr)
            run = subprocess.run(['java','-cp',tmp,'com.boop.alpha1.BoopNativeRoutingHarness'],capture_output=True,text=True)
            self.assertEqual(0,run.returncode,run.stderr)
            self.assertIn('BOOP_NATIVE_ROUTING_PASS',run.stdout)

    def test_native_choice_is_exercised_by_the_real_emulator_gate(self):
        smoke = (ROOT/'scripts/smoke-wall-chat-mode.py').read_text()
        self.assertIn("tap_description(root, 'Use Native Chat')", smoke)
        self.assertIn("assert_saved('native_chat')", smoke)
        self.assertIn("assert_selected_mode(root, 'Native Chat')", smoke)

    def test_materialized_native_route_keeps_existing_response_path(self):
        import importlib.util
        spec = importlib.util.spec_from_file_location('chat_patch', ROOT/'scripts/patch-wall-chat-mode.py')
        patch = importlib.util.module_from_spec(spec); spec.loader.exec_module(patch)
        materialized = patch.patch_text((ROOT/'source/MainActivity.java').read_text())
        self.assertIn('OpenAiRelayConfig.fromBuildConfig()', materialized)
        self.assertIn('relayAssistant::ask', materialized)
        self.assertIn('BoopChatMode.NATIVE_CHAT', materialized)
        self.assertIn('relayAssistant.close()', materialized)
        self.assertIn('speakThenOpenAssistantFollowUp(LocalReply.forOutcome(outcome));', materialized)
        self.assertIn('openFreeChat(transcript);', materialized)
        self.assertEqual(materialized, patch.patch_text(materialized))
        script = (ROOT/'scripts/materialize-android.sh').read_text()
        self.assertIn('OpenAiRelayAssistantClientHarness.java', script)

class NativeRepliesTest(unittest.TestCase):
    def test_native_failures_use_existing_speech_mapper_without_house_auth_side_effects(self):
        with tempfile.TemporaryDirectory() as tmp:
            args = ['javac', '--release', '17', '-d', tmp]
            args += [str(ROOT / 'source' / p) for p in ('CommandOutcome.java', 'LocalReply.java')]
            args += [str(ROOT/'tests/java/BoopNativeReplyHarness.java')]
            compiled = subprocess.run(args, capture_output=True, text=True)
            self.assertEqual(0, compiled.returncode, compiled.stderr)
            run = subprocess.run(['java', '-cp', tmp, 'com.boop.alpha1.BoopNativeReplyHarness'], capture_output=True, text=True)
            self.assertEqual(0, run.returncode, run.stderr)
            self.assertIn('BOOP_NATIVE_REPLIES_PASS', run.stdout)

if __name__ == '__main__':
    unittest.main()
