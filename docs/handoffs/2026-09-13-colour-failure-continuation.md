# BOOP continuation prompt: colour failure first, preserved speed work second

Prepared 2026-09-13 to move out of the repeatedly crashing Unified feature bumps chat. Read this as the user's continuation brief, alongside current repository instructions. Do not restart the project, redesign approved art, or throw away the existing implementation.

## 1. Immediate user report and priority

Ryan's latest feature feedback was: "stopping you quick.. i just tested eye change.. it isnt working.. did you finish ?"

The answer was NO, not fully verified. The prior assistant had proved saving settings and a Shield-to-Home-Assistant exchange, but had not proved the complete visible eye-colour behavior across devices. Ryan then asked for this complete continuation prompt because the chat window was crashing.

This user-observed failure is the current blocker. Eye colour is NOT signed off. Pause further animation-speed development and deployment until the exact colour failure is reproduced and fixed. Preserve the existing speed code and its tests; pausing is not reverting or rebuilding from scratch.

The failing device and surface were not specified before the handover. The last unanswered question was whether the number/slider changes but the same device's eyes do not, or whether another device fails to follow. Do not invent that answer, assume a disabled toggle is the explanation, or blame the previously locked Pixel 7 without checking current evidence. After reading current source and allowed device state, ask one focused question only if that distinction is still unresolved.

Ryan explicitly confirmed: "old threads are irrelevant.. your work is primary". This continuation owns the job. Preserve other work and check live branch changes, but do not repeatedly stop just because old recovery notes mention competing sessions. A newly observed conflicting edit still needs a real diff and safe reconciliation, never a force-push.

## 2. Source of truth and exact checkpoints

Repository: `ryankemble2006-web/boop`.
Active feature branch: `boop-unified-eye-sync-safe-v159`.
Last source commit verified while preparing this handover: `d149cb509ec376779daf84c50f621d8adcbacd24`.
Its tree is `e123c6e20efadacde61d062395e80146aa8cd5f9`.
Its source version is `160 / 1.2.160-colour-animation-speed`, package `com.boop.alpha1`.

A documentation-only handover commit may now follow that source commit. Fetch the LIVE branch reference, then fetch files at the returned full SHA. Never treat the branch's v159 name as the current APK version, assume main contains the newest app, or overwrite newer commits with a historical tree.

Read the live branch's `SESSION_HANDOFF.md`, `BOOP_STATUS.md`, `BOOP_UNIFIED_MEMORY.md`, applicable `BOOP_CONTEXT.md`, and these retained plans:
- `docs/superpowers/plans/2026-09-13-shared-colour-and-speed.md`
- `docs/superpowers/plans/2026-09-13-colour-recovery-validation.md`

Read current `main:BOOP_START_HERE.md` for routing and resource-recovery rules. Applicable AGENTS instructions still matter, but current user instructions and the newest task-specific checkpoint supersede obsolete historical routing. Do not turn missing/deleted historical root files into another project restart.

## 3. Fresh GitHub evidence at this handover

The three runs for source `d149cb509ec376779daf84c50f621d8adcbacd24` now report SUCCESS:
- Appearance logic: `34760362361`.
- Animation timing: `34760362356`.
- Full Unified build: `34760362417`.

Full-build artifact: `BOOP-Unified`, artifact ID `10318393790`, created 2026-09-13 at 13:41:59 UTC. Its GitHub artifact-archive digest is `sha256:7933748103a6535113c45c9fdfe9ddb6172501a01745dd1db1fb1d601bd9b40b`. That is NOT the installed APK's digest. Obtain the actual APK hash, built-commit and signing receipts from the artifact before using it.

This supersedes older notes saying v160 CI was still pending. It does NOT supersede Ryan's reported colour failure. Successful compilation, numerical timing and source contracts do not prove visible colour changes or real two-device synchronization. No v160 emulator/device deployment is established by this handover. Do not install it merely because its CI is green.

The handover itself does not change app code, install software, alter permissions or fix the reported bug.

## 4. Last-known installed build and what it actually proved

The preceding continuation installed v159 `1.2.159-shared-eye-colour`, built from `0a4134ebfe8049254378b4706d2ee1df73cd7e87`.
Full build: `34757337845`; artifact `10317198102`, name `BOOP-Unified`.
Recorded APK SHA256: `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`.
Permanent signer SHA256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

That artifact was installed using update-in-place, and its installed package hash was read back on both laptop emulators, the Shield and Pixel 7. The physical Pixel 10 was excluded. Recheck current installed versions before testing; these are recorded observations, not timeless device state.

Recorded v159 build evidence: 235 focused Unified and 68 focused Shield tests, no failures/errors/skips, plus configured integration, ownership, approved-asset, colour and signing checks. Earlier pure-Java tests reported 397 colour/state checks and 25 HA-flow checks. These counts cover those tests, not all possible runtime behavior.

Recorded emulator observations:
- Phone AVD `Pixel_10_Pro_XL_API_36` was `emulator-5570`; TV AVD `BOOP_Android_TV_API_36` was `emulator-5572`. Rediscover serials.
- Deliberate migration test hues 73 and 288 survived upgrade. These were fixtures seeded by recovery work, NOT Ryan's actual colour preferences.
- The new settings screen opened. Cancel left sharing off. Local slider changes updated the displayed value and were later persisted. A disk read immediately after SharedPreferences.apply was briefly older than the displayed value, then caught up; that observation alone is not the visual bug.
- TV D-pad moved hue 190 to 195, and settings reopening retained 195.
- A missing-HA connection produced the intended explanatory message without discarding local hue.
- Phone Wall initially showed a black idle screen. Tapping woke the approved eyes; renderer-ready appeared and the inspected crash buffer was empty. That is a wake/render observation, NOT proof that all hues work or that two devices sync.

Recorded Shield HA test:
- Explicit opt-in reached "Eye colour is shared through Home Assistant."
- The Shield published hue 195.
- Sharing was turned off and a local value changed to 289.
- Rejoining retrieved shared 195 rather than overwriting HA with the offline value.
- The test restored shared hue to 190 afterward.
This demonstrates a Shield-to-HA store/read/rejoin path. It does NOT demonstrate a second BOOP receiving and visibly rendering the colour.

Pixel 7 received the APK, but was locked during the attempted UI/shared-colour test. No unlock or credential bypass was attempted. Do not report it as tested end to end. Do not claim the installed Wall app was never touched: v159 was installed. The narrower preservation claim is that authored visual/hue assets were intended to remain unchanged.

## 5. Scope that must remain intact

The Shield sweep through v156 is user-accepted and is not a new task list:
- corrected rounded artwork corners and thin cyan focus outline;
- progress Left/Right minus/plus 10 seconds; Down from progress to Pause;
- 250 ms settings hold, not 650 ms;
- reserved Now Playing BOOP bay preventing Lyrics/Close player seek flashes;
- favourites right-edge stop and BOOP-owned + Add favourites selector;
- artist name focus turns cyan; OK opens the matched artist in Deezer without autoplay.
Nvidia's launcher is gone. BOOP's favourites selector is independent of it. Do not replace accepted behavior while repairing colour.

The accepted source is preserved at `boop-unified-artist-link-v156`, app source `a901c1e9f31e55c710e31ac7ff4f5924c9769d56`; its acceptance-doc checkpoint is `086ae4d383c5c3f176baafe803d01628e30251a9`.

BOOP has approximately 26 approved coded animations, not a wobbling PNG substitute. Preserve the canonical animation definitions, proportions, expressions, keyframes, pauses, signs and approved yellow hands. Replace incorrect integration rather than stacking another pair of eyes. No image generation unless Ryan specifically requests it.

Iris colour only: never tint the whites, pupil, highlights, lids or whole texture. Preserve the approved master `unified/assets/boop-eyes/boopApprovedEyes.png` and the relevant shaders and geometry. The recorded master SHA256 is `ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22`.

Existing Wall hue uses `boop_eyes` / `hue_degrees`, range 0..359, original default 190. Never reset a user's saved hue on upgrade. A colour repair must not remove the existing working Wall controls.

BOOP's authored animations must keep working when Android's three system animation scales are zero. Ryan observed that disabling those Android settings stops Deezer's curtains. Do not change physical devices' global settings as an unrequested side effect. BOOP speed changes must not affect music, speech, media seeking or Android UI speed.

## 6. Colour design and files already written

The selected implementation uses the existing local hue store plus opt-in authenticated Home Assistant sharing, rather than the old unauthenticated UDP experiment. Sharing defaults OFF. Explicit setup may create one marked `input_text` helper; ordinary startup/reconnect must not invent helpers or push cached defaults. Discover the actual helper/entity IDs, including renamed entities, and validate the bounded payload and marker.

The intended behavior retains the last local colour offline and reads the current shared value before publishing on reconnect. Sharing currently operates while BOOP is open; do not secretly add a persistent service or promise background synchronization that has not been implemented. Same-device renderer binding is separate from cross-device HA transport.

Inspect the actual live versions of:
- `source/BoopEyeHue.java`, `BoopEyeHueMath.java`, `BoopEyeHueOverlay.java`, `BoopEyeHueSettings.java`, `BoopIrisTint.java`, `BoopIrisTintMath.java`;
- `unified/animation/java/com/boop/eyes/EyeColourBinding.java`, `CanonicalEyeRenderer.java`, and `unified/animation/assets/eyes.frag`;
- `source/SharedEyeColourProtocol.java`, `SharedEyeColourState.java`, `SharedEyeColourHaProtocol.java`, `SharedEyeColourLink.java`, `BoopSharedEyeColourRuntime.java`, `BoopAppearanceActivity.java`;
- `unified/UnifiedApplication.java`, `unified/BoopProfileActivity.java`, `source/AndroidManifest.xml`;
- `source/BoopCanonicalFaceView.java`, `BoopNotificationPuppetView.java`, `BoopCanonicalAnimationActivity.java`;
- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java`.

The settings entry is currently under BOOP device/profile settings -> Eyes and animation. On Shield, enter Home settings -> BOOP device and room settings -> Eyes and animation. Do not assume the visible label proves renderer behavior.

The renderer binding observes the existing preferences and installs after setRenderer, with attach/detach listener cleanup. The runtime reuses existing saved authentication through `BoopVoiceTokenStore` / `HomeAssistantAuth` and `HomeAssistantWebSocket`. Trace the actual calls and materialized build, not just intended architecture. `scripts/materialize-unified.sh`, `scripts/materialize-android.sh` and `scripts/patch-unified-canonical-animations.py` matter because the shipped Wall source is assembled by patches.

Preserved tests: `tests/test_shared_eye_colour.py`, `tests/java/SharedEyeColourHarness.java`, `tests/java/SharedEyeColourLinkHarness.java`, and `tests/fixtures/appearance-v156-baseline.json`. Source-string checks are not enough to diagnose Ryan's failure.

## 7. Speed work is already published: preserve it

The earlier missing-speed red baseline is `3806cd34f90486da9a22234f9e05d485a2b7deff`, run `34759118959`. Speed implementation is now published at `d149cb509ec376779daf84c50f621d8adcbacd24`, not merely an unattached tree. Its timing and full-build checks subsequently passed, as recorded above.

Existing additions include `AnimationClock.java`, `AnimationSpeedPreferences.java`, `AnimationSpeedBinding.java`, clock integration in `ProductionAnimationController.java`, lifecycle bindings on Wall/Now Playing/notifications/embedded Lab, and settings buttons 0.5x / 1x / 1.5x / 2x. The rate is device-local, default 1x, in `boop_appearance` / `animation_speed`.

The numeric tests compare authored clips and state/trigger/blend behavior to the v156 controller at those rates, including exact pose bits at 1x, fractional timing, monotonic input, continuous speed changes and sleep-hide deadlines. Notifications' hands and eyes share one timeline. Lab slow-review stays separate; avoid scaling its controller and frame delta twice. Files: `tests/test_animation_speed.py`, `tests/java/BoopAnimationSpeedHarness.java`, `.github/workflows/check-boop-speed.yml`.

Do not confuse these numerical passes with visible runtime acceptance. Once colour is fixed, verify speed changes in real runtime, including sleep/wake, signs, pause/resume, changing speed mid-clip and Android scales at zero on a controlled emulator. Check the materialized APK path for system-scale dependencies; do not assume source-only inspection resolves that.

## 8. Preserve the research, but distinguish research from proof

The existing plan records the researched decisions and these primary sources. Recheck relevant current documentation/source when implementation depends on it; do not restart unrelated broad research or claim a guaranteed outcome.

Android:
https://developer.android.com/reference/android/content/SharedPreferences
https://developer.android.com/reference/android/content/SharedPreferences.OnSharedPreferenceChangeListener
https://developer.android.com/reference/android/opengl/GLSurfaceView
https://developer.android.com/reference/android/os/SystemClock

Home Assistant:
https://developers.home-assistant.io/docs/api/websocket/
https://www.home-assistant.io/integrations/input_text/
https://github.com/home-assistant/core/blob/dev/homeassistant/components/input_text/__init__.py
https://github.com/home-assistant/core/blob/dev/homeassistant/helpers/collection.py
https://github.com/home-assistant/core/blob/dev/tests/components/input_text/test_init.py

The recorded research selected helper storage without an initial value for state restoration, explicit/admin-gated helper creation, entity permission checks for writes, actual entity identity rather than display-name guessing, and authenticated state subscriptions. Unsupported or denied setup must preserve local hue rather than silently weakening authentication. These were design decisions, not proof that the current implementation satisfies every case.

The Lab reference is `animation-lab-system-scale-poc`, particularly `animation-lab/canonical-eyes/java/com/boop/eyes/MotionPolicy.java` and its tests. The older `boop-unified-puppet-integration` code is research material only: it used unauthenticated LAN state and also changed blink/voice behavior. Do not merge it as a shortcut.

## 9. Tool and device workflow

GitHub owns source editing, commits, non-visual tests, builds and permanent signing. Use GitHub connector reads/writes. Laptop tools are for permitted runtime checks, emulator/device installation of verified artifacts, screenshots and logs, not another competing local implementation.

Discover current tool schemas instead of reusing stale function/resource IDs. A historical `/response/...` ID is not a durable repository file address. For a lookup failure, rediscover the relevant GitHub action and fetch the live ref and exact commit-pinned file. Verify returned content. Do not infer moved files or lost permissions from a generic resource error. For an uncertain write, read back live state before retrying. A non-fast-forward rejection is a concurrency guard, not grounds for force-pushing. Distinguish genuine safety denial from missing resources and do not route around a safety denial.

Do not send all source-file edits as long persistent-terminal command strings. This thread had both interrupted calls and Python REPL quoting/syntax errors. Recover/reconcile results rather than assuming a call either all ran or all failed. Do not reuse old REPL globals, PIDs or device addresses blindly.

Known working Desktop Commander command, only when reconnection is actually needed:
`npx.cmd -y @wonderwhy-er/desktop-commander@0.2.47 remote`
Keep 0.2.47 while working. No proactive upgrade, permission broadening or installation changes.

Laptop checkout exists at `%USERPROFILE%/Documents/Codex/BOOP` (Ryan's configured location is under `C:/Users/ryank/Documents/Codex/BOOP`). Owning worktrees are under `.worktrees/`. An unrelated starting folder is not evidence of missing source/tools. The old sync receipt is historical; live GitHub handoffs take priority.

Android SDK is under `%LOCALAPPDATA%/Android/Sdk`. Discover current targets with the existing adb. Local AVDs include `Pixel_10_Pro_XL_API_36` and `BOOP_Android_TV_API_36`; `Pixel_7_Pro_API_36` and `BOOP_Sign_Show_API36` were also listed. The physical Pixel 10 Pro XL is NOT the Pixel 10 emulator: emulator testing is authorized; physical Pixel 10 remains excluded. Physical targets for this task are Shield and Pixel 7 only, subject to current reachability and lock state.

Do not bypass a phone lock, touch private notifications/credentials unnecessarily, clear app data, uninstall to fix signatures, grant permissions silently or substitute signing keys. No changes to the protected voice/media/HA installations without specific scope. Ryan's real-device visual judgment remains the acceptance gate.

Runtime receipts from earlier work are under `.worktrees/boop-unified-eye-sync-safe-v159/work/runtime-ci-34757337845`; a recovery window also used `%TEMP%/boop-colour-v159-checks` for rollback APKs, migration fixtures and screenshots. Those are private/local evidence caches, not source authority. Do not publish tokens, network addresses, notification contents or private screenshots to the public repository.

## 10. Recovery snapshots and unrelated work

Already preserved, not current replacements:
- `wip/boop-colour-v158-recovery` at `484d292fc5a0f72e2e73603e7084713b6ec660b5`.
- `wip/boop-colour-v159-recovery` at `7ea7d26d10ae2ffb50afcd77a983047f508ad419`.
- `wip/boop-colour159-guarded-438ca83a` at `0d67e4ce3936af8471241c0052d0f581f1dad776`.

Some historical notes incorrectly called a recovered runtime truncated. Later source inspection found it complete at `04f7c10c`, with startup/settings wiring missing. Later implementation supersedes that state. Preserve provenance without rebuilding from stale diagnoses. Current branch includes the reconciled implementation and tests.

Do not merge native lyrics (`boop-unified-native-lyrics-v157`), Johnny, 3D artwork or other concurrent projects into this task. Do not rename the user's chats or create recurring monitoring unless requested.

## 11. Exact next work and completion requirements

First, confirm the live branch/file/CI/installed-package identities and read this latest failure note. Establish the failing device and screen without making Ryan repeat the entire story. Treat his failure as real evidence regardless of green CI.

Reproduce locally where possible, then on the authorized affected device. Trace separately:
1. user action -> existing hue preference -> active view listener -> renderer hue rotation -> GL uniform/render request -> actual visible iris;
2. sender preference -> authenticated HA write -> actual HA stored state -> receiving device subscription -> receiving preference -> active renderer.
Use noticeably different test hues and a controlled scene/pose. Preserve original values and restore them afterward; distinguish fixture values from user choices. Record both numerical state and visible output. Do not use a changed number, successful toast or "sharing ready" text as proof that the eyes changed.

Check duplicate/competing writers, stale callbacks, old cached hue in existing controls, active face ownership, lifecycle attach/detach, and which patched source actually shipped. These are investigation targets, NOT diagnosed causes. Add a regression test that reproduces the discovered failure before the smallest repair. Do not mask a colour-binding problem by replacing the artwork or rewriting authored motion.

Run existing GitHub preservation, colour, timing and full build/signing gates for the exact repair commit. Install the verified artifact on local emulators first for substantive changes. Then check the authorized affected physical device. Do not deploy speculative patches across devices or onto physical Pixel 10. Do not claim cross-device success until two active, authorized devices visibly converge in both directions and reconnect behavior is verified. Never manufacture approval.

Only after the colour failure is resolved resume the preserved speed work. Its code and passing CI should be reused, but runtime/visual testing is still needed. No reapproval loop for accepted old Shield polish.

Before ending meaningful work, publish exact handoff/status/memory and relevant context updates with current source commit, artifact/build identity, tests actually run, devices changed, outstanding failures and next step. Verify the live branch after writes. Give brief milestone updates during longer work instead of leaving a wall of "Thinking". Do not claim background activity after a turn ends unless an authorized automation exists.

Start with the colour failure. Do not state either requested feature is finished merely because the files are present or CI is green.
