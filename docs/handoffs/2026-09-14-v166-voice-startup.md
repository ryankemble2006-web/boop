# v166 voice startup: installed and command/reply accepted on Shield and Pixel 10 Pro XL

Updated 2026-09-14. Task owner `boop-voice-ack-v166`.

## Source and build

Based on installed Shield v164 source `f9f65569250b9dc02602101ef4d56195824e0380`; no unrelated branch merge. Signed source `982a2ddeed73d673be6fe0fe2251a74d290c07c5`. Package `com.boop.alpha1`, version `166 / 1.2.166-voice-startup`. GitHub run `34861777001`, job `104035563344`, SUCCESS. Artifact `10355475599`, name `BOOP-Unified-v166-Voice-Startup`.

APK SHA256 `5f1041581c9d0272eb8943ac54a101895614fbfc5a48ba23f0fc9efa9727fd03`. Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Downloaded APK hash, package/version and signature independently verified; installed APK bytes on both devices match.

## Tests and review

Original source RED run `34861028390`: five expected failures, including reply discarded before engine readiness. Review exposed pause/wake ownership and late-background-response holes. They were reproduced RED in `34861528841`, then repaired. Final run passed 14 behavioral cases against both source and actual fully materialized reply/onInit/speak/onPause methods with the real backend and wake-state model. Nine existing natural voice contracts passed; native lyrics functional checks, six timing tests and ten ownership tests passed. Two inherited one-time v161/v162 source-freeze checks skipped later versions. Signed build completed successfully. Read-only reviewer found no remaining blockers in production revision `06abee21`; later changes are tests/workflow only. No emulator or hosted visual testing.

## Device evidence

User authorized Shield and Pixel 7 installation/testing, then explicitly substituted Pixel 10 Pro XL for Pixel 7. Shield updated from v164 and Pixel 10 Pro XL from v151 with `adb install -r`, both Success. Installed version/hash verified. Existing HOME, assistant selection, notification-listener settings and Android grant entries are unchanged on both. All Shield preference-file hashes were unchanged. Phone preference hashes changed only for notification state; voice, house connection, appearance and colour preferences remained intact. Private rollback APKs retained. No grants, data clear, role changes or settings resets.

Shield uses Android speech; phone has its existing Emma selection. Natural voice code/selection/tuning was preserved. This does not establish physical audio acceptance.

## Physical acceptance — 14 September 2026

User confirmed Pixel 10 Pro XL "is saying done. and actually doing it". This accepts the tested phone command and spoken acknowledgement using its preserved voice selection.

The first post-install Shield attempt returned empty recognition results / NO_SPEECH_DETECTED; retain it as a failed input attempt, not a successful command or proof of the cause of the original intermittent issue.

A coordinated Shield retest detected speech, returned withSpeech=true, submitted the Android reply, then logged playback started and completed. User confirmed "she did it that time and stayed up longer to listen", and explicitly answered "Yes, she spoke" when asked about the acknowledgement. This accepts the tested real-remote candle command and audible reply on installed v166.

Both requested targets are installed and the requested command/reply path is physically accepted. This is not an extended microphone-reliability soak, a blanket acceptance of all commands, or a claim that every no-speech event has been eliminated. No settings or app changes were made between the first failed Shield input attempt and the successful retest. Keep rollback APKs and raw diagnostics private. No additional deployment or unrelated merge is pending for this task.
