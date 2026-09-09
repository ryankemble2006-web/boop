# BOOP Unified v70 developer-lab / notification-doods release receipt

Date: 2026-09-09

Canonical branch: `boop-unified`

Package: `com.boop.alpha1`

Version: `70` / `1.2.24-unified-dev-menu-doods`

## Release commit

Final production/build commit:

`825593a16c004d9c0825720eb014c4f5cc8e58af`

App implementation commit:

`2f4a62150121b299a433674e971de1e058f6330f`

Materialized-router test correction:

`9c907d3497067eb88ea1308875084b8965413951`

The final two commits between `9c907d3` and `825593a` are workflow-only CI changes that add `unified/app-build.gradle` to the separate Shield HOME routing workflow trigger and then correct the preserved `android-actions/setup-android` input indentation. No app/runtime source changed after `9c907d3`.

`main` was not modified for v70.

## What v70 adds

### Local spoken developer command

`dev menu` is intercepted locally through `BoopDevMenuIntent` in BOOP's recognized-speech path before Home Assistant, the command router and chat fallback. It opens `BoopDevMenuActivity` immediately and has no Chat Mode, OpenCode/ChatGPT Web or internet dependency. Existing local verbal commands remain ahead of the downstream HA/chat boundary.

`BoopDevMenuActivity` remains `android:exported="false"`.

### Fullscreen developer lab

BOOP Dev uses immersive/fullscreen black presentation, hides system bars where supported, restores the normal activity lifecycle on exit, and provides large scrollable/remote-friendly controls.

Animations exercise real BOOP methods:

- Wake
- Think
- Stop
- Berry 1
- Berry 2
- Berry 3
- Shake
- Sleep

Finite previews can be replayed. The indefinite Think path has an explicit Stop/reset route.

### App-specific notification doods

Local fake/demo presentations are provided for:

- Facebook
- WhatsApp
- Gmail
- X / Twitter
- YouTube
- Messenger
- Instagram
- Discord
- Spotify
- Reddit
- Locked privacy-safe preview
- Bundle preview

The concept sheet `Glossy Boop App Icon Collection.png` from Ryan's ChatGPT Library was consulted for service identity/style direction only. Runtime doods do not bake the old concept-sheet face or hands into the presentation.

Runtime presentation uses the existing `BoopNotificationPuppetView`, BOOP's current procedural-eye stack and the exact approved five-finger yellow hands. Dev-only service identity/icon treatment supplies recognizable app identity.

Notification demos are local presentation models only. The developer activity/preview path does not post Android shade notifications, call `NotificationManager`, invoke `BoopNotificationRuntime` or the notification-listener service, require notification-listener access merely to preview, or depend on live external messages.

Returning from a dood returns cleanly to BOOP Dev.

### Locked privacy contract

Locked preview still flows through the production notification presentation redaction model. Before authentication, message title/body are absent; only app identity/icon/count-style information is allowed.

## Locked source contracts retained

Exact approved hands:

- path: `unified/assets/boop-notifications/boop-yellow-hands-approved.png`
- size: `1,809,990` bytes
- SHA-256: `26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1`
- Git blob: `d47037271bf320f4f110e3f8416f59882062afac`

Finished procedural-eye materialization order remains:

1. `scripts/patch-unified-reading-eyes.py`
2. `scripts/patch-v64-procedural-sclera.py`
3. `scripts/patch-v65-feathered-sclera.py`

No later bitmap hue-cache setter is allowed after that stack.

## TDD / debugging evidence

RED head `4c81770aa869a46352115572c0757ca0f9876847` triggered workflow `34321173070`. It failed as intended because the new tests referenced the not-yet-implemented v70 intent/action/notification-identity classes.

Implementation head `2f4a62150121b299a433674e971de1e058f6330f` triggered workflow `34321836150`. Production materialization worked, but one test expected the old one-argument `commandRouter.process(transcript)` boundary. The already-approved Chat Mode materializer intentionally rewrites that later boundary to the guarded two-argument router form.

`9c907d3497067eb88ea1308875084b8965413951` fixed that assertion only. Workflow `34321947467` then passed the complete canonical build.

Because v70 app files did not originally match the separate Shield HOME workflow path filter, the release CI contract was tightened so `unified/app-build.gradle` version changes trigger that workflow. Final exact-head workflows below both passed.

## Final CI evidence

Build workflow:

- workflow: `Build BOOP Unified APK`
- run ID: `34322564398`
- head: `825593a16c004d9c0825720eb014c4f5cc8e58af`
- result: SUCCESS

Separate Shield HOME routing workflow:

- workflow: `Test Shield HOME routing`
- run ID: `34322564357`
- head: `825593a16c004d9c0825720eb014c4f5cc8e58af`
- result: SUCCESS
- Shield HOME routing contract: PASS

Final build checks passed:

- non-visual integration contracts
- canonical materialization
- v70 notification/dev contracts
- local `dev menu` speech-order contract
- wake handoff contract
- Launcher preservation/lint
- Shield focused functional tests
- Unified focused functional tests
- permanent-signer preparation
- signed APK assembly
- package/version/launchable activity verification
- permanent signer verification
- APK ZIP integrity
- artifact upload

Functional totals:

- Shield focused functional tests: 58/58, failures 0, errors 0, skipped 0
- Unified focused functional tests: 144/144, failures 0, errors 0, skipped 0

## Artifact receipt

Artifact:

- name: `BOOP-Unified`
- artifact ID: `10092558111`
- size: `62,739,368` bytes
- workflow run: `34322564398`
- built commit from artifact: `825593a16c004d9c0825720eb014c4f5cc8e58af`

Artifact ZIP SHA-256:

`3c68ba78f2fb36bf50d6bbaf0d85a50a8a51dc5d345349b2dc70c32ae45c00e1`

APK SHA-256:

`53c2956873e7a7268b829da5d9bd4f23d0f6ee20a0919051cb95bbd275f867a4`

Permanent signer certificate SHA-256:

`f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Independent verification performed after download:

1. The exact final workflow artifact ZIP was downloaded.
2. Its SHA-256 was computed independently and matched both GitHub's artifact metadata digest and the upload-action digest.
3. The artifact was extracted and its APK SHA-256 was computed independently; it exactly matched `apk-sha256.txt`.
4. The APK v2 signing block was parsed independently and the embedded signer certificate was SHA-256 hashed; it matched both `signer-sha256.txt` and the canonical permanent BOOP signer.
5. CI also verified package `com.boop.alpha1`, versionCode `70`, versionName `1.2.24-unified-dev-menu-doods`, the Unified launchable activity and archive integrity.

## Acceptance boundary

**GitHub performed NO visual acceptance.** No screenshot tests, golden-image tests, pixel comparisons or automated visual judgments were added or used.

Physical v70 acceptance is pending. Ryan owns appearance, animation, device and acoustic acceptance.

No v70 rollback checkpoint was created or repointed. Latest physically accepted rollback remains v59.

Suggested physical test pass:

1. Say `dev menu` from normal BOOP local speech and verify BOOP Dev opens without Chat Mode/internet.
2. Verify immersive/fullscreen presentation and clean return/exit.
3. Exercise Wake, Think + Stop, Berry 1/2/3, Shake and Sleep repeatedly.
4. Open all ten service doods and judge service recognition, current procedural eyes and exact five-finger hands.
5. Check Locked leaks no title/body; check Bundle behavior.
6. Confirm no demo creates a real Android shade notification.
7. Recheck the carried v68 iris hue behavior and finished v65 sclera/white blend physically.
