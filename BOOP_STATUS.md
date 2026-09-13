# BOOP status: v161 installed on Shield and Pixel 7; joint testing next

Updated 2026-09-13. Ryan explicitly requested installation and changed the ongoing workflow to GitHub development plus device testing together with him. Earlier emulator-first gates and the physical deployment hold are superseded for this requested delivery. No automatic emulator or hosted visual testing.

Installed on BOTH Nvidia Shield Android TV and Pixel 7 Pro: `com.boop.alpha1`, versionCode161, `1.2.161-lab-scale-independent`. Each ordinary `adb install -r` returned Success/exit0. Subsequent package/version and installed-base-APK SHA256 checks matched the signed GitHub build. Physical Pixel10 was not targeted.

App/build commit `0b6ee6f91e05f00138a94ec2c9fd846117020754`; full build `34770388933`, artifact `10321956422` / `BOOP-Unified`. APK SHA256 `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`. Permanent signer unchanged. Existing successful CI/signature/package results were rechecked; no new build or code test was run for this install.

Verification level: INSTALLED AND IDENTITY-VERIFIED. Actual visible speed behaviour: awaiting Ryan's test. No app navigation, animation test, permission grant, setting reset or data clear occurred. Existing source worktree and emulators were not changed. No source/artwork/signing edits.

Wall -> Shield eye colour remains user-accepted. No fixture colour was restored or sharing changed. v160 speed implementation, v161 Lab scale-zero repair, exact1x, approved assets and accepted Shield polish remain intact.

Current workflow is saved on main at `24a260b6e7cdd5aed792ccfbb683e8e495eb5f80` in `BOOP_START_HERE.md`: GitHub source/non-visual tests/build/signing, requested install, then joint device testing. Do not claim CI or installation proves physical motion. No autonomous emulator gate before Ryan gets the APK.

Next: Ryan tests the installed v161; investigate his specific observations together. Current details: `SESSION_HANDOFF.md`, `BOOP_UNIFIED_MEMORY.md`, `docs/handoffs/2026-09-13-v161-installed-joint-testing.md`. Preserve older regression/acceptance receipts as history, not current deployment blockers.
