# BOOP Shield Home inventory / 80% scale build receipt

Updated 2026-09-07. Owning branch: `boop-shield-media-puppetry`.

## Provenance

- Application/build head: `794aacaa9f179721eb5a1eb4ddb2ec6e16a6d73d`.
- Documentation handoff commit follows the build and does not alter APK bytes.
- Package: `com.boop.shieldoverlay`.
- GitHub Actions run: `34085857386`.
- Job: `101629645397`.
- Artifact: `10005203826` / `BOOP-Shield-Overlay-POC-debug`.
- Artifact ZIP digest reported by GitHub: `sha256:439a72b2e509f3c59842157961721209cbd049af45f1c78390afb3e11cdef9b5`.
- Delivered APK: `BOOP-Shield-Home-Inventory-80pct.apk`.
- APK SHA-256: `e738b171373ed854064dd6f71966d652b4e87c405bf58d7b1a85f8a1daf5f69a`.
- Existing permanent BOOP development signer continuity: PASS in CI.

## Requested behavior

Ryan supplied physical Shield evidence that BOOP Home showed one Living Room
favourite (`AI Sync Box strip`) and clipped `Rooms` text underneath. D-pad Down did
not expose the rest of the room. The source root cause was structural: only one
selected favourite was placed into `HomeDashboardController.ViewState` and
`TvHomeView`, although `HomeAssistantRepository` already returns the complete set of
eligible room controls.

The candidate retains the complete room card list, materializes it in a vertical
Home `ScrollView`, keeps the existing favourite first, and routes Select on every
rendered device card through the existing exact-card binary toggle path. Activity
UI density is 80% of the Shield's normal density so Home, Routines, Settings,
pairing and other app activities render 20% smaller. `BoopOverlayService` is not
scaled; accepted Deezer puppet/eye geometry is preserved.

## Verification boundary

Ryan explicitly requested no tests. The final workflow push used
`[boop-build-only]`. In run `34085857386`:

- BOOP source regression suite: SKIPPED.
- Shield unit tests: SKIPPED.
- APK assembly: PASS.
- Package/permission inspection: PASS.
- Stable signer verification: PASS.
- Artifact upload: PASS.

No emulator or physical runtime verification is claimed for this candidate.
Physical acceptance remains Ryan's next step. Existing Home/Routines checkpoints
are unchanged and must not be repointed from build-only evidence.
