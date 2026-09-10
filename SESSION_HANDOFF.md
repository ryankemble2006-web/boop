# BOOP animation workshop handoff — 2026-09-10

## Active continuation: all eye motions in shared code

Ryan said the v3 MP4 blink was gorgeous and identified outer-edge artifacts in
the slow GIF. Lossless frames confirmed a deformation edge issue, not only GIF
quantization. He then authorized all other eye motions to use the default method,
with brand-new animations allowed, explicitly "code not jiggle a png".

`animation-lab/canonical-eyes/` now contains the shared four-channel state engine,
26-clip catalogue, source-texture GLES2 shader and existing Animation Lab v5 host.
Local state tests passed 30,636 checks. Source `73dfa160649d9462839db7e47cbef939f8ec894f`
built/signed in Actions run 34435432070 (artifact 10135991882). All 26 clips ran
in the Android TV API36 emulator, with 18 extra state switches and Back/Home
resume; renderer initialized without a logged failure. See canonical-eyes/VERIFICATION.md.
No new artwork or
production app change. The concurrently owned canonical rebuild checkout is
untouched. See canonical-eyes/DESIGN.md and README.md.

Current owner: `animation-idle-blink-cleanup` (existing art/animation branch).
Canonical app remains `boop-unified`; this is not a parallel application.
Fresh main rules supersede the historical app map in this branch.

Laptop workshop: `C:/Users/ryank/Documents/Codex/BOOP/animation-work`.
Source inputs: Unified `11650313221ae5bf997dbb93b6a905bfdc7da1ed`, followed
by documentation-only `625257e7f0706c42286e2632bb5b8ac7c1be3d40`; main
`5179f95961c9c43b4939dd1ea4349a32eb7f99d1`. Concurrent Unified handoff changes
were preserved. App source/packages/signing/permissions were not changed.

Read `animation-lab/idle-blink/WORKSHOP_20260910.md` and `BOOP_ANIMATION_MEMORY.md`.
Sealed recovery: `animation-work/archive/BOOP-animation-work-recovery-20260910-0441.zip`
(295 files, SHA-256 `00f51509463ccdbfdf1226237f405e25c0c864916d0df7bfd298e21d1fb7ba88`).
It captures published recipe commit `f9a384fb8402aae5c2842f7d569a949113cef1e6`.
Exact canonical ZIP/master recovered and hash verified. Blink v2 and v3 have
25 PNG states, GIF/MP4, recipes, timing and checksums locally; v3 is a review
candidate, not visually accepted. Separate headphones alpha-cleanup candidate
uses existing Shield occlusion regions and still needs Ryan's edge review.

Nine selected Actions APKs are retained locally with source/run/hash/package
index. Fourteen family source exports preserve original ownership/dependencies.
The v0.4 Animation Lab APK was installed only on Android TV API36 emulator:
cold start, Back/reopen, 12 blink triggers and Wake/Listening/Think/Stop completed
with the same process PID. This does not test the new blink inside Android.
Logs/video stay local; no automated visual acceptance or physical test occurred.

Next: Ryan reviews the 26-motion runtime MP4 and headphone alpha cuts; refine if requested,
then integrate through the approved canonical app/lab path and test that exact
candidate. Existing full canonical rebuild remains separately gated on natural
voice acceptance. Latest usable physical rollback is v88
`f5f086fc4f67712b5746be067aff852331299bb0` (Android TTS accepted, natural voice
not accepted). Latest canonical reference is signed v91, artifact `10135283428`;
it is not an animation release from this task.

## Historical handoff (not current animation status)

# BOOP animation-lab handoff: official yellow hands, 2026-09-07

Owner: `animation-freddie-mercury`. This is an art-only workspace, not an alternate app lineage. Normal application work belongs on `boop-unified` per fetched main.

Ryan approved the latest mirrored, hands-only yellow pair as BOOP's official hands everywhere. Read `BOOP_YELLOW_HANDS.md`, `animation-lab/shared-assets/boop-yellow-hands/README.md`, its manifest and `animation-lab/freddie-mercury/STATUS.md`. Preserve the exact plush yellow material/proportions, rounded cuffs, four fingers plus a thumb per hand, floating hands and true transparency. Poses may change; the design and underlying BOOP character must not. The talking-hand gesture uses fingers, not an animal transformation.

Source image: `a_clean_isolated_png_style_image_on_a_transparent.png`. Reserved filename: `boop-yellow-hands-approved.png`. Dimensions 1774 x 887 RGBA, 1541931 bytes. SHA-256 `74e3b162d8fa750491b9a1577d51d043e1f7fdcc3940cbdf22f742bc58c9f556`. No original editable generator layers were supplied.

Starting live remote heads checked: animation `3548e6aeb787556b1994cbafb3bffb4c6c017d05`, unified `8cfb14e001b946f0bdb33011c8cbe3778ed59019`, main `4323bb747f1ddaced24a5372854377acccaa4228`.

Publication boundary: this change saves documentation and checksum records only. The binary PNG is still pending manual upload from `boop-official-yellow-hands.zip`, which supplies it in both branch destination folder layouts. Next safe step is uploading the exact file, verifying its SHA-256 and updating the transfer record. This task did not change app code, build settings, signing, permissions, version or deployment; it does not implement an Easter egg or establish device acceptance. No laptop checkout synchronization is claimed.

## Historical inherited handoff, retained for provenance

# Shared-context hub handoff — 2026-09-06

## Continuity refresh — 2026-09-06

Ryan explicitly requested an "update your memory" refresh. No new product
decision, implementation result, physical test, or app-branch task was supplied.
The shared context, rules, status, and this handoff were reread; live `main` was
fetched and verified at `7c0cf6b` before this documentation-only update. This
does not promote any app state or substitute for fetching an owning app branch
before its next change.

Main is the cross-project entry point, NOT a consolidated latest build of all apps.
This change is documentation only. Application source/build configuration on main
is left at its previous8fcd6da baseline; do not build current Wall from it.
Use BOOP_START_HERE.md to select each app's development branch and read that
branch's SESSION_HANDOFF.md for fresh evidence and pending work.

Ryan approved: laptop develops Shield, Android develops Launcher; GitHub shares
reviewed code, decisions, memory and status. Agents check/fetch before editing
and update/publish handoffs at the end. Never silently overwrite concurrent work.
The local clone had been fetching only the old Shield Home branch; active app
fetch mappings and a local Launcher checkout were added.
New tasks need BOOP repo access. This is not automatic raw conversation syncing.

## Latest save: Shield 2.5D and five-digit pose studies, 2026-09-07

Ryan asked to save the hand posing work together with deliberately using the Shield GPU for 2.5D animation. Read `animation-lab/shield-2.5d/README.md` and `STATUS.md`; the complete engineering/evidence brief is `docs/animation/SHIELD_2_5D_HANDS.md` on `boop-unified`. Preserve the official yellow plush design and existing BOOP eyes/headphones. Every hand keeps four fingers plus a thumb throughout every pose/transition. Retain grip/earcup-adjustment, pointing, wave, gaze-led motion, headphone lag/recoil and subtle layered-depth ideas as future work, not a working rig.

This later save publishes one small corrected open-hand WebP preview in `animation-lab/shared-assets/boop-yellow-hands/pose-studies/`, with checksum/source manifest. Blob SHA `854fd36b870f1c6e647c5fdce853003cd05e5e2f` matches the locally verified derivative. The exact approved master was reverified unchanged from the ZIP. Full-resolution master/source PNG transfer remains pending; the other pose studies were unavailable as separate source files. The earlier PNG-pending notes above remain true. This preview is lossy, black-background reference art, not the transparent master or a complete archive of the illustrated set.

Starting live heads checked for this save: animation `193b7b7c798cdf5dfec44cc32a152e1461b52f1c`, unified `0eedc515edda55ec9a8c6c5584f27b985e0b66e2`, main `4a7036a6a4066d19a3a7c5c5af3862c242c8a426`. The laptop checkout/receipt is not mounted here; no laptop synchronization is claimed. Main's existing shared five-digit hand contract is unchanged.

Verification is limited to available image bytes/dimensions/hashes, preview blob identity and reviewed documentation. No app code, live resources, build/workflow, version, signer, permission, deployment or accepted checkpoint changed. No 2.5D animation, GPU benchmark, target frame rate or physical Shield acceptance is claimed. Next safe step is exact-PNG transfer/checksum verification; a separate request is needed for rigging/runtime integration, with actual-device profiling and media/lifecycle/reduced-motion safeguards.

## Portable notification / lock-screen reference saved, 2026-09-08

Ryan approved the visual direction of BOOP on a phone lock-screen-style surface using only the permanent paired eyes and the floating yellow hands, with BOOP holding an anonymous app-notification icon/card. No sender, message text, preview, photo or account detail belongs on the locked display.

The future animation direction is layer-based puppetry of approved assets: idle float, eye darts/blink, small hand grip adjustment, icon pop/bounce, gaze toward the held icon and calmer motion when settled. Do not regenerate BOOP per pose and do not add a body.

Reference folder: `animation-lab/lockscreen-notifications/`. The exact latest AIO eye master is copied there by Git blob identity from `boop-unified` as `boopApprovedEyes.png`. The current chat-supplied hands PNG and the approved generated lock-screen concept image are recorded there with exact dimensions/hashes but remain pending binary transfer because the connected GitHub actions in ChatGPT expose no local-file binary upload parameter. Do not substitute regenerated binaries. No app code, notification permission, lock-screen integration, package, signer, build or deployment changed.
