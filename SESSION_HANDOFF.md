# Shield full-screen Deezer puppet handoff — 2026-09-07

Owner: isolated Shield experiment on `boop-shield-fullscreen-deezer-wip`.
Base lineage: live `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
Package: `com.boop.shieldoverlay`.

## What this candidate does

Ryan asked to turn the Shield music puppet into effectively **Wall BOOP owning the whole TV** while Deezer remains the real player underneath.

This first WIP deliberately preserves the existing Deezer observer/state/clock path and H1 headphones asset. When the existing Deezer policy enters either `HEADPHONES_REST` or `HEADPHONES_PLAYING`:

- the application overlay expands to the complete Shield display;
- the canvas becomes pure black;
- the existing H1 headphones puppet is centred and enlarged to roughly Wall-BOOP scale;
- the existing Deezer state continues to drive rest/play animation exactly as before;
- the overlay window remains `TYPE_APPLICATION_OVERLAY` + `FLAG_NOT_FOCUSABLE` + `FLAG_NOT_TOUCHABLE`, so Shield/Deezer remote input continues through to the underlying player;
- BOOP Home still uses its existing hide/show action, so opening BOOP Home hides the full-screen puppet and returning restores it.

When Deezer falls back to `EYES` (feature off, no grant/session, stopped/unsupported state), the overlay returns to the existing small ordinary-eye geometry. No new microphone, accessibility, usage-history, HA socket, network path or foreground-app permission was added.

## Important behaviour boundary

The existing Deezer design intentionally permits background playback and does **not** identify which third-party app is currently foreground. This WIP therefore uses the same signal: while Deezer has an eligible headphone state, BOOP owns the full screen even if Deezer has technically continued playing in the background.

That is intentional for this first physical experiment. Do not add UsageStats/accessibility or other broad foreground-tracking access without a new explicit user decision. If physical testing proves exact Deezer-only foreground gating is required, investigate a narrow Shield-safe signal separately.

## Implementation

- New `FullscreenDeezerGeometry` centres H1 on the entire display and uses the measured H1 nod/sway envelope to prevent clipping at HD/UHD and smaller fixtures.
- `BoopOverlayService` switches its existing overlay window between normal compact geometry and full-display geometry based only on the existing `DeezerPuppetPolicy.Mode`.
- `BoopOverlayView` paints black only in non-`EYES` Deezer puppet modes; ordinary overlay eyes retain their transparent background.
- Existing `MediaPuppetFrameLoop`, `DeezerSessionObserver`, play/pause policy, H1 renderer, animation-scale handling and Power Saver behaviour are unchanged.

## Verification

GitHub Actions workflow `Build BOOP Shield Fullscreen Deezer` run `34096866418` built commit `cd56e3bd0bf4ff6547e8cd2ea45631cde1418c36` successfully.

Passed:
- full Python source regression suite;
- full Shield JVM/unit test suite;
- new full-screen geometry/envelope tests;
- stable-signed APK assembly;
- package/permission inspection;
- permanent BOOP signer continuity;
- artifact upload.

Artifact: `BOOP-Shield-Fullscreen-Deezer` / ID `10008967780`.
Extracted APK SHA-256: `bb225e7c7f9fbfed13d758155b42208482921c4ef815d509e78dc5bb5edf5b4e`.

CI green is not physical green. No Shield install or permission change was performed from this task.

## Preserve

- `boop-shield-media-puppetry` and its physically accepted H1 placement remain untouched.
- `checkpoint-shield-home-f8e8135` and `checkpoint-shield-routines-3fa18c6` remain protected.
- Existing Deezer notification-listener access state is not changed.
- Existing HA auth/socket, Home, Routines and signer are preserved.

## Physical test next

Install the candidate over the existing Shield BOOP without clearing data. With the existing Deezer puppet feature enabled:

1. Start/resume Deezer: TV should become black with a large centred H1 BOOP; dance continues.
2. Pause: H1 should remain visible and rest/freeze using the existing pause state.
3. Skip/resume: no reset flash; existing metadata/session behaviour should continue.
4. Verify Shield remote play/pause/skip still reaches Deezer through the full-screen overlay.
5. Open BOOP Home: puppet should disappear; leaving Home should restore the latest Deezer puppet state.
6. Stop/end Deezer session: ordinary small Shield eyes should return.
7. Deliberately leave Deezer playing and navigate elsewhere. Decide whether full-screen BOOP following background music is desirable or whether a future Deezer-foreground gate is needed.

Do not promote or move any physical checkpoint until Ryan reports the actual Shield result.
