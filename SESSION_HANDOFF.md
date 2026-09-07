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

Local JVM routing/hold tests: PASS (new tests were first observed failing before
implementation). Python syntax and workflow YAML parsing: PASS. Full source,
Android build, stable signer, real wake microphone and UI emulator gates are
PENDING in the dedicated GitHub candidate workflow. No candidate APK has yet
been verified. Do not present this initial commit as a tested release.

This session has GitHub connector access, not the Windows laptop filesystem or
LAN. The Linux scratch snapshot has no Android SDK and no working direct DNS.
No laptop synchronization, physical installation or physical test is claimed.

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

Next: inspect the candidate CI run, fix any demonstrated failure, record exact
build/artifact evidence, then request physical acceptance without replacing the
physical checkpoint by inference. See `BOOP_CHAT_MODE_MEMORY.txt` and
`docs/BOOP-WALL-FREE-CHAT.md`; fetched main remains the shared-context authority.
