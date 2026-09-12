# BOOP Shield Launcher split implementation plan

Goal: Deliver a permanent-signed standalone Shield launcher with its existing advanced tools, without Unified or HA setup.
Approval: User approved the preceding extraction design, explicitly excluding failed uncommitted integration work.
Source: boop-shield-defaults@503cdb63d64716c9c1a568aadca97ba1d24680cd (v145). Do not import v146 integration, even if another task publishes it later.
Identity: BOOP Launcher; com.boop.shieldlauncher. Preserve com.boop.alpha1 and historical com.boop.shieldhome installs.
Worktree/branch: .worktrees/boop-shield-launcher-standalone / boop-shield-launcher-standalone.

## Constraints
No new art, voice, microphone, HA, phone/Wall UI, recipes, Animation Lab, copied settings/ADB identities or signing key changes.
Retain approved launcher/favourites/Now Playing and audio behavior, Startup Manager, defaults preview/Apply/Undo and exact receiving-device recovery.
No automatic installs, HOME selection, permissions, preset application or changes to either physical Shield.
GitHub performs nonvisual tests, compilation, signature/package/integrity verification; real visual/device acceptance remains manual.

## Tasks
- [x] Baseline: run scripts/test-startup-manager.py and scripts/test-startup-defaults.py with Java 17. Preserve any baseline failures.
- [x] Extraction contract RED: tests/test_shield_launcher_split.py asserts new manifest, independent package, explicit launcher/tool components, no assistant permissions/components, generated sources without Unified/HA imports.
- [x] Packaging: create shield-launcher/{settings.gradle,build.gradle,app/build.gradle,app/src/main/AndroidManifest.xml}; scripts/materialize-shield-launcher.py copies the committed Shield HOME module, required local ADB/eyes/media helpers and locked assets into ignored work/shield-launcher-src, excluding whole assistant/overlay modules.
- [x] Routing: standalone overrides restore OS HOME/accessibility routes and remove HA/profile buttons. Keep Startup Manager and media access. Opening setup never grants permissions or applies defaults.
- [x] Local close RED: pure Java tests verify exact selected-session checks, supported native package allowlist, Cast never force-stopped, cancellation, no shell injection, and both explicit close modes. Implement local ADB close without HA credentials, with bounded worker/cancellation and status on the main thread.
- [x] GREEN: materialize, run extraction/policy and existing startup suites, compile actual Android sources and assemble candidate using installed Java/SDK/Gradle caches.
- [x] Review: inspect component resolution, generated dependency closure, package isolation, key ownership, startup restore behavior and every native-close/cancel path. Re-run checks on reviewed source.
- [x] Publish: use separate workflow .github/workflows/build-shield-launcher.yml with existing BOOP signing secrets, no replacement signer; upload BOOP-Launcher.apk plus source/hash/signer receipts.
- [x] Verify exact signed artifact package/version/components/signature, document physical-test limits, update handoff/status/memory and shared main routing only, scoped commit/push and live HEAD verification.
