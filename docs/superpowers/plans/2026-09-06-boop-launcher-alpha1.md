# BOOP Launcher Alpha 1 Implementation Plan

> **For agentic workers:** Use superpowers:subagent-driven-development to implement the app and review it; controller owns independent CI and delivery.

**Goal:** Build an installable signed companion launcher APK.
**Architecture:** Isolated native Android project, pure model for layout/navigation, platform activity and widget host, explicit companion edge service.
**Tech Stack:** Java 17, Android SDK 36, AGP 9.4.0, Gradle 9.6.0, JUnit 4.
**Spec:** docs/superpowers/specs/2026-09-06-boop-launcher-alpha1-design.md

## Global Constraints
- Only launcher/, its new workflow, and these design/plan files change.
- com.boop.launcher; BOOP Launcher; Android min 29, compile/target 36.
- No microphone or network permission; no secret contents in repo.
- Pure black, original icons, big controls, no dock/folders/clock/permanent plus.
- BOOP Wall and Shield code untouched.

### Task 1: Native launcher app
**Files:** launcher/settings.gradle, build.gradle, app/build.gradle, app/src/main/AndroidManifest.xml, Java classes under app/src/main/java/com/boop/launcher/, tests under app/src/test/, README.md.
**Interfaces:** MainActivity exported HOME and LAUNCHER entry; companion return intent action com.boop.launcher.RETURN; release output app/build/outputs/apk/release/app-release.apk. Controller supplies CI.
- [x] Write behavioral tests for collision rejection, clamping, reflow and navigation before production logic; run red then green using Gradle CI if local toolchain unavailable.
- [x] Implement persisted page items with normalized positions and sizes, deterministic no-overlap placement and reflow.
- [x] Implement main canvas, app catalog/drawer/search, edit dragging/sizing/moving/removal, real widget binding/configuration and pending cancellation cleanup.
- [x] Implement explicit BOOP launch and optional foreground overlay swipe return; dismiss on launcher return or screen off; stop action, safe denied-permission paths.
- [x] Document setup and concrete phone checks; commit app files only.

### Task 2: Signed build and delivery
**Files:** .github/workflows/build-launcher-alpha1.yml, launcher/scripts/smoke.py.
- [x] Build task 1 red test commit in CI before implementation if available; otherwise document local compiler absence and preserve behavioral tests.
- [x] Configure branch push workflow: Java/SDK/Gradle, unit tests, lint, permanent release signing and apksigner verification.
- [x] Run Android emulator smoke on signed APK; capture screenshots for portrait/landscape and exercise navigation/persistence/bailout.
- [x] Review app and fix material findings, rerun affected checks.
- [x] Push branch, obtain successful exact-commit artifact, extract APK, save deliverable and provide direct APK link.

## Completed verification — 2026-09-06

- Signed app source: `fa66d8697920d01f4fe782df1c8649a935be4ab7`. Build run `34058128961` build job passed compilation, lint, 10 unit tests and permanent signature verification.
- APK SHA-256: `81b297ce52864c6b776f6c0462c851e09bda8c225cd6ab939ad7929f32a6fc90`.
- Emulator run `34058658667` exercised real default HOME, searchable drawer, app pinning, process-death persistence, widget-page/back navigation, rotation and two phone-sized viewports.
- Its last assertion incorrectly required `com.android.settings`; Android actually opened `com.google.android.permissioncontroller` with the correct Default home app picker and Pixel Launcher choice. Captured UI and activity logs verify Bail out. The assertion is corrected to the visible Home-picker contract and validated against captured XML; the full corrected CI script has not been rerun. No runtime crash was recorded.
- Real Home Assistant widget binding/configuration and BOOP edge-return handoff still require phone testing. Existing BOOP/Shield sources unchanged.
- APK saved for direct installation. Implementation reviewed; branch retained independently from main.
