# BOOP Wall Free Chat candidate handoff

Updated 2026-09-07. Task branch: `boop-wall-free-chat-wip`.
Base: live `boop-wall-resurrection@3a702f89b7f317649d267f25c34c6c9655edcff8`.
Wall package remains `com.boop.alpha1`; candidate version 31 / `0.4.11-wall-free-chat`.

## Current delivery status: signed test candidate, NOT fully verified

Built code: `e5c5598c2915a0a12b51f91cecebf9f51c1b8662`.
Completed GitHub run: 34070255788; job: 101586202899.
The final job-step report was reread after completion. It confirms:
- PASS: source/bridge/chat-routing regressions, preserved Java harnesses,
  materialization and wake mappings, Android unit tests and APK assembly.
- PASS: package/version/archive inspection and full apksigner verification against
  the existing permanent BOOP certificate.
- PASS: disposable emulator startup, supplied-audio recognizer selection, and
  signed-app startup with the real positive-read wake microphone gate.
- FAIL: menu/persistence/revert/gesture-cancellation interaction test.
- SKIPPED: subsequent Shield pairing-return test.

The menu test's exact latest failing assertion/root cause has not been established
from the bounded final step report. Do not mislabel this as a startup/wake failure,
assume an emulator-only problem, or call the requested interaction fully working.
No physical phone test or installation was performed.

Artifact 10000300048, `BOOP-Wall-Free-Chat-candidate`, was downloaded and extracted
as `BOOP-Wall-v31-Free-Chat.apk` for delivery. Independent checks confirmed the
artifact's built-commit receipt, archive integrity, APK SHA-256 and certificate
identity. CI performed full cryptographic APK signature verification.
- APK bytes: 139485298.
- APK SHA-256: `2d8c858da399c2e6f1f9e7cbbed8199de6bcb9ca3d207442fd49252bb3626d29`.
- Certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
This is the stable-signed debug variant, not an optimized release or an accepted
physical checkpoint. The known-good physical Wall remains the preferred baseline.

## Approved change

Hold the face for three seconds, past the existing MemberBerry animation, to
choose OpenCode / Free Chat / Cancel. OpenCode remains the default; selection
persists separately from house credentials. Same menu reverses the choice.
The local processor always runs first. Free Chat handles only NO_MATCH; it must
not receive local successes, missing targets, offline devices or auth failures.

Free Chat is an explicit browser-backed ChatGPT handoff. The question is copied
for the user to paste and send. No invisible webpage injection, response scraping,
credential extraction, direct OpenAI key, or promise of unlimited/free quota.
The menu explains the browser and clipboard behaviour. Browser Back is the intended
return route to Wall; login and service limits belong to the browser/ChatGPT session.
The physical browser/login/Back flow has not been verified on either Pixel.

## Implementation and prior evidence

The repository already applies wake-partial and toast patches when materializing
MainActivity. `scripts/patch-wall-chat-mode.py` follows that existing convention,
after those patches. It fails on changed/ambiguous source anchors and is tested
for idempotency. Do not build directly from raw MainActivity without materializing.
Checked-in MainActivity, face drawing, wake assets, HA clients, voice and companion
sources remain frozen. Existing launcher swipe is retained; gesture cancellation
is strengthened so moved/multi-pointer releases do not become speech taps.

Initial implementation was already on this branch at `5c717c8` when the parallel
session located it. Run 34069260038 built and signed v31, but failed emulator readiness.
Workflow-only fix `c1a5e965ffd8aecb0ca0f1bcdb1062c9f30fd3e6` made the disposable
emulator service readiness explicit and bounded, without weakening the wake gate.

Run 34069662757 at `c1a5e965ffd8aecb0ca0f1bcdb1062c9f30fd3e6` passed:
- 7 bridge tests, 134 Python/source tests, preserved Java harnesses, Android unit tests;
- materialization and all 33 wake phrase mappings;
- APK assembly, package/version/archive inspection and permanent signer verification;
- clean emulator install, startup, and the real positive-read wake microphone gate.
It FAILED the first real UI assertion: the three-second hold did not leave a
Chat mode menu visible. Later menu/persistence/revert checks and pairing were
not completed. A signed APK existed, but it was NOT a fully green candidate.

A concurrent advance `7e06da7` added background recognizer ownership cleanup,
scrollable mode choices and non-all-caps labels. It was reviewed and preserved,
not overwritten. `e5c5598c2915a0a12b51f91cecebf9f51c1b8662` adds UI diagnostics,
uses actual visible BOOP-face bounds rather than natural panel dimensions,
and prevents stale UI dumps. Six focused bounds tests and Python compilation
passed locally. The completed run for that code is recorded above.

This session has GitHub connector access, not the Windows laptop filesystem or
LAN. The Linux scratch snapshot has no Android SDK and no working direct DNS.
No laptop synchronization, physical installation or physical test is claimed.
Main documentation commit `2912a198e3f9f2b67f89a39739159c8172c654f2` maps this
candidate and the approved product contract for Work/phone handoffs. Already-open
Work sessions must fetch/reread it. Ryan's "update memory" request is documentation
only unless he separately requests code, signing, permissions or installs.

## Preserve

- Physical Wall checkpoint: `595e1daa43393882a0e5de43967545ac526b8b66`,
  version 29 / `0.4.9-alpha6.5.6-wall`, signed run 33992704568, Pixel 7 Pro accepted
  2026-09-05. The annotated tag `checkpoint-boop-wall-595e1da` was reread live and
  still resolves to that exact commit. Do not move it.
- Current base v30 includes the reviewed 96dp / 1.5x left swipe to the separately
  installed `com.boop.launcher`; inherited evidence is emulator-only.
- Existing permanent signer and existing Wall workflow are untouched. Candidate
  signing uses the same GitHub secrets and checks the public certificate digest.
- Shield Home/Routines checkpoints and all other app branches stay unchanged.
- Timed voice routines remain absent.

Next: diagnose the latest menu interaction failure from focused UI evidence and
fix its demonstrated cause without weakening the test. Re-run the full gates,
then obtain physical acceptance before promoting any checkpoint. See
`BOOP_CHAT_MODE_MEMORY.txt` and `docs/BOOP-WALL-FREE-CHAT.md`; fetched main remains
the shared-context authority. Documentation-only advances do not change the APK.
