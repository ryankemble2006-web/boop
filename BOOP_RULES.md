# BOOP Rules

BOOP is a puppet interface, not a chatbot.

Never:
- add cloud dependency for basic control
- add microphone to overlay
- make screen touch required
- replace existing working functions without approval

Always:
- local Home Assistant first
- remote friendly
- big chunky UI
- plain English errors
- test before claiming done

## Manual visual acceptance, approved 2026-09-08

Ryan owns BOOP visual acceptance on his real devices. Do not add or run GitHub visual checks, screenshot/golden-image comparisons, appearance/layout/animation judging, or source-string guards that try to certify how BOOP looks. Do not silently re-enable them in later work. Only Ryan's explicit reversal changes this rule.

Keep non-visual compilation, focused functional tests, package integrity, permanent-signer verification and security checks. A process/entry-activity crash smoke test is non-visual; it must not be presented as proof that the UI looks right. Upload the verified signed test APK before an optional slower non-visual emulator smoke so visual inspection is not delayed.

## Permanent default eye master

The permanent BOOP default is `boop-unified/unified/assets/boop-eyes/boopApprovedEyes.png`. Exact identity: 1774 x 887 RGBA, 936,803 bytes, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`, Git blob `f95375356b20297fa2b27ab8887f65d4cce5c7fd`.

Do not edit, flatten, recompress, regenerate, recreate, crop, resize on disk, recolor/recolour, threshold/flood-fill, reconstruct transparency or substitute this source. Runtime scaling, positioning, masking, blinking and animation are allowed only when non-destructive to the master bytes. User-selected eye hue may affect the iris at runtime only. Replacing the permanent master requires Ryan's explicit approval of a new exact image and recorded identity.

Approved artwork remains locked. Animate or mask the existing art in code; do not regenerate it per pose. User-selected eye hue changes only the blue/cyan iris region, not the sclera, pupil, highlights or the rest of the eyeball. Physical appearance and acoustic wake accuracy remain manual acceptance requirements.
