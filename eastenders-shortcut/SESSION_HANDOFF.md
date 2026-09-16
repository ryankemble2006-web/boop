## Both iPlayer shortcuts: direct launch and return Home — 16 September 2026

Current owner: `iplayer-shortcuts-home-20260916`. Built source: `4a8701d346898c71e981426df7b047e7994a5e32`.

EastEnders 1.8 (code 9) and Casualty 1.2 (code 3) remove the old shortcut selector/recovery UI and finish the launcher activity after dispatching the existing deep link. Both retain the existing profile/newest-episode automation and identical exact visible/enabled `Skip trailer` handling, once within the first 60 seconds.

The existing post-playback episode-focus repair is replaced with one-shot Home. The helper must first see the programme page disappear, then see its title and real newest-row episode card return. Player pause/title overlays without that grid card do not qualify. The guard expires after two hours and cancels when another app gains the foreground. Accessibility foreground events are no longer package-filtered, but only the expected iPlayer root is traversed; other-app events only cancel the armed session. No new Android permission or enabled-service setting was granted. A rejected episode click disarms the session. Tree traversal now enforces the existing 1,500-node limit through the child loop.

Verification: initial return-Home test failed in run 35052717646. Review found a legacy filtered-event cancellation hole; its integration regression failed in run 35052861624 before the fix. Final [GitHub run 35052948737](https://github.com/ryankemble2006-web/boop/actions/runs/35052948737) passed five Java suites per programme, the service event-delivery contract, both APK builds and artwork checks. Independent read-only review cleared the foreground fix with no remaining concrete source blocker. SDK setup failures were infrastructure issues (missing setup and a retired tools package), corrected in the final successful workflow.

GitHub built aligned unsigned artifacts. The original shortcut keys exist only locally, so those exact artifacts were signed locally without uploading the keys or compiling app source locally. Signatures match the previous installed packages. Every unsigned APK entry was verified identical after signing; packaged banner/icon bytes equal the previous installed APKs.

Both APKs were installed on the physical Shield with `adb install -r`; read-back APK hashes and installed package/version match. Existing enabled accessibility-service list is identical, and both helpers are bound. Original installed APKs were backed up privately before updating. BOOP Unified, other devices, iPlayer settings and shortcut artwork were not changed.

**Physical acceptance pending:** jointly check each tile, an offered trailer, pause/resume, stop/Back return Home, and natural episode completion. Source/CI/install verification is not a claim that these visible playback paths have been accepted. Casualty already contained trailer handling in its prior source and installed baseline; this update explicitly aligns and checks both implementations rather than treating historical documentation as device acceptance.

- `eastenders` 1.8: APK SHA256 `54ada3a32ba6c7af0678513ee77681653409f1abf78e4c8c738220fb30ab2d54`; signer `6f85ae49982e0b38b9f134b6882fdbe4da6091051f828fb980c5a03fff439535`.
- `casualty` 1.2: APK SHA256 `94645fe3b2c7191c24686f126b0c75d72e58095c054af846234517a374c56b1d`; signer `d198e64f5cce0ccc77201bebd41db0283832be7b05476e26c2370b72d018a7ef`.

## EastEnders 1.7 Skip trailer verified, 2026-09-12

Ryan approved optional exact `Skip trailer` handling in standalone `uk.local.eastenders`. Version 1.7 / code 8 keeps the 1.6 return-focus repair and adds a bounded 60-second one-shot trailer gate after the helper starts the newest episode. It matches only an exact visible/enabled `Skip trailer` text or accessibility label, uses no coordinates or fixed 18-second delay, and does nothing when absent.

Fresh focused tests passed after an observed RED for the missing policy/gate. Signed APK SHA256 `a311d6cce19413a3a9531ec6bef28d20e1fcfcc93a9d324bf79f44e0a3d715ca`; signer SHA256 remains `6f85ae49982e0b38b9f134b6882fdbe4da6091051f828fb980c5a03fff439535`. Physical Shield install succeeded. With Deezer fully stopped, a real EastEnders launch logged `Skip trailer click accepted: true` and `Trailer skipped` about two seconds after episode launch. Back from playback then logged `Return episode focus accepted: true`, and the actual first episode node was `focused=true`. Natural episode-end return remains a separate final user acceptance case.
## EastEnders 1.6 return-focus repair installed, 2026-09-12

Published source commit: e067bbdbb64f93740eb74dde758a72641147bf19 on oop-v125-animation-integration.

Ryan reproduced an iPlayer post-playback return where the EastEnders programme page was visible but the physical remote could not navigate. Live Shield evidence captured Android InputDispatcher with no focused application or focused window even though iPlayer PlayerActivity was resumed. One injected D-pad event restored the iPlayer window and navigation, isolating the failure to lost focus rather than a dead page or EastEnders key interception.

Standalone `uk.local.eastenders` versionCode 7 / versionName 1.6 adds a bounded one-shot return-focus guard after the helper's own newest-episode click. The guard must first observe the EastEnders programme page disappear for playback, preventing an early transition race. If the EastEnders page later returns while the guard is active, it requests accessibility focus on the first real episode card and then disarms. No D-pad/Back interception, new permission, iPlayer modification, or BOOP Unified behavior change.

TDD: the new return-state regression was observed failing before `returnPageReady` existed, then passed after the state-machine implementation. Fresh build passed RouteTest, ClickGateTest, AutoplayPolicyTest, LaunchPolicyTest and the artwork-preservation regression. Signed APK SHA256 `a543957c1c48335ce0ca4904a47f6e1ccd11f22aea7ab3d04f4ee501fcdabf86`; signer SHA256 `6f85ae49982e0b38b9f134b6882fdbe4da6091051f828fb980c5a03fff439535`, matching 1.5. Installed 1.5 APK was privately preserved before update.

Physical proxy verification on Shield: shortcut launch auto-selected the existing iPlayer profile, found EastEnders, clicked the newest episode, observed the programme page disappear, then Back from playback returned to the programme page. The helper logged `Return episode focus accepted: true`; a fresh hierarchy showed the first episode card `focused=true`. This validates the return path without waiting for a full episode to finish. The original natural end-of-episode reproduction should still be treated as the final user acceptance case.
## Completed 1.5 artwork update, 2026-09-11

Published source/artwork commit: `b2106b1308477ad29a0ae2cdb866eb5535d8b562` on `boop-v125-animation-integration`.

- package `uk.local.eastenders`, versionCode 6 / versionName 1.5
- signed APK SHA256 `1ac6e4d99340fc4415f105cdef68dde700f05ab88fefe88ba2836781f7909d66`
- signer SHA256 `6f85ae49982e0b38b9f134b6882fdbe4da6091051f828fb980c5a03fff439535`, identical to installed 1.4
- four existing Java suites passed; artwork-preservation build regression passed
- supplied 16:9 banner and direct-crop square icon are packaged in the APK; packaged resources manually inspected
- physical Shield `adb install -r` succeeded; Android reports 1.5 installed at 2026-09-11 23:49:07
- prior installed 1.4 APK privately preserved, SHA256 `302071a90e2ee6941cdb8154f614d9c1d0ef491ba88aa508f935d82c70433093`
- no permission/settings/iPlayer/BOOP changes; user visual acceptance of the final tile remains separate

The earlier WIP section below is historical and superseded for implementation/install state.

# EastEnders artwork update: WIP, not installed

Recorded 2026-09-11 after the laptop Remote Desktop Commander bridge stopped responding. This commit publishes only this handoff, not the locally prepared app source or artwork. Do not report completion or infer an installation from the generated mock-up shown in the chat; that image was rejected and is not evidence.

## Approved scope and correct app

Ryan approved applying the supplied EastEnders map artwork as the wide launcher banner, creating a matching square icon, publishing the changes to GitHub, and installing the updated shortcut on his connected Shield. Keep the lettering centred. Preserve the existing iPlayer behaviour and existing standalone signer. No new permissions or BOOP app update are needed.

The button is the separately installed `uk.local.eastenders`, not a built-in BOOP action. Actual Shield query reported versionCode 5 / versionName 1.4. BOOP's `unified/shield-home/src/main/java/com/boop/shieldhome/TvAppCardView.java` loads the target application's banner/icon through PackageManager, so updating the shortcut's resources supplies its new art to BOOP.

## Recovered source and local isolation

The existing standalone project was recovered at `%USERPROFILE%/Documents/Codex/2026-09-10/referenced-chatgpt-conversation-this-is-an/work/eastenders`. It was not a Git checkout. The GitHub account listing returned the BOOP repository only; no existing EastEnders branch was found in the live BOOP branch listing.

A task-owned detached worktree was created at `%USERPROFILE%/Documents/Codex/BOOP/.worktrees/eastenders-artwork-20260911`, based on live `boop-v125-animation-integration@f93e85128982a3bc1399fe75d4086dcf73964bb2`. The `.worktrees` ignore rule was verified first. No concurrent worktree was switched or edited.

Only the existing standalone `src`, `res`, `tests`, manifest, build script and README were copied into its new `eastenders-shortcut/` folder. Existing standalone signing files were copied privately into the new project's local signing directory for the same-key update; they must NEVER be staged or published.

IMPORTANT: writing `eastenders-shortcut/.gitignore` was the first operation to time out. Its existence/content is unverified. Before any staging, ensure `/build/`, `/signing/`, `/test-classes/`, `/test-out/`, `*.apk`, `*.apk.idsig`, `*.p12`, `*.jks`, and `*.keystore` are ignored. Use explicit reviewed staging paths, not an unreviewed recursive add.

## Artwork prepared locally

Approved originals remain in `%USERPROFILE%/Downloads/EastEnders-Shield-20260911-224831`.

Source: https://image.tmdb.org/t/p/original/rzfia5PauRCY0NezLhGBpzUjDTy.jpg

Original dimensions 680 x 1000; SHA256 `642e23d60e2de46fd25767038de6f071f89832f78b4388affaf9b87fa8058bcd`.

The previously approved 640 x 360 wide image and 320 x 180 banner were retained unchanged. The square is a direct source crop, rectangle x=30, y=34, width=640, height=640, with the original lettering retained. It was also resized to a 192 x 192 launcher icon. No generated substitute art is used.

The local project has the original and both 640-pixel masters under `artwork/`; `res/drawable-nodpi/banner.png` is the approved 320 x 180 banner, and `icon.png` is the 192 x 192 square.

Resource SHA256 values:
- banner: `1f5623bb500f27252eeebeb98c379aeb765bbf9ed57bdee56f8ca5fef175b2e1`
- icon: `fc26cd1156555713fd32f62cfc1765cf9347744548629bf918fb577b8e715c4a`

The source is third-party artwork supplied by the user. Do not describe availability on TMDB as a free redistribution licence or imply BBC endorsement. Add accurate provenance in the source documentation.

## Local changes and evidence

- Found the concrete build issue: `build.ps1` regenerated its old typographic placeholder banner and icon on every build.
- Added `tests/BuildAssetsTest.ps1`, which executes actual pre-compilation preparation in a private temporary directory and checks that supplied resources remain byte-identical. It first failed with `Build overwrote approved banner resource`.
- Replaced only the placeholder-generation block with required-resource checks. The same regression then passed. It checks build integrity, not appearance.
- Manifest bumped to versionCode 6 / versionName 1.5; build output paths changed to `EastEnders-1.5.apk`. Added the asset-preservation regression to the build.
- All six existing application Java files are unchanged. Existing RouteTest, ClickGateTest, AutoplayPolicyTest and LaunchPolicyTest passed before build-script modification.
- The original README is stale 1.0 documentation and still needs correction to reflect existing 1.4 behaviour and the 1.5 artwork-only scope.
- A full APK build has NOT run. No new APK has been signed, verified, installed or physically accepted. No device permissions, settings or playback were changed.

PowerShell's default script policy rejected direct script invocation before the RED run; the tests were then run with a child `powershell.exe -NoProfile -ExecutionPolicy Bypass -File ...`. No persistent execution-policy change was made.

## Next safe steps

Reconnect the existing 0.2.47 laptop bridge, inspect the actual local files, fetch current main and the intended continuation branch, and reconcile this documentation commit without disturbing the untracked local project. Verify ignore rules before staging anything. Finish README/provenance and review exact scoped changes. Use the installed JDK 17 / Android SDK 36 tools and original standalone signing identity; never generate a replacement key for this update.

Preserve the actual installed shortcut APK privately, verify its signer against the new APK, verify package/version and packaged resources, rerun all tests, publish only reviewed source/artwork/documentation, then perform the user-authorized `adb install -r` against the verified physical Shield. Confirm the installed version and actual launcher artwork without unnecessarily starting iPlayer playback. Record build/hash/signer/install results and verify the live GitHub HEAD before reporting success. No GitHub visual tests or BOOP Unified build is needed for this standalone resource update.
