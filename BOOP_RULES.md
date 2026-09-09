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

## Manual visual and device acceptance, approved 2026-09-08

Ryan owns BOOP visual acceptance on his real devices. Do not add or run GitHub visual checks, screenshot/golden-image comparisons, appearance/layout/animation judging, or source-string guards that try to certify how BOOP looks. Do not silently re-enable them in later work. Only Ryan's explicit reversal changes this rule.

Ryan subsequently expanded this to remove automated tests he can perform on-device himself, including emulator install/launch and physical navigation/animation acceptance, from the unified delivery workflow. Do not run the previously optional post-upload emulator smoke in this path. Keep non-visual compilation, focused functional/logic tests, package integrity, permanent-signer verification and security checks. Upload the signed test APK after those checks; no visual or device success is implied. Historical tests remain available but are not selected by this workflow.

## Permanent default eye master

`unified/assets/boop-eyes/boopApprovedEyes.png` is the permanent BOOP default approved by Ryan. Exact identity: 1774 x 887 RGBA, 936,803 bytes, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`, Git blob `f95375356b20297fa2b27ab8887f65d4cce5c7fd`.

Do not edit, flatten, recompress, regenerate, recreate, crop, resize on disk, recolor/recolour, threshold/flood-fill, reconstruct transparency or substitute this source. Runtime scaling, positioning, masking, blinking and animation are allowed only when non-destructive to the master bytes. User-selected eye hue may affect the iris at runtime only. Replacing the permanent master requires Ryan's explicit approval of a new exact image and recorded identity.

Approved artwork remains locked. Animate or mask the existing art in code; do not regenerate it per pose. Shield eyes use the canonical phone eyes and corrected landscape geometry, with the exact same `BoopIdleBlink` duration/curve/delay. TV framing and non-focusable overlay lifecycle are adaptations, not character redesigns. Preserve the locked headphones renderer.

User-selected eye hue changes only the blue/cyan iris region, not the sclera, pupil, highlights or the rest of the eyeball. Physical appearance and acoustic wake accuracy remain manual acceptance requirements.
