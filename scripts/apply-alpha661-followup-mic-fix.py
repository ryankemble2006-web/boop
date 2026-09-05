from pathlib import Path

# One-shot CI patch used only to apply the tested Alpha 6.6.1 mic handoff fix.
path = Path("source/MainActivity.java")
text = path.read_text(encoding="utf-8")

old = '''    private void finishTtsUtterance() {
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
        if (listenAfterTts) {
            listenAfterTts = false;
            beginTapToSpeak();
        }
    }
'''

new = '''    private void finishTtsUtterance() {
        boolean followUpListen = listenAfterTts;
        listenAfterTts = false;

        if (followUpListen && wakeCoordinator != null) {
            wakeCoordinator.onTapStarted();
        }
        if (wakeCoordinator != null) {
            wakeCoordinator.onTtsFinished();
        }
        if (followUpListen) {
            recognitionMode = RecognitionMode.TAP;
            startListening();
        }
    }
'''

if text.count(old) != 1:
    raise SystemExit(f"expected one finishTtsUtterance block, found {text.count(old)}")

path.write_text(text.replace(old, new, 1), encoding="utf-8")
