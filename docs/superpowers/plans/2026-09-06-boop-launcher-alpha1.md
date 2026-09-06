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
- [ ] Write behavioral tests for collision rejection, clamping, reflow and navigation before production logic; run red then green using Gradle CI if local toolchain unavailable.
- [ ] Implement persisted page items with normalized positions and sizes, deterministic no-overlap placement and reflow.
- [ ] Implement main canvas, app catalog/drawer/search, edit dragging/sizing/moving/removal, real widget binding/configuration and pending cancellation cleanup.
- [ ] Implement explicit BOOP launch and optional foreground overlay swipe return; dismiss on launcher return or screen off; stop action, safe denied-permission paths.
- [ ] Document setup and concrete phone checks; commit app files only.

### Task 2: Signed build and delivery
**Files:** .github/workflows/build-launcher-alpha1.yml, launcher/scripts/smoke.py.
- [ ] Build task 1 red test commit in CI before implementation if available; otherwise document local compiler absence and preserve behavioral tests.
- [ ] Configure branch push workflow: Java/SDK/Gradle, unit tests, lint, permanent release signing and apksigner verification.
- [ ] Run Android emulator smoke on signed APK; capture screenshots for portrait/landscape and exercise navigation/persistence/bailout.
- [ ] Review app and fix material findings, rerun affected checks.
- [ ] Push branch, obtain successful exact-commit artifact, extract APK, save deliverable and provide direct APK link.
