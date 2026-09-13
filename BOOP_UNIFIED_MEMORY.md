# Current Unified memory

Ryan: this work is primary; old recovery threads are irrelevant, preserve their work without stopping progress. Source/build/tests through GitHub; visual/runtime checks on local emulators, then Shield/Pixel 7. Physical Pixel 10 stays excluded.
Accepted v156 sweep is finished. v159 shared colour source 0a4134eb passed GitHub, was installed/hash-verified on emulators and Shield/Pixel 7. Shield HA publishing/rejoin succeeded and shared hue restored to 190. Pixel 7 lock prevented two-device sharing verification; no bypass.
Next candidate v160 adds saved device-local animation speed .5/1/1.5/2. 1x must match v156 exactly, no authored keyframe/blink/artwork changes. Preserve boop_eyes/hue_degrees range0..359/default190 and working Wall hue. Shared colour opt-in, authenticated HA only, no stale-default writes.
Require both timing CI and full build success plus local runtime checks before deployment. Current speed implementation is not yet a passing build. No unrelated native-lyrics merge. Research and interrupted drafts remain preserved in Git history and docs/superpowers/plans/2026-09-13-shared-colour-and-speed.md.
