# Current handoff: v161 speed and bidirectional colour user-accepted

Updated 2026-09-13. After the verified v161 installation on Shield and Pixel 7, Ryan confirmed actual device behaviour: speed changes work on BOTH devices and eye colour automatically propagates in BOTH directions. This supersedes the earlier awaiting-test state. Speed and live colour delivery are now user-accepted, not just CI-green or installed. Do not reopen either repair or require another approval loop without a new reported problem.

## Latest physical acceptance

Ryan reported: "both devices, speed change working also colour is automagically changing from device to device, to and from both in both directions."

Accepted on the installed v161:
- Animation-speed changes work on Nvidia Shield Android TV.
- Animation-speed changes work on Pixel 7 Pro / Wall.
- Automatic eye-colour delivery works Wall / Pixel 7 -> Shield AND Shield -> Wall / Pixel 7.

This is Ryan's physical test report, not a new assistant-run test. Speed remains a device-local preference; the bidirectional sharing report concerns colour, not speed. The report does not enumerate every animation surface, rate or lifecycle edge case. Do not invent those additional test results or turn unreported cases into blockers to this acceptance.

Acceptance receipt: `docs/handoffs/2026-09-13-v161-speed-colour-accepted.md`.

## Installed and accepted app identity

Owner branch: `boop-unified-eye-sync-safe-v159`. App/build commit: `0b6ee6f91e05f00138a94ec2c9fd846117020754`. Version `161 / 1.2.161-lab-scale-independent`, package `com.boop.alpha1`. The branch name is not the APK version.

Full GitHub build `34770388933`, artifact `10321956422` / `BOOP-Unified`:
- APK SHA256: `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`.
- ZIP SHA256: `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`.
- Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Previous installation verification identified both authorized devices on v159, with no showing lock screen, then updated each using ordinary `adb install -r`. Both returned Success/exit0. Subsequent version/name and installed-base-APK SHA256 matched the exact GitHub v161 APK above. No grant, downgrade, uninstall, data-clear or setting-reset action was used. Physical Pixel10 was not targeted. Receipt: `docs/handoffs/2026-09-13-v161-installed-joint-testing.md`.

This acceptance-recording continuation changed documentation only. No new app build, code-test run, installation, device query/input, screenshot, emulator action, permission change or settings change occurred. The installed app identity comes from the preceding installation receipt; the new physical result comes from Ryan.

## Current workflow

GitHub owns source edits, non-visual logic/functional tests, builds, permanent signing and durable documentation. After those checks pass, provide the APK and perform the installation Ryan requests. Device behaviour is tested together with Ryan, guided by his observations and agreed next checks. No emulator-first delivery gate, autonomous visual sweep or hosted Android acceptance tests. Emulators are optional only when Ryan explicitly asks.

Desktop Commander/ADB may stage a GitHub-built APK, carry out requested installations, read back installation identity and assist with agreed joint diagnosis. Do not edit/build source locally or bypass a tool denial. Preserve dirty/concurrent work. Physical targets remain Shield and Pixel 7; physical Pixel10 remains excluded.

Shared workflow: `main@24a260b6e7cdd5aed792ccfbb683e8e495eb5f80:BOOP_START_HERE.md`. This supersedes inherited cosmetic/substantive emulator distinctions and historical deployment holds. Main is a shared-context hub; this branch owns implementation and acceptance state. No shared workflow or ownership changed in this documentation update, so main does not need an ordinary-progress edit.

## Preserved implementation and test provenance

v161 retains the published v160 clock, four saved device-local speeds, hue integration and all coded animations. Its only behaviour repair removes the embedded Lab's zero Android animator-scale freeze condition while retaining the existing power-saver condition. Manual Pause motion, Slow review, explicit freeze and original1x timing are unchanged.

Previously verified CI: timing `34770388848`, appearance `34770388845`, full build `34770388933`, all successful. Six timing functions include 160720 timing checks, 1157272 edge checks and 20920 Lab assertions per raw/materialized path. Build receipts report 235 Unified and 68 Shield functional tests, zero failures/errors/skips. All 13 packaged assets matched v160 byte-for-byte. These results were not rerun for this documentation-only acceptance record.

Complete Lab red/green evidence: `docs/handoffs/2026-09-13-lab-scale-zero-verification.md`. Test-first `fc9e6633ca7b578b7729cc5ee0e134294969075a` remains at `wip/boop-lab-scale-zero-red-fc9e663`; failed timing `34770049893` and full build `34770049896` proved the old defect before the repair. Earlier v160, colour-accepted and colour-failure receipts remain historical provenance, not current blockers.

## Next and boundaries

Speed changes on both devices and live two-way colour delivery are closed as accepted. Await Ryan's next requested change or specific contrary observation, then work/test together. Do not restart speed/colour diagnosis, deploy another APK or restore historical hue fixtures without a new reason and authorization.

A deliberate offline/reconnect cycle, each individual surface/rate, mid-clip changes, sleep/wake and scale-zero physical behaviour were not separately enumerated in this report. They remain unclaimed coverage only, not an automatic testing backlog or condition of accepting what Ryan verified.

Preserve approved masters/shaders, coded animations, exact original1x, working Wall hue controls, accepted Shield polish, voice/media behaviour and single-face ownership. No permission changes, lock bypass, data clears, key replacement, unrelated merges or physical Pixel10 access. Source worktrees remain untouched and are not claimed synchronized. No queued device inputs or scheduled monitoring exists. This session remains primary. Publish material handoff/status/memory results and verify LIVE GitHub HEAD.
