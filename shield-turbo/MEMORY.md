# SHIELD TURBO durable decisions

Updated 2026-09-08. SESSION_HANDOFF.md owns exact current evidence/receipts; STATUS.md is the concise view. Fresh physical evidence overrides old pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Keep work scoped to Turbo and its workflow. Check live Turbo and main heads before publication, preserve concurrent work, never force-push, and verify remote HEAD afterward. GitHub publication is not Windows synchronization or Shield deployment.

Use only the established secret-backed `boop-dev` signer. Certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Physical CLEAN START decision

Ryan has four Kodi forks he does not want lingering after boot. The old v0.4 app-op mechanism failed and manual swipe/force-close noticeably improved Shield performance. v0.5.0 replaced that with real force-stop/read-back CLEAN START.

The CLEAN START core is physically positive. Most recently on v0.5.2, Ryan rebooted, saw no CLEAN START notice, then confirmed the target apps were stopped. Task-manager cards persisted from the previous session, but the apps were not loaded and reloaded only when focused. Preserve the interpretation: recents/task cards can survive force-stop; normal deliberate launch releases stopped state. The remaining issue is presentation during the noticeable roughly three-second Home-screen freeze, not whether the stop occurred.

## Static startup indicator lock

Ryan requires visible feedback during CLEAN START and **no movement whatsoever**. Static text remains `SHIELD TURBO · CLEAN START` and `Tidying startup apps`.

It must remain non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, animation, repeated layout animation or focus effects. It appears, remains fixed, then disappears. If overlay permission is unavailable, cleanup continues normally without it.

v0.5.1 physically failed notice timing by flashing only at the end. v0.5.2/code 9 added a 500 ms worker-thread preroll but physically produced no visible notice. That proves a fixed delay after `WindowManager.addView` is not a reliable proxy for actual frame presentation on Shield boot.

v0.5.3/code 10 therefore waits on a **bounded first-frame presentation signal**, not a guessed sleep. `CleanStartIndicator` uses a `CountDownLatch`; API 29+ hardware-accelerated rendering releases it from `registerFrameCommitCallback`, with an OnDraw fallback. Overlay-unavailable/addView-failure paths release immediately. The CLEAN START worker waits up to 3000 ms before constructing `LocalBridge`. Keep that wait on the worker, never the main/UI thread. Do not change the 30/60/120 boot scheduler to solve presentation.

Physical acceptance still belongs to Ryan: expected order is notice actually presented first -> cleanup/freeze underneath -> notice disappears. Machine checks verify code ordering, API safety and absence of known animation behavior, not what the TV physically shows.

## CLEAN START mechanism and safety retained

`STOP + VERIFY NOW` uses current-user ADB `am force-stop` for one validated selected package, then verifies no matching processes remain, Android stopped state is true, and the package remains enabled. CLEAN START targets are a private reviewed list.

AUTO CLEAN START is opt-in and bounded. Boot cleanup uses only the already-trusted loopback ADB key through `withTrustedAdb`, never a fresh RSA approval. Attempts remain ~30/60/120 seconds after boot, max 3; no periodic/resident cleaner. Current resumed app is skipped. Background-only playback is not separately detected. CLEAN START is post-boot cleanup, not guaranteed pre-execution interception; deliberate manual launch releases stopped state.

Only eligible non-system user apps are targets. Preserve BOOP/Android/NVIDIA/Google-core/system exclusions. Do not broaden to blanket system killing just to maximize free RAM. HARD BLOCK remains separate and explicit. Preserve old StartupLedger undo records.

No `pm clear`, uninstall, cache/login/data deletion, broad kill-all, rooting, device-owner/bootloader work, third-party re-signing, overclocking or fake RAM score. Keep local ADB loopback-only and its private key in `noBackupFilesDir`.

Keep physically proven brightness behavior, APPS direct launch, labels, Cancel/Back and other accepted controls. Display & Sound and Accessibility remain parked.

## Testing and latest receipt

Ryan owns all real-device visuals and remote acceptance. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment or source-string visual certification. Allowed gates are functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

Latest candidate: v0.5.3/code 10, source `a65c800c459d292a37a092f430d08ddfd2be3742`, run `34221284613`, job `102044740488`, success; **68 JVM tests, 22 source/security contracts, lint 0 errors/24 warnings**. Signed artifact `10053936105`, ZIP SHA-256 `083fe26f122d10f0bc27ef4569ba9731799b3b0c4e24bcc6c15be874f0f81b1f`; tests artifact `10053981252`, SHA-256 `e6277ca06695eae76ddc16c23bf6eb897b89e050b00742ede23fddf2426fc869`. APK `Shield-Turbo-v0.5.3.apk`, 2319002 bytes, SHA-256 `fa50dafa7f01f6c6d2e8c9e840449f0a4fd2d57674ec172fe16e02040da133b1`. Permanent signer as above. Nonvisual install/cold/warm launch/no-fatal smoke passed. No visual tests ran.

v0.5.3 TDD: RED `a3cf5618ecac10e422d25c09246b919e22514dbd` left all 68 JVM tests green and failed the new presentation ordering contract before build/signing. GREEN implementation `92ebc39494610bb5fef8f95eb5fc3489351db3db` passed unit/source/lint/build/signer and nonvisual launch gates. Final stamped release source is `a65c800c459d292a37a092f430d08ddfd2be3742`.

Historical v0.5.2/v0.5.1 indicator receipts and older startup work remain in Git history; do not repoint old checkpoints.
