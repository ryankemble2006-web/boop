# CURRENT: Voice deferred for Astra; split authorized with Voice unchanged

Updated 2026-09-16. This direction supersedes every earlier requirement in the historical material below to finish Voice, fix latency, gather another Try Emma / TEST VOICE sample or obtain further Voice acceptance before splitting. The v206 UI remains accepted.

Ryan has explicitly deferred the approximately 12-second natural-voice latency investigation, alternative sources/voices and further pitch/cadence work for Astra later. Leave the current Voice implementation completely alone in BOTH split apps. Preserve the working natural backend, voice choices, model/download path, existing pitch/cadence code and voice-profile sharing. Do not optimize, remove controls, replace providers/models or roll back to a pre-tuning implementation. Working natural voices are the preservation requirement; the latency is not claimed fixed.

Proceed with the agreed Wall/Shield packaging task from the latest live combined lineage. Voice work is no longer a prerequisite. Preserve all accepted UI/features and concurrent work. The package-sensitive integration audit, both signed/verified replacement APKs, recovery checks and stop-at-first-real-setup deployment conditions remain mandatory.

Current execution status: no split source changes, new APKs or installation changes yet. One multi-file source-audit command was blocked by the tool safety check; it was not rerouted. This publication records the approved Voice deferral only. Current receipt: `docs/handoffs/2026-09-16-voice-deferred-split-authorized.md`.

## Historical pre-deferral record below

Earlier Voice prerequisites below are preserved for history and are NOT current instructions.

---

# Wall/Shield separation: approved task and prerequisite review

Date: 2026-09-16. Current owner: `boop-hand-colour-v191`.

## Gate before architecture work

Ryan has agreed to separate the current combined BOOP into Wall and Shield apps, then clean-install each on its target for a genuine newcomer test. This approval is conditional: current Voice/UI work must first be finished, verified with Ryan and preserved. Do not interrupt another window's unfinished work or overwrite its fixes. Do not freeze a stale split baseline; re-read live heads and incorporate all subsequent accepted work before branching/implementing.

This review did not start the split, change app source or workflows, create replacement APKs, uninstall/reinstall anything, or navigate setup. It reconciles source/device facts and records the prerequisite and Home acceptance.

## Source and workspace reconciliation

Live main: `c9684d82818d6143ee7abc8c8494059aed8e36df`. Live owning branch before this documentation update: `9a21703be2bb423607fcca87cc75c1718ed7dd54`. Built source: `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`.

The requested local AGENTS, start-here, context and owning worktree handoff were read. Local shared documents were dated 6 September and the owning worktree was at `0517f73a398b43b165b1557ca6a11b26f5704615`, with dirty v203 handoff/status/memory and an untracked v203 receipt. All were preserved untouched. Current main has intentionally replaced the old root AGENTS/context stack with `BOOP_START_HERE.md`; its current routing still names `boop-hand-colour-v191` as the combined-app owner. Do not use stale local standalone pointers as a rollback direction.

The current `unified/app-build.gradle` has application ID `com.boop.alpha1`, versionCode 206, `1.2.206-idle-home-corner`. This is one installed application identity, not already-existing `com.boop.unified.wall` / `com.boop.unified.shield` packages.

## Current verification and acceptance

GitHub signed run `35099151524` was re-read and reports success for all steps: focused ownership/Voice checks, inherited functional tests, Android build, existing signer and packaged identity/content checks. Artifact `10447197742`, `BOOP-Unified-v206-Idle-Home-Corner`. This review did not rerun the build or mistake its success for Voice acceptance.

Fresh targeted ADB reads:

- Shield: `com.boop.alpha1`, `206` / `1.2.206-idle-home-corner`, installed APK SHA-256 `b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca`, matching the v206 receipt.
- Pixel 7 Pro: `com.boop.alpha1`, `191` / `1.2.191-hand-colour`, installed APK SHA-256 `bfc1204c5da030a94991af021a0d0c4f85adb01b22d28825ce68fa7a065843f1`. Current Voice changes are not established on this earlier installation.

Ryan accepted v205 Home spacing/left alignment and subsequently confirmed the bottom-right repair after reload: "tested after you reloaded.. perfection. thanks boops". Record that as scoped physical Home acceptance. Preserve all four top buttons, fixed favourites, equal visible gaps, removed heading and the idle-corner behavior.

Latest clarification, 2026-09-16: Ryan explicitly confirms v206 as the working baseline and the UI as accepted. Do not retain a pending UI/focus gate from an earlier review. His earlier Voice test reported natural voices present and Pitch/Cadence affecting speech, but long Try Emma / TEST VOICE delays. Current v206 response-delay acceptance and cross-device voice-sharing verification remain open; determine the collaborating window's completion state and finish those joint Voice checks before splitting. Missing final acceptance is not permission to rewrite current voice implementations.

This continuation independently rechecked the same installed v206/v191 identities and APK hashes above, and the successful v206 build job `104803873837` plus separate Home-layout, voice-profile and TV/Voice runs `35099151474`, `35099151512`, `35099151480`. Current `source/BoopNaturalSpeechBackend.java` and `tests/test_natural_voice_latency_v203.py` retain pitch playback, four-thread inference and request/model/synthesis/playback timing markers. A read-only check of retained Shield `BOOP-VoiceLatency` logs found no stage samples; no fresh voice playback was triggered or accepted. The next joint timing sample is Try Emma, TEST VOICE, then Try Emma again on current v206, without disrupting another window's test.

A concurrent documentation publication advanced the owner to `926ce10b852328a73166ed87bfcc5e6a22968bd8`. The stale-head guard stopped this continuation's earlier proposed write before any write occurred; this clarification is based on the newer documents, preserving that publication and its archived history. No application source, workflow, install, permission or default-Home change is included.

## Approved packaging outcome

Keep one repository with separate application shells and genuinely shared functionality maintained together. Wall package `com.boop.alpha1` automatically uses Wall/phone experience and its appropriate built-in launcher. Shield package `com.boop.shieldoverlay` automatically uses TV/Home. Neither asks which device/profile to choose. Retain useful room, voice and access settings; automatic BOOP routing must not silently grant permissions or assign Android HOME.

Use the latest accepted combined implementation. This is packaging/architecture work, not redesign or permission to resurrect older standalone apps. Retain photographic felt/animations, appearance controls, HA, shared assistant/voice functionality, cross-device sharing, notifications and applicable launcher/media features. Separate genuinely device-specific responsibilities without gratuitous feature removal.

Audit current manifests, explicit intents, HOME routing, services, receivers, provider authorities and consumers, package visibility, permissions and signing checks. Johnny's HA/state bridge must be audited on both sides. The historically hard-coded `com.boop.alpha1.johnny_states` is an inspection target, not proof that present consumers have already been audited or migrated. Use focused behavioral/package integration regressions and the existing GitHub build/permanent-signer checks for both apps. Compilation alone is insufficient.

## Clean-install and recovery gate

Fresh Shield HOME resolution is `com.boop.alpha1/.UnifiedEntryActivity`. The only other enabled HOME activity returned was `com.android.tv.settings/.system.FallbackHome`. Do not claim that supplies a tested usable launcher recovery path. Establish recovery before removal without silently selecting/enabling another default Home.

Before uninstalling anything, both replacement APKs must be built, permanently signed, package/version/content-checked and staged, with suitable known-good recovery APKs retained. Re-identify current devices/packages and Home ownership at that time. No replacement/recovery staging is claimed by this review.

After the prerequisite and deployment gates are met, uninstall existing Unified from Pixel 7 Pro and Shield and install only Wall on Pixel 7 Pro and only Shield on Nvidia Shield. This is an intentional fresh installation, not an in-place update or dual installation on either target. Do not retain/restore app data, onboarding completion, access grants or sign-in. Ryan understands the loss of BOOP-local setup.

Do not reset devices, alter HA or remove unrelated apps. Leave physical Pixel 10 entirely alone. Launch each replacement at its genuine first setup screen and STOP: no click-through, credentials, pre-grants, forced default Home or bypasses. This is a fresh-app test on existing hardware, not a factory-reset or sideload-installer test.

## Working and reporting boundaries

Work directly in chat with GitHub and the connected Yoga bridge. Discover current transports; do not assume historical addresses. In this review Desktop Commander worked, ADB initially listed no transports, mDNS discovered current services, paired phones appeared, and the discovered Shield transport connected successfully. Only Shield/Pixel 7 were targeted. No bridge restart or upgrade was needed.

Publish reviewed scoped changes, update owning context/handoff/status/memory, and verify the live remote head. Preserve dirty and concurrent work; no resets, force pushes or silent lineage merges. When actual ownership/package contracts change, update the shared context on main without treating main as the app source.

End-state of this prerequisite review: existing setups untouched; neither device is at fresh onboarding step one. The split and newcomer test await completion/acceptance of the current Voice/UI work.

## Supplemental device evidence: recovery retained; accepted UI preserved

Both existing installed APKs are now retained privately outside the checkout, with copied hashes and permanent signatures verified. No BOOP app data was backed up. Normal Shield UI navigation opened and privately captured the existing v206 Voice screen without changing settings or triggering speech.

The newest v206 UI sign-off and earlier successful pitch/cadence observations remain accepted. Do not reopen a UI/focus gate. Current Voice response-delay acceptance and voice-profile sharing remain outstanding; no split or clean install has begun. Recovery copies are not replacement APKs or proof of a tested HOME recovery route.

Exact recovery evidence and continuation boundary: `docs/handoffs/2026-09-16-split-recovery-and-voice-ready.md`. The concurrent `de28b0762b616acfaff61cf246cb8d6670dea5c5` clarification and dirty local v203 documents were preserved.
