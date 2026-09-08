#!/usr/bin/env python3
from pathlib import Path


MAIN = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java")
WAKE = Path("boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/BoopWakeRecognitionIntent.java")
MARKER = "// BOOP_UNCENSORED_SPEECH_V1"


def replace_once(path: Path, old: str, new: str, label: str) -> None:
    text = path.read_text(encoding="utf-8")
    if MARKER in text:
        return
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{label}: expected one anchor, found {count}")
    path.write_text(text.replace(old, new, 1), encoding="utf-8")


def apply() -> None:
    replace_once(
        MAIN,
        "        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, assistantFollowUpListening);\n",
        "        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, assistantFollowUpListening);\n"
        "        intent.putExtra(RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS, false);\n"
        f"        {MARKER}\n",
        "tap recognizer profanity-mask override",
    )
    replace_once(
        WAKE,
        "        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);\n",
        "        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);\n"
        "        intent.putExtra(RecognizerIntent.EXTRA_MASK_OFFENSIVE_WORDS, false);\n"
        f"        {MARKER}\n",
        "wake recognizer profanity-mask override",
    )
    print("Unified tap and wake speech recognition now request unmasked offensive words")


if __name__ == "__main__":
    apply()
