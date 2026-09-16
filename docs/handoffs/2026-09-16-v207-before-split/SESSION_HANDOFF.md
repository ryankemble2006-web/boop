# CURRENT: Voice deferred for Astra; split authorized with Voice unchanged

Updated 2026-09-16. This direction supersedes every earlier requirement in the historical material below to finish Voice, fix latency, gather another Try Emma / TEST VOICE sample or obtain further Voice acceptance before splitting. The v206 UI remains accepted.

Ryan has explicitly deferred the approximately 12-second natural-voice latency investigation, alternative sources/voices and further pitch/cadence work for Astra later. Leave the current Voice implementation completely alone in BOTH split apps. Preserve the working natural backend, voice choices, model/download path, existing pitch/cadence code and voice-profile sharing. Do not optimize, remove controls, replace providers/models or roll back to a pre-tuning implementation. Working natural voices are the preservation requirement; the latency is not claimed fixed.

Proceed with the agreed Wall/Shield packaging task from the latest live combined lineage. Voice work is no longer a prerequisite. Preserve all accepted UI/features and concurrent work. The package-sensitive integration audit, both signed/verified replacement APKs, recovery checks and stop-at-first-real-setup deployment conditions remain mandatory.

Current execution status: no split source changes, new APKs or installation changes yet. One multi-file source-audit command was blocked by the tool safety check; it was not rerouted. This publication records the approved Voice deferral only. Current receipt: `docs/handoffs/2026-09-16-voice-deferred-split-authorized.md`.

## Historical pre-deferral record below

Earlier Voice prerequisites below are preserved for history and are NOT current instructions.

---

# BOOP current handoff: v206 UI accepted; finish Voice before splitting

Updated 2026-09-16. Current application owner remains `boop-hand-colour-v191`. Main is the shared context hub, not the latest app source. The Wall/Shield split is authorized only after the current voice/UI work is finished, jointly accepted and preserved. No split implementation or clean install has begun in this prerequisite review.

## Verified source and device baseline

Live heads read for this review: main `c9684d82818d6143ee7abc8c8494059aed8e36df`; application branch `9a21703be2bb423607fcca87cc75c1718ed7dd54` before this documentation publication. Latest built application source is `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`.

Fresh targeted ADB reads identify Shield as `com.boop.alpha1`, versionCode `206`, `1.2.206-idle-home-corner`, APK SHA-256 `b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca`. This matches the signed v206 receipt. GitHub run `35099151524` was rechecked and all build/test/signer/package steps report success. Artifact: `10447197742`, `BOOP-Unified-v206-Idle-Home-Corner`.

Pixel 7 Pro still has `com.boop.alpha1`, versionCode `191`, `1.2.191-hand-colour`, installed APK SHA-256 `bfc1204c5da030a94991af021a0d0c4f85adb01b22d28825ce68fa7a065843f1`. This is installed-identity evidence, not a claim that the current Voice changes have been delivered to or accepted on Pixel 7. No command was directed to physical Pixel 10.

## Home acceptance is now recorded

Ryan accepted the v205 visible spacing and left alignment, then confirmed the corner repair after reload: "tested after you reloaded.. perfection. thanks boops". Preserve the four top buttons, equal visible gaps, fixed favourites position, removed Favourite apps heading, and larger idle BOOP bottom-right when Now Playing disappears. Do not request another Home redesign or infer a need to replace accepted code.

Receipt: `docs/handoffs/2026-09-16-v206-idle-home-host.md`. This acceptance is scoped to the tested Home behavior; it is not a new exhaustive test of every external app/Cast exit route or a sign-off of Voice.

## Prerequisite still open: current Voice response delay and sharing

The source contains the v200 TEST VOICE and natural pitch/rate work, v201 surface-ownership repair, v202 focus/slider fixes, and subsequent natural-voice latency/four-thread changes. Do not roll back to a v201 or v198 implementation merely because an old handoff describes it.

Ryan explicitly confirmed v206 as the working baseline and its UI as accepted in the continuing chat. Do not keep a pending UI/focus acceptance gate from an older paragraph or redesign accepted controls. His earlier voice test reported natural voices present, Pitch/Cadence affecting speech and acceptable blue highlights, but long Try Emma / TEST VOICE delays. Current v206 response-delay acceptance and cross-device voice-sharing verification remain open; Pixel 7 is still on v191. Preserve the current pitch playback, latency diagnostics and four-thread inference rather than returning to old voice implementations. Determine the collaborating window's completion/ownership status and finish the remaining joint Voice checks before splitting, retaining all subsequent accepted fixes from the live owner.

## Next task and safety gate

Full approved split/newcomer-test context: `docs/handoffs/2026-09-16-wall-shield-split-prerequisites.md`.

The current implementation has one application ID, `com.boop.alpha1`. Future identities are Wall `com.boop.alpha1` and Shield `com.boop.shieldoverlay`, derived from the latest accepted combined source with shared functionality maintained together, not resurrected standalone implementations.

Shield currently resolves HOME to `com.boop.alpha1/.UnifiedEntryActivity`. The only other enabled HOME activity returned by the query was `com.android.tv.settings/.system.FallbackHome`. A usable recovery route still needs to be established before any uninstall; do not silently enable/reassign a Home app.

No app source, workflow, signing configuration, permissions, app data, onboarding flags or installed package was changed in this review. Neither device is at fresh setup step one. Do not remove Unified until both replacement APKs and appropriate recovery APKs are built/retained, identity-checked and staged, and the prerequisite is closed.

## Continuity

The local owning worktree is still at `0517f73a398b43b165b1557ca6a11b26f5704615` with dirty v203 documentation. It was left untouched, not synchronized over. Read live GitHub rather than that stale working copy. Before future edits recheck both live heads and concurrent work.

The complete prior root handoff/status/memory documents are preserved byte-for-byte in `docs/handoffs/2026-09-16-pre-split-archive/`; their mixed v198/v200/v201/v205 status paragraphs are historical. Dated feature receipts remain intact. This publication is documentation-only.

## Supplemental device evidence: recovery retained; accepted UI preserved

Both existing installed APKs are now retained privately outside the checkout, with copied hashes and permanent signatures verified. No BOOP app data was backed up. Normal Shield UI navigation opened and privately captured the existing v206 Voice screen without changing settings or triggering speech.

The newest v206 UI sign-off and earlier successful pitch/cadence observations remain accepted. Do not reopen a UI/focus gate. Current Voice response-delay acceptance and voice-profile sharing remain outstanding; no split or clean install has begun. Recovery copies are not replacement APKs or proof of a tested HOME recovery route.

Exact recovery evidence and continuation boundary: `docs/handoffs/2026-09-16-split-recovery-and-voice-ready.md`. The concurrent `de28b0762b616acfaff61cf246cb8d6670dea5c5` clarification and dirty local v203 documents were preserved.
