# BOOP Animation Lab v0.1 receipt

Date: 2026-09-09

Branch: `boop-animation-lab`

Purpose: standalone Android test app for physically reviewing current BOOP animations and the notification doods under live testing without launching the normal BOOP AIO runtime.

## Identity

- package: `com.boop.animationlab`
- versionCode: `1`
- versionName: `0.1-animation-lab-v70`
- launch activity: `com.boop.alpha1.BoopDevMenuActivity`
- build/source head: `5cb41240d53997cd222acc38d9b887dad9eceed8`
- permanent signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

This branch was forked from the then-live `boop-unified` v70 docs head and the lab build first runs `scripts/materialize-unified.sh`, so the runtime animation and notification presentation code is the finished Unified v70 materialization rather than a reimplementation.

## Lab contents

Animation shelf:

- Wake
- Idle Blink
- Listening / Reading
- Think
- Stop / Reset
- Berry 1
- Berry 2
- Berry 3
- Shake
- Sleep

Notification dood shelf retains the current v70 local previews:

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
- Locked
- Bundle

The existing current v70 notification presentation classes and locked five-finger yellow-hand asset remain the source of the dood previews.

## Standalone boundary

After exact Unified materialization, `scripts/materialize-animation-lab.py` gives the generated app its separate package identity, removes the generated Unified routing/bootstrap entry sources, removes the embedded Launcher and Shield library dependencies, and writes a minimal manifest declaring only the Animation Lab launcher activity.

The lab manifest does not declare the normal BOOP assistant services, notification-listener service, Launcher HOME surface, Shield overlay services, providers, receivers, or microphone permission. It is intended to install beside BOOP and exist only as a visual/animation test surface.

## TDD and debugging evidence

RED run `34381502070` failed 3/3 animation-lab contract tests exactly because `scripts/materialize-animation-lab.py` did not yet exist.

Implementation run `34381718790` then passed the contract and Unified materialization gates but failed Java compilation because the unused generated `UnifiedApplication.java` still referenced Shield's `LaunchCrashRecorder` after the lab correctly removed the Shield library dependency.

Commit `5cb41240d53997cd222acc38d9b887dad9eceed8` fixed that boundary by dropping the two unused generated Unified bootstrap source files from the lab tree. It did not restore the Shield library or broaden the lab manifest.

Exact-head workflow `34381973605` completed successfully. Contract tests, exact Unified materialization, Android setup, signing, APK assembly, standalone package/manifest verification, APK signature verification, ZIP integrity and artifact upload all passed.

## Artifact receipt

- workflow run: `34381973605`
- artifact: `BOOP-Animation-Lab`
- artifact ID: `10116256135`
- artifact size: `61,008,083` bytes
- artifact ZIP SHA-256: `34915d14ab934f53d8078d23566c66472d8460857be8808d3cd172c746da853e`
- APK SHA-256: `454bd5323fce8ca9a56b061acdef35f8f2958249d8c76f10a3b208109e595cc3`
- signer certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

The GitHub artifact was downloaded after the green run and independently extracted. The APK SHA-256 matched the CI receipt and its signer certificate SHA-256 matched the permanent BOOP signer.

## Acceptance boundary

GitHub performed no visual acceptance and no physical Android-device launch. Ryan owns animation appearance, dood appearance, scrolling/usability and physical-device acceptance.

This sidecar branch does not modify `boop-unified`, `main`, accepted rollback checkpoints, or the canonical Unified package/version line.
