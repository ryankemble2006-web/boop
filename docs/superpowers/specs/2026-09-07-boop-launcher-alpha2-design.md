# BOOP Launcher Alpha 2 — Pixel-like clean-sheet design

## Purpose

Replace the existing BOOP Launcher Alpha 1 user interface with a new launcher that feels modern and Pixel-like while remaining BOOP-owned, minimal, open, locally signed, and free of copied Google proprietary code or artwork.

Alpha 1 remains preserved only as a historical escape hatch. Alpha 2 must not reuse the old launcher UI architecture as its foundation.

## Locked product decisions

- Package remains `com.boop.launcher`.
- Existing GitHub release-signing identity and secret variables remain unchanged. Never replace the BOOP signing key.
- Target Android API remains 36; minimum supported Android remains API 29 unless a Launcher3 dependency proves a higher floor is unavoidable.
- The launcher remains an ordinary Android HOME app. No root, privileged/system signature, Accessibility abuse, hidden APIs requiring a modified OS, or Google proprietary components.
- No Internet permission and no microphone permission for basic launcher operation.
- Pixel Launcher is a behavioral/design reference only. Do not copy Google proprietary source, resources, icons, trademarks, or bundled assets.
- AOSP Launcher3 on the Android 16 release line is the preferred technical reference/base because it provides mature workspace, app-drawer, drag/drop, widget, device-profile, responsive-layout and animation infrastructure under Apache 2.0 terms.
- Lawnchair may be inspected as an implementation reference for Pixel-like launcher behavior, but Alpha 2 must not import Lawnchair code unless its GPL obligations are explicitly accepted in a later decision.

## Visual target

### Home

The default home screen is a pure black edge-to-edge canvas.

It contains no permanent clock, no At a Glance panel, no Google search field, no permanent dock/hotseat, no folder bar, no plus button, no page furniture, and no launcher-created icon skin.

Installed apps placed by the user appear using their native Android app icons and labels. The visual rhythm should follow current Pixel conventions: generous spacing, restrained typography, smooth transitions, and no decorative chrome.

System status/gesture regions remain Android-owned. On Android 15+ the launcher draws edge-to-edge behind transparent system bars. The launcher must not claim it can remove the Android gesture handle or system navigation controls without privileged OS access.

### App drawer

Swipe up from the home canvas opens the app drawer using Pixel-like motion and spatial continuity.

The drawer itself is pure black and minimal. It shows the installed app grid/list without a permanently visible search pill.

Search is on demand only. It should appear when intentionally invoked, such as by typing from the drawer, tapping a lightweight search affordance that appears contextually, or another minimal interaction chosen during implementation. Merely opening the drawer must not add a permanent search bar.

### Motion

Motion should feel close to modern Pixel Launcher behavior without requiring byte-for-byte duplication. Prioritize:

- direct finger tracking;
- springy but restrained settling;
- spatial continuity between home and drawer;
- smooth app placement and drag/drop;
- no modal instructional dialogs during gesture streams;
- no layout rebuilds that steal an active touch sequence.

## Functional Alpha 2 scope

The first Alpha 2 physical-test build should contain only the launcher fundamentals required to judge feel:

1. HOME role / launcher startup.
2. Pure black edge-to-edge home canvas.
3. Swipe-up app drawer with installed apps.
4. On-demand app search, with no permanent search field.
5. Add/pin an app from the drawer to home.
6. Move placed apps using continuous drag interaction.
7. Remove placed apps using an obvious non-modal drag target or similarly direct gesture.
8. Android widgets: add, bind/configure, place, move, resize, persist and recover from cancelled configuration.
9. Multiple home pages only when user content requires them; no empty permanent page carousel.
10. Correct Home/Back behavior and state persistence across process death, rotation and relaunch where Android permits.
11. Existing BOOP app package interoperability may be retained only where it does not pollute the clean baseline. BOOP-specific return-strip flourishes are deferred until the basic launcher feels right on the Pixel 10 Pro XL.

## Explicitly deferred

Do not implement these in the first Alpha 2 physical-test build unless required by Launcher3 internals:

- folders;
- icon packs or icon theming;
- backup/restore;
- work-profile customization beyond whatever Launcher3 needs to enumerate launchable apps correctly;
- notification dots;
- smart suggestions / predicted apps;
- weather;
- news feed;
- Google Discover integration;
- web search integration;
- custom wallpaper features;
- BOOP return-strip overlay;
- BOOP voice controls;
- Home Assistant-specific widgets beyond normal Android widget support;
- hidden Easter eggs;
- broad settings screens.

## Technical architecture

### Base strategy

Use Android 16 AOSP Launcher3 concepts/components wherever this produces a maintainable implementation without importing unrelated Pixel or Google proprietary dependencies.

The implementation may either:

1. vendor a narrowly selected Launcher3-compatible subset into `launcher/` and remove/disable non-required subsystems; or
2. rebuild the current `launcher/` module around Launcher3-inspired components if directly importing the AOSP tree would create a build-system dependency explosion in this repository.

The decisive criterion is not ideological purity. It is the smallest maintainable codebase that preserves Launcher3-quality behavior for workspace, all-apps, drag/drop, widgets, device profiles and motion.

Do not drag the old Alpha 1 `MainActivity` interaction architecture forward merely to save effort.

### Main units

The Alpha 2 launcher should be decomposed around these responsibilities:

- **Launcher activity / state coordinator**: owns HOME lifecycle, mode transitions, Back/Home semantics and edge-to-edge window setup.
- **Workspace**: owns placed app/widget pages, placement model and persistence.
- **All Apps**: owns app enumeration, drawer presentation, filtering and launching.
- **Search controller**: activates only on demand and filters the app model locally.
- **Drag controller**: owns a complete pointer stream from pickup to drop/cancel and never invokes modal UI mid-drag.
- **Widget host/controller**: owns `AppWidgetHost`, bind/config flows, cancellation cleanup, sizing and persistence.
- **Device/profile/insets**: computes grids, icon sizing, system-bar safe regions and responsive layout.
- **Animation/state transitions**: owns home↔drawer and drag settling motion so gestures remain direct and testable.

Each unit must expose a small interface so behavior can be unit-tested independently rather than living in one giant Activity.

## Data and persistence

Persist only user-owned launcher state: placed apps, widget IDs/positions/sizes, page arrangement and minimal launcher preferences.

No cloud sync. No account requirement. No analytics dependency.

When upgrading from Alpha 1 to Alpha 2, old layout preferences may be intentionally discarded if their schema would force Alpha 1 architecture into the new design. If migration is not clean and deterministic, prefer a one-time fresh Alpha 2 workspace and document that choice clearly before publishing the APK.

## Error handling

User-facing failures use plain English.

Examples:

- app no longer installed: remove stale placement cleanly;
- widget provider removed: release host ID and remove placeholder;
- widget bind/config cancelled: restore launcher state without leaving ghost items;
- HOME role not selected: launcher remains independently launchable and provides a simple route to Android Home settings only when the user asks;
- search with no match: show an empty local result state, never route to the web.

No scary generic “error” page for recoverable launcher conditions.

## Testing and verification

Automated testing is a guardrail, not a substitute for physical feel.

### Required automated checks

- compile and release-sign using the existing BOOP GitHub signing workflow;
- unit tests for placement, overlap/clamping rules, page creation/removal and local search filtering;
- widget cancellation and stale-widget cleanup tests where feasible;
- installed-app smoke tests for HOME startup, drawer open/close, app launch and persistence;
- gesture regression tests ensuring pickup owns one continuous touch stream and no modal UI appears during drag;
- AndroidRuntime crash scan in CI.

### Required physical checks

Ryan’s Pixel 10 Pro XL is the authority for whether Alpha 2 feels right.

The first physical pass should judge:

- home looks genuinely black and empty when no apps are placed;
- no permanent clock, dock or search furniture exists;
- swipe-up drawer follows the finger naturally;
- drawer feels Pixel-like rather than like an old launcher skin;
- on-demand search is discoverable without permanently occupying space;
- icons look native and correctly scaled;
- app pickup/move/remove feels direct;
- widgets can be bound/configured/cancelled and still behave normally;
- gesture-navigation region visually blends into the black canvas;
- Home and Back never expose a broken intermediate state.

A CI pass is not physical acceptance. A screenshot is not proof that motion feels correct.

## Source and licensing notes

Primary public reference:

- AOSP Launcher3 Android 16 release source: `https://android.googlesource.com/platform/packages/apps/Launcher3/+/refs/heads/android16-release/`

Android system-bar guidance:

- `https://developer.android.com/about/versions/15/behavior-changes-15`
- `https://developer.android.com/design/ui/mobile/guides/foundations/system-bars`

Any vendored AOSP files must retain their required Apache 2.0 notices/headers. Do not import Google Pixel Launcher proprietary APK code or resources into the BOOP repository.

## Publication strategy

Development branch: `boop-launcher-alpha2`.

Preserve `boop-launcher-alpha1` as historical reference/checkpoint. Alpha 2 becomes authoritative only after a signed build installs and Ryan physically accepts the new launcher baseline.

Before each material publish: fetch/recheck live branch state, preserve concurrent work, run the appropriate tests, commit reviewed files, push, then verify the live GitHub branch HEAD.
