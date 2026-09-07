# BOOP Wall status: Free Chat test candidate

Updated 2026-09-07. This is the isolated `boop-wall-free-chat-wip` branch, not a
replacement for the accepted Wall physical checkpoint or another app's branch.

## Candidate: built and signed, NOT fully verified

- Base: `boop-wall-resurrection@3a702f89b7f317649d267f25c34c6c9655edcff8` (v30 swipe).
- Built code: `e5c5598c2915a0a12b51f91cecebf9f51c1b8662`.
- Version: 31 / `0.4.11-wall-free-chat`; package `com.boop.alpha1`.
- Implemented in source: deliberate three-second mode menu after the playful hold,
  persistent OpenCode/Free Chat choice, local-first routing and browser handoff.
- Free Chat copies the question for manual paste/send; it is not a hidden API.
- Final run 34070255788, job 101586202899: source/bridge/JVM checks, Android unit
  tests, APK assembly, package/version/signature checks and real emulator wake
  startup PASS. The menu/persistence/revert/cancellation test FAILS. Pairing test
  SKIPPED. Exact latest interaction failure cause is not established.
- Physical acceptance: NOT TESTED. Do not promote this as a fully working release.
- Artifact: 10000300048 / `BOOP-Wall-Free-Chat-candidate`.
- Downloaded APK: `BOOP-Wall-v31-Free-Chat.apk`, 139485298 bytes.
- Candidate APK SHA-256:
  `2d8c858da399c2e6f1f9e7cbbed8199de6bcb9ca3d207442fd49252bb3626d29`.
- Downloaded provenance/archive/certificate identity independently checked;
  full cryptographic APK signature verification passed in CI.
- Stable-signed debug variant; not an optimized release.

## Accepted Wall baseline (unchanged)

- Physical code: `595e1daa43393882a0e5de43967545ac526b8b66`.
- Annotated tag `checkpoint-boop-wall-595e1da` reread live and still points there.
- Version: 29 / `0.4.9-alpha6.5.6-wall`.
- CI: 33992704568; Pixel 7 Pro acceptance 2026-09-05.
- Natural wake, tap speech, conversation, immediate house/media, QR pairing and
  firm shake-to-puppet response were physically verified on that baseline.
- With OpenCode stopped, local house/media continued working.
- Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- Accepted APK SHA-256: `79ac40677687c4225989fa095644d5d97d36876150afe704e80eb6091d55530b`.
- v30 swipe inherited from the current base has emulator evidence only; do not
  call either v30 or this new candidate physically green without user testing.

## Protected

`checkpoint-boop-wall-595e1da`, `checkpoint-shield-home-f8e8135`,
`checkpoint-shield-routines-3fa18c6` and their accepted APKs remain untouched.
No changes to Shield overlay/Home/Routines, Launcher, signing credentials,
HomeAssistantRepository, FocusCardView or the local HA clients.
No automatic physical installations or permission grants. Timed voice routines
remain excluded. Shared app ownership is still defined by fetched main.

See SESSION_HANDOFF.md for history, exact evidence and the remaining menu test.
