from pathlib import Path
import subprocess, tempfile
r=Path(__file__).resolve().parents[1]
timeline=r/'source/AnimationReviewTimeline.java'
assert timeline.exists(), 'Current felt lab needs a seekable, pausable review timeline'
with tempfile.TemporaryDirectory() as d:
 subprocess.run(['javac','-d',d,str(r/'unified/animation/java/com/boop/eyes/EyeMotion.java'),str(r/'unified/animation/java/com/boop/eyes/EyeCatalogue.java'),str(timeline),str(r/'tests/java/FeltReviewHarness.java')],check=True)
 subprocess.run(['java','-cp',d,'com.boop.alpha1.FeltReviewHarness'],check=True)
ui=(r/'source/BoopCanonicalAnimationActivity.java').read_text()
appearance=(r/'source/BoopAppearanceActivity.java').read_text()
assert 'Felt animation lab' in appearance and 'BoopCanonicalAnimationActivity.class' in appearance
for marker in ('Half blink','Animation position','timeline.halfBlink()','timeline.seek(','timeline.step(','EyeColourBinding.install','onSaveInstanceState','Show controls'):
 assert marker in ui, marker
assert 'AnimationSpeedPreferences.save' not in ui
assert 'FeltColourPreferences.save' not in ui
assert 'new CanonicalEyeRenderer' in ui
print('Lab entry, saved colour binding, stage controls and no persistent setting writes verified')
