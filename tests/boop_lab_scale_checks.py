"""Host-only checks of verbatim Lab callback bodies. No Android or graphics runtime."""
from pathlib import Path
import hashlib
import os
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]


def run_lab_checks(activity: Path, engine: Path) -> None:
    text = activity.read_text(encoding='utf-8')
    class_at = text.index('public final class BoopCanonicalAnimationActivity ')
    fields_at = text.index('{', class_at) + 1
    fields_end = text.index('    @Override public void onCreate(', fields_at)
    methods_at = text.index('    @Override protected void onResume()')
    methods_end = text.rfind('\n}')
    assert methods_end > methods_at, 'Lab callback block is incomplete'
    fields = text[fields_at:fields_end]
    methods = text[methods_at:methods_end]
    for anchor in ['void onResume()', 'void onPause()',
                   'void onWindowFocusChanged(', 'void updateLoop()', 'void doFrame(']:
        assert methods.count(anchor) == 1, 'Expected one actual Lab callback: ' + anchor
    template = (ROOT / 'tests/java/BoopLabScaleHarness.java.in').read_text(encoding='utf-8')
    for marker in ['// PRODUCTION_FIELDS', '// PRODUCTION_CALLBACKS']:
        assert template.count(marker) == 1, 'Ambiguous host harness marker: ' + marker
    program = template.replace('// PRODUCTION_FIELDS', fields).replace('// PRODUCTION_CALLBACKS', methods)
    home = os.environ.get('JAVA_HOME')
    def tool(name):
        found = str(Path(home) / 'bin' / (name + ('.exe' if os.name == 'nt' else ''))) if home else shutil.which(name)
        assert found, 'Java test tool is unavailable: ' + name
        return found
    names = ['EyeMotion.java', 'EyeCatalogue.java', 'ProductionAnimationController.java',
             'AnimationClock.java', 'SignMotion.java', 'FreddieMotion.java']
    print('Lab callback source:', activity.relative_to(ROOT),
          'SHA256=' + hashlib.sha256(activity.read_bytes()).hexdigest(), flush=True)
    with tempfile.TemporaryDirectory(prefix='boop-lab-scale-') as out:
        harness = Path(out) / 'BoopLabScaleHarness.java'
        harness.write_text(program, encoding='utf-8')
        subprocess.run([tool('javac'), '-encoding', 'UTF-8', '-d', out,
                        *[str(engine / name) for name in names], str(activity.parent / "AnimationReviewTimeline.java"), str(harness)], check=True, timeout=90)
        subprocess.run([tool('java'), '-cp', out, 'BoopLabScaleHarness'], check=True, timeout=30)
