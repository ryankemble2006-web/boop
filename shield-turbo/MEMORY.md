# SHIELD TURBO durable decisions

Updated 2026-09-08. SESSION_HANDOFF.md owns exact current evidence/receipts; STATUS.md is the concise view. Fresh physical evidence overrides old pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Keep work scoped to Turbo and its workflow. Check live Turbo and `main` heads before publication, preserve concurrent work, never force-push, and verify remote HEAD afterward. GitHub publication is not Windows synchronization or Shield deployment.

Use only the established secret-backed `boop-dev` signer. Certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Physical CLEAN START decision

Ryan has four Kodi forks he does not want lingering after boot. The old v0.4 app-op mechanism failed and manual force-close noticeably improved Shield performance. v0.5.0 replaced that with real force-stop/read-back CLEAN START.

The CLEAN START core is physically positive from earlier real-Shield tests. Stale Recents/task-manager cards may persist after force-stop, while the target apps are not loaded and reload only when focused. Preserve the interpretation: cards are history; deliberate normal launch releases stopped state. Do not change the accepted stop/read-back mechanism to solve presentation.

Latest v0.5.4 physical result is specifically: no startup sign, but much faster completion with navigation available again within roughly one second. Treat this as positive evidence for the 500 ms fail-open timing and negative evidence for the overlay's visibility. The user's latest v0.5.4 message did not separately confirm target stopped state, so do not turn it into a new cleanup acceptance claim.

## Static startup indicator lock

Ryan requires visible feedback during CLEAN START and **no movement whatsoever**. Static text remains `SHIELD TURBO · CLEAN START` and `Tidying startup apps`.

It must remain non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, animation, repeated layout animation or focus effects. If overlay permission/presentation is unavailable, cleanup continues normally without it.

Physical history:
- v0.5.1 flashed only at the end;
- v0.5.2's 500 ms guessed preroll showed nothing;
- v0.5.3's committed-frame wait showed nothing and caused almost eight seconds total Turbo completion time;
- v0.5.4's Android 11+ display-bound window context still showed nothing, but its 500 ms fail-open restored navigation within about one second.

Three presentation attempts have therefore failed physically. **Do not make a fourth blind overlay/timing fix.** Systematic debugging now requires evidence at the Android lifecycle/compositor boundary.

## v0.5.5 diagnostic decision

v0.5.5/code 12 is an evidence-gathering release. It keeps the v0.5.4 overlay path and 500 ms fail-open unchanged and records locally:
- boot-time overlay permission result;
- window-context path;
- addView accepted/failed;
- draw/frame-commit/timeout/bypass result;
- elapsed milliseconds;
- bounded failure detail.

The diagnostic is stored in Turbo's existing private SharedPreferences before ADB cleanup begins and shown as a non-focusable grey line in CLEAN START. It contains no ADB secret or personal/account data.

Use the diagnostic to choose architecture:
- permission `NO`: resolve permission state, not render timing;
- `add=FAILED`: investigate the returned window/addView boundary;
- `add=ADDED` + `present=TIMEOUT`: Android accepted the window but did not draw/commit within the safe bound;
- `DRAWN` or `FRAME_COMMITTED` while physically invisible: treat the overlay architecture as suppressed/occluded by Shield composition and abandon it rather than extending waits.

Ryan remains the only visual authority. Machine `DRAWN`/`FRAME_COMMITTED` is not visual certification.

## CLEAN START mechanism and safety retained

`STOP + VERIFY NOW` uses current-user ADB `am force-stop` for one validated selected package, then verifies no matching processes remain, Android stopped state is true, and the package remains enabled. CLEAN START targets are a private reviewed list.

AUTO CLEAN START is opt-in and bounded. Boot cleanup uses only the already-trusted loopback ADB key through `withTrustedAdb`, never a fresh RSA approval. Attempts remain ~30/60/120 seconds after boot, max 3; no periodic/resident cleaner. Current resumed app is skipped. Background-only playback is not separately detected. CLEAN START is post-boot cleanup, not guaranteed pre-execution interception; deliberate manual launch releases stopped state.

Only eligible non-system user apps are targets. Preserve BOOP/Android/NVIDIA/Google-core/system exclusions. Do not broaden to blanket system killing just to maximize free RAM. HARD BLOCK remains separate and explicit. Preserve old StartupLedger undo records.

No `pm clear`, uninstall, cache/login/data deletion, broad kill-all, rooting, device-owner/bootloader work, third-party re-signing, overclocking or fake RAM score. Keep local ADB loopback-only and its private key in `noBackupFilesDir`.

Keep physically proven brightness behavior, APPS direct launch, labels, Cancel/Back and other accepted controls. Display & Sound and Accessibility remain parked.

## Testing and latest receipt

Ryan owns all real-device visuals and remote acceptance. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment or source-string visual certification. Allowed gates are functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

Latest candidate: v0.5.5/code 12, source `e22beecbaa9d95aeab036ae403684a32a7a33979`, run `34226811605`, job `102062828791`, success; **68 JVM tests, source/API/security contracts passed, lint 0 errors/24 warnings**. Signed artifact `10056167964`, ZIP SHA-256 `85cb44b357b5c7979e3e719ac4b55088a6e8c860774c51047c40dcb0d9391340`; tests artifact `10056218423`, SHA-256 `f6fd08f62ccacd0f22baee16cae71357e29319f4b6d7603808e019a834537e25`. APK `Shield-Turbo-v0.5.5.apk`, 2328962 bytes, SHA-256 `40323820eda72df3592fc756a2b30ee15816cc8633a3e577ade65b5475d7b77f`. Permanent signer as above. Nonvisual install/cold/warm launch/no-fatal smoke passed. No visual tests ran.

v0.5.5 diagnostics TDD: RED `888235117ec3b6bb6ea12534ee2a82625aab8eb3` left all 68 JVM tests green and failed the new persisted-diagnostics contract. Final GREEN diagnostic implementation `f0563b064724f4c665bf62c1f797780c5a03a349` passed the full machine gate in run `34226102737`, job `102060638029`. Final stamped release source is `e22beecbaa9d95aeab036ae403684a32a7a33979`.

Historical v0.5.4/v0.5.3/v0.5.2/v0.5.1 receipts and older startup work remain in Git history; do not repoint old checkpoints.

## Next physical evidence required

After v0.5.5 reboot, capture the exact `STARTUP NOTICE DIAGNOSTIC:` line from CLEAN START, whether the sign appeared, whether navigation remains quick, and whether the selected Kodi forks are stopped. Do not change presentation architecture again until that evidence exists.
