# BOOP Music Lab status: installed side by side on Shield

Updated 2026-09-13. Owner: `boop-music-lab-side-by-side-v161`. No merge into another branch is authorized; other operations continue independently.

## Latest installation outcome

Ryan explicitly requested a retry after the preceding tool-blocked attempt. The ordinary ADB install through the existing Desktop Commander connection returned Success and exit code 0. A separate read-only process completed with exit code 0 and verified the installed package, version, launcher and exact signed APK bytes.

**INSTALLED:** BOOP Music Lab, `com.boop.musiclab`, `1 / 0.1.1-v161-audio-prompt`, on Shield only. Installed SHA256 `9e717f4c28abccb951d3c03831c8d08c794e50064d85ea43627607233d66e8cb` matches the staged/GitHub-signed candidate.

**PRESERVED:** Unified remains `162 / 1.2.162-native-lyrics`, with the same APK SHA256 before/after. HOME still resolves to `com.boop.alpha1/.UnifiedEntryActivity`. No Unified replacement, merge, data clear or credential transfer.

At readback, Music Lab was installed=true, stopped=true, notLaunched=true; RECORD_AUDIO remained granted=false. No app launch, permission grant, playback command, phone or emulator operation occurred. Installation verification is not runtime or visual acceptance.

The earlier tool block is preserved at `4968d324204f74bf26420effa8bb9991391d5b69`; it is now historical, not an outstanding install failure. The explicit new user request was followed by a normal same-connector install, not a permissions or transport workaround.

## Candidate and retained build checks

Source/build commit `f613c5033e54b55bdbfe0087b0e253fd75efa7e9`; signed GitHub run `34774532761`, artifact `10322813560`. Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. No rebuild, new version or signer change in this retry.

Full v161-derived fork plus the conditional music permission entry, with independent app identity/storage/grants and no competing HOME/ASSIST/boot registration. Artwork and timing retained. Prior five fork checks, four permission tests with 18 Java decisions, six timing functions, materialized consistency, 11 focused canonical/manifest checks and signed assembly/package checks passed; see `docs/handoffs/2026-09-13-music-lab-fork.md`. Those are historical build checks, not new UI tests.

## Remaining scope

Not yet done: first launch, Android permission-dialog acceptance, Visualizer sampling or real VU bounce. Open Music Lab, choose Shield, then Launcher Settings > Now Playing > Music audio access for Ryan's consent test. The sampler/bounce remains unimplemented: music should determine bounce height while saved animation speed independently controls blinks, with no physical microphone fallback.

Read SESSION_HANDOFF.md for full installation evidence. Source/build/signing remain on GitHub; no laptop source or worktree was edited or synchronized. Only documentation is published for this installation retry. Keep the lab separate until explicit later integration instructions.
