# BOOP Unified v70 developer-menu physical hotfix receipt

Date: 2026-09-09

Canonical branch: `boop-unified`

Package: `com.boop.alpha1`

Version remains: `70` / `1.2.24-unified-dev-menu-doods`

## Physical report that triggered this hotfix

Ryan physically tested v70 and reported two regressions:

1. saying `dev menu` caused the BOOP app/window to close instead of presenting BOOP Dev;
2. the `Dev menu` control in Voice Settings was below the reachable touch area because the settings column itself was not vertically scrollable.

Physical acceptance of v70 therefore remains pending. The physically accepted rollback remains v59.

## Root cause and repair

The local recognized-speech `dev menu` branch switched activities directly from the speech-result path while the wake session was still in PROCESSING. The v70 hotfix now releases wake processing before the activity hop and posts the explicit `BoopDevMenuActivity` launch through the interaction surface so the recognizer callback can unwind first. Launch failure is caught and returns BOOP to the ordinary interaction face.

Voice Settings previously attached its full-height `LinearLayout` directly to the interaction surface. Later v70 rows for Notifications and Dev menu could extend below the physical viewport. The hotfix now wraps the generated Voice Settings content in a full-height vertical `ScrollView`, while preserving the existing content column and dismissal path.

The scroll repair is deliberately materialized only after the older Wall/eye/wake patches have consumed their established `MainActivity` anchors and before Notifications and Dev menu rows are appended.

No package, permission, signer, version, notification privacy contract, eye artwork, hands artwork, HA behavior, Shield behavior, or automatic-install policy changed.

## Test-first evidence

A regression contract was added before the successful implementation. It requires:

- `dev menu` to remain a local recognized-speech interception before downstream voice/router handling;
- the spoken branch to call the shared `openDevMenu()` path and return;
- `openDevMenu()` to finish wake processing, use explicit `BoopDevMenuActivity`, and post the activity launch through the interaction surface;
- generated Voice Settings to contain a `ScrollView` around the settings column and remove that scroll root on dismissal;
- the v70 scroll repair to run before later Notifications and Dev menu materializers.

Several intermediate attempts failed during canonical materialization because they anchored too broadly in generated `MainActivity`. Those failures were not shipped. The final helper works against the bounded `showVoiceSettings()` / `hideVoiceSettings()` method seams and the exact successful build below.

## Verified hotfix build

App implementation head:

`95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a`

Build workflow:

- workflow: `Build BOOP Unified APK`
- run ID: `34381830799`
- result: SUCCESS

The exact workflow passed:

- non-visual integration contracts;
- canonical materialization;
- v70 notification and developer-lab regression contracts;
- seamless wake-command handoff contract;
- Launcher preservation/lint;
- Shield focused functional tests;
- wake/name/routing/lifecycle/assistant-policy tests;
- permanent signer preparation;
- signed APK assembly;
- package/version/launchable-activity/permanent-signer/archive verification;
- artifact upload.

Artifact:

- name: `BOOP-Unified`
- artifact ID: `10116298801`
- size: `62,739,475` bytes
- artifact ZIP digest from GitHub: `sha256:8007675a308e3921fe6bde895c0d60bec7c5beb550a9ae31dac604f9112ed7ee`
- workflow head: `95224d95a04b6a79a8d8c4e9e5ac4c35cf64ca9a`

## Physical retest boundary

GitHub performed no device, visual, microphone or acoustic acceptance. Ryan should physically retest this exact v70 hotfix by:

1. saying `dev menu` from normal BOOP speech and confirming BOOP Dev opens rather than BOOP disappearing;
2. opening Voice Settings, scrolling to the bottom, and confirming both `Dev menu` and `Done` are reachable and tappable;
3. exiting BOOP Dev back to BOOP and repeating the spoken command to ensure the wake/microphone path remains healthy;
4. rechecking the previously pending v70 dood/animation acceptance as desired.

Do not mark v70 physically accepted or create/repoint a rollback checkpoint until Ryan confirms the physical pass.
