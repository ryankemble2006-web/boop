# Shield handoff — 2026-09-07

## Home inventory + 80% app-screen scale — signed no-test candidate

Owner: Ryan's Shield task. Branch: `boop-shield-media-puppetry`.
Application/build head: `794aacaa9f179721eb5a1eb4ddb2ec6e16a6d73d`.
Signed GitHub Actions run: `34085857386`; job `101629645397`.
Artifact: `BOOP-Shield-Overlay-POC-debug` / artifact `10005203826`.
Delivered APK: `BOOP-Shield-Home-Inventory-80pct.apk`.
APK SHA-256: `e738b171373ed854064dd6f71966d652b4e87c405bf58d7b1a85f8a1daf5f69a`.
Package remains `com.boop.shieldoverlay`; existing permanent BOOP development signer verified by CI.

### User evidence and root cause

Ryan physically showed BOOP Home on the Shield: Living Room / Favourites displayed
only `AI Sync Box strip`, while the next `Rooms` heading was clipped below the fold.
D-pad Down did not reveal the other Living Room devices. Source inspection found the
root cause: `HomeDashboardController.ViewState` exposed only one selected favourite,
and `TvHomeView` only created one favourite card. This was not merely a broken
ScrollView; the rest of the room inventory was never materialized into the Home UI.

### Scoped implementation

- `HomeDashboardController` now retains the complete list returned by the existing
  Home Assistant room dashboard query while preserving the existing favourite-first
  ordering and cached-favourite offline fallback.
- `TvHomeView` is now a real vertical `ScrollView`; it materializes every simple
  on/off room device under Favourites, keeps the existing favourite first, and D-pad
  focus naturally moves/scrolls through the remaining cards.
- Every materialized device card routes Select through the same existing
  `HomeAssistantRepository.toggleBinary` path. No second HA socket or new entity
  targeting logic was added.
- App activity UI density is overridden to 80% in `BoopApplication`, reducing Home,
  Routines, Settings, pairing and crash-report screens by 20%. The noninteractive
  puppet overlay service is intentionally excluded so accepted Deezer eye placement
  and overlay geometry are not silently rescaled.
- The Shield workflow now recognizes `[boop-build-only]` for user-requested fast
  signed builds: source regressions and Shield unit tests skip, while APK assembly,
  package/permission inspection, permanent signer verification and artifact upload remain.

### Verification boundary

Ryan explicitly requested: **do not test; sign**. Final delivery run `34085857386`
shows both source regression and Shield unit-test steps as SKIPPED. APK assembly,
package/permission checks, stable signer verification and artifact upload PASS.
No emulator or physical runtime test is claimed. The next evidence must come from
Ryan's Shield: confirm that Living Room devices are all visible/reachable, Select
controls the intended device, and the 80% activity UI scale feels right.

## Preserve

- checkpoint-shield-home-f8e8135
- checkpoint-shield-routines-3fa18c6
- H1 physically accepted lower placement from application commit `4fe28a4`
- Existing Deezer observer/puppet behavior and Android access state
- Existing HA socket/auth/signing/permissions
- No Google Assistant fallback and no cancelled setup-heavy Deezer Cast bridge

## Previous settings-scroll candidate

The prior Settings-specific remote-scroll candidate remains recorded at application
commit `7cd636086ea7991659550cf8d5b87da321eab5f3`, workflow commit
`6b87b682e22bbeac09635d7f340b3c328a700507`, signed run `34083898275`.
That change addressed Settings navigation only. Ryan's later Home screenshot proved
the Home inventory issue had a different root cause, now addressed above.

## Next safe step

Install `BOOP-Shield-Home-Inventory-80pct.apk` over the existing Shield app and test
from the sofa. Record only Ryan's actual physical result before promoting or moving
any checkpoint.
