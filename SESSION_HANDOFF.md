# Shield full-screen Deezer puppet handoff — 2026-09-07

Owner: isolated Shield experiment on `boop-shield-fullscreen-deezer-wip`.
Base lineage: live `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
Package: `com.boop.shieldoverlay`.
Current candidate: versionCode 2 / `0.2-fullscreen-puppetry`.

## Product state

Ryan physically confirmed the first full-screen candidate reached the Shield and exposed the large full-screen/debug presentation. He asked to preserve the useful diagnostics for later and move on to puppetry. The diagnostics code remains available and was not deleted or redesigned in this motion pass.

When the existing Deezer policy enters either `HEADPHONES_REST` or `HEADPHONES_PLAYING`, BOOP still owns the whole TV with a pure-black application-overlay canvas while Deezer remains the real player underneath. The overlay stays `TYPE_APPLICATION_OVERLAY` + `FLAG_NOT_FOCUSABLE` + `FLAG_NOT_TOUCHABLE`, so remote/media input remains pass-through.

## v2 puppetry pass

The existing Deezer session observer, controller selection, media clock and permission path remain unchanged. v2 changes only the acting/render seam:

- `PLAYING`: H1 uses `FullscreenPuppetMotion.groove(...)`, a richer but still gentle 3.6-second periodic sway/nod with small secondary head lag/listening weight. It reuses the accumulated `MediaPuppetFrameLoop` time, so pause/resume does not mechanically restart the dance.
- `PAUSED`: the current playing pose is captured and eases to neutral over 520 ms rather than snapping to rest.
- Explicit Deezer skip states (`SKIPPING_TO_PREVIOUS`, `SKIPPING_TO_NEXT`, `SKIPPING_TO_QUEUE_ITEM`, playback-state integers 9/10/11): BOOP performs a short 700 ms acknowledgement, peaking around 180 ms with a small lift/tilt, then returns to neutral. This is driven by the already-observed playback state, not metadata polling or audio analysis.
- Other `HEADPHONES_REST` states remain neutral and preserve the old quiet-equivalent-state contract.
- Full-screen geometry tests now cover the richer groove, pause settle and skip accent across HD/UHD so the measured H1 alpha envelope remains on-screen.
- The existing H1 asset, black canvas, ordinary-eye fallback, BOOP Home hide/show, animation-scale handling and Power Saver behaviour remain intact.

## Important behaviour boundary

The existing Deezer design intentionally permits background playback and does **not** identify which third-party app is currently foreground. This WIP therefore continues to use the same signal: while Deezer has an eligible headphone state, BOOP can own the full screen even if Deezer has continued playing in the background.

Do not add UsageStats/accessibility or other broad foreground-tracking access without a new explicit user decision. If physical testing proves exact Deezer-only foreground gating is required, investigate a narrow Shield-safe signal separately.

## TDD and verification

TDD RED: workflow run `34097940701` failed at Shield unit-test compilation because the newly added `FullscreenPuppetMotionTest` referenced the intentionally missing `FullscreenPuppetMotion` class. Source regressions passed before the expected compile failure.

GREEN candidate: build commit `0d9f5e6f2cc3249541667976a41405efd686a52a`; GitHub Actions run `34098619403` completed successfully.

Passed on that exact candidate:
- full Python source regression suite;
- complete Shield JVM/unit suite, including `FullscreenPuppetMotionTest`, preserved `MediaPuppetStateTest`, explicit track-change delivery and full acting-envelope geometry tests;
- stable-signed APK assembly;
- package/permission inspection;
- permanent BOOP signer continuity;
- artifact upload.

Artifact: `BOOP-Shield-Fullscreen-Deezer` / ID `10009620031`.
Extracted APK SHA-256: `ea9fa94f0868943cf559b5b8e1406dcf28e07264a415aed7d58e7cc61c292a18`.

Branch review against the first full-screen candidate `2aa4608a80dfdbc1193d9e4acf41aeeb1292c953` shows the app changes are confined to fullscreen puppet motion/state/rendering, version identity, and focused tests. HA, permissions, Deezer observer/session ownership, playback controls, pairing and signing were not changed.

CI green is not physical green. No install, data clear or permission change is claimed from this task.

## Preserve

- `boop-shield-media-puppetry` and its physically accepted corner-H1 placement remain untouched.
- `checkpoint-shield-home-f8e8135` and `checkpoint-shield-routines-3fa18c6` remain protected.
- Existing Deezer notification-listener access state is not changed.
- Existing HA auth/socket, Home, Routines and signer are preserved.
- Keep the useful diagnostic/debug machinery available for later work; do not delete it merely to clean the puppet presentation.

## Physical test next

Install v2 over the current Shield BOOP without clearing data. With the existing Deezer puppet feature enabled:

1. Play Deezer: confirm the richer large-centred groove feels alive rather than mechanically rotated.
2. Pause mid-motion: confirm BOOP settles naturally over roughly half a second instead of snapping.
3. Resume: confirm the groove continues rather than visibly restarting at phase zero.
4. Skip next/previous: confirm a brief perk/lift appears, then returns cleanly to the groove once PLAYING resumes.
5. Verify remote play/pause/skip still reaches Deezer through the full-screen overlay.
6. Open BOOP Home and return; verify hide/show and latest Deezer state recover correctly.
7. Stop/end the Deezer session; ordinary compact Shield eyes should return.
8. Deliberately leave Deezer playing and navigate elsewhere; decide later whether the background-music full-screen behaviour is desirable.

Do not promote or move any physical checkpoint until Ryan reports the actual Shield result.
