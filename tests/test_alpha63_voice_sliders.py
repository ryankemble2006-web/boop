import unittest
from pathlib import Path


class Alpha63VoiceSlidersTest(unittest.TestCase):
    def test_voice_settings_is_intercepted_locally_before_house_routing(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        settings_check = main.find('BoopVoiceSettingsIntent.matches(transcript)')
        house_check = main.find('tokenStore.hasConnection()')
        router_call = main.find('commandRouter.process(transcript)')
        self.assertGreaterEqual(settings_check, 0)
        self.assertGreater(house_check, settings_check)
        self.assertGreater(router_call, settings_check)
        self.assertIn('showVoiceSettings()', main)

    def test_overlay_has_pitch_cadence_and_done_controls(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('voiceSettingsOverlay', main)
        self.assertIn('SeekBar pitchSlider', main)
        self.assertIn('SeekBar cadenceSlider', main)
        self.assertIn('"Pitch"', main)
        self.assertIn('"Cadence"', main)
        self.assertIn('"Done"', main)
        self.assertIn('interactionSurface.addView(voiceSettingsOverlay', main)
        self.assertIn('hideVoiceSettings()', main)

    def test_slider_changes_apply_and_persist_through_voice_controller(self):
        controller = Path('source/BoopVoiceController.java').read_text(encoding='utf-8')
        self.assertIn('KEY_PITCH', controller)
        self.assertIn('KEY_SPEECH_RATE', controller)
        self.assertIn('void setPitch(float pitch)', controller)
        self.assertIn('void setSpeechRate(float speechRate)', controller)
        self.assertIn('float pitch()', controller)
        self.assertIn('float speechRate()', controller)
        self.assertIn('tts.setPitch(currentPitch)', controller)
        self.assertIn('tts.setSpeechRate(currentSpeechRate)', controller)
        self.assertIn('putFloat(KEY_PITCH', controller)
        self.assertIn('putFloat(KEY_SPEECH_RATE', controller)

    def test_slider_mapping_is_bounded_for_a_puppet_voice(self):
        tuning_path = Path('source/BoopVoiceTuning.java')
        self.assertTrue(tuning_path.exists(), 'BoopVoiceTuning.java must exist')
        tuning = tuning_path.read_text(encoding='utf-8')
        self.assertIn('MIN_PITCH', tuning)
        self.assertIn('MAX_PITCH', tuning)
        self.assertIn('MIN_RATE', tuning)
        self.assertIn('MAX_RATE', tuning)
        self.assertIn('pitchFromProgress', tuning)
        self.assertIn('rateFromProgress', tuning)
        self.assertIn('progressFromPitch', tuning)
        self.assertIn('progressFromRate', tuning)


if __name__ == '__main__':
    unittest.main()
