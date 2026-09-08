#!/usr/bin/env python3
from pathlib import Path

path = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
text = path.read_text(encoding='utf-8')
marker = '// BOOP_WAKE_REAL_ATTEMPT_V1'
if marker in text:
    print('Wake real-attempt gate already patched')
    raise SystemExit(0)

start = text.index('    private void checkWakeRecognitionSupport() {')
end = text.index('    private void handleRecognizedSpeech(String transcript) {', start)
replacement = '''    private void checkWakeRecognitionSupport() {
        // BOOP_WAKE_REAL_ATTEMPT_V1
        if (wakeCoordinator == null) {
            return;
        }
        // checkRecognitionSupport() is advisory and has returned false negatives on
        // otherwise working Android recognizers. Sherpa must still arm; the existing
        // wake recognition start/error path remains the real capability check.
        wakeSupportCheckInFlight = false;
        wakeCoordinator.setRecognitionSupported(
                BoopWakeRecognitionCapability.canAttempt(
                        Build.VERSION.SDK_INT,
                        recognizer != null));
    }

'''
text = text[:start] + replacement + text[end:]
path.write_text(text, encoding='utf-8')
print('Wake arming now uses the real recognizer attempt instead of advisory support probing')
