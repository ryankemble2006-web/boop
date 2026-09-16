# CURRENT: Voice deferred for Astra; split authorized with Voice unchanged

Updated 2026-09-16. This direction supersedes every earlier requirement in the historical material below to finish Voice, fix latency, gather another Try Emma / TEST VOICE sample or obtain further Voice acceptance before splitting. The v206 UI remains accepted.

Ryan has explicitly deferred the approximately 12-second natural-voice latency investigation, alternative sources/voices and further pitch/cadence work for Astra later. Leave the current Voice implementation completely alone in BOTH split apps. Preserve the working natural backend, voice choices, model/download path, existing pitch/cadence code and voice-profile sharing. Do not optimize, remove controls, replace providers/models or roll back to a pre-tuning implementation. Working natural voices are the preservation requirement; the latency is not claimed fixed.

Proceed with the agreed Wall/Shield packaging task from the latest live combined lineage. Voice work is no longer a prerequisite. Preserve all accepted UI/features and concurrent work. The package-sensitive integration audit, both signed/verified replacement APKs, recovery checks and stop-at-first-real-setup deployment conditions remain mandatory.

Current execution status: no split source changes, new APKs or installation changes yet. One multi-file source-audit command was blocked by the tool safety check; it was not rerouted. This publication records the approved Voice deferral only. Current receipt: `docs/handoffs/2026-09-16-voice-deferred-split-authorized.md`.

## Historical pre-deferral record below

Earlier Voice prerequisites below are preserved for history and are NOT current instructions.

---

# BOOP durable memory: accepted v206 UI and gated Wall/Shield split

Updated 2026-09-16. Current application owner remains `boop-hand-colour-v191`; read LIVE GitHub heads and latest receipts before any edit. A version-like branch name and the primary checkout are not application-version evidence.

## Current source, installation and acceptance

Latest verified build source is `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`, v206 `1.2.206-idle-home-corner`; signed run `35099151524`, artifact `10447197742`. The source was followed by documentation head `9a21703be2bb423607fcca87cc75c1718ed7dd54` before this reconciliation.

Fresh ADB reads confirm Shield `com.boop.alpha1` v206 with APK hash `b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca`. Pixel 7 Pro still runs `com.boop.alpha1` v191 `1.2.191-hand-colour`, hash `bfc1204c5da030a94991af021a0d0c4f85adb01b22d28825ce68fa7a065843f1`.

Ryan explicitly accepted the Home visual spacing and left alignment, then the corner behavior after reload. Preserve four buttons across the top; Now Playing below; equal visible 16dp spacing to the aligned wide favourite artwork and between the wide tiles; no Favourite apps heading; favourites parked even when the panel vanishes; one larger idle BOOP in the bottom-right. The v206 owner guard prevents the media attach path from overwriting the Home host's size/gravity/margins. The tested Home result is accepted; not every external playback-exit route was individually tested.

Preserve subsequent Voice work already in the source: TEST VOICE, natural pitch/rate playback, modal eye-surface hiding, TV focus/slider fixes, voice latency markers and four-thread Shield natural inference. Old v198 statements that natural pitch is deliberately unapplied are historical, not current implementation instructions. Ryan now explicitly confirms the v206 UI as accepted; do not reopen a UI/focus acceptance gate. Earlier voice testing reported natural voices present and Pitch/Cadence affecting speech, with long Try Emma / TEST VOICE delays. Current v206 response-delay acceptance and cross-device voice-sharing verification remain pending. Pixel 7's v191 identity does not prove those later features, and the UI confirmation is not a new v206 audio test.

## Preserve accepted functionality in any later split

Keep the photographic felt character, rounded moving eyelids, original timing, coded animations and permanent artwork masters. Preserve appearance controls and eye/felt/hand/speed sharing, five-digit hand anatomy, saved-choice support, local Home Assistant control, voice behavior and optional model downloads, notifications, native lyrics, media controls/bounce, applicable built-in launcher functionality and Johnny's HA/state integration. Preserve the locked felt default `boop-felt-default-v189` and the accepted v191 appearance/hand-sharing checkpoint. A clean-install test deliberately removes BOOP-local data later; it does not authorize changing features, Home Assistant itself or artwork.

## Agreed architecture task, not yet started

One repository; separate Wall and Shield application shells with shared functionality maintained together. Restore package identities Wall `com.boop.alpha1` and Shield `com.boop.shieldoverlay`. These names are not directions to revert to old standalone code. The current app actually has one application ID, `com.boop.alpha1`; earlier claims of existing `com.boop.unified.wall` / `com.boop.unified.shield` packages were incorrect.

Automatic app routing: Wall/phone UI and appropriate built-in launcher on Wall; TV/Home UI on Shield. No device/profile-selection question. Keep useful room, voice and access settings and genuine Android permission/default-Home confirmations. Do not strip shared assistant capabilities merely because responsibilities are device-specific.

Mandatory gate: finish and jointly accept current Voice/UI, preserve it, and check concurrent work before split edits. Incorporate all subsequent accepted fixes, not a stale v206 snapshot. Full context and deployment sequence: `docs/handoffs/2026-09-16-wall-shield-split-prerequisites.md`.

## Device/deployment boundaries

Only Pixel 7 Pro and Shield are targets. Leave physical Pixel 10 entirely alone. Use the already-running Desktop Commander bridge on Yoga and discover current ADB identities. Restart the bridge only after a real bridge failure, with `npx.cmd -y @wonderwhy-er/desktop-commander@0.2.47 remote`; no upgrades or Work-mode prompts.

Before uninstalling Unified, have both permanently signed replacement APKs identity-checked/staged, suitable recovery APKs retained, and a usable Shield recovery route. Fresh read: Unified owns Shield HOME; the other returned enabled HOME activity is Android settings FallbackHome. This is not yet a proven usable recovery launcher.

Then intentionally uninstall Unified on each target and install only its corresponding replacement, without retaining/restoring app data or setup/access flags. Do not reset hardware, change HA or remove unrelated apps. Open each real first setup screen and stop. No sign-in, pre-grants, forced default Home or ADB shortcuts past onboarding.

## Working evidence and historical continuity

Source edits, non-visual tests, builds, permanent signing and durable handoffs stay on GitHub. Use focused functional regressions, then existing build/signing checks; compilation, installation and human acceptance are separate. No autonomous emulator/visual acceptance gate. Do not bypass tool denials or overwrite concurrent work.

The dirty local v203 documentation was left untouched. Full previous root documents are preserved byte-for-byte at `docs/handoffs/2026-09-16-pre-split-archive/`, and dated v198/v200/v201/v205/v206, voice-blocker and storage receipts remain available. Historical device versions and old next steps must not override current readings.

## Supplemental device evidence: recovery retained; accepted UI preserved

Both existing installed APKs are now retained privately outside the checkout, with copied hashes and permanent signatures verified. No BOOP app data was backed up. Normal Shield UI navigation opened and privately captured the existing v206 Voice screen without changing settings or triggering speech.

The newest v206 UI sign-off and earlier successful pitch/cadence observations remain accepted. Do not reopen a UI/focus gate. Current Voice response-delay acceptance and voice-profile sharing remain outstanding; no split or clean install has begun. Recovery copies are not replacement APKs or proof of a tested HOME recovery route.

Exact recovery evidence and continuation boundary: `docs/handoffs/2026-09-16-split-recovery-and-voice-ready.md`. The concurrent `de28b0762b616acfaff61cf246cb8d6670dea5c5` clarification and dirty local v203 documents were preserved.
