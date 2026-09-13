# Current handoff: Wall-to-Shield colour accepted (2026-09-13)

Ryan's latest feedback: "it works btw, i just tested eye colour :) from wall to shield". This is user-confirmed physical acceptance of Wall -> Shield colour changes. It supersedes the earlier failure blocker. Do not restart colour repairs or ask Ryan to approve this route again without new contrary evidence.

Read `docs/handoffs/2026-09-13-colour-accepted-wall-shield.md` for the current runtime evidence, limits and device state. The older `docs/handoffs/2026-09-13-colour-failure-continuation.md` remains preserved research/provenance, not the latest acceptance status. This continuation remains primary; old recovery threads are not blockers.

## Source and installed identity

Owner: `boop-unified-eye-sync-safe-v159`. Live GitHub base before this documentation update: `0492fc57431474d9c89c9d536ebc12f935bd8238`. Application source remains `d149cb509ec376779daf84c50f621d8adcbacd24`, v160 / `1.2.160-colour-animation-speed`. No app code was edited or APK deployed during this diagnostic continuation.

Physical Shield and Pixel 7 still run v159 / `1.2.159-shared-eye-colour`, source `0a4134ebfe8049254378b4706d2ee1df73cd7e87`, full run `34757337845`, artifact `10317198102`. Their installed APK SHA256 was verified as `80e86119d4771624ff47617373df2cebc03c0454aa47f10a0996a6d507b68353`.

Both existing main laptop emulators were found already on v160, installed APK SHA256 `a584381d10ebb220ab686acec22f7668384e240f39ade052e4177583aa4adc7b`. This corrects the older handover's unknown emulator deployment status; it is not a physical speed acceptance. Physical Pixel 10 remains excluded and untouched.

## Actual visible colour evidence

- Phone emulator Wall: settings hue 225 -> 122 visibly changed the irises from blue/violet to green. Restoring 225 restored the visible blue/violet irises and saved value.
- Physical Shield Now Playing: isolated local hue 80 -> 260 -> 80 visibly changed yellow-green -> purple -> yellow-green. Sharing was temporarily disabled for isolation, then restored through the normal UI.
- Physical Pixel 7: Wall visibly rendered its original hue 48. Sharing was initially off. Explicit normal-UI opt-in read the existing shared 80; Wall visibly changed to yellow-green.
- Shield -> Pixel 7: shared hue 260 reached the receiving preference and visibly purple Wall irises. Both physical surfaces were captured.
- Pixel 7 -> Shield: shared hue 122 reached the receiving preference and visibly green Now Playing irises. Both physical surfaces were captured.
- Ryan independently confirmed Wall -> Shield works. A subsequent read-only receipt found both physical devices on hue 2 with sharing on. Leave this newer user state intact; do not restore historical fixture hues 48/80/122/260.

No new colour code repair was needed to obtain these results. Do not invent a root cause for the original report or treat the initially off sharing toggle as a proven explanation of everything Ryan saw.

## Preserved speed candidate and next step

The published v160 implementation and existing successful CI remain intact: appearance `34760362361`, exact timing `34760362356`, full permanent-signed build `34760362417`, artifact `10318393790` / `BOOP-Unified`. These runs were rechecked during diagnosis. No replacement signing key, build or CI rerun is implied by this documentation-only update.

The reported colour blocker is superseded by physical acceptance. Remaining colour coverage is a deliberate two-device offline/reconnect cycle; earlier Shield-to-HA read-before-write/rejoin evidence remains valid but is narrower. Record that separately, not as a reason to reopen accepted live delivery.

Next implementation work is runtime validation of the already-published v160 speed candidate, not rewriting it: 0.5x/1x/1.5x/2x, mid-clip changes, sleep/wake, notification hands/eyes, pause/resume and exact original 1x. Keep physical v160 deployment on hold until local runtime gates pass. No speed acceptance is claimed here.

## Boundaries and continuity

GitHub owns source edits, non-visual tests, builds and permanent signing. Laptop emulators are the default runtime/visual loop; authorized physical targets are Shield and Pixel 7 only. Preserve the approved artwork, authored animations, existing Wall hue controls, accepted v156 Shield polish and single-face ownership. No permission changes, lock bypass, private credential inspection or unrelated lyrics/Johnny merges.

The task worktree contains historical dirty recovery files and is not the live source. It was left untouched. Private screenshots and runtime receipts remain under its `work/colour-failure-20260913-primary/` directory and must not be published. No automated device input sequence remains queued. Current handoff/status/memory publication is documentation only.
