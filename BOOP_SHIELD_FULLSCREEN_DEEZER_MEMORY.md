# BOOP Shield Full-screen Deezer Memory

Updated 2026-09-07.

Current experiment branch: `boop-shield-fullscreen-deezer-wip`.
Base lineage: `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
Current candidate: versionCode 3 / `0.3-friendly-deezer-access`.
Build commit: `b5e9e33bf1b98bcb94176afeea3fb21ad99ab9fa`.
Green workflow run: `34100989718`.
Artifact ID: `10010497159`.
APK SHA-256: `06ef591b117720334f6a9c2c6b7f0c8f8e66dc448a375ad9fb9b35b68fc617c9`.
Package/signing remain `com.boop.shieldoverlay` with the permanent BOOP development signer.

## Product decision

For Deezer music, BOOP is being tested as a full-screen puppet rather than a corner overlay. Deezer remains the underlying player and source of media-session state. BOOP owns the picture with a pure-black canvas and large centred H1 puppet.

Ryan liked the usefulness of the large debug/diagnostic presentation and asked that it be kept handy for later. Do not delete useful diagnostics during puppet-polish work merely to make the screen cleaner.

## Latest physical finding

Ryan installed v2 and BOOP stayed as ordinary compact eyes instead of entering full-screen mode. BOOP Home -> Settings reported `Access needed`, with Android notification-listener access `Not granted`.

Therefore the currently confirmed blocker is the missing Android notification-listener grant. Without it, BOOP cannot observe Deezer's active media session and correctly remains in `EYES`. Do not claim a reason for the grant being absent beyond that observed state.

## Friendly access contract introduced in v3

Notification-listener access is Android special settings access, not a normal runtime permission. BOOP must never silently grant it.

- First entry to BOOP Home offers `Let BOOP see Deezer playback?` once.
- Copy explains Android's Notification access label and BOOP's narrow use of Deezer playback state while ignoring notification contents.
- `Continue` enables Deezer puppet and opens Android access settings if the grant is missing.
- API 30+ tries BOOP's dedicated notification-listener detail activity first using the BOOP listener component string.
- If unavailable, use the generic Notification access list. Older Android uses the generic list directly.
- Returning to BOOP refreshes the access state.
- `Manage Deezer access` uses the same route.
- Choosing `Not now` records that the setup offer happened, preventing repeated nagging; the Settings route remains available later.
- Only if both Android settings routes fail should the UI fall back to computer-setup wording.

## Full-screen puppetry retained

- PLAYING uses the richer 3.6-second periodic `FullscreenPuppetMotion.groove` and keeps the accumulated media clock.
- PAUSED captures the current pose and settles to neutral over 520 ms.
- Resume continues the accumulated clock rather than restarting at phase zero.
- Deezer skip states 9/10/11 trigger a 700 ms perk/lift/tilt acknowledgement.
- Other REST states remain neutral and quiet.
- Full-screen BOOP remains a black, non-focusable, non-touchable application overlay so remote/media input passes through.
- Ordinary fallback `EYES`, Home hide/show, H1 asset, HA auth/socket, Home, Routines, pairing and useful diagnostics are preserved.
- The WIP still follows eligible Deezer session state even if Deezer continues playing in the background. Do not add UsageStats/accessibility foreground tracking without a new explicit decision.

## Verification evidence

Friendly-access TDD RED run `34100353543` failed at Shield unit-test compilation because the newly added tests referenced the intentionally absent one-time setup preference methods and `DeezerAccessSettingsPlan`.

GREEN run `34100989718` passed on build commit `b5e9e33bf1b98bcb94176afeea3fb21ad99ab9fa`: 46 Python source regressions, complete Shield JVM/unit suite, friendly access route/preference tests, existing fullscreen puppet motion/state/geometry tests, stable-signed APK assembly, package/version/permission inspection and signer continuity.

Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

This is not a physical checkpoint. Install v3 over v2 without clearing data, use the first-home Continue path, toggle BOOP on in Android Notification access, return and confirm BOOP reaches `On`, then retest Deezer fullscreen puppetry.

The source `boop-shield-media-puppetry` branch and its physically accepted H1 placement remain untouched.
