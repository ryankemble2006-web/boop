# Current handoff: v161 installed; Ryan and assistant test together

Updated 2026-09-13. Ryan explicitly requested installation of the signed candidate for his own testing and changed the ongoing workflow: development stays on GitHub, device testing happens together with him. This supersedes the earlier emulator-first requirement and the physical-deployment hold. It is NOT permission to change permissions, bypass locks or access the physical Pixel 10.

## Installed candidate

Owner branch: `boop-unified-eye-sync-safe-v159`. App/build commit remains `0b6ee6f91e05f00138a94ec2c9fd846117020754`. Version `161 / 1.2.161-lab-scale-independent`, package `com.boop.alpha1`.

GitHub build `34770388933` was rechecked as successful, including package and permanent-signer verification. Artifact `10321956422` / `BOOP-Unified` was downloaded through the connector and staged on the laptop solely for installation. Its ZIP and APK SHA256 matched the recorded build receipts before installation.

- APK SHA256: `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`.
- ZIP SHA256: `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Both authorized targets were identified live as Nvidia Shield Android TV and Pixel 7 Pro, both initially on v159. Their lock-state queries showed `showing=false`. Each received the exact APK via an ordinary `adb install -r`, with no grant, downgrade, uninstall or data-clear flag. Both installations returned `Success` and exit code 0. Afterward each reported versionCode161 and the expected versionName; SHA256 of each installed base APK matched the exact GitHub APK above.

Installation is verified on BOTH Shield and Pixel 7. On-screen speed behaviour is awaiting Ryan's test, not yet accepted. No animation/navigation input, settings changes, screenshots or autonomous runtime tests were sent. Physical Pixel 10 received no targeted commands or installation. Existing emulators and the dirty source worktree were left alone. No app code, artwork, signing configuration or permissions changed in this continuation.

Detailed installation receipt: `docs/handoffs/2026-09-13-v161-installed-joint-testing.md`.

## Current workflow

GitHub owns source edits, non-visual logic/functional tests, builds, permanent signing and durable documentation. Once those existing checks pass, provide the APK and perform the installation Ryan requests. Do not withhold it for an emulator-first gate or add automated visual tests to GitHub. Device testing is a joint session with Ryan, guided by his observations and agreed next checks. An emulator is optional only when he explicitly asks for one.

Desktop Commander/ADB remains available for requested installations, package/version/hash readback and agreed joint diagnosis. This is not a local source-edit/build loop or permission for autonomous testing. Do not reopen the historical blocked combined test command, alter tools/permissions or disguise device execution as a GitHub workflow. The explicitly requested installation above succeeded through the ordinary authorized tool path.

The shared workflow is published in `main/BOOP_START_HERE.md` at `24a260b6e7cdd5aed792ccfbb683e8e495eb5f80`. This current user instruction overrides older cosmetic/substantive emulator distinctions in inherited context and older handoffs. Main remains a shared-context hub; this branch owns app implementation and installation state.

## What the candidate contains

The published v160 clock, four saved device-local speeds, accepted hue integration and all authored animations are retained. v161 changes only the embedded Lab's zero Android animator-scale freeze condition, retaining the existing power-saver condition, plus its version. Manual Pause motion, Slow review, freeze controls and original1x timing are unchanged.

Prior verified CI: timing `34770388848`, appearance `34770388845`, full build `34770388933`, all successful. The six timing tests include 160720 timing and 1157272 edge checks, plus 20920 Lab assertions per raw/materialized source path. Build receipts report 235 Unified and 68 Shield functional tests, zero failures/errors/skips. These were not rerun during installation; the existing build result was rechecked. All 13 APK assets previously matched v160 byte-for-byte.

Full regression evidence remains in `docs/handoffs/2026-09-13-lab-scale-zero-verification.md`. Red checkpoint `fc9e6633ca7b578b7729cc5ee0e134294969075a` remains on `wip/boop-lab-scale-zero-red-fc9e663`. Do not restart that repaired source diagnosis from an old note.

## Accepted colour and next step

Ryan's Wall -> Shield eye-colour acceptance remains valid; prior controlled captures also demonstrated Shield -> Pixel7 and Pixel7 -> Shield. No colour setting was read or changed during this installation, and no old fixture hue was restored. A successful package update does not establish a new colour or speed acceptance result.

Wait for Ryan's v161 observation and test together. Available speed controls are 0.5x, 1x, 1.5x and 2x. Speed is device-local, not shared via Home Assistant. Focus next diagnosis only on the device/surface and behaviour he reports. Sleep/wake, signs/eyes, mid-clip changes and scale-zero behaviour can be checked together, not turned into a fresh autonomous gate. Colour offline/reconnect coverage remains separate.

Preserve approved masters/shaders, coded animations, exact1x, working Wall hue controls, accepted Shield polish, voice/media behaviour and single-face ownership. No permissions, lock bypass, data clear, key replacements, unrelated branch merges or physical Pixel10 access. Source worktrees were not synchronized or changed. Only the verified installer artifact remains staged locally. No queued device inputs or scheduled monitoring exists.

Prior provenance remains in the v160 GitHub verification, colour-accepted and historical colour-failure receipts. This session is primary. Update current handoff/status/memory after material results and verify live GitHub HEAD.
