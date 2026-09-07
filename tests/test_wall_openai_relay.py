"""Build/secret invariants. Real protocol behavior is covered by JVM and Worker tests.

Mutations caught: provider credentials in Android, a missing private build field,
secret logging, a missing normal relay gate, or destructive mode replacement.
"""
from pathlib import Path
import re
import unittest

ROOT = Path(__file__).resolve().parents[1]

class RelayBuildBoundaryTest(unittest.TestCase):
    def test_private_build_fields_have_empty_defaults_and_safe_java_escaping(self):
        gradle = (ROOT/'source/app-build.gradle').read_text()
        self.assertIn("System.getenv('BOOP_RELAY_URL')", gradle)
        self.assertIn("System.getenv('BOOP_RELAY_TOKEN')", gradle)
        self.assertIn('buildConfig true', gradle)
        self.assertIn("buildConfigField 'String', 'BOOP_RELAY_URL'", gradle)
        self.assertIn("buildConfigField 'String', 'BOOP_RELAY_TOKEN'", gradle)
        self.assertIn('groovy.json.JsonOutput.toJson', gradle)
        self.assertIn("System.getenv('BOOP_REPOSITORY_PRIVATE') != 'true'", gradle)
        self.assertIn('Configured relay builds require a private repository', gradle)
        self.assertIn("boopRelayUrl = ''", gradle)
        self.assertIn("boopRelayToken = ''", gradle)
        self.assertIn("boopRelayToken.startsWith('sk-')", gradle)

    def test_secrets_are_build_scoped_and_not_logged(self):
        workflow = (ROOT/'.github/workflows/build-boop-wall-free-chat.yml').read_text()
        self.assertEqual(2, workflow.count('BOOP_RELAY_URL: ${{ secrets.BOOP_RELAY_URL }}'))
        self.assertEqual(2, workflow.count('BOOP_RELAY_TOKEN: ${{ secrets.BOOP_RELAY_TOKEN }}'))
        self.assertNotIn('OPENAI_API_KEY:', workflow)
        steps = workflow.split('      - name: ')
        for step in steps:
            if 'secrets.BOOP_RELAY_TOKEN' in step:
                self.assertTrue(step.startswith('Build '), step.splitlines()[0])
                self.assertNotRegex(step, r'(?i)(echo|print|printf).*BOOP_RELAY_(TOKEN|URL)')
        self.assertIn('npm test', workflow)
        self.assertIn('relay/cloudflare', workflow)
        self.assertIn('OpenAiRelayAssistantClientHarness.java', (ROOT/'scripts/materialize-android.sh').read_text())

    def test_provider_keys_and_raw_credentials_never_belong_in_android_or_public_source(self):
        files = list((ROOT/'source').rglob('*')) + list((ROOT/'relay/cloudflare').rglob('*'))
        for path in files:
            if not path.is_file() or path.suffix not in ('.java', '.gradle', '.js', '.json', '.toml', '.md'):
                continue
            text = path.read_text()
            self.assertNotRegex(text, r'sk-(?:proj-)?[A-Za-z0-9_-]{20,}', str(path))
            self.assertNotIn('-----BEGIN PRIVATE KEY-----', text, str(path))
            if path.suffix in ('.java', '.gradle'):
                self.assertNotIn('OPENAI_API_KEY', text, str(path))
        ignore = (ROOT/'.gitignore').read_text()
        for pattern in ('.env', '.dev.vars', '.wrangler/', 'node_modules/'):
            self.assertIn(pattern, ignore)

    def test_legacy_scanner_exception_is_only_the_bare_rejection_prefix(self):
        for source in ('"sk-" + "x"', '"sk-' + 'x' * 40 + '"',
                       'startsWith("sk-' + 'x' * 40 + '")'):
            stripped = source.replace('startsWith("sk-")', '').replace("startsWith('sk-')", '')
            self.assertIn('sk-', stripped)
        self.assertNotIn('sk-', 'token.startsWith("sk-")'.replace('startsWith("sk-")', ''))

    def test_native_route_is_additive_and_has_no_new_permissions(self):
        modes = (ROOT/'source/BoopChatMode.java').read_text()
        for name in ('OPENCODE', 'FREE_CHAT', 'NATIVE_CHAT'):
            self.assertIn(name, modes)
        manifest = (ROOT/'source/AndroidManifest.xml').read_text()
        permissions = re.findall(r'<uses-permission\s+android:name="([^"]+)"', manifest)
        self.assertEqual({'android.permission.INTERNET', 'android.permission.RECORD_AUDIO',
            'android.permission.NEARBY_WIFI_DEVICES'}, set(permissions))

if __name__ == '__main__':
    unittest.main()
