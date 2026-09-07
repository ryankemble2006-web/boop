# BOOP Wall Free Chat candidate handoff

Updated 2026-09-07. Task branch: `boop-wall-free-chat-wip`.
Base: live `boop-wall-resurrection@3a702f89b7f317649d267f25c34c6c9655edcff8`.
Wall package remains `com.boop.alpha1`; candidate version 31 / `0.4.11-wall-free-chat`.

## Approved change

Hold the face for three seconds, past the existing MemberBerry animation, to
choose OpenCode / Free Chat / Cancel. OpenCode remains the default; selection
persists separately from house credentials. Same menu reverses the choice.
The local processor always runs first. Free Chat handles only NO_MATCH; it must
not receive local successes, missing targets, offline devices or auth failures.

Free Chat is an explicit browser-backed ChatGPT handoff. The question is copied
for the user to paste and send. No invisible webpage injection, response scraping,
credential extraction, direct OpenAI key, or promise of unlimited/free quota.
The menu explains the browser and clipboard behaviour. Browser Back returns to
Wall; login and service limits belong to the browser/ChatGPT session.

## Implementation and evidence

The repository already applies wake-partial and toast patches when materializing
MainActivity. `scripts/patch-wall-chat-mode.py` follows that existing convention,
after those patches. It fails on changed/ambiguous source anchors and is tested
for idempotency. Do not build directly from raw MainActivity without materializing.
Checked-in MainActivity, face drawing, wake assets, HA clients, voice and companion
sources remain frozen. Existing launcher swipe is retained; gesture cancellation
is strengthened so moved/multi-pointer releases do not become speech taps.

Initial implementation was already on this branch at `5c717c8` when this session
located it. Run 34069260038 built and signed v31, but failed emulator readiness.
Workflow-only fix `c1a5e965ffd8aecb0ca0f1bcdb1062c9f30fd3e6` made the disposable
emulator service readiness explicit and bounded, without weakening the wake gate.

Run 34069662757 at `c1a5e965ffd8aecb0ca0f1bcdb1062c9f30fd3e6` passed:
- 7 bridge tests, 134 Python/source tests, preserved Java harnesses, Android unit tests;
- materialization and all 33 wake phrase mappings;
- APK assembly, package/version/archive inspection and permanent signer verification;
- clean emulator install, startup, and the real positive-read wake microphone gate.
It FAILED the first real UI assertion: the three-second hold did not leave a
Chat mode menu visible. Later menu/persistence/revert checks and pairing were
not completed. A signed APK exists, but this is NOT a fully green candidate.

A concurrent advance `7e06da7` added background recognizer ownership cleanup,
scrollable mode choices and non-all-caps labels. It was reviewed and preserved,
not overwritten. `e5c5598c2915a0a12b51f91cecebf9f51c1b8662` adds UI diagnostics,
uses actual visible BOOP-face bounds rather than natural panel dimensions,
and prevents stale UI dumps. Six focused bounds tests and Python compilation
pass locally. Run 34070255788 is the next candidate run; inspect its live result,
not this historical in-progress note, before making any completion claim.

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
  2026-09-05. Keep `checkpoint-boop-wall-595e1da` unchanged.
- Current base v30 includes the reviewed 96dp / 1.5x left swipe to the separately
  installed `com.boop.launcher`; inherited evidence is emulator-only.
- Existing permanent signer and existing Wall workflow are untouched. Candidate
  signing uses the same GitHub secrets and checks the public certificate digest.
- Shield Home/Routines checkpoints and all other app branches stay unchanged.
- Timed voice routines remain absent.

Next: inspect the instrumented UI run, fix the demonstrated cause, record exact
build/artifact evidence, then request physical acceptance without replacing the
physical checkpoint by inference. See `BOOP_CHAT_MODE_MEMORY.txt` and
`docs/BOOP-WALL-FREE-CHAT.md`; fetched main remains the shared-context authority.
