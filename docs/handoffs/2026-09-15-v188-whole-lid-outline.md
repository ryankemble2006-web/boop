# v188: whole photographic lid outline moves — 2026-09-15

Owner `boop-whole-lid-motion-v188`, based on v187 delivery `1e3b1d10fc6585c400c3b266427f69f12bfa0452`.
Ryan used the new lab to hold Blink at50.5ms /77% and identified all four inner/outer ends staying high while the middle descended.

## Root cause and correction
The shader sampled moving felt RGB at `(p.x,lidSampleY)` but multiplied it by alpha from the original destination `p`. It also returned early where that original alpha was zero. The fixed photograph outline therefore clipped the moving cap ends even after the rig-coordinate repairs.
The v185 assumption that a fixed overall footprint was sufficient is superseded by this held-frame evidence.

Now lid RGB and alpha share the same warped coordinate. Premultiplied base and moving-lid contributions replace one another using the existing coverage feather. This lets the outline descend and avoids retaining the old cap under transparent moved pixels. The zero-destination-alpha early return is removed. The open-pose branch keeps the previous output arithmetic exactly.
Original PNG, PngPuppetRig bytes, palette, gaze/iris math, motion catalogue, clocks, sharing and v187 lab controls remain unchanged. No new/generated artwork or texture is introduced.

## Verification before delivery
Red run34942867068 reproduced actual outer-left clipping at x180,y410.23 at50% closure: original alpha0, warped cap alpha1, old output0.
New numeric tests use actual PNG-derived rig bytes and evaluate the shader's output arithmetic at all four end regions, at50/77/100% closure. They cover open/central equality, premultiplied edges, clearing old-only silhouette and preserving the original faint background-noise alpha bound in the eye gap. They do not render images or run hosted visual tests.
Candidate `a50482170261e61894c9100ba49bdee7b0b41b13` independently reviewed with no blockers. [Run34942957660](https://github.com/ryankemble2006-web/boop/actions/runs/34942957660) passed at that exact source, including the existing PNG/rig, hue/felt sharing, speed, lab callbacks, voice, music, lyrics and ownership checks. The two historical one-time freeze skips remain.

## Signed delivery
Permanent-signed com.boop.alpha1 version188 /1.2.188-png-puppet. APK SHA256 `5dd6059325b76fbc7f079158babd348c6f6d479f0bd8bfe27355ddc26688709c`; signer SHA256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Installed and actual base-APK version/hash verified on Shield and Pixel7Pro. Shield16/16 preference files unchanged; phone10/11 unchanged, only notification bookkeeping changed. Saved eye and appearance hashes rechecked unchanged after the lab comparison. Phone rotation remains auto1/user0; no rotation or speed changes were made for this comparison. V187 and earlier rollback APKs retained.

## Joint paused-frame evidence
Ryan's original held screenshot showed Blink50.5ms,77%/77%,Paused. After installation, the existing UI route opened Felt animation lab; the slider was set to50.4ms, also77%/77%,Paused. These are near-matched positions, not identical timestamps.
The assistant inspected both actual phone captures: the v188 frame shows all four inner/outer ends descending with the stretched lid, instead of clipping at the old open outline. Phone is left in that paused lab for Ryan to inspect. Shield returned Home; a separate full Shield animation review has not been performed.
Captures stay private and local. This observation plus build/install evidence is **not Ryan's full physical acceptance**. Next: his slider/full-blink verdict on the current devices. The lab's usefulness is user-confirmed.
