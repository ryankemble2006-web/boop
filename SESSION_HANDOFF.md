# Native conversation relay implementation (v34), verification pending

Updated 2026-09-07. Current explicit user request is to execute the committed
OpenAI relay plan through tests, signing and publication. It supersedes the older
v33 no-tests instruction below for THIS relay change. Normal tests are required.

The native relay/client, additive Native Chat menu, existing speech-error mapper,
Cloudflare Worker and private build configuration have been implemented. Local
source/JVM tests and mocked Worker tests pass; signed Android CI is not yet checked.
No live relay is deployed or authenticated. A build without relay URL/token is a
setup candidate, not functioning native conversation. Configured APKs contain an
extractable relay bearer token; CI refuses configured builds in a public repository.
No provider credential belongs in Android. No secrets/visibility/installs changed.

The subagent launcher is not available in this chat. Execution and scoped review
were serial, not independently delegated. Existing v33 blink/text/wake/house/media
source and protected checkpoints remain untouched. Full current build evidence
will replace this pending notice when CI finishes. Previous v33 handoff follows.

---

# BOOP Wall v33: 20 percent faster blink

Updated 2026-09-07. Owning branch: `boop-wall-free-chat-wip`.
Application/build commit: `e3507bde3f296dcb419a1dcef0faf735c7243525`.
Shared main checked: `2912a198e3f9f2b67f89a39739159c8172c654f2`.
VersionCode 33 / `0.4.13-wall-blink-speed`, package `com.boop.alpha1`.
Exact signing/artifact receipt: `docs/BOOP-WALL-V33-BUILD-RECEIPT.md`.

## Latest user evidence and request

Ryan physically observed v32 and said: "he blinks just fine". This establishes
that the shipped v32 blink works on his tested device; it does not establish
text readability, sleep behavior or every other flow. The previous automated
visual gate remains unresolved, not evidence that the physical blink is broken.
Ryan requested only 20 percent faster blink animation, no tests, and the fastest
possible build/sign/delivery. No tests or emulator runs are authorized for this
update. Do not turn build/sign success into a full-CI/runtime pass.

## Scoped change

Only production behavior change: BoopIdleBlink.DURATION_MS 220 -> 183 ms,
220/1.2 rounded to the nearest whole millisecond. This is 1.2x animation speed,
not 20 percent more frequent blinking. Irregular 3-7 second gaps, shape, text,
sleep deadline, voice, wake, local HA/media, gestures, permissions and signer
are unchanged. Build identity is incremented to distinguish the faster candidate.

Existing GitHub build/sign workflow recognizes an explicit `[boop-build-only]`
commit marker. Use it only when Ryan expressly requests no tests. It skips test
dependencies, tests, instrumentation assembly and emulator steps, reuses installed
build tools and retains permanent signer/package checks. The separate polish test
workflow skips the same marked push. Normal pushes/manual runs retain the full
verification route. Never claim that a skipped test passed. Do not automatically
retry the old visual tests after a no-tests request.

## Evidence and preservation

The complete previous v32 handoff, exact APK receipt and failed visual-gate history
are preserved at `docs/BOOP-WALL-V32-HANDOFF.md`; v31 history remains in
`docs/BOOP-WALL-V31-HANDOFF.md`. Ryan confirmed v31 query-copy/new-chat/paste
instructions and now v32 blinking. New v33 animation speed still awaits his use.
Text appearance, landscape and other unreported physical behavior stay unverified.

Do not promote or repoint accepted checkpoints. Preserve checkpoint-boop-wall-595e1da
and all Home/Routines/Shield/Launcher work. No physical install or permission
change was performed. Existing permanent GitHub signer only. Timed routines remain
excluded. Keep the accepted APKs and preserved Wall branch unchanged.

## Session environment and next step

This session used the connected GitHub repository and hosted signing workflow,
not the laptop's Windows checkout, GPU or LAN. Direct local clone access was
unavailable; no user checkout or concurrent local work was overwritten.
Source review: the app delta is one timing constant plus version metadata; the
other changes implement explicit no-test build routing and update documentation.

Next: record only Ryan's actual v33 feedback. Further tests require a later request;
this build deliberately omits them. Final handoff/docs commit uses [skip ci] so
recordkeeping cannot start a new build/test run. "Update memory" stays docs-only.
