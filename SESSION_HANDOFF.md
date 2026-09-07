# BOOP Wall v33: 20 percent faster blink

Updated 2026-09-07. Owning branch: `boop-wall-free-chat-wip`.
Starting HEAD: `8cb02ce1e422d34b0ac9694c70b62bf61ce33779`.
Shared main checked: `2912a198e3f9f2b67f89a39739159c8172c654f2`.

## Latest user evidence and request

Ryan physically observed v32 and said: "he blinks just fine". This establishes
that the shipped v32 blink works on his tested device; it does not establish
text readability, sleep behavior or every other flow. The previous automated
visual gate remains unresolved, not evidence that the physical blink is broken.
Ryan requested only 20 percent faster blink animation, no tests, and the fastest
possible build/sign/delivery. Do not run tests or an emulator for this update.

## Scoped change

Only production behavior change: BoopIdleBlink.DURATION_MS 220 -> 183 ms,
220/1.2 rounded to the nearest whole millisecond. This is 1.2x animation speed,
not 20 percent more frequent blinking. Irregular 3-7 second gaps, shape, text,
sleep deadline, voice, wake, local HA/media, gestures, permissions and signer
are unchanged. Build identity becomes versionCode 33 / 0.4.13-wall-blink-speed.

Existing GitHub build/sign workflow now recognizes an explicit
`[boop-build-only]` commit marker. Use it only when Ryan expressly requests no
tests. It skips test dependencies, tests, instrumentation assembly and emulator
steps, reuses installed build tools and retains permanent signer/package checks.
The separate polish test workflow skips the same marked push. Normal pushes
and manual runs retain the full verification route; no assertion was weakened.

## Build status

Build/sign requested by this commit; artifact/run receipt follows when available.
No tests have been run for v33. Compilation and signature verification are not
runtime acceptance. The previous complete v32 handoff and exact failed-CI history
are preserved at docs/BOOP-WALL-V32-HANDOFF.md.

No laptop Windows checkout/GPU/LAN was used: this session uses the connected
GitHub repository and hosted signing workflow. Direct local clone access was
unavailable; no user checkout or concurrent local work was overwritten.

## Preservation

Do not promote/repoint accepted checkpoints. Preserve checkpoint-boop-wall-595e1da
and all Home/Routines/Shield/Launcher work. No physical install or permission
change is authorized. The v32 text remains unchanged and its physical appearance
is not yet confirmed. Final handoff/docs commit must use [skip ci] so it does not
start tests after this explicitly build-only request.
