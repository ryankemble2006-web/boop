# BOOP Wall status: Free Chat candidate

Updated 2026-09-07. This is the isolated `boop-wall-free-chat-wip` branch, not a
replacement for the accepted Wall physical checkpoint or another app's branch.

## Candidate

- Base: `boop-wall-resurrection@3a702f89b7f317649d267f25c34c6c9655edcff8` (v30 swipe).
- Version: 31 / `0.4.11-wall-free-chat`; package `com.boop.alpha1`.
- Implemented: deliberate three-second mode menu after the playful hold, persistent
  OpenCode/Free Chat choice, local-first routing and explicit browser handoff.
- Free Chat copies the question for manual paste/send; it is not a hidden API.
- Local behavioral JVM suite and syntax checks pass.
- Full regression, Android build, stable-signing and emulator results: pending.
- Physical acceptance: NOT TESTED. No verified candidate APK yet.

## Accepted Wall baseline (unchanged)

- Physical code: `595e1daa43393882a0e5de43967545ac526b8b66`.
- Version: 29 / `0.4.9-alpha6.5.6-wall`.
- CI: 33992704568; Pixel 7 Pro acceptance 2026-09-05.
- Natural wake, tap speech, conversation, immediate house/media, QR pairing and
  firm shake-to-puppet response were physically verified on that baseline.
- With OpenCode stopped, local house/media continued working.
- Signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
- APK SHA-256: `79ac40677687c4225989fa095644d5d97d36876150afe704e80eb6091d55530b`.
- v30 swipe inherited from the current base has emulator evidence only; do not
  call either v30 or this new candidate physically green without user testing.

## Protected

`checkpoint-boop-wall-595e1da`, `checkpoint-shield-home-f8e8135`,
`checkpoint-shield-routines-3fa18c6` and their accepted APKs remain untouched.
No changes to Shield overlay/Home/Routines, Launcher, signing credentials,
HomeAssistantRepository, FocusCardView or the local HA clients.
No automatic physical installations or permission grants. Timed voice routines
remain excluded. Shared app ownership is still defined by fetched main.

See SESSION_HANDOFF.md for the exact current task and verification boundary.
