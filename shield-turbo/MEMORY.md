# SHIELD TURBO durable decisions

Updated 2026-09-07. Read `SESSION_HANDOFF.md` for the actual current build receipt and `STATUS.md` when present for the current candidate. This file records decisions, not a claim of physical acceptance.

## Identity and ownership

SHIELD TURBO is an independently installed app, package `com.boop.shieldturbo`, in `shield-turbo/` on branch `shield-turbo-v01`. The repository is shared with BOOP for convenience. It is not another BOOP body or a replacement for the unified `com.boop.alpha1` APK. Never merge the old BOOP runtime inherited from main into the concurrent `boop-unified` app branch.

Ryan approved the design and implementation and requested building/signing through GitHub. Do not keep asking him to reapprove the same architecture or switch to Work mode. Development is in chat with connected GitHub tools; build, tests and signing run in GitHub Actions. Work mode is reserved for his later tuning pass. Give meaningful progress updates during an active response, but do not promise asynchronous monitoring or control over client sleep.

## Product rules

The approved first release is an honest read-only analyser. No speed-up scores, blanket RAM cleaning, overclocking, process killing, other-app data clearing, network tweaking or automatic changes. Optimisation features require separate evidence of benefit and a clear undo/privilege story.

Measurements are snapshots on demand, not continuous monitoring. Keep UI work on the main thread and probes on a bounded-use worker. Cancel scanning when leaving the screen. No idle wake lock, keep-screen-on flag, foreground service, microphone or camera. Scans and permission checks stay local; the initial APK requests only ACCESS_NETWORK_STATE.

CPU frequency is not load. Available memory includes reclaimable memory and is not a performance score. A generic thermal zone is not a named CPU/GPU sensor without sensor identity evidence. The thermal-zone temp ABI uses millidegrees Celsius. Missing, permission-denied, malformed and unexpected read failures must remain distinguishable and must not crash unrelated readings. Internet validation does not measure throughput.

## Privilege direction

STANDARD is the expected ordinary app state. ADB TURBO requires actual elevated diagnostic evidence; usage access or enabled debugging is not proof. The first release has no ADB helper or setup/grant action. A one-time grant is not a permanent shell connection, and reboot/disabling debugging behavior must be physically verified before promises are made.

ROOT is reported only for actual root process authority. The existence of a su executable or root-management app is not authority. Do not invoke su to fill in a badge or imply the app is rooted merely because the device might be.

Reuse the existing secret-backed boop-dev signer and verify public certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Never create a replacement key, read/expose secret values, copy private keys into Git, use BOOP relay credentials, or deploy to a physical device without explicit instruction.

## Physical Shield acceptance checklist

These are tests to perform, not completed results:

1. Install the signed SHIELD TURBO APK independently of BOOP. Confirm it appears in the Android TV app launcher and starts without a flash/crash.
2. With only the remote, activate ANALYSE SHIELD; verify the scan completes and every card is reachable with Up/Down. Centre opens details and Back closes them. No touch input is required.
3. Compare memory/storage/device/transport with device settings. Record which exact CPU/thermal source paths are exposed, restricted or absent. Do not identify a zone as CPU/GPU by its index alone.
4. Confirm ACCESS DETAILS changes no setting, grants no permission and truthfully describes the missing ADB setup/helper in v0.1.
5. Leave the app during a scan, return, repeat. Confirm scan state recovers and ordinary sleep/media playback/BOOP behavior is unchanged. Record physical power/thermal observations separately, without inventing savings.
6. Record the exact source commit, workflow run, artifact and APK SHA-256 with Ryan's result. Only explicit positive hardware evidence creates the first physical rollback checkpoint.

The CI emulator currently uses API 30 with a handheld profile and simulated D-pad input. It tests app behavior, not real Tegra telemetry, Shield firmware or measured performance gains.
