import unittest
from pathlib import Path

# Alpha 6.5.5 physical-whimsy regression gate.

class Alpha655ShakeMuppetTests(unittest.TestCase):
    def test_main_registers_accelerometer_only_while_foreground(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('Sensor.TYPE_ACCELEROMETER', main)
        self.assertIn('sensorManager.registerListener(this, shakeSensor', main)
        self.assertIn('sensorManager.unregisterListener(this)', main)
        self.assertIn('implements RecognitionListener, TextToSpeech.OnInitListener, SensorEventListener', main)

    def test_shake_only_wakes_idle_face_and_does_not_open_mic_or_make_sound(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        start = main.index('private void handleShakeWake')
        end = main.index('\n    private ', start + 1)
        block = main[start:end]
        self.assertIn('presenceState.isIdleBlack()', block)
        self.assertIn('face.playShakeMuppet', block)
        self.assertIn('scheduleFaceIdle();', block)
        self.assertNotIn('startListening', block)
        self.assertNotIn('playWakeAcceptedCue', block)
        self.assertNotIn('ToneGenerator', block)

    def test_face_uses_screen_bounds_without_drawing_a_box(self):
        face = Path('source/BoopFaceView.java').read_text(encoding='utf-8')
        self.assertIn('BoopShakeEyeMotion.pose', face)
        self.assertIn('getWidth()', face)
        self.assertIn('getHeight()', face)
        self.assertNotIn('drawRect(', face)
        self.assertNotIn('drawRoundRect(', face)

    def test_shake_animation_is_short_muppet_whimsy_and_settles(self):
        face = Path('source/BoopFaceView.java').read_text(encoding='utf-8')
        self.assertIn('SHAKE_MUPPET_DURATION_MS', face)
        self.assertIn('ValueAnimator.ofFloat(0f, 1f)', face)
        self.assertIn('shakeMuppetActive = false', face)
        self.assertIn('invalidate();', face)

if __name__ == '__main__':
    unittest.main()
