# BOOP v70 developer-menu in-place hotfix receipt

Date: 2026-09-09
Branch: `boop-unified`
Package: `com.boop.alpha1`
Release identity remains `versionCode 70`, `versionName 1.2.24-unified-dev-menu-doods`.

## Physical evidence that triggered this repair

Ryan physically tested the earlier v70 activity-hop hotfix and reported that saying `dev menu` still forced BOOP to close. That invalidates the earlier activity-hop candidate as a physical fix even though its CI build was green. The Voice Settings vertical-scroll repair from that candidate remains valid and is preserved.

## Root repair

The developer-menu route no longer launches `BoopDevMenuActivity` from recognized speech or from the Voice Settings button. The current materialized route stays inside the existing `MainActivity` and places a fullscreen developer-menu overlay on BOOP's existing `interactionSurface`.

The exact local spoken trigger is now `developer menu`. The former `dev menu` phrase is intentionally rejected by `BoopDevMenuIntent`. The local interception remains before Home Assistant, command-router and chat fallback.

The in-place menu keeps the existing developer-lab behavior: Wake, Think, Stop, Berry 1/2/3, Shake, Sleep, the app-specific notification dood previews, Locked and Bundle. Notification previews remain local fixtures and do not post Android shade notifications or expand permissions.

Voice Settings remains vertically scrollable, and its row label is now `Developer menu`.

## Test-first evidence

The new materialized contract requires the spoken route to call `showDeveloperMenu()` and forbids `startActivity(` / `BoopDevMenuActivity.class` inside that route. The exact phrase contract requires `developer menu` and rejects `dev menu`.

A stale JUnit intent test still expected the old phrase and caused workflow `34384614738` at head `d5788c1482496c34d73f212fb0de3e678b941599` to fail at the focused developer-lab test gate. Updating that stale test to the new contract produced the exact green candidate below.

## Exact CI/signer-green candidate

Code/test head:

`c17e98d9a09471cf8f53f2bee171a77e3b3b1203`

GitHub Actions:

- workflow: `Build BOOP Unified APK`
- run: `34385817960`
- result: SUCCESS
- exact workflow head: `c17e98d9a09471cf8f53f2bee171a77e3b3b1203`
- developer-lab/materialized/JUnit gate: PASS
- seamless wake-command handoff: PASS
- Launcher preservation/lint: PASS
- Shield focused functional tests: 58/58, zero failures/errors/skips
- Unified focused functional tests: 144/144, zero failures/errors/skips
- signed APK build: PASS
- package/permanent-signer/archive verification: PASS
- artifact upload: PASS

Artifact:

- name: `BOOP-Unified`
- ID: `10117795236`
- size: `62,741,119` bytes
- GitHub artifact ZIP SHA-256: `5f11f925c097b67f1650fcb445918148e633071739b23245c54742d984c20faf`
- APK SHA-256: `c45962533574b9a0ef5bd08ad3f785c94668e9ec793ed6377967d7cae5195a20`
- permanent BOOP signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

The artifact ZIP was independently downloaded after CI; its SHA-256 matched GitHub's artifact digest. The extracted APK hash matched the included CI receipt, and the signer receipt matched the permanent BOOP signer.

## Physical acceptance boundary

This candidate is CI/signer green only. Ryan has not yet physically accepted it.

Physical check:

1. install the APK from workflow `34385817960`;
2. say `developer menu` and confirm BOOP stays open and presents BOOP Dev;
3. confirm saying `dev menu` no longer invokes BOOP Dev;
4. open Voice Settings, scroll to `Developer menu`, open it, use `Done`, then repeat the spoken command;
5. confirm wake/microphone behavior remains healthy after entering and leaving the in-place menu.

Do not create or repoint a v70 physical rollback checkpoint until Ryan confirms the real-device pass. Latest physically accepted rollback remains v59: `checkpoint-boop-unified-v59-uncensored-speech` -> `136b56e6faac8ce450b957ac3057a379c68c7b7b`.

No package, permission, signer, notification privacy contract, approved eye/hands asset, Home Assistant behavior, Shield behavior or automatic-install policy changed in this repair.
