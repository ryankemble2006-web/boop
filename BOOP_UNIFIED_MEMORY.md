# Unified memory: v161 speed and bidirectional colour user-accepted

## Latest physical result, 2026-09-13

After v161 was installed and identity-verified on Shield and Pixel 7, Ryan reported: "both devices, speed change working also colour is automagically changing from device to device, to and from both in both directions."

Record this as physical USER ACCEPTANCE of animation-speed changes on BOTH Nvidia Shield Android TV and Pixel 7 Pro / Wall, and automatic eye-colour delivery in BOTH directions between them. This is not inferred from a slider, HA ready message, package-manager success or CI. It is the user's actual device test result. The former speed-awaiting-test and old colour-failure blockers are superseded. Do not reopen either repair or demand another acceptance loop without new contrary evidence.

Speed remains device-local. Colour is the shared preference. The report does not establish speed synchronization or enumerate all rates, all surfaces, mid-clip changes, sleep/wake, zero-scale settings or a deliberate offline/reconnect cycle. Do not fabricate those results or make unreported coverage an automatic gate. Follow the next task Ryan chooses.

Acceptance receipt: `docs/handoffs/2026-09-13-v161-speed-colour-accepted.md`. This acceptance continuation is documentation-only; no new build, test execution, installation, device query/input, capture, permission action or settings change occurred.

## Ongoing workflow

Ryan requested that development stay on GitHub and device behaviour be tested together with him. This replaces earlier emulator-first defaults and physical-deployment holds. GitHub owns source edits, non-visual tests, builds, permanent signing and handoffs. Once existing code/package/signer checks pass, deliver the candidate and perform the installation he requests; do not add a local emulator hurdle or hosted visual test.

Desktop Commander/ADB may stage a GitHub-built APK, perform requested installation and identity readback, and assist with diagnosis agreed during joint testing. No local source edit/build loop or autonomous runtime acceptance. Emulators are optional only on explicit request. Keep existing AVDs/worktrees unchanged otherwise. Physical Pixel10 remains excluded; authorized physical targets are Shield and Pixel 7. No permissions, locks, data clears, key replacement or settings resets are implied.

Shared rule: `main@24a260b6e7cdd5aed792ccfbb683e8e495eb5f80:BOOP_START_HERE.md`. Fetch live main and the current app handoff on continuation, not stale root maps. This result changes acceptance state, not ownership or the shared workflow; ordinary progress stays on the app branch.

## Installed and accepted application

Owner `boop-unified-eye-sync-safe-v159`. App/build commit `0b6ee6f91e05f00138a94ec2c9fd846117020754`, package `com.boop.alpha1`, version161 / `1.2.161-lab-scale-independent`.

Both devices were previously identified live on v159, then updated with exact signed GitHub APK using ordinary `adb install -r`. Both returned Success/exit0. Readback confirmed version161/expected name and each installed-base-APK SHA256 matching GitHub. Physical Pixel10 was not targeted. The installation-only state was followed by Ryan's speed/two-way-colour acceptance above.

Build `34770388933`, artifact `10321956422`. APK SHA256 `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`. ZIP SHA256 `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`. Permanent signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Artifact hashes matched before installation. Receipt: `docs/handoffs/2026-09-13-v161-installed-joint-testing.md`.

The laptop was used only for the requested installation and identity checks in that earlier continuation. Its dirty source worktree was not touched or synchronized. The earlier denied combined test request was not forced through or disguised; the later explicitly requested normal installation succeeded.

## Preserved implementation and evidence

v161 preserves v160's .5/1/1.5/2 device-local speed implementation. Its sole behaviour repair removes the embedded Lab's zero Android animator-duration-scale condition for reduced motion, retaining the power-saving condition. Manual Pause motion, Slow review, explicit freeze, frame clock, coded motion and original1x are unchanged.

Prior CI timing `34770388848`, appearance `34770388845` and signed build `34770388933` succeeded. Six timing functions included 160720 timing/1157272 edge checks and 20920 Lab assertions per raw/materialized path, plus 235 Unified and 68 Shield functional tests with zero failures/errors/skips. All 13 packaged assets matched v160. These historical results were not rerun for documentation. Numerical/source evidence remains distinct from Ryan's new physical acceptance.

Full Lab red/green evidence: `docs/handoffs/2026-09-13-lab-scale-zero-verification.md`; test-first `fc9e6633ca7b578b7729cc5ee0e134294969075a`, red branch `wip/boop-lab-scale-zero-red-fc9e663`, failed timing `34770049893` and build `34770049896` remain provenance. Do not restart that repaired diagnosis from an old note.

Original v160 source `d149cb509ec376779daf84c50f621d8adcbacd24`, gate-build source `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`, build `34769075927`, artifact `10321686042`, APK `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b` and older research/acceptance receipts remain preserved.

## Colour, preservation and next work

Earlier Wall -> Shield acceptance and controlled captures of both directions are now supplemented by Ryan confirming automatic colour delivery BOTH ways on v161. The historical hue2/sharing-on receipt is not permission to reset his newer choices. This documentation update did not read or change any colour/speed preference. Colour sharing remains opt-in with saved/authenticated Home Assistant access; speed remains local.

Speed changes and live bidirectional colour are complete for the reported physical acceptance scope. Wait for Ryan's next requested feature or a specific new issue and test it together. Do not autonomously reinstall, perform another acceptance sweep, add an emulator gate, merge unrelated branches or reopen colour because an old recovery note reports failure.

Preserve approved eye/hand masters and shaders, all coded clips, exact original1x, existing Wall hue controls, accepted v156 Shield polish, voice/media behaviour and single-face ownership. This session is primary. Publish material handoff/status/memory updates through GitHub, verify LIVE HEAD, and do not imply unattended inputs, background monitoring or local synchronization that did not occur.
