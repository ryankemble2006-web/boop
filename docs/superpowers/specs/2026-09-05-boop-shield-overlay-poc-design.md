# BOOP Shield Transparent Overlay POC — Design

Date: 2026-09-05
Status: Approved architecture, written-spec review pending
Reference puppet checkpoint: `776e75cda3793aa43eeaa5ef80ec77d163ff0e44`

## Purpose

Build a deliberately disposable, isolated proof-of-concept APK for Nvidia Shield that proves BOOP's approved eyes can exist as a persistent transparent Android TV overlay above the launcher, Kodi, video playback, and app switching without taking remote focus or dragging any Pixel/HA/speech functionality into the experiment.

The POC answers one question: can BOOP's eyes safely escape the app surface and remain present as a small non-interactive puppet overlay across normal Shield use, including HDR/Dolby Vision mode changes?

## Hard Scope

The APK contains only:

- BOOP's approved eye artwork and existing puppetry derived from checkpoint `776e75c`.
- One launch activity whose only job is to obtain Android's "display over other apps" permission and start the overlay service.
- One foreground service that owns the overlay window.
- One transparent, non-focusable, non-touchable `TYPE_APPLICATION_OVERLAY` window containing the eye view.
- The minimum notification/channel required by Android for a foreground service.

Explicitly excluded:

- Home Assistant.
- Microphone access.
- Speech recognition or synthesis.
- Settings UI.
- Touch/click interaction.
- Remote/key handling.
- Black or opaque background.
- Network access.
- Accounts, storage, analytics, telemetry, boot receivers, accessibility services, or any other integration.
- Changes to the existing BOOP Pixel APK/package.

## Package Isolation

This is a separate Android package, proposed as:

`com.boop.shieldoverlay`

It must install beside BOOP Alpha 1 and must not share runtime state, activities, services, preferences, permissions, or process assumptions with `com.boop.alpha1`.

The implementation lives as a separate Shield overlay project/module in the BOOP repository. Source may copy the small eye-rendering logic needed from the approved checkpoint, but it must not import the Pixel app as a runtime dependency.

For this disposable POC, the existing `BOOP-Alpha1-project.zip` in the repository is allowed to act as the source of the approved `boop_eyes` bitmap during CI/build so the binary artwork does not need to be recreated or altered.

## Overlay Window

The service creates one `WindowManager` overlay using:

- `WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY`.
- `PixelFormat.TRANSLUCENT`.
- `FLAG_NOT_FOCUSABLE` so BOOP can never acquire key/remote focus.
- `FLAG_NOT_TOUCHABLE` because this POC has no interaction of any kind.
- No dimming, modal, fullscreen, or focus-changing flags.

The overlay window is deliberately only large enough to contain the small eye puppet rather than being a full-screen transparent surface. This minimizes interference with underlying apps and avoids a large invisible overlay participating in Android's obscuring/touch-security rules.

The view itself has a transparent background. Neither the view nor its `onDraw` path may clear or paint the canvas black.

## Visual Treatment

The POC reuses the approved eye source crop and motion language from checkpoint `776e75c` rather than redrawing BOOP.

For the first physical test:

- Render the two isolated eyes only.
- Scale them substantially smaller than the Pixel face presentation so they read as BOOP exploring the TV rather than occupying it.
- Start at a low-obstruction screen position near an edge/corner rather than the center of content.
- Preserve the approved proportions and anti-aliased bitmap rendering.
- Keep animation minimal: an initial wake/open may use the existing puppet motion, after which the eyes remain calmly visible. No automatic interaction or speech-driven behavior is introduced for this test.

Exact position/scale are implementation constants, intentionally easy to tweak after the first Shield photograph/test.

## Launch and Permission Flow

First launch:

1. `MainActivity` checks `Settings.canDrawOverlays()`.
2. If not granted, it opens the system overlay-permission screen for `com.boop.shieldoverlay`.
3. When the user returns and permission is granted, the activity starts `BoopOverlayService` while the app is in the foreground.
4. The service immediately promotes itself to a foreground service, creates the overlay, and keeps ownership of it.
5. The activity exits/finishes so the Shield returns to normal launcher/app use with only the eyes remaining.

Subsequent launch:

- If overlay permission is already granted, start/refresh the service and finish immediately.

There is no custom settings screen and no second in-app permission flow.

## Foreground Service

The app targets a current Android SDK and therefore declares:

- `android.permission.SYSTEM_ALERT_WINDOW`.
- `android.permission.FOREGROUND_SERVICE`.
- `android.permission.FOREGROUND_SERVICE_SPECIAL_USE`.
- Service type `specialUse` with a manifest subtype explanation such as `persistent_noninteractive_visual_overlay_poc`.

The service creates the required low-importance notification channel and foreground notification. The POC does not request the Android notification runtime permission; notification permission is not required to launch a foreground service, while the service still supplies the mandatory foreground notification object.

The service returns `START_STICKY` so ordinary process reclamation can request recreation, but the POC does not add a boot receiver or any self-start mechanism. Explicit force-stop or reboot is allowed to stop BOOP; relaunching the APK is the recovery path for this experiment.

## Lifecycle and Cleanup

`BoopOverlayService` owns exactly one overlay view.

- `onCreate`/start: create notification, then add overlay if permission exists and no overlay is already attached.
- Repeated starts: do not create duplicate eyes.
- `onDestroy`: remove the overlay view defensively.
- Permission missing/revoked: remove any view, stop foreground state, stop the service, and leave recovery to manual relaunch.

No app-switch callbacks, polling loops, media listeners, accessibility hooks, or launcher integration are needed.

## Source Boundaries

Suggested Shield project files:

- `shield-overlay/settings.gradle`
- `shield-overlay/build.gradle`
- `shield-overlay/app/build.gradle`
- `shield-overlay/app/src/main/AndroidManifest.xml`
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/MainActivity.java`
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayService.java`
- `shield-overlay/app/src/main/java/com/boop/shieldoverlay/BoopOverlayView.java`
- focused unit/source tests for layout/window configuration where practical
- dedicated GitHub Actions workflow for the Shield POC APK

The existing Pixel source remains untouched unless a build-only read/copy of the approved eye asset is required.

## Build Strategy

Use a dedicated GitHub Actions workflow for the Shield overlay so Alpha 1's existing workflow and artifact remain independent.

The workflow should:

1. Check out the repository.
2. Extract/copy only the approved `boop_eyes` bitmap from `BOOP-Alpha1-project.zip` into the Shield project's drawable resources.
3. Set up Java/Android SDK/Gradle using the repository's proven build pattern.
4. Run Shield overlay tests.
5. Build the debug APK.
6. Inspect APK badging/permissions/package name.
7. Upload a clearly named artifact such as `BOOP-Shield-Overlay-POC-debug`.

The build must verify that the output package is `com.boop.shieldoverlay` and that no microphone, network, HA, or speech permissions/dependencies are present.

## Test Plan

### Automated/build checks

- Project compiles on the selected Android SDK.
- Package ID is `com.boop.shieldoverlay`.
- Manifest contains overlay + foreground-service permissions only as required by this design.
- No `RECORD_AUDIO`, internet/network, accessibility, boot-completed, or HA-related permissions are present.
- Overlay layout parameters use `TYPE_APPLICATION_OVERLAY`, `FLAG_NOT_FOCUSABLE`, and `FLAG_NOT_TOUCHABLE`.
- View drawing path never paints an opaque/black background.
- Service start is idempotent: one service, one overlay view.

### First physical Shield test

Install the POC, launch once, grant "display over other apps", then verify in this order:

1. Shield launcher — eyes visible, background truly transparent, remote normal.
2. Open Kodi — eyes remain visible, remote navigation remains entirely with Kodi.
3. Start normal video playback — eyes remain visible without stealing focus.
4. Switch between launcher/Kodi/another app — eyes persist.
5. Trigger refresh-rate/HDR/Dolby Vision mode changes — observe whether the overlay survives and whether display mode transitions cause flicker, disappearance, black backing, or process death.
6. Judge scale/position subjectively: visible enough to feel alive, small enough not to annoy or obscure content.

## Acceptance Criteria

The POC is successful when:

- It installs beside existing BOOP builds as a separate package.
- The only manual setup is Android's overlay permission.
- BOOP's isolated approved eyes appear over normal Shield UI/apps/video with genuinely transparent surroundings.
- Shield remote input goes straight to the underlying app; BOOP never receives focus or interaction.
- Eyes persist through normal app switching while the foreground service is alive.
- No HA, microphone, speech, settings, black background, touch handling, or unrelated subsystem exists in the APK.
- The first HDR/DV test gives us a clear empirical answer about overlay survival/visibility, even if that answer exposes a Shield/platform limitation.

## Non-Goals After This POC

Movement around the screen, contextual reactions, media awareness, HA control, voice, boot persistence, richer puppetry, and any user-facing controls are all explicitly deferred until the transparent-overlay behavior is proven on the physical Shield.
