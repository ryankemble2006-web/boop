# BOOP Wall status: Free Chat test candidate

Updated 2026-09-07. Owning branch: `boop-wall-free-chat-wip`.

## Candidate: signed and CI/emulator GREEN; physical acceptance pending

- Base: `boop-wall-resurrection@3a702f89b7f317649d267f25c34c6c9655edcff8` (v30 swipe).
- Verified build: `0ceb97bc7c258835ce292391d483849398016020`.
- Version: 31 / `0.4.11-wall-free-chat`; package `com.boop.alpha1`.
- Full successful run: `34071614834`; build job `101589892017`.
- All source/bridge/JVM/Android unit, materialization, package/version/archive,
  stable-signature and real emulator wake-microphone checks PASS.
- Actual emulator UI PASS: short hold does not open menu, three-second menu,
  OpenCode default, Free Chat selection, force-stop/restart persistence, revert
  through the same menu, vertical drag cancellation and background cancellation.
- Existing Shield pairing-return check PASS.
- Artifact: `10000728933`, `BOOP-Wall-Free-Chat-candidate`.
- Downloaded APK: `BOOP-Wall-v31-Free-Chat.apk`, 139485298 bytes.
- APK SHA-256: `2d8c858da399c2e6f1f9e7cbbed8199de6bcb9ca3d207442fd49252bb3626d29`.
- Artifact ZIP SHA-256: `3d2d7e818413640c0a9ed887fac90e5123336e5c8fe858f56ba402476d7e52d7`.
- Downloaded build receipt, ZIP digest/integrity, APK digest and inner archive
  integrity independently checked. Full cryptographic APK signature passed in CI.
- Stable-signed debug variant; not an optimized release.
- Physical Pixel 7 / Pixel 10, v31 in-place update, real browser/login/Back and
  real-house command acceptance are NOT TESTED on this candidate.

Free Chat copies the question for manual browser paste/send; it is not a hidden
API or unlimited quota. Local control runs first; only NO_MATCH may reach chat.
The earlier failed UI gates are resolved by specific emulator onboarding/capture
fixes. Actual interaction assertions were not removed or weakened. See handoff.

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
- Preserved Wall branch is still `3a702f8`. Its v30 swipe has emulator evidence
  only. Do not promote a physical checkpoint without Ryan's device acceptance.

## Protected / next

`checkpoint-boop-wall-595e1da`, `checkpoint-shield-home-f8e8135`,
`checkpoint-shield-routines-3fa18c6` and their accepted APKs remain untouched.
No changes to Shield overlay/Home/Routines, Launcher, signing credentials,
HomeAssistantRepository, FocusCardView or the local HA clients.
No automatic physical installations or permission grants. Timed voice routines
remain excluded. Shared app ownership is still defined by fetched main.

Next: physical Pixel 7 update and hold/menu/persistence/revert, local media,
general-question browser handoff and Back acceptance. Record results before
promoting. Documentation-only updates do not change the verified APK build.
