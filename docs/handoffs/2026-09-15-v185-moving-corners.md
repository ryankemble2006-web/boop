# v185: moving photographic corners — 2026-09-15

Owner `boop-moving-corners-v185`; built and reviewed source `a4df9e4154a7e1fd0fda843af7af09106d6691a2`.
[Run34939256214](https://github.com/ryankemble2006-web/boop/actions/runs/34939256214) passed. Permanent-signed com.boop.alpha1 version185 /1.2.185-png-puppet.
APK SHA256 `52499d603ec7caa91df3bbf0667e1db7ea7b357795ecfb9bed42556450703fca`; signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Cause and correction
V184 central seam was a user-reported huge improvement, but the cap corners retained stationary dark strips.
No-white columns used edge640, barely moving to642 at full closure; dark actual cap material also remained partly transparent. Red34938495251 reproduced x200 edge640 and alpha159 at y350.
The rig now follows sustained visible cap components, rejects disconnected reflection spikes with valid neighbouring boundaries, confines samples to their own cap, and eases corner transitions. A corrected opaque corner envelope supplements the unchanged v184 body mask.
Central RGBA across x300..690 and845..1240/all640rows is byte-identical to pinned v184. Eight actual corner alpha/motion fixtures, transition checks, photographed endpoint bounds, original PNG provenance, eye coverage and inherited voice/music/lyrics/sharing checks pass. Source review found no remaining blocker.
Original PNG, shader, palette, animation clock, interactions, preview and sharing are unchanged. No local app-source edits/builds or hosted visual tests.

## Joint delivery and latest verdict
Installed185 on Shield and Pixel7Pro; actual installed APK hashes and package versions matched. Shield16/16 preference hashes unchanged; phone10/11 unchanged, notification bookkeeping only. V184 signed rollback retained.
Private Shield and landscape Pixel7 recordings were inspected in partial/closed poses. The larger stationary dark corner strips are reduced and the central seam remains absent; these are assistant observations.
**User reports it is much better, but a tiny green remnant stays still at both inner eye corners. V185 is an improved baseline, not fully accepted. Investigate the remaining inner-edge pixels next.**
First phone capture was portrait/black and was not counted as visual evidence. A temporary landscape rotation lock enabled a useful2340x1080 capture; original auto-rotation1/user-rotation0 were restored and verified. Shield/reference animation speeds remain restored1x. Captures/media remain private; no main merge.
