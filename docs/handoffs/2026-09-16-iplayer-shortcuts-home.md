## Standalone iPlayer shortcuts accepted on Shield — 16 September 2026

Current owner: `iplayer-shortcuts-home-20260916`. Current accepted source: `acb814d392fd5ce207930121e85391a46e5ec1f8`.

The EastEnders and Casualty shortcuts are now fully standalone at runtime. Each APK contains its own private local-ADB cleanup service/key, does not call BOOP/Startup Manager or the sibling shortcut, stores no BBC login/password/profile name, and uses the first real existing iPlayer profile tile. The silent cleanup path is hardcoded to `am force-stop --user current com.nvidia.bbciplayer` plus process verification before launch/after playback exit.

Physical diagnosis found the Shield only surfaced `com.android.systemui/.usb.UsbDebuggingActivity` when both normal USB debugging and Network debugging were enabled. With only Network debugging enabled, `adbd` logged a pending auth request but SystemUI never created the approval activity. Dad-facing setup instructions now explicitly require both debugging switches before Authorise. Each shortcut intentionally has its own private ADB identity, so a fresh Shield requires one approval per shortcut.

EastEnders was physically accepted end-to-end on the Shield after its one-time approval. Logs confirmed the private key became authorised, iPlayer was silently force-stopped and background processes verified dead, the macro armed, and the first profile was selected. Ryan then physically confirmed rapid launch into EastEnders, pressed Back from playback, and iPlayer was killed and Home restored. He repeated the flow a second time and reported it was perfect.

During acceptance logs exposed an old two-profile handoff rule that allowed a second profile click after 1.5 seconds. That behavior was removed. Current source allows exactly one profile click per launch and the setup page now says USB debugging + Network debugging. TDD run `35065326525` failed on the old two-click/setup contract; final run `35065452329` passed the targeted contract, both programme suites, both Android builds and artifact upload.

Exact final signed artifacts from run `35065452329` were installed on the Shield with original signers preserved: EastEnders 1.9 SHA256 `0215bffae131bebc4bf8bb0dc45b9acfc6b528ad26c42cbad95fbed971f13070`; Casualty 1.3 SHA256 `7a4706a722c1ec1f6bf7a7d15ea0865df7282901cf7b63a1204d7ec2f5777ea8`. Existing accessibility helpers remained enabled. BOOP Unified and iPlayer data/login were not modified.

**Remaining physical acceptance:** Casualty still needs its own one-time ADB approval on this Shield because it deliberately owns a separate private key. After approval, verify its first-profile-once launch and Back-exit silent kill. The shared implementation/tests are green, but do not claim Casualty physical acceptance until that separate prompt/test is completed.

## iPlayer force-stop confirmation fix — 16 September 2026

Current owner: `iplayer-shortcuts-home-20260916`. Current code candidate: `c522f122f96f9096ec0abbfeac6517d1c0d33f06`.

Ryan physically reported that the startup force-stop flash was acceptable, but Back/exit left a Force stop confirmation requiring manual input and could appear twice. The cleanup click path was therefore split from elapsed-time debouncing into an explicit one-shot `CleanupSequence`: one Force stop request, one confirmation request, then two stopped observations before success. The old timer-only duplicate guard is gone.

Runtime diagnosis on Shield then isolated the remaining confirmation failure. With iPlayer warm, the helper successfully logged `Requested iPlayer Force stop`; `onInterrupt()` did not fire, the service remained alive, and its 12-second watchdog eventually timed out while Android TV's guided `OK` confirmation was visibly present. `uiautomator` could see that guided confirmation while the service's single `getRootInActiveWindow()` scan could not. This ruled out the earlier interruption hypothesis and identified a second interactive Settings window as the missing observation surface.

The current candidate adds `flagRetrieveInteractiveWindows` to both accessibility-service configs and scans all Settings roots returned by `getWindows()` as well as the active root. The existing one-shot cleanup sequence remains the only click gate, so simultaneous/stale App Info and guided-confirmation windows cannot emit a second accepted Force stop or OK action. EastEnders and Casualty are kept implementation-parallel.

TDD/CI evidence: contract commit `983ac3e303a7a7463832c5951b24b1448b487736` made the iPlayer workflow trigger on its root contract and run `35059348348` failed exactly because interactive-window retrieval was absent. After `c522f122`, run `35060123770` passed the targeted functional contract, both programme test suites, both Android builds and artifact upload. No GitHub visual test was added.

**Next physical step only:** when Yoga/ADB is reachable, sign the exact run `35060123770` artifact with the existing original EastEnders/Casualty keys, install the two candidates, and jointly verify (1) warm iPlayer startup auto-clicks Force stop and OK once, then launches normally, and (2) a real launcher-tile playback Back/exit auto-clicks Force stop and OK once, verifies iPlayer stopped, and returns BOOP Home. Do not treat shell-launched task-stack Back behavior as physical tile acceptance. No permission/settings changes are required.

## Both iPlayer shortcuts: direct launch and return Home — 16 September 2026

Current owner: `iplayer-shortcuts-home-20260916`. Built source: `4a8701d346898c71e981426df7b047e7994a5e32`.

EastEnders 1.8 (code 9) and Casualty 1.2 (code 3) remove the old shortcut selector/recovery UI and finish the launcher activity after dispatching the existing deep link. Both retain the existing profile/newest-episode automation and identical exact visible/enabled `Skip trailer` handling, once within the first 60 seconds.

The existing post-playback episode-focus repair is replaced with one-shot Home. The helper must first see the programme page disappear, then see its title and real newest-row episode card return. Player pause/title overlays without that grid card do not qualify. The guard expires after two hours and cancels when another app gains the foreground. Accessibility foreground events are no longer package-filtered, but only the expected iPlayer root is traversed; other-app events only cancel the armed session. No new Android permission or enabled-service setting was granted. A rejected episode click disarms the session. Tree traversal now enforces the existing 1,500-node limit through the child loop.

Verification: initial return-Home test failed in run 35052717646. Review found a legacy filtered-event cancellation hole; its integration regression failed in run 35052861624 before the fix. Final [GitHub run 35052948737](https://github.com/ryankemble2006-web/boop/actions/runs/35052948737) passed five Java suites per programme, the service event-delivery contract, both APK builds and artwork checks. Independent read-only review cleared the foreground fix with no remaining concrete source blocker. SDK setup failures were infrastructure issues (missing setup and a retired tools package), corrected in the final successful workflow.

GitHub built aligned unsigned artifacts. The original shortcut keys exist only locally, so those exact artifacts were signed locally without uploading the keys or compiling app source locally. Signatures match the previous installed packages. Every unsigned APK entry was verified identical after signing; packaged banner/icon bytes equal the previous installed APKs.

Both APKs were installed on the physical Shield with `adb install -r`; read-back APK hashes and installed package/version match. Existing enabled accessibility-service list is identical, and both helpers are bound. Original installed APKs were backed up privately before updating. BOOP Unified, other devices, iPlayer settings and shortcut artwork were not changed.

**Physical acceptance pending:** jointly check each tile, an offered trailer, pause/resume, stop/Back return Home, and natural episode completion. Source/CI/install verification is not a claim that these visible playback paths have been accepted. Casualty already contained trailer handling in its prior source and installed baseline; this update explicitly aligns and checks both implementations rather than treating historical documentation as device acceptance.

- `eastenders` 1.8: APK SHA256 `54ada3a32ba6c7af0678513ee77681653409f1abf78e4c8c738220fb30ab2d54`; signer `6f85ae49982e0b38b9f134b6882fdbe4da6091051f828fb980c5a03fff439535`.
- `casualty` 1.2: APK SHA256 `94645fe3b2c7191c24686f126b0c75d72e58095c05476e26c2370b72d018a7ef`; signer `d198e64f5cce0ccc77201bebd41db0283832be7b05476e26c2370b72d018a7ef`.

