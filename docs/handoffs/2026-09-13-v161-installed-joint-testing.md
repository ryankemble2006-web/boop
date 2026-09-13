# v161 requested installation and joint-testing receipt

Date: 2026-09-13. Repository `ryankemble2006-web/boop`, owner branch `boop-unified-eye-sync-safe-v159`.

## Scope

Ryan requested installation of the ready signed APK for his own test and established GitHub development plus testing together with him as the ongoing workflow. This specifically replaces the former emulator-first delivery prerequisite. It does not authorize autonomous runtime acceptance, permission changes, lock bypass, data clear or physical Pixel10 access.

Starting live owning-branch HEAD: `c4a8234c6a2deaf8003b62fd813f295059dd0fea`. Source edits, logic tests, builds and permanent signing remain on GitHub. No new source change, build, code-test run, artwork change or signing change was made in this installation continuation.

## Artifact verified before delivery

Existing full run `34770388933` rechecked successful, including permanent signer/package verification. Artifact metadata `10321956422` reported the correct source commit `0b6ee6f91e05f00138a94ec2c9fd846117020754` and unexpired archive. Download used the GitHub connector and was staged in a dedicated Downloads folder on the authorized laptop, outside the source worktree.

ZIP SHA256 matched `623805381b04c2ce61a8a5bfcb56e663a9a2fc8eddbc1b9eb9b98ed987ccdf3b`.
APK SHA256 matched `c68b81be9b7d3e10883d2aea52c05dc7b8c187fcd835eaddf73e25e91cb4acd6`.
The staged built-commit receipt read `0b6ee6f91e05f00138a94ec2c9fd846117020754`.
Signer receipt remained `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

No installer or application source was rebuilt locally. Desktop Commander remained the existing version0.2.47; its configuration, command and permissions were not changed.

## Installation result

Live ADB inventory distinguished the authorized Shield and Pixel7 from the excluded Pixel10 and existing emulator. Only Shield and Pixel7 received targeted commands. Both were initially version159 and reported no showing lock screen.

| Target | Action result | Installed version readback | Installed base APK SHA256 |
| --- | --- | --- | --- |
| Nvidia Shield Android TV | Normal `adb install -r`: Success, exit0 | 161 / `1.2.161-lab-scale-independent` | Exact match to signed GitHub APK above |
| Pixel 7 Pro | Normal `adb install -r`: Success, exit0 | 161 / `1.2.161-lab-scale-independent` | Exact match to signed GitHub APK above |

Package on both is `com.boop.alpha1`. The update used no `-g`, downgrade, uninstall, data-clear or permission-grant operation. No colour/speed setting, shared-colour state or Android animation scale was changed. No app-navigation input, emulator launch or runtime/visual test was sent. Physical Pixel10 was not targeted. Its presence in inventory was not authorization to operate it.

This is installation and identity verification only. Ryan's actual on-screen v161 speed test remains pending. No user acceptance is inferred from package-manager success or code-test results.

## Durable rule and next step

The shared rule was saved in `main/BOOP_START_HERE.md` at `24a260b6e7cdd5aed792ccfbb683e8e495eb5f80`. The owning branch's startup note, handoff, status and memory record the same workflow and installed state. Historical emulator-first and deployment-hold notes are superseded by Ryan's current request, not active blockers.

Wait for Ryan's report, then diagnose/test the specific behaviour together. GitHub retains non-visual compilation, focused code tests, permanent signing and package integrity checks. Do not add an emulator gate or hosted visual tests. Requested installation and read-only installation identity checks remain permitted uses of the laptop/ADB; autonomous runtime testing and local source development do not.

Protect accepted Wall -> Shield colour, all coded animations, original1x, approved masters/shaders, single-face ownership and accepted Shield polish. Speed is device-local; colour is the separately shared preference. Earlier Lab regression evidence is in `docs/handoffs/2026-09-13-lab-scale-zero-verification.md`, and older speed/colour receipts are preserved.

Private addresses/serials and raw tool dumps are intentionally absent from this public receipt. The installer files are staged locally in `Downloads/BOOP-v161-install-0b6ee6f`; source worktrees and existing emulators were not changed or synchronized. Both install processes completed. No pending device inputs or scheduled monitors exist.
