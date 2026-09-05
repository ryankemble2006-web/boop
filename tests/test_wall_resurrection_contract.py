from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]


class WallResurrectionContractTest(unittest.TestCase):
    def test_exact_natural_wake_collection_is_preserved(self):
        raw = (ROOT / "wake-assets/boop-kws/keywords_raw.txt").read_text(encoding="utf-8")
        compiled = (ROOT / "wake-assets/boop-kws/keywords.txt").read_text(encoding="utf-8")
        raw_lines = [line.strip() for line in raw.splitlines() if line.strip()]
        compiled_lines = [line.strip() for line in compiled.splitlines() if line.strip()]
        self.assertEqual(33, len(raw_lines))
        self.assertEqual(33, len(compiled_lines))
        for phrase in ("BOOP", "HEY BOOP", "EY BOOP", "HELLO BOOP", "OI BOOP",
                       "WAKE UP BOOP", "ARE YOU THERE BOOP", "EXCUSE ME BOOP"):
            self.assertTrue(any(line.startswith(phrase + " :") for line in raw_lines), phrase)
        self.assertTrue(all(line.endswith("@BOOP") for line in compiled_lines))

    def test_required_smart_paths_and_tap_fallback_are_present(self):
        main = (ROOT / "source/MainActivity.java").read_text(encoding="utf-8")
        ha = (ROOT / "source/HomeAssistantClient.java").read_text(encoding="utf-8")
        for required in ("BoopWakeWordController", "BoopCommandRouter",
                         "HomeAssistantGeneralAssistantClient", "startListening"):
            self.assertIn(required, main)
        self.assertIn("HomeAssistantDirectMediaClient", ha)

    def test_timed_routines_are_absent(self):
        source = "\n".join(
            path.read_text(encoding="utf-8")
            for path in (ROOT / "source").rglob("*.java")
        )
        for forbidden in ("BoopTimedRoutineFlow", "Once or recurring?",
                          "Recurring routines need setup first"):
            self.assertNotIn(forbidden, source)

    def test_resurrection_build_identity_is_monotonic(self):
        gradle = (ROOT / "source/app-build.gradle").read_text(encoding="utf-8")
        self.assertIn("versionCode 29", gradle)
        self.assertIn('versionName "0.4.9-alpha6.5.6-wall"', gradle)


if __name__ == "__main__":
    unittest.main()
