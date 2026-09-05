from pathlib import Path
import hashlib
import shutil
import subprocess
import tempfile
import textwrap
import unittest


ROOT = Path(__file__).resolve().parents[1]


class WallResurrectionContractTest(unittest.TestCase):
    def test_wake_marker_requires_first_positive_audio_read(self):
        controller = (ROOT / "source/BoopWakeWordController.java").read_text(encoding="utf-8")
        loop = controller.split("private void audioLoop() {", 1)[1].split("private void beginCommandCapture", 1)[0]
        self.assertEqual(1, controller.count('Log.i(TAG, "Wake microphone armed")'))
        self.assertRegex(loop, r"boolean activationLogged = false;[\s\S]*while \(running\)")
        self.assertRegex(loop, r"record.read\(buffer, 0, buffer.length\);[\s\S]*if \(count <= 0\) \{\s*continue;\s*\}\s*if \(!activationLogged\) \{\s*Log.i\(TAG, \"Wake microphone armed\"\);\s*activationLogged = true;\s*\}")

    def test_ci_rejects_live_process_without_bounded_wake_activation(self):
        workflow = (ROOT / ".github/workflows/build-boop-wall-resurrection.yml").read_text(encoding="utf-8")
        launch = workflow.split("      - name: Launch BOOP with wake microphone armed and verify process survives", 1)[1].split("      - name:", 1)[0]
        script = textwrap.dedent(launch.split("run: |\n", 1)[1])
        self.assertIn("Wake microphone armed", script)
        self.assertIn("{1..30}", script)
        self.assertLess(script.index("adb logcat -c"), script.index("adb shell am start"))
        bash = shutil.which("bash") or r"C:\Program Files\Git\bin\bash.exe"
        for mode, expected in (("armed", 0), ("absent", 1), ("wrong", 1)):
            with self.subTest(mode=mode), tempfile.TemporaryDirectory() as temp:
                stub = '''
                reads=0
                adb() {
                  if [[ "$*" == 'logcat -d -v raw'* ]]; then
                    reads=$((reads + 1))
                    if [[ "$mode" == armed && "$reads" -ge 3 ]]; then
                      echo 'Wake microphone armed'
                    elif [[ "$mode" == wrong ]]; then
                      echo 'Wake microphone armed failed'
                    fi
                  elif [[ "$*" == 'shell pidof'* ]]; then
                    echo 1234
                  fi
                  return 0
                }
                sleep() { :; }
                '''
                result = subprocess.run([bash, "-c", "mode=" + mode + "\n" + textwrap.dedent(stub) + script], cwd=temp, capture_output=True, text=True, timeout=10)
                self.assertEqual(expected, result.returncode, result.stdout + result.stderr)

    def test_ci_selects_supplied_audio_capable_recognizer_before_launch(self):
        workflow = (ROOT / ".github/workflows/build-boop-wall-resurrection.yml").read_text(encoding="utf-8")
        boot = workflow.split("      - name: Boot clean Android 16 Pixel 7 Pro emulator", 1)[1].split("      - name:", 1)[0]
        launch_index = workflow.index("      - name: Launch BOOP with wake microphone armed")
        for required in (
                "query-services --brief --components",
                "android.speech.RecognitionService",
                r"com\.google\.android\.as/",
                "voice_recognition_service",
        ):
            self.assertIn(required, boot)
        self.assertLess(workflow.index("voice_recognition_service"), launch_index)

    def test_unified_workflow_gates_resurrection_apk(self):
        workflow = (ROOT / ".github/workflows/build-boop-wall-resurrection.yml").read_text(encoding="utf-8")
        for required in ("boop-wall-resurrection", "testDebugUnitTest", "versionCode='29'",
                         "assets/boop-kws/keywords.txt", "libsherpa-onnx-jni.so",
                         "BOOP-Wall-Resurrection-debug"):
            self.assertIn(required, workflow)

    def test_exact_natural_wake_collection_is_preserved(self):
        raw = (ROOT / "wake-assets/boop-kws/keywords_raw.txt").read_text(encoding="utf-8")
        compiled = (ROOT / "wake-assets/boop-kws/keywords.txt").read_text(encoding="utf-8")
        raw_lines = [line.strip() for line in raw.splitlines() if line.strip()]
        compiled_lines = [line.strip() for line in compiled.splitlines() if line.strip()]
        self.assertEqual(33, len(raw_lines))
        self.assertEqual(33, len(compiled_lines))
        normalize = lambda lines: "\n".join(lines)
        self.assertEqual(
            "5bf3fc9d69d2038da50c1da97a0df5908f2c3b462da5a2ecbf15edbb8afba71d",
            hashlib.sha256(normalize(raw_lines).encode()).hexdigest(),
        )
        self.assertEqual(
            "6e6eda57e99827d9acad5a0b7ed8711d75cf839694d5c30507c929e780b9fbb0",
            hashlib.sha256(normalize(compiled_lines).encode()).hexdigest(),
        )
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
        self.assertRegex(gradle, r"(?m)^\s*versionCode\s+29\s*$")
        self.assertRegex(gradle, r'(?m)^\s*versionName\s+"0\.4\.9-alpha6\.5\.6-wall"\s*$')


if __name__ == "__main__":
    unittest.main()
