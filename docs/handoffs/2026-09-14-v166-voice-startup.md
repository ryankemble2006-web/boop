# v166 voice startup: installed, physical retest unresolved

Updated 2026-09-14. Task owner `boop-voice-ack-v166`.

## Source and build

Based on installed Shield v164 source `f9f65569250b9dc02602101ef4d56195824e0380`; no unrelated branch merge. Signed source `982a2ddeed73d673be6fe0fe2251a74d290c07c5`. Package `com.boop.alpha1`, version `166 / 1.2.166-voice-startup`. GitHub run `34861777001`, job `104035563344`, SUCCESS. Artifact `10355475599`, name `BOOP-Unified-v166-Voice-Startup`.

APK SHA256 `5f1041581c9d0272eb8943ac54a101895614fbfc5a48ba23f0fc9efa9727fd03`. Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Downloaded APK hash, package/version and signature independently verified; installed APK bytes on both devices match.

## Tests and review

Original source RED run `34861028390`: five expected failures, including reply discarded before engine readiness. Review exposed pause/wake ownership and late-background-response holes. They were reproduced RED in `34861528841`, then repaired. Final run passed 14 behavioral cases against both source and actual fully materialized reply/onInit/speak/onPause methods with the real backend and wake-state model. Nine existing natural voice contracts passed; native lyrics functional checks, six timing tests and ten ownership tests passed. Two inherited one-time v161/v162 source-freeze checks skipped later versions. Signed build completed successfully. Read-only reviewer found no remaining blockers in production revision `06abee21`; later changes are tests/workflow only. No emulator or hosted visual testing.

## Device evidence

User authorized Shield and Pixel 7 installation/testing, then explicitly substituted Pixel 10 Pro XL for Pixel 7. Shield updated from v164 and Pixel 10 Pro XL from v151 with `adb install -r`, both Success. Installed version/hash verified. Existing HOME, assistant selection, notification-listener settings and Android grant entries are unchanged on both. All Shield preference-file hashes were unchanged. Phone preference hashes changed only for notification state; voice, house connection, appearance and colour preferences remained intact. Private rollback APKs retained. No grants, data clear, role changes or settings resets.

Shield uses Android speech; phone has its existing Emma selection. Natural voice code/selection/tuning was preserved. This does not establish physical audio acceptance.

## Unresolved physical test

After installation the user reported BOOP flashed and ignored the request; clarification of which device is pending. The captured Shield attempt opened recognition, reached Android speech readiness, then the system recognizer returned empty results and NO_SPEECH_DETECTED. No reply was queued or played in that attempt. This does not prove the original command-and-acknowledgement symptom fixed and must not be recorded as success. Phone acoustic result remains unconfirmed. Next: identify the reported device, repeat a coordinated real command with fresh logs, then resolve the observed failure before claiming acceptance. Preserve logs privately; do not publish raw device dumps, addresses or voice profiles.
