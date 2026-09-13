"""Behaviour/integrity checks only; no visual CI judgement or real HA writes."""
from pathlib import Path
import hashlib
import json
import os
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def test_colour_state_and_protocol():
    sources = [ROOT / 'source/SharedEyeColourProtocol.java',
               ROOT / 'source/SharedEyeColourState.java']
    assert all(p.is_file() for p in sources), 'Shared colour protocol/state is not implemented'
    home = os.environ.get('JAVA_HOME')
    def tool(name):
        suffix = '.exe' if os.name == 'nt' else ''
        return str(Path(home) / 'bin' / (name + suffix)) if home else shutil.which(name)
    with tempfile.TemporaryDirectory(prefix='boop-colour-') as out:
        subprocess.run([tool('javac'), '-encoding', 'UTF-8', '-d', out,
                        *map(str, sources), str(ROOT / 'tests/java/SharedEyeColourHarness.java')], check=True)
        subprocess.run([tool('java'), '-cp', out, 'com.boop.alpha1.SharedEyeColourHarness'], check=True)


def test_working_wall_hue_and_authored_motion_are_unchanged():
    pins = json.loads((ROOT / 'tests/fixtures/appearance-v156-baseline.json').read_text())
    for path, expected in pins.items():
        data = (ROOT / path).read_bytes()
        if Path(path).suffix != '.png': data = data.replace(b'\r\n', b'\n')
        assert hashlib.sha256(data).hexdigest() == expected, path


def test_colour_binding_covers_existing_renderers_without_replacements():
    paths = ['source/BoopCanonicalFaceView.java', 'source/BoopNotificationPuppetView.java',
             'source/BoopCanonicalAnimationActivity.java',
             'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java']
    for name in paths:
        source = (ROOT / name).read_text(encoding='utf-8')
        assert 'EyeColourBinding.install' in source, name + ' does not observe shared local hue'
        assert source.index('setRenderer(') < source.index('EyeColourBinding.install'), name
    binding = ROOT / 'unified/animation/java/com/boop/eyes/EyeColourBinding.java'
    assert binding.is_file(), 'Lifecycle-safe colour binding is missing'
    text = binding.read_text()
    assert 'registerOnSharedPreferenceChangeListener' in text
    assert 'unregisterOnSharedPreferenceChangeListener' in text
    assert 'onViewDetachedFromWindow' in text and 'onViewAttachedToWindow' in text


if __name__ == '__main__':
    for name, fn in list(globals().items()):
        if name.startswith('test_') and callable(fn):
            fn()
            print('PASS', name)
