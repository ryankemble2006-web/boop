# Animation memory — 2026-09-10

Latest continuation: v9 grip candidate, source
d220933572ed6ea91db4841ae03f512c138860ec, Actions 34440698857 success.
Ryan explicitly wants thumbs BEHIND the sign and FOUR fingers facing us,
bent from the supplied open hands. Do not return to v8's full open-hand overlay.
NotificationSignView samples four separate non-thumb strips per hand once,
foreshortens them with a small Canvas mesh, and puts palm/thumb material at rear
depth. All layers share the sign transform. Exact source hands and v7 eyes are
unchanged. This is a limited 2D grip, not generated fingers or a full 3D hand.
New appearance pending Ryan's review; no new physical acceptance.

Previous continuation: Ryan accepted the v7 corner preview ("absolutely perfect")
and approved a five-finger notification sign-holder routine. v8 source
3a7feea9f4e1706fb873e21a3fd7675ebe16197b adds four local arrow-sign fixtures,
independent original hand layers, wrist motion and coordinated existing eyes.
No new finger rig or live notification wiring. Exact hand master SHA-256
26fe95570ac995e08b693107db4324f038cebe9e4fe76b9174ec41d7556fe2f1 is checked
in prepare and APK integrity. V7 shader/art/timing catalogue are unchanged.
SignMotion's finite show settles after eight seconds; existing logical clock
handles pause/focus/reduced motion. See canonical-eyes/SIGN_SHOW.md and
VERIFICATION.md. New sign appearance remains pending Ryan's review.

Latest repair: lab v7, source 31735d1395ab1c5167bd5bdbcb0c48c6f77a752c,
Actions 34437977312 / artifact 10136874223. Ryan's circled inner-corner shelf
was reproduced at Thinking 1100ms. v6 alpha-only did not fix it and was reverted.
The source trace's horizontal end-cap must not define the moving destination:
v7 uses a second rig row for a smoothly continued inner arc, retaining the
original material trace. Shader correction eases from zero near open and goes
to zero at full closure. Repackage the new shader AND two-row lid-rig together.
Do not mix them with the v5 one-row rig. Master/catalogue/timing are unchanged.
Ryan reviews the actual v7 runtime preview; no new physical acceptance.
Local repair checkpoints/receipts: animation-work/library-v2.

This existing art branch owns the durable animation workshop. The canonical app
continues on boop-unified. Do not revive old separate-app architecture from
inherited files. Do not start the separately queued full canonical rebuild.

The canonical-idle-blink-v1.zip archive was recovered intact. Master SHA-256:
ffbd67af22c2f11b4a109bd83e8c5197c266a777df2fbc97ce1ab5163e9fed22.
ZIP SHA-256: 3493ccc9c943ec45b52157348080c619053953bd59c168604324697c62eeaff0.
Concept sequence is reference only. Never use it as replacement runtime art.

v3 stretches a manually rigged 64-source-pixel band of existing lid skin below
fixed roots. Source master and open endpoint files are byte-identical. No
bottom lid or synthetic replacement artwork. Duration 183ms; close 73.2ms,
new candidate hold 8ms, reopen 101.8ms. Shield interval 3–7s, 18% double, 110ms gap
are preserved as transplant configuration. The hold is a requested refinement,
not previously physically accepted timing. GIF timing is approximate; JSON is
the intended timing authority. v2 is retained diagnostic history.

Legacy headphones contain old eyes/arcs. A separate alpha mask uses existing
Shield occlusion regions; it does not reconstruct hidden pixels. Ryan must
review edges. If wanted artwork is cut, obtain original editable layers rather
than painting replacements. Never mutate the eye master to solve accessories.

Current runtime evidence: shared code library in animation-lab/canonical-eyes,
26 lid/gaze clips, signed lab v5 source 73dfa160649d9462839db7e47cbef939f8ec894f.
Actions 34435432070/artifact 10135991882; APK SHA-256
7841fb99b4fc42449d361c849e3368030b35af3350ef3008708e4cce34271f69.
Installed and exercised on Android TV API36 emulator only. No physical test or
visual acceptance. Ryan liked the prior v3 MP4; this does not accept the new rig.
Smooth interpolated lid lookup and fractional shader coverage replace the old
integer-edge deformation. Runtime uses original texture material, not PNG frame
playback or whole-image jiggle. All eye clips share one renderer/controller.
Accessories and production Unified integration remain outside this pass.
New recovery/checkpoints live under animation-work/library-v1; the earlier full
workshop archive remains intact and contains the historical references.

Local recovery root: C:/Users/ryank/Documents/Codex/BOOP/animation-work.
Detailed receipts: notes/RECOVERY.md, reference-apks/INDEX.json,
notes/ANIMATION_INVENTORY.json and emulator-tests/lab-f80d5a3-20260910/RESULT.json.
Keep physical recordings/APKs local; publish only sanitized source and receipts.
