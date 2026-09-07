# Shield full-screen Deezer puppet handoff — 2026-09-07

Owner: isolated Shield experiment on `boop-shield-fullscreen-deezer-wip`.
Base lineage: live `boop-shield-media-puppetry@4f57a5b45adec5d4872dd83ec9301093f4c4d5d5`.
Package: `com.boop.shieldoverlay`.
Current candidate: versionCode 3 / `0.3-friendly-deezer-access`.

## Latest physical evidence

Ryan installed the v2 full-screen puppetry build. BOOP showed ordinary compact eyes instead of entering full-screen Deezer mode. In BOOP Home -> Settings the Deezer status read `Access needed`, and the explanatory text confirmed Android notification-listener access was not granted.

That is the current physical root cause for the ordinary-eye fallback: without Android's notification-listener grant, BOOP cannot observe Deezer's media session and therefore remains in `EYES`. Do not claim why Android lost/withheld the grant; only the missing grant itself is physically confirmed.

The v1 large full-screen/debug presentation previously reached the Shield, so preserve its useful diagnostics. The accepted corner-H1 branch remains untouched.

## v3 friendly Deezer access flow

Android notification-listener access is special settings access, not a normal runtime permission, so BOOP cannot silently grant it or show the standard runtime Allow/Deny sheet. v3 makes the required user action feel like normal first setup:

- The first time BOOP reaches Home, it offers `Let BOOP see Deezer playback?` once.
- The copy explains that Android calls this Notification access and that BOOP ignores notification contents, using only Deezer playback state for the headphone/music puppet.
- `Continue` enables the Deezer puppet feature and, if access is not already granted, opens Android settings for BOOP's own notification-listener component.
- On API 30+, BOOP first tries `ACTION_NOTIFICATION_LISTENER_DETAIL_SETTINGS` with `EXTRA_NOTIFICATION_LISTENER_COMPONENT_NAME` set to the BOOP listener component string.
- If the Shield does not expose that detail screen, BOOP falls back to `ACTION_NOTIFICATION_LISTENER_SETTINGS`.
- Older Android goes directly to the generic notification-listener screen.
- Returning to BOOP refreshes the access state immediately.
- `Manage Deezer access` uses the same detail-first/fallback launcher.
- The one-time offer is persisted. Choosing `Not now` does not nag on every launch; Settings remains the manual route later.
- Existing `one-time computer setup` wording is now only the last fallback if Android exposes neither settings route.

No permission is granted automatically. The user still has to toggle BOOP on in Android's system UI.

## Full-screen puppet behaviour retained

When Deezer observation becomes eligible, the v2 puppetry remains intact:

- eligible Deezer headphone states own the full TV with a pure-black noninteractive application-overlay canvas;
- `PLAYING` uses the richer 3.6-second `FullscreenPuppetMotion.groove` and accumulated media clock;
- `PAUSED` settles the current pose to neutral over 520 ms;
- skip states 9/10/11 trigger the 700 ms perk/lift/tilt acknowledgement;
- remote input remains pass-through via `FLAG_NOT_FOCUSABLE` + `FLAG_NOT_TOUCHABLE`;
- BOOP Home hide/show, compact `EYES` fallback, H1 asset, debug machinery, HA auth/socket, Home, Routines and signing are preserved.

The existing design still follows Deezer session state rather than third-party foreground-app identity. Do not add UsageStats/accessibility or broad foreground tracking without a new explicit user decision.

## TDD and verification

Friendly-access TDD RED: workflow run `34100353543` failed at Shield unit-test compilation because the new tests intentionally referenced the not-yet-existing one-time setup preference API and `DeezerAccessSettingsPlan`.

GREEN build commit: `b5e9e33bf1b98bcb94176afeea3fb21ad99ab9fa`.
GitHub Actions run: `34100989718` completed successfully.

Passed on that exact build commit:
- 46 Python source regression tests, including the friendly first-home access offer and detail-settings/fallback wiring;
- complete Shield JVM/unit suite, including the new one-time preference and settings-route tests plus existing fullscreen puppetry/state/geometry tests;
- stable-signed APK assembly;
- package/version/permission inspection;
- permanent BOOP signer continuity;
- artifact upload.

Candidate identity: versionCode 3 / `0.3-friendly-deezer-access`.
Artifact: `BOOP-Shield-Fullscreen-Deezer` / ID `10010497159`.
APK SHA-256: `06ef591b117720334f6a9c2c6b7f0c8f8e66dc448a375ad9fb9b35b68fc617c9`.
Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

CI green is not physical green. No install, data clear or Android permission grant is claimed.

## Preserve

- `boop-shield-media-puppetry` and its physically accepted corner-H1 placement remain untouched.
- `checkpoint-shield-home-f8e8135` and `checkpoint-shield-routines-3fa18c6` remain protected.
- Existing HA auth/socket, Home, Routines, pairing, overlay pass-through and signer are preserved.
- Keep useful debug/diagnostic machinery available for later work.
- Do not silently grant Android special access.

## Physical test next

Install v3 over the current Shield BOOP without uninstalling or clearing data.

Because the v3 one-time offer flag did not exist in v2 data, first entry to BOOP Home should show the new Deezer access prompt. Choose `Continue`.

1. Confirm Android opens BOOP's own Notification access detail page if Shield supports it; otherwise confirm the generic Notification access list appears.
2. Toggle BOOP on and press Back.
3. Confirm BOOP Settings changes from `Access needed` through `Connecting` to `On`.
4. Start Deezer playback and confirm full-screen black BOOP/H1 appears.
5. Check play/pause/resume/skip puppetry and remote pass-through.
6. If access reaches `On` but ordinary eyes remain, next debugging target is Deezer media-session discovery/package/state, not the renderer.

Do not promote or move any physical checkpoint until Ryan reports the actual Shield result.
