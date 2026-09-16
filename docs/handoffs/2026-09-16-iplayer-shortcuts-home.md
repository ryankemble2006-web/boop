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

