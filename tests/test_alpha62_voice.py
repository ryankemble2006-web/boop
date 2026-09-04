import unittest
from pathlib import Path


class Alpha62VoiceTest(unittest.TestCase):
    def test_voice_controller_exists_and_stays_local(self):
        controller_path = Path('source/BoopVoiceController.java')
        self.assertTrue(controller_path.exists())
        text = controller_path.read_text(encoding='utf-8')
        self.assertIn('TextToSpeech', text)
        self.assertIn('getVoices()', text)
        self.assertIn('isNetworkConnectionRequired()', text)
        self.assertIn('SharedPreferences', text)
        self.assertIn('setVoice(', text)
        self.assertIn('setPitch(', text)
        self.assertIn('setSpeechRate(', text)
        self.assertNotIn('HomeAssistant', text)
        self.assertNotIn('OpenCode', text)

    def test_voice_change_is_intercepted_before_house_or_mothership(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('private BoopVoiceController voiceController;', main)
        self.assertIn('voiceController.maybeChangeVoice(transcript)', main)
        voice = main.index('voiceController.maybeChangeVoice(transcript)')
        house = main.index('if (!tokenStore.hasConnection())')
        router = main.index('commandRouter.process(transcript)')
        self.assertLess(voice, house)
        self.assertLess(voice, router)

    def test_voice_change_reply_is_spoken_after_voice_switch(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('String voiceReply = voiceController.maybeChangeVoice(transcript);', main)
        self.assertIn('if (voiceReply != null)', main)
        self.assertIn('speak(voiceReply);', main)

    def test_init_applies_saved_or_best_local_english_voice(self):
        main = Path('source/MainActivity.java').read_text(encoding='utf-8')
        self.assertIn('voiceController.initialize(tts, Locale.getDefault())', main)


if __name__ == '__main__':
    unittest.main()
