# v187: felt animation review lab — 2026-09-15

Owner `boop-felt-review-lab-v187`, based on v186 receipt `a375a40c37df1d679799aa0281d10694a325723d`.
Ryan reports a few stationary pixels remain in v186 and requests a lab view he can pause halfway through a blink.

## Change
The existing embedded lab uses the same photographic PNG, PngPuppetRig, shader, palette binding and animation catalogue as production. Add an accessible **Felt animation lab** button in **Eyes and animation / Build a Boop**.
Lab-only controls: Pause/Resume, Slow review at15% of the saved device speed, per-clip timeline, one-millisecond steps, exact Half blink, hide/show controls and Done. Half blink selects the authored Blink at36.6ms, the midpoint of its closing segment, yielding50% closure on both lids. The timeline retains the sampled pose while paused and resumes without catching up. Selecting a clip restarts it; finished one-shot clips hold their final frame. Rotation recreates the same clip/time/pause/slow/visibility state. Local sign previews remain available.
The original standalone Animation Lab reference is preserved. The complete production animation directory and Wall face adapter compare byte-for-byte with v186. No production clock, appearance/sharing setting, artwork, eyelid-rig or shader changes.

## Verification
Red run34941333710 proved the seekable timeline was absent. Candidate34941474759 passed timeline, original rig, palette and shader checks; stopped on the older music test production-file allowlist. Only the explicitly authorized two lab Java paths were added to that list.
Initial application source `54710e332d00623c9cd17c0de792575786ac33f0` was reviewed. Run34941553708 then caught a lost saved-speed binding. Final source `b8308f4a18237b6d97ecab38ee3fd9de64ff7b6a` restores read-only saved speed and the previous power-saving preview policy. Pause captures the visible power-saving pose before taking manual control, avoiding a frame jump. The final correction was independently reviewed with no remaining blockers.
Pure Java checks cover frozen half blink, pause, resume, one-ms step, seek bounds, one-shot completion, loop wrap, slow rate, invalid delta and every pose channel across all26 catalogue clips.

## Final delivery
[Run34941872719](https://github.com/ryankemble2006-web/boop/actions/runs/34941872719) passed at `b8308f4a18237b6d97ecab38ee3fd9de64ff7b6a`. Actual source and materialized callback bodies each passed20,956 numeric checks across animator scales, saved rates, lifecycle, sign poses and power-saving pause. Existing PNG/palette/voice/music/lyrics/hue/felt-sharing/ownership checks passed; the two historical one-time source-freeze skips remain.
Permanent-signed com.boop.alpha1 version187 /1.2.187-png-puppet, APK SHA256 `0b721ba91de6f5b8dd228edc41757de40191497266bddfdd317f13ebf35787cf`. Signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Installed and actual base-APK hash/version verified on Shield and Pixel7Pro. Shield16/16 preferences unchanged; phone10/11 unchanged, only notification bookkeeping changed. Original reference lab and v186 rollback retained.

## Joint device check
On Pixel7Pro, navigated through existing UI into Eyes and animation, then Felt animation lab. Saved green felt and blue iris visible. Half blink displayed36.6ms,50%/50%,Paused; subsequent readback remained the same. +1ms gave37.6ms and52%/52%; -1ms restored36.6ms. Hide controls produced the large stage. A temporary landscape rotation preserved the held pose; Show controls readback remained36.6ms,50%/50%,Paused. Original phone auto-rotation1/user-rotation0 restored and verified. Phone left in paused lab with controls available. Appearance and eye preference hashes rechecked unchanged after use. Shield returned Home; its lab controls were not separately exercised.
Private screenshots remain local; no emulator or hosted visual tests. Lab controls are verified to the above extent. **The last stationary felt pixels remain unresolved; Ryan's next step is to hold the exact problem pose and identify them.** Do not claim visual acceptance or change the rig speculatively.
