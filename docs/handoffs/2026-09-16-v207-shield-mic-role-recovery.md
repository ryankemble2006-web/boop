# v207 Shield microphone-button role recovery

Date: 2026-09-16. Owner: `boop-wall-shield-split-v207`. Starting LIVE HEAD: `2079a505e1ee43ef738e9925fba4f5c9a1873919`.

## Request and boundaries

Ryan reported that the microphone button had reverted to Google after the Wall/Shield split, despite the earlier successful takeover. He explicitly requested restoration and authorized Shield runtime access, with a Deezer Flow cue for the real-button test. Leave the natural Voice implementation, pitch/cadence, models, providers and unrelated UI alone. No Work-mode handoff, bridge upgrade or emulator gate.

The existing Desktop Commander 0.2.47 bridge on Yoga was online. File reads and actual Shield ADB commands succeeded in this chat. The earlier conversational claims that this chat could not use the bridge were incorrect. One combined shell discovery call was blocked; no permission/configuration widening was performed. The later narrow ADB device query and scoped runtime calls succeeded normally.

## Diagnosis

The installed package was `com.boop.shieldoverlay`, version `207` / `1.2.207-shield`. Android reported Google Katniss as ASSISTANT role holder and in both assistant and voice-interaction settings. The speech recognizer was KatnissRecognitionService.

The new BOOP package already had `shield_mic_button_choice_v1=use_boop`. A package-scoped query returned exactly one DEFAULT ASSIST activity, `com.boop.shieldoverlay/com.boop.alpha1.BoopAssistantActivity`, and no discoverable VoiceInteractionService. Source inspection confirmed the existing activity-based assistant and shared namespace were preserved by shell materialization. No missing-code defect was established.

Historical reference: `docs/history/startup-v133/SESSION_HANDOFF.md`, v105 role-assignment receipt and subsequent v108 physical remote acceptance. v105 replaced the malformed voice-service route with an activity assistant, clearing the interactor while retaining system recognition. v108 was accepted for real remote candle and specific music commands. These are the relevant proven ancestors, not a reason to reinstall an old APK.

## Authorized repair

Executed on Shield only:

```text
cmd role add-role-holder --user 0 android.app.role.ASSISTANT com.boop.shieldoverlay
```

The new application ID is `com.boop.shieldoverlay`; the shared class remains `com.boop.alpha1.BoopAssistantActivity`. Do not incorrectly shorten that component to a class in the shell package.

Readback after the command:

- ASSISTANT role holder: `com.boop.shieldoverlay`.
- Secure assistant: `com.boop.shieldoverlay/com.boop.alpha1.BoopAssistantActivity`.
- Secure voice-interaction service: empty.
- Secure recognition service: original `com.google.android.katniss/com.google.android.apps.tvsearch.voice.recognition.KatnissRecognitionService`.
- BOOP Shield already held HOME before this repair and still held it afterward; this task did not change HOME.
- Same system-server process and boot-completed state before/after: no system restart observed.

Google Assistant no longer owns this route. Google-provided speech recognition is intentionally retained, as in the accepted implementation; this is not a claim that all Google software has been removed.

## Verification and limits

An injected KEYCODE_ASSIST (219) opened `com.boop.shieldoverlay/com.boop.alpha1.MainActivity`. The fresh activity/window readback and UI hierarchy identified BOOP's face, not Google Assistant. This establishes software-trigger routing, not physical Bluetooth remote input, captured speech or a successful house command. No synthetic house-control phrase was issued.

Microphone permission was denied before repair and still denied immediately after role assignment. During the injected-button check, a permission activity was observed; a later read showed RECORD_AUDIO granted with USER_SET. This session did not execute `pm grant`, change app-ops, or send a selection input to the permission dialog. The source of the intervening permission choice was not established from these observations. Preserve it; do not silently restore the earlier denied snapshot.

The installed APK was hashed again and remains:

```text
d4cff18acc85e7360601de042c37281ae6f262018b428525834ed70188574a0f
```

This exactly matches the signed v207 delivery from source `aa8fd9f6d79f28b441a48df31138a75d38420118`, run `35110823569`, artifact `10451993779`. No APK was rebuilt or installed, and no source, artwork, natural Voice, signing key, app data or stored appearance setting was changed. No emulator or physical Pixel phone was operated. Documentation-only publication does not require rerunning the Android build; no new CI/code-test result is claimed.

## Requested attention cue

The installed Deezer has no activity resolving the Flow HTTPS VIEW intent, so that intent was not launched. Its actual Leanback entry was resolved and started. A normal MAIN/LEANBACK launch returned success and a fresh read confirmed Deezer foreground. Later activity logs showed Recents launches and subsequent foreground returned to YouTube; Deezer's process also died/restarted during that period. The cause/actor was not established. Navigation stopped rather than competing with a possible user or concurrent session. Flow was not selected or verified playing. Do not report the music cue as completed.

## Next safe step

Ryan presses the real Shield microphone button and first tries a harmless greeting. Confirm BOOP activation, actual recognition and response before trying a room-control command. Record his verdict separately from the successful role/default and injected-input checks. Voice latency remains deferred. Do not reinstall or reset v207 just to repair an already corrected OS assignment.

All device addresses, raw diagnostics and private UI material are omitted from this public receipt. Local source worktrees and sibling branches remain untouched.
