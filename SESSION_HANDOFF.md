# BOOP Wall: text and idle-blink update

Updated 2026-09-07. Owning branch: `boop-wall-free-chat-wip`.
Current work: v32 / `0.4.12-wall-blink-text`, package `com.boop.alpha1`.
Status: implemented and locally reviewed; signed build/emulator verification pending.
Start point was live `e337ef2f2d890710da5af731a6779f44fcd46d2d` (the existing red tests).
Fetched main `2912a198e3f9f2b67f89a39739159c8172c654f2` owns shared contracts.

## Approved scope

- The copied-question message is 1.5x its original text size, bold, two short
  lines: "Question copied." / "Paste into Free Chat." No extra tap, screen or
  permission. Standard system text toast with Parcelable text spans; a custom
  background toast is deliberately avoided. Copy failure has different wording.
- A gentle single blink at irregular 3-7 second intervals while awake and waiting.
  It closes/reopens over 220ms, does not speak, and does not reset the 30-second
  sleep timer. Listening/thinking/speaking, menus, touching and existing playful
  animations suppress it; asleep/off-screen views have no blink loop.
- Preserve the clipboard/browser handoff, three-second mode menu, local-first
  routing, wake/speech, layout, swipe, all other apps and the permanent signer.

## What Ryan actually tested

After v31 delivery Ryan reported "Worked perfectly": his query was copied,
a new chat opened, and the paste instruction appeared. This is positive physical
browser-handoff evidence on the phone being used in this conversation. It does
not independently verify every restart/revert/local-control/Back scenario or
both Pixel models. He then approved larger instructions and idle blinks.
No v32 physical test or installation has yet been performed.

## Implementation and verification

Use `scripts/materialize-android.sh`. The new `patch-wall-idle-blink.py` follows
the existing chat-mode patch; raw MainActivity and BoopFaceView are unchanged.
It validates source anchors and idempotency, only registers a read-only busy-state
predicate in MainActivity, and adds separate eyelid drawing/scheduling to the face.
No edits to the original sleep method or timeout. BoopIdleBlink is pure Java;
BoopFreeChatNotice supplies the system toast text. Instrumentation is exclusively
in `source-android-test`, never in the shipped app's main source set.

The existing three red tests were observed failing for absent implementation.
After implementation, all 155 local Python/source/JVM checks passed. The offline
materialization pipeline (excluding the wake-asset download) and repeated blink
patch passed; workflow YAML parses and the scoped diff was reviewed. No Android
SDK/GPU is exposed in this hosted chat workspace; Android compile/signing and
real rendering checks are delegated to the existing GitHub runner. No independent
reviewer agent was available; review here was an inline source/diff review.

The candidate workflow retains every existing safety/signing/menu/wake gate and
adds a separate instrumented APK: styled-text assertions, real system-toast
screenshots over another app, natural portrait blink/reopen, busy flags, the
original sleep deadline, landscape drawing and off-screen cancellation. It uses
only a disposable emulator, never a physical device or real chatbot/account.

## Last verified APK and preserved checkpoints

v31: built `0ceb97bc7c258835ce292391d483849398016020`, run `34071614834`,
job `101589892017`, artifact `10000728933`.
APK SHA-256 `2d8c858da399c2e6f1f9e7cbbed8199de6bcb9ca3d207442fd49252bb3626d29`.
Its full CI/emulator run was green; detailed history is in
`docs/BOOP-WALL-V31-HANDOFF.md`. No v32 pass is inferred from that run.

Keep accepted physical Wall `595e1daa43393882a0e5de43967545ac526b8b66` and tag
`checkpoint-boop-wall-595e1da` untouched. The preserved Wall branch remains the
separate v30 lineage `3a702f8`; do not merge app branches or repoint checkpoints.
Permanent signer fingerprint:
`f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
No keys, permissions, physical installs, Launcher or Shield source were changed.

Next: complete v32 CI, inspect rendering evidence, download the exact signed
artifact, update this handoff and then obtain physical acceptance. Documentation
updates do not change APK bytes. "Update memory" remains documentation-only.
This workspace is a verified GitHub source snapshot, not the Windows laptop;
GitHub heads must be reread before publishing any further changes.
