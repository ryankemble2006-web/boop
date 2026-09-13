"""Numeric timing and wiring checks. No screenshots or visual judgement in CI."""
from pathlib import Path
import os
import shutil
import subprocess
import tempfile

ROOT = Path(__file__).resolve().parents[1]
ENGINE = ROOT / 'unified/animation/java/com/boop/eyes'
BASELINE = 'a901c1e9f31e55c710e31ac7ff4f5924c9769d56'


def test_exact_baseline_and_speed_clock():
    assert (ENGINE / 'AnimationClock.java').is_file(), 'BOOP speed clock is not implemented'
    assert 'setSpeed(' in (ENGINE / 'ProductionAnimationController.java').read_text(), 'Production speed is missing'
    baseline = subprocess.check_output(['git', 'show', BASELINE + ':unified/animation/java/com/boop/eyes/ProductionAnimationController.java'], cwd=ROOT).decode('utf-8')
    baseline = baseline.replace('ProductionAnimationController', 'V156ProductionAnimationController')
    home = os.environ.get('JAVA_HOME')
    def tool(name):
        return str(Path(home) / 'bin' / (name + ('.exe' if os.name == 'nt' else ''))) if home else shutil.which(name)
    with tempfile.TemporaryDirectory(prefix='boop-speed-') as out:
        old = Path(out) / 'V156ProductionAnimationController.java'
        old.write_text(baseline, encoding='utf-8')
        files = [ENGINE / name for name in ['EyeMotion.java', 'EyeCatalogue.java', 'ProductionAnimationController.java', 'AnimationClock.java']]
        subprocess.run([tool('javac'), '-encoding', 'UTF-8', '-d', out, *map(str, files), str(old), str(ROOT / 'tests/java/BoopAnimationSpeedHarness.java')], check=True)
        subprocess.run([tool('java'), '-cp', out, 'com.boop.eyes.BoopAnimationSpeedHarness'], check=True)


def test_every_canonical_surface_uses_saved_speed_without_rewriting_clips():
    files = ['source/BoopCanonicalFaceView.java', 'source/BoopNotificationPuppetView.java',
             'source/BoopCanonicalAnimationActivity.java',
             'unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java']
    for path in files:
        text = (ROOT / path).read_text()
        assert 'AnimationSpeedBinding.install' in text, path + ' does not use saved BOOP speed'
    wall = (ROOT / files[0]).read_text()
    assert 'sleepHideDeadlineMs' in wall and 'realDelayUntil' in wall, 'Sleep hide must follow scaled motion, including changes mid-clip'
    assert 'handler.postDelayed(sleepHide, duration)' not in wall
    notice = (ROOT / files[1]).read_text()
    assert 'AnimationClock' in notice, 'Notification hands and eyes need the same scaled time'
    lab = (ROOT / files[2]).read_text()
    assert 'speedMultiplier' in lab, 'Embedded Lab must scale its existing delta, not double-scale its controller'
    assert 'controller.setSpeed(' not in lab, 'Do not apply the Lab speed twice'
    prefs = (ENGINE / 'AnimationSpeedPreferences.java').read_text()
    assert 'animation_speed' in prefs and '1f' in prefs
    assert 'hue_degrees' not in prefs and 'speech_rate' not in prefs
    binding = (ENGINE / 'AnimationSpeedBinding.java').read_text()
    assert 'registerOnSharedPreferenceChangeListener' in binding
    assert 'unregisterOnSharedPreferenceChangeListener' in binding
    screen = (ROOT / 'source/BoopAppearanceActivity.java').read_text()
    assert 'AnimationSpeedPreferences.save' in screen
    for label in ['0.5x', '1x', '1.5x', '2x']:
        assert label in screen, label


def test_irregular_rates_one_shot_boundaries_and_notification_pose_bits():
    home = os.environ.get('JAVA_HOME')
    def tool(name):
        return str(Path(home) / 'bin' / (name + ('.exe' if os.name == 'nt' else ''))) if home else shutil.which(name)
    with tempfile.TemporaryDirectory(prefix='boop-speed-edges-') as out:
        references = []
        for name in ['ProductionAnimationController', 'SignMotion']:
            text = subprocess.check_output(['git', 'show', BASELINE + ':unified/animation/java/com/boop/eyes/' + name + '.java'], cwd=ROOT).decode('utf-8')
            target = Path(out) / ('V156' + name + '.java')
            target.write_text(text.replace(name, 'V156' + name), encoding='utf-8')
            references.append(target)
        names = ['EyeMotion.java', 'EyeCatalogue.java', 'ProductionAnimationController.java', 'AnimationClock.java', 'SignMotion.java']
        files = [ENGINE / name for name in names] + references + [ROOT / 'tests/java/BoopAnimationSpeedEdgeHarness.java']
        subprocess.run([tool('javac'), '-encoding', 'UTF-8', '-d', out, *map(str, files)], check=True)
        subprocess.run([tool('java'), '-cp', out, 'com.boop.eyes.BoopAnimationSpeedEdgeHarness'], check=True)


def test_authored_motion_still_matches_accepted_v156_bytes():
    for name in ['EyeMotion.java', 'EyeCatalogue.java', 'SignMotion.java']:
        expected = subprocess.check_output(['git', 'show', BASELINE + ':unified/animation/java/com/boop/eyes/' + name], cwd=ROOT)
        assert (ENGINE / name).read_bytes() == expected, 'Authored motion changed: ' + name


def test_signed_apk_requires_timing_and_materialized_source_checks():
    workflow = (ROOT / '.github/workflows/build-boop-unified.yml').read_text(encoding='utf-8')
    gate = ('      - name: Test exact BOOP animation speed before signing\n'
            '        run: |\n'
            '          python tests/test_animation_speed.py\n'
            '          python tests/test_materialized_speed.py\n')
    assert gate in workflow, 'Signed APK build does not require the exact speed and materialized-source gates'
    assert workflow.index('      - name: Materialize one APK') < workflow.index(gate)
    assert workflow.index(gate) < workflow.index('      - name: Prepare permanent BOOP signer')
    assert 'continue-on-error:' not in workflow, 'Timing failures must prevent signing'


if __name__ == '__main__':
    for name, test in list(globals().items()):
        if name.startswith('test_') and callable(test):
            test()
            print('PASS', name)
