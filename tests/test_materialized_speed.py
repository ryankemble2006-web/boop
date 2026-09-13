"""Build-time source identity gate. Requires materialization; never launches Android."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ENGINE = ROOT / 'unified/animation/java/com/boop/eyes'
BUILT = ROOT / 'boop-build/BOOP-Alpha1/animation-lib/src/main'


def test_materialized_motion_and_bindings_are_the_reviewed_source():
    names = ['AnimationClock.java', 'AnimationSpeedPreferences.java',
             'AnimationSpeedBinding.java', 'ProductionAnimationController.java',
             'EyeColourBinding.java', 'CanonicalEyeRenderer.java',
             'EyeMotion.java', 'EyeCatalogue.java', 'SignMotion.java']
    for name in names:
        path = BUILT / 'java/com/boop/eyes' / name
        assert path.is_file(), 'Missing materialized speed source: ' + str(path)
        assert path.read_bytes() == (ENGINE / name).read_bytes(), 'Materialization replaced reviewed source: ' + name
    for name in ['eyes.vert', 'eyes.frag', 'catalogue.json']:
        assert (BUILT / 'assets' / name).read_bytes() == (ROOT / 'unified/animation/assets' / name).read_bytes(), 'Materialized animation asset changed: ' + name
    for target, source in [
        ('boopApprovedEyes.png', 'unified/assets/boop-eyes/boopApprovedEyes.png'),
        ('boop-notification-hands.png', 'unified/assets/boop-notifications/boop-yellow-hands-approved.png'),
    ]:
        assert (BUILT / 'assets' / target).read_bytes() == (ROOT / source).read_bytes(), 'Materialized approved master changed: ' + target
    print('Materialized speed, colour, authored-motion and approved-master bytes match reviewed GitHub source')


if __name__ == '__main__':
    test_materialized_motion_and_bindings_are_the_reviewed_source()
