# SHIELD TURBO durable decisions

Updated 2026-09-08. SESSION_HANDOFF.md owns current evidence and source references; STATUS.md is the concise verification view. New user evidence overrides dated pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`. It is not the unified BOOP body. Keep changes scoped to Turbo and its existing workflow. Use connected GitHub tools in chat when available, not a mandatory Work-mode detour. Read main startup/rules and Turbo docs, check live main and owning branch before editing/publishing, preserve concurrent/dirty work, and verify live HEAD after publication. GitHub publication is not Windows synchronization or device deployment.

Use only the established secret-backed boop-dev signer; certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. Never replace/expose it, publish credentials or private device dumps, or repoint checkpoints. Detailed historical receipts remain in the handoff history.

## User goal and physical evidence

Ryan has four Kodi forks he does not want waking after boot. He wants essential Android/Shield functionality only at startup, with optional apps available for deliberate manual use. He explicitly clarified that the forks remain in the **Shield task manager** and that swiping/force-closing them noticeably improves performance. This is not just an enabled Force Stop button in App Info. Do not ask that distinction again or dismiss his observed improvement. Task-list entries are not quantified process/RAM evidence; collect that through an in-app read-only snapshot instead of inventing it.

v0.4.1's action menu and ordinary manual launch have physical confirmation. Boot suppression failed acceptance. v0.4.0's information-only dialog is historical and was repaired by removing setMessage from the setItems builder. Earlier accepted behavior: bedroom brightness, corrected STANDARD maintenance selectability, Developer Options opening. Display & Sound and Accessibility both remain physically unresolved and parked.

## Existing startup behavior and limits

The delivered BLOCK STARTUP / KEEP LAUNCHABLE action changes only RUN_IN_BACKGROUND and RUN_ANY_IN_BACKGROUND to ignore, with original-state capture and read-back. Those app-ops are not a verified universal boot block. They do not themselves force-stop an app or prove no process exists. Do not keep presenting saved restrictions as a successful fix.

HARD BLOCK is a separate explicitly confirmed package disable. It prevents normal launching until restored and must not become the default or a hidden fallback. No package uninstall, pm clear, cache/file/login removal, rooting, bootloader work, overclocking or arbitrary governors.

The exact rollback ledger must survive updates. First original wins. Verify restoration before removing a record; keep failed/unverified entries. Undo All handles each recorded app separately. Cancelling an operation is not undo, and force-stop cannot restore interrupted playback or unsaved work.

## Proposed clean-start direction, not implemented

A real selected-app STOP AND VERIFY action should precede automated enforcement. Use current-user ADB force-stop, verify actual target/stopped/enabled/process state and keep manual launching and app data. Do not use a task card or a saved preference as proof of a stopped process.

Optional automatic CLEAN START would require a small bounded post-boot task, explicitly enabled in-app for a reviewed group. It would wait for already-authorised loopback ADB readiness, stop only approved eligible apps, record outcome and finish. No indefinite retries, resident RAM killer, periodic sweeps, remote targets, unsolicited boot-time permission prompts or silent newly-installed-app inclusion. Protect active manual use/playback and provide KEEP RUNNING exceptions. Skip and report when access or identity cannot be verified.

That proposed boot task differs from the previously chosen no-boot-task implementation; it is not in v0.4.1 and is not silently authorised or installed by this note. It must be visible in future design/release notes and controllable by the user. No RECEIVE_BOOT_COMPLETED or background service was added during this documentation update.

Do not promise optional apps never execute momentarily at boot: post-boot stopping is cleanup, not interception. Force-stopped state is released by deliberate launch; explicit activation mechanisms may bypass normal broadcast exclusions. Stock shell component-state restrictions also prevent guaranteeing individual receiver disabling for arbitrary non-test apps. Root/re-signing/device-owner work are not implied solutions.

Essential classification must account for actual roles and dependencies, including launcher, input/remote/accessibility, networking/VPN where used, media/DRM and necessary NVIDIA services. Neither com.android prefix nor system/non-system status alone proves necessity. Keep existing system exclusions until reviewed replacement protection exists. Optional vendor/system components need individual evidence rather than blanket disabling.

## Existing controls to preserve

Keep the proven 10-100% brightness overlay and non-exported BrightnessService unchanged; 100% removes the overlay without intercepting input. Normal APPS OK launches directly. Keep actual installed launcher/application labels ahead of package fallback and never invent fork names or IDs. AlertDialog list choices must not compete with message content. Cancel and remote Back must remain usable while busy.

Local ADB uses only loopback port 5555 and a private per-install RSA key in noBackupFilesDir. This is not the APK signer and must not be exported. Initial Network Debugging switch and RSA trust approval belong to the user. Verify shell identity on each transient connection; a secure-settings grant alone does not prove current ADB availability. No LAN scanning, listening server or persistent shell. Sleep/reboot are confirmed disruptive actions; single-app restart preserves data; animation settings retain original-value undo; diagnostics are bounded snapshots.

## Testing, delivery and latest receipt

Ryan owns all real-device visuals and remote acceptance. No GitHub screenshots, UI hierarchy dumps, image/golden/layout/appearance checks or source-string appearance certification. Keep focused API/data/logic/protocol/security tests, lint, compilation, permanent-signer/package/archive verification and basic nonvisual crash smoke. Offer a verified signed APK before slower optional smoke, but never equate any machine gate with effective startup suppression.

Latest APK remains v0.4.1/code 6, built `0961153e5dea94c38027cdf31530f500c5b29573`, run `34204102153`, job `101989443983`, artifact `10047107169`, SHA-256 `c7bc147a70378dfe62a14e542aeb5dcb1036fbe6818551e98ddcf6fe061793fa`, 2283246 bytes. Its historical 58 JVM tests / 13 Python checks / lint 0 errors and 22 warnings / signer/package/nonvisual launch results passed. Physical acceptance is partial as above.

Current change is documentation/research only. No new APK, app permissions, boot tasks, signing, visual tests, installations or physical-device actions. Primary-source references for the proposed mechanism and limits are in SESSION_HANDOFF.md. Preserve all historical receipt details via the handoff at `d64d51db1fd6b7201429ce484be3e93973ff622c` and older Git history.
