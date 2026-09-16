# BOOP current handoff: preserve accepted Home; finish Voice before splitting

Updated 2026-09-16. Current application owner remains `boop-hand-colour-v191`. Main is the shared context hub, not the latest app source. The Wall/Shield split is authorized only after the current voice/UI work is finished, jointly accepted and preserved. No split implementation or clean install has begun in this prerequisite review.

## Verified source and device baseline

Live heads read for this review: main `c9684d82818d6143ee7abc8c8494059aed8e36df`; application branch `9a21703be2bb423607fcca87cc75c1718ed7dd54` before this documentation publication. Latest built application source is `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`.

Fresh targeted ADB reads identify Shield as `com.boop.alpha1`, versionCode `206`, `1.2.206-idle-home-corner`, APK SHA-256 `b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca`. This matches the signed v206 receipt. GitHub run `35099151524` was rechecked and all build/test/signer/package steps report success. Artifact: `10447197742`, `BOOP-Unified-v206-Idle-Home-Corner`.

Pixel 7 Pro still has `com.boop.alpha1`, versionCode `191`, `1.2.191-hand-colour`, installed APK SHA-256 `bfc1204c5da030a94991af021a0d0c4f85adb01b22d28825ce68fa7a065843f1`. This is installed-identity evidence, not a claim that the current Voice changes have been delivered to or accepted on Pixel 7. No command was directed to physical Pixel 10.

## Home acceptance is now recorded

Ryan accepted the v205 visible spacing and left alignment, then confirmed the corner repair after reload: "tested after you reloaded.. perfection. thanks boops". Preserve the four top buttons, equal visible gaps, fixed favourites position, removed Favourite apps heading, and larger idle BOOP bottom-right when Now Playing disappears. Do not request another Home redesign or infer a need to replace accepted code.

Receipt: `docs/handoffs/2026-09-16-v206-idle-home-host.md`. This acceptance is scoped to the tested Home behavior; it is not a new exhaustive test of every external app/Cast exit route or a sign-off of Voice.

## Prerequisite still open: current Voice and wider TV focus

The source contains the v200 TEST VOICE and natural pitch/rate work, v201 surface-ownership repair, v202 focus/slider fixes, and subsequent natural-voice latency/four-thread changes. Do not roll back to a v201 or v198 implementation merely because an old handoff describes it.

The fetched receipts and this conversation do not establish final human acceptance of audible current-backend output, pitch/cadence, current cross-device voice-profile sharing, or uniform Home-style focus across all requested TV controls. Pixel 7 remains on an earlier app. Establish the other window's completion/ownership status and finish the agreed joint Voice/UI checks before beginning the split. Preserve any subsequent fixes from the live owning branch; v206 is not a frozen future split base.

## Next task and safety gate

Full approved split/newcomer-test context: `docs/handoffs/2026-09-16-wall-shield-split-prerequisites.md`.

The current implementation has one application ID, `com.boop.alpha1`. Future identities are Wall `com.boop.alpha1` and Shield `com.boop.shieldoverlay`, derived from the latest accepted combined source with shared functionality maintained together, not resurrected standalone implementations.

Shield currently resolves HOME to `com.boop.alpha1/.UnifiedEntryActivity`. The only other enabled HOME activity returned by the query was `com.android.tv.settings/.system.FallbackHome`. A usable recovery route still needs to be established before any uninstall; do not silently enable/reassign a Home app.

No app source, workflow, signing configuration, permissions, app data, onboarding flags or installed package was changed in this review. Neither device is at fresh setup step one. Do not remove Unified until both replacement APKs and appropriate recovery APKs are built/retained, identity-checked and staged, and the prerequisite is closed.

## Continuity

The local owning worktree is still at `0517f73a398b43b165b1557ca6a11b26f5704615` with dirty v203 documentation. It was left untouched, not synchronized over. Read live GitHub rather than that stale working copy. Before future edits recheck both live heads and concurrent work.

The complete prior root handoff/status/memory documents are preserved byte-for-byte in `docs/handoffs/2026-09-16-pre-split-archive/`; their mixed v198/v200/v201/v205 status paragraphs are historical. Dated feature receipts remain intact. This publication is documentation-only.
