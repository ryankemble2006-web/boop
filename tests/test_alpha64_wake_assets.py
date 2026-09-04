import unittest
from pathlib import Path


class Alpha64WakeAssetsTest(unittest.TestCase):
    def test_wake_dependencies_are_pinned(self):
        fetch = Path('scripts/fetch-wake-assets.sh').read_text(encoding='utf-8')
        self.assertIn('SHERPA_VERSION="1.13.7"', fetch)
        self.assertIn('c4ef49e309f24fcee5c106b8a279481aaecaabb078cd37b2cd6e9a62cc8a73c8', fetch)
        self.assertIn('sherpa-onnx-kws-zipformer-gigaspeech-3.3M-2024-01-01-mobile.tar.bz2', fetch)
        self.assertIn('checksum.txt', fetch)

    def test_model_checksum_parser_accepts_upstream_column_order_without_weakening_fail_closed(self):
        fetch = Path('scripts/fetch-wake-assets.sh').read_text(encoding='utf-8')
        self.assertIn('$1 == name {print $2; exit}', fetch)
        self.assertIn('$2 == name {print $1; exit}', fetch)
        self.assertIn('test -n "$MODEL_SHA256"', fetch)
        self.assertIn("sha256sum -c -", fetch)

    def test_only_boop_is_configured_as_the_raw_keyword(self):
        raw = Path('wake-assets/boop-kws/keywords_raw.txt').read_text(encoding='utf-8').strip()
        self.assertEqual('BOOP :1.5 #0.25 @BOOP', raw)

    def test_materializer_places_aar_and_runtime_model_assets(self):
        text = Path('scripts/materialize-android.sh').read_text(encoding='utf-8')
        self.assertIn('bash scripts/fetch-wake-assets.sh "$ROOT/app"', text)
        gradle = Path('source/app-build.gradle').read_text(encoding='utf-8')
        self.assertIn("implementation files('libs/sherpa-onnx-1.13.7.aar')", gradle)
        self.assertIn("implementation 'org.jetbrains.kotlin:kotlin-stdlib:1.7.20'", gradle)


if __name__ == '__main__':
    unittest.main()
