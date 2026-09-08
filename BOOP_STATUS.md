# BOOP unified status

Updated 2026-09-08. Canonical branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged.

## Latest state

**HA names and Home buttons: physically accepted.** Preserve working device naming/control code.

**Blink: working, user-confirmed.** Ryan reports he had turned it off himself. Earlier missing-blink wording is superseded. No blink code, timing or system-setting change is needed.

**New permanent face: approved, NOT yet integrated.** Ryan approved `glossy_cartoon_eyes_with_black_eyelids.png`, with the blue eyelid lines blackened, as the permanent default for every animation. Accessories are later separate additions. He explicitly requests replacing phone/Wall and Shield. Read `BOOP_EYES_MASTER.md`.

The exact master is 1774 x 887 RGBA, 936803 bytes, SHA-256 `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`. It is checked locally and backed up by Ryan, but not yet in GitHub or the APK. The exposed connector has no mounted-file upload argument; direct container GitHub networking failed. Ryan has been asked to upload the saved PNG to the root of `boop-unified`, with its existing filename. Recheck live HEAD, validate the file, then integrate both renderers without alpha guessing or changing blink/iris tint.

**Assistant: still not selected.** Last physical result was `Assistant choice was not changed`. Remote-button activation and audio from THAT remote remain unaccepted. The prior investigation identified missing recognitionService metadata, not a proven firmware-wide limitation. Phone acoustic wake remains unresolved; no fresh room-switch/scale acceptance.

Overall release is still only partially physically accepted. This checkpoint changes documentation only, not app code or assets, and produces no new APK or fresh functional-test pass.

## Last delivered artifact

Code `949f1085328a3e815d9bc57747425f1f930c48db`; version 45 / `1.1.2-unified-assist-repair`; successful run `34201200463`; artifact `BOOP-Unified` ID `10045928699`.
APK SHA-256 `217e004f26bca33066e3d2089d2e2bc448c102c332abb25f97cf00122d5ed239`.
Signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Historical checks: 58 Shield + 66 unified focused tests, zero failures/errors/skips; Launcher lint, compilation, package/manifest presence, permanent signer and ZIP integrity. These did not certify appearance or Android's real assistant selection. No visual/golden/screenshot/aesthetic-string or emulator/device acceptance.

Keep approved iris-only hue, headphones/puppetry, five-digit hands, wake ownership, room isolation and idempotent Shield scale. No installs/grants, signer/package changes, Windows sync or unattended monitoring. Protected rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881`. Earlier detailed investigation remains in Git at `438c3076875a56338ef26bd430f744f0a0cace32`.
