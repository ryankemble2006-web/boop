# BOOP durable memory: shared implementation, separate Wall and Shield apps

Updated 2026-09-16. Current owner is `boop-wall-shield-split-v207`; this filename remains for continuity. One repository, one maintained shared assistant and existing shared render/media libraries, two application shells. Never resurrect the older standalone implementations when fixing these packages.

## Shield mic-button recovery after the split

The v207 microphone regression was an Android assistant-assignment problem, not missing mic code. Live v207 had the correct ASSIST activity and `shield_mic_button_choice_v1=use_boop`, while the OS role/default still selected Google Katniss. With Ryan's explicit restoration request, reassigned ASSISTANT to `com.boop.shieldoverlay`. Correct component: `com.boop.shieldoverlay/com.boop.alpha1.BoopAssistantActivity`. Keep application ID and unchanged Java namespace distinct.

This restores the successful v105+ activity-based design: voice-interaction setting empty, existing Katniss speech-recognition provider retained. Do not reintroduce the historical malformed VoiceInteractionService registration, disable the recognizer or replace the natural Voice stack. In-app opt-in alone does not prove Android role ownership. Package-changing clean installs require a fresh role check and explicit user choice, not forced defaults during newcomer setup.

Role/default readback and injected KEYCODE_ASSIST -> BOOP MainActivity/face were verified without a system restart. The installed v207 hash was unchanged; no new APK or source build was needed. Real remote speech/commands still need Ryan's acceptance. Microphone permission was initially denied, remained denied immediately after reassignment, then was observed granted with USER_SET after a permission activity appeared. This session issued no grant or dialog-selection input; do not rewind that later state.

Deezer was launched for the requested Flow cue, but Recents/YouTube transitions interrupted navigation. Further input stopped; Flow playback is not established. Full receipt: `docs/handoffs/2026-09-16-v207-shield-mic-role-recovery.md`.

## Delivered applications and lineage

Wall `com.boop.alpha1`, `207` / `1.2.207-wall`, was clean-installed only on Pixel 7 Pro. Shield `com.boop.shieldoverlay`, `207` / `1.2.207-shield`, was clean-installed only on Nvidia Shield. Installed build source `aa8fd9f6d79f28b441a48df31138a75d38420118`, signed run `35110823569`, artifact `10451993779`. Exact hashes, permanent signature, recovery, first-screen and subsequent-state evidence are in `docs/handoffs/2026-09-16-v207-signed-clean-install.md`.

The accepted v206 combined source is retained, including all subsequent accepted UI fixes. Original owner `boop-hand-colour-v191` remains at `841458b8bbc53773d16a18bb0359de1c0550f5e2`; its dirty local v203 documents were preserved. Fetch current main and application heads before changes. Main is a routing hub, not the app source.

## Preserved Voice and character

Voice is completely frozen in both shells: natural backend, model download/choices, Android fallback, existing pitch/cadence, inference settings and sharing. Ryan deferred the roughly 12-second latency investigation and alternative sources/voices for Astra later. This is neither a fix claim nor a request to revert to pre-tuning code. Do not introduce Voice tests as another split prerequisite.

Preserve the photographic felt master, rounded moving lids, original animation timing, coded animations, free independent eye/felt/hand controls, five-digit anatomy and shared appearance/speed choices. Preserve accepted v206 Home: four top controls, fixed parked favourites, equal visible spacing, no Favourite apps heading, large bottom-right idle BOOP when Now Playing disappears. No Home/art/Voice source replacement was made by the split. All 16 native libraries in both APKs match the signed v206 reference byte-for-byte.

## Routing and package-sensitive contracts

The installed application ID owns the body. No device/profile chooser remains; historical override preferences cannot turn Wall into Shield. Wall retains its built-in phone launcher and shared assistant functions. Shield opens TV/Home. Room, voice and access settings survive. BOOP routing does not grant Android privileges or force Android's Home role.

Keep Java namespaces and semantic action/protocol IDs distinct from Android application IDs. Most explicit class routing already uses getPackageName with fully qualified shared classes. The launcher self-filter uses the process UID, tested with the actual generated repository for both shells.

Shield alone owns legacy authority `com.boop.alpha1.johnny_states` plus `com.boop.shieldoverlay.johnny_states`. The current signed Johnny consumer queries/calls the legacy provider authority; retain compatibility without modifying Johnny. The fixed read-only snapshot and caller package/certificate guard are unchanged. Do not expose credentials or silently broaden HA scope.

## Newcomer test and subsequent progress (split-delivery history)

Both old Unified installs were deliberately removed without keeping data. No old app data, voice models, HA credentials, setup flags or access grants were restored. Both new apps initially reached their real first setup screens with incomplete flags; no Continue, sign-in or completion-flag write was issued by the split sequence.

Pixel 7 remained at initial setup in that sequence's last read and was not operated during mic recovery. Shield later had enabled BOOP listener/overlay access; the split sequence cleared only those grants for the fresh-access brief, preserving unrelated listeners. A following read showed Shield setup completed and YouTube foreground. The actor responsible was not established. Its no-further-input stopping point is historical; the later mic recovery above was separately authorized. Do not reset, reinstall, regrant or force the devices back to first setup.

## Working boundaries

Source edits, focused tests, builds, permanent signing and durable publication stay on GitHub. Desktop Commander on Yoga handles authorized device work with existing tools. Do not upgrade the pinned bridge or demand Work mode. Pixel 10 stays entirely outside routine work. No automatic emulator, autonomous visual acceptance or blind merge/reset.

Build checks, installed hashes, captured first screens and Ryan's physical acceptance are separate. Onboarding and later HA/media/notifications/sharing/Johnny behavior still require joint acceptance; existing source tests do not certify them. Preserve current user/device progress. Full historical root documents remain byte-for-byte in `docs/handoffs/2026-09-16-v207-before-split/`, alongside older dated archives and feature receipts.
