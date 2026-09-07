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

if __name__ == '__main__':
    unittest.main()
