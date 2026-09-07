# BOOP Wall: v32 text and idle-blink handoff

Updated 2026-09-07. Owning branch: `boop-wall-free-chat-wip`.
Status: **signed test APK available; new visual gate FAILS; not a full-green release**.
Fetched main `2912a198e3f9f2b67f89a39739159c8172c654f2` owns shared contracts.

## Exact delivered candidate

- Package `com.boop.alpha1`, versionCode 32, `0.4.12-wall-blink-text`.
- Built commit `6e48e3bc05f7269376d54179ab32025e1a0b72b9`; application implementation is `cbe9e14`.
- Latest completed run `34077206213`; job `101605575582`.
- APK artifact `10002578514`; downloaded as `BOOP-Wall-v32-Blink-Text.apk`.
- APK bytes: 139485310. SHA-256: `3316a193188cb0b9a0cc31846771b3f57c7987da13747d6b1fe11da22647c96b`.
- Artifact ZIP SHA-256: `e99a5258e1e5e6d3191818d5f63ec7950d8de889640f475f59808d058784dfd1`.
- Effective MainActivity SHA-256: `368c21b1f2f0642c3cec6b05a3588119325270f2496e4815a43b7790567cdf5e`.
- Existing signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

The exact build receipt, outer ZIP digest/integrity, APK digest and inner archive
integrity were checked after downloading. DEX inspection confirms the new blink
and notice classes are in the shipped APK; the instrumentation class is not.
Full cryptographic APK signature, package and version checks passed on GitHub.
This is the existing stable-signed debug variant, not an optimized release.
No physical installation or permissions were performed by this session.

## Approved implementation

Free Chat keeps its automatic copy-and-open handoff. Its success instruction is
1.5x text size, bold, two lines: "Question copied." / "Paste into Free Chat."
A standard system text Toast carries Parcelable spans; no custom overlay, extra
tap, screen or permission. Copy failure uses different wording.

Awake-idle single blinks use irregular 3-7 second delays and a 220ms close/reopen.
Busy/listening/thinking/speaking, menus, touching and playful-animation settling
suppress blinking. Sleeping, hidden or detached views cancel the loop. The blink
only changes eyelid geometry; it never calls interaction or resets the 30-second
sleep timer. Raw MainActivity and BoopFaceView stay unchanged; the materializer
applies `patch-wall-idle-blink.py` after the existing chat-mode patch. Always use
`scripts/materialize-android.sh`, not the raw baseline files alone.

## Verification: exact limits

PASS: 159 local source/Python/JVM tests and scoped diff review. Latest CI also
passes bridge regressions, preserved Java harnesses, Android unit tests,
materialization/wake mappings, signed APK assembly/inspection, real positive-read
wake-microphone startup, and the existing menu/default/selection/restart/revert/
gesture-cancellation tests. No independent reviewer agent was available; review
was an inline source/diff review.

FAIL in the new instrumentation: `No actual natural blink frame:
eyes-blink-portrait.png; awake=true, focused=true, attached=true, alpha=1.0,
foreground=true, thinking=false, listening=false`.
The cause is NOT established. Do not assert that the emulator merely missed a
working blink or that the blink definitely does not work. Landscape and subsequent
pairing checks were not completed in this final run. Earlier run `34076082955`
passed portrait blink/reopen/busy-state/sleep checks with the same application
source, but its landscape sampling check failed. That is partial evidence only,
not a replacement for the failing final visual gate.

Latest evidence artifact `10002576627` was downloaded, digest/integrity checked:
`bf57fe45cf3a54ebb574cb323c71809c5b9502fcddc39f9ac5fbff98f5df6d03`.
Toast shown/hidden callbacks and 1.5x/bold span assertions passed before the blink
failure. Actual inspected screenshots show transition black frames or Settings
without a visible toast. They DO NOT verify enlarged text rendering/no clipping.
The test's Settings handoff is synthetic and uses no real chatbot login/account.
The real Pixel still needs the two visual checks. The user is being given this
as a signed test candidate with the verification gap disclosed, not as all-green.

## Physical evidence and preservation

Ryan reported v31 "Worked perfectly": query copied, new chat opened, instruction
to paste appeared. This supports that tested phone's browser-handoff path, not
all modes/restarts/Back/house commands or both Pixels. He then approved this v32
readability/blink update. v32 physical acceptance is still pending.

Last fully green v31: `0ceb97bc7c258835ce292391d483849398016020`, run34071614834,
artifact10000728933, APK SHA-256
`2d8c858da399c2e6f1f9e7cbbed8199de6bcb9ca3d207442fd49252bb3626d29`.
Its detailed handoff is preserved at `docs/BOOP-WALL-V31-HANDOFF.md`.
Keep physical checkpoint `595e1daa43393882a0e5de43967545ac526b8b66` and its
`checkpoint-boop-wall-595e1da` tag unchanged; do not promote this candidate.
Preserved Wall branch `3a702f8`, Launcher, Shield, local HA clients, signer and
permissions are untouched. Timed voice routines remain absent.

## CI history and next safe step

`e337ef2` supplied the red tests. `cbe9e14` implements the app. Later commits
only refine the separate instrumentation and diagnostics: foreground-context
Toast handoff; stable accessible mode selection; then pre-draw capture of a
natural blink rather than sampling a value and posting a later screenshot.
Run34076741236 failed earlier on emulator supplied-audio support unavailable(4),
without a crash. The unchanged-tree retry `6e48e3bc05f7269376d54179ab32025e1a0b72b9` passed that wake gate but
still failed the new visual capture as recorded above. No passing assertion was
manufactured and no physical wake change was made to get through CI.

Next: Ryan tests v32's enlarged paste instruction and awake-idle blinks on the
Pixel, including normal sleep. Record only what he actually observes. Diagnose
the remaining visual-test failure with actual frame/timer evidence before calling
this fully green or promoting any checkpoint. Do not restart from old main.
GitHub is the handoff, not a claim of access to the laptop's GPU/files/LAN.
"Update memory" remains documentation-only unless separately authorized.
Documentation-only commits after this build do not change the APK.
