#!/usr/bin/env python3
from pathlib import Path


MAIN = Path('boop-build/BOOP-Alpha1/app/src/main/java/com/boop/alpha1/MainActivity.java')
MARKER = '// BOOP_WAKE_COMMAND_SEAM_V1'


def apply() -> None:
    text = MAIN.read_text(encoding='utf-8')
    if MARKER in text:
        print('Seamless wake-command handoff already patched')
        return

    old = '''                    wakeFaceForInteraction();
                    playWakeAcceptedCue();
                    startWakeRecognition(session);
'''
    new = '''                    wakeFaceForInteraction();
                    // BOOP_WAKE_COMMAND_SEAM_V1
                    // Keep the wake-to-command handoff acoustically silent. The
                    // live microphone is already capturing the user's command.
                    startWakeRecognition(session);
'''
    count = text.count(old)
    if count != 1:
        raise SystemExit(f'wake-command seam: expected one cue anchor, found {count}')

    MAIN.write_text(text.replace(old, new, 1), encoding='utf-8')
    print('Wake-to-command handoff is silent; no speaker cue overlaps command audio')


if __name__ == '__main__':
    apply()
