# Unified memory: v161 installed; GitHub development and testing with Ryan

## Latest user decision, 2026-09-13

Ryan explicitly requested the signed candidate be installed so he can test, and set the ongoing workflow: keep development on GitHub and test device behaviour together with him. This replaces the earlier emulator-first default and physical-deployment hold. Do not keep asking for or enforcing automated local speed/visual acceptance before he receives a signed candidate. Do not move those visual tests into GitHub.

GitHub owns source edits, non-visual tests, builds, permanent signing and handoffs. Desktop Commander/ADB may stage a built APK, carry out the installation he requests, read back package/version/hash, and help with diagnosis agreed during the joint test. No autonomous emulator runs or local source builds. Existing AVDs/worktrees remain untouched unless specifically requested. Physical Pixel10 remains excluded. No permissions, locks, data clears, keys or settings resets are implied.

Shared rule is published at `main@24a260b6e7cdd5aed792ccfbb683e8e495eb5f80:BOOP_START_HERE.md`. It supersedes inherited historical cosmetic/substantive emulator rules. Fetch live main/current app handoff next time; do not reconstruct state from stale root maps.

## Current installed app

Owner `boop-unified-eye-sync-safe-v159`. App/build commit `0b6ee6f91e05f00138a94ec2c9fd846117020754`, package `com.boop.alpha1`, version161 / `1.2.161-lab-scale-independent`.

Nvidia Shield Android TV and Pixel 7 Pro were identified live on v159, then both updated with the exact signed GitHub APK using `adb install -r`. Both reported Success and exit0. Readback confirmed version161/expected name and matching SHA256 of each installed base APK. No targeted Pixel10 command or installation was sent. No app-navigation or animation test occurred. Installation is verified; speed is NOT yet physically accepted.

Build `34770388933` rechecked successful; artifact `10321956422`. APK SHA256 `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`. ZIP SHA256 `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`. Signer SHA256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. The staged archive and APK matched these receipts before installation. No app source, workflow, signing configuration or permissions changed in this delivery.

Receipt: `docs/handoffs/2026-09-13-v161-installed-joint-testing.md`. The laptop was used only for the authorized installation and identity checks; its historical dirty source worktree was not touched or synchronized. No further device action is queued. The earlier denied combined test request was not forced through or disguised; the later explicitly requested normal installation completed successfully.

## Preserved implementation and evidence

v161 preserves v160's .5/1/1.5/2 device-local speed implementation. Its sole app-behaviour repair removes the embedded Lab's dependency on zero Android animator-duration scale for reduced motion; the existing power-saving condition remains. Manual Pause motion, Slow review, freeze controls, frame clock, authored motion and original1x are retained.

Prior CI timing `34770388848`, appearance `34770388845` and signed build `34770388933` succeeded. The build includes six timing functions, 160720 timing/1157272 edge checks, 20920 Lab assertions on each raw/materialized path, plus 235 Unified and 68 Shield functional tests, zero failures/errors/skips. All 13 packaged asset entries matched v160. These are code/source/integrity results, not visual or real Android scheduling proof, and were not rerun for installation.

Keep complete Lab red/green evidence in `docs/handoffs/2026-09-13-lab-scale-zero-verification.md`; test-first commit `fc9e6633ca7b578b7729cc5ee0e134294969075a`, red branch `wip/boop-lab-scale-zero-red-fc9e663`, failed timing `34770049893` and full build `34770049896` remain provenance. Do not confuse that known repaired source defect with a new device-reported failure.

Original v160 source `d149cb509ec376779daf84c50f621d8adcbacd24`, gate build source `1d8bf3d39a0858aa0c4f2b435fc92b1b16ef486a`, build `34769075927`, artifact `10321686042`, APK `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b` and earlier research/acceptance receipts remain preserved.

## Accepted colour and next joint test

Ryan accepted Wall -> Shield eye colour. Prior controlled captures also demonstrated both directions with Pixel7. Later historical receipt showed his hue2/sharing-on choice; do not treat that historical value as permission to reset his current preferences. This installation neither read nor changed colour settings and did not clear app data. Colour sharing remains opt-in/authenticated Home Assistant. Speed is device-local, not shared.

Wait for Ryan's actual observation of installed v161. Test together, concentrating on the specific device/surface he reports. Do not declare speed working from a slider number or green CI, or reopen accepted colour merely because an old handoff says failure. Offline/reconnect colour coverage is separate. Preserve approved masters/shaders, all coded clips, exact1x, existing Wall controls, accepted v156 Shield polish, voice/media behaviour and single-face ownership.

This session is primary. Publish material results and updated handoff/status/memory through GitHub, verify live branch HEAD, and never imply unattended device inputs, background monitoring or synchronization that did not happen.
