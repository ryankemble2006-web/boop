# SHIELD TURBO durable decisions

Updated 2026-09-08. SESSION_HANDOFF.md owns exact current evidence/receipts; STATUS.md is the concise view. Fresh physical evidence overrides old pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Keep work scoped to Turbo and its workflow. Check live Turbo and main heads before publication, preserve concurrent work, never force-push, and verify remote HEAD afterward. GitHub publication is not Windows synchronization or Shield deployment.

Use only the established secret-backed `boop-dev` signer. Certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Physical CLEAN START decision

Ryan has four Kodi forks he does not want lingering after boot. The old v0.4 app-op mechanism failed and manual swipe/force-close noticeably improved Shield performance. v0.5.0 replaced that with real force-stop/read-back CLEAN START.

Ryan physically reported v0.5.0 appears to have beaten the startup problem. Treat that as current positive functional evidence, while avoiding broader claims about untested system-app cleanup or universal startup interception. The observed cost is a noticeable roughly three-second Home-screen freeze while CLEAN START executes.

## Static startup indicator lock

Ryan explicitly requires user feedback during that pause and **no movement whatsoever**. v0.5.1/code 8 implements one static top-centre application overlay during the automatic CLEAN START job. Text is `SHIELD TURBO · CLEAN START` and `Tidying startup apps`.

The indicator must remain non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, animation, repeated layout updates or focus effects. It appears, remains fixed, then disappears. If overlay permission is unavailable, the cleanup must continue normally without it.

The indicator is presentation-only. Do not alter the working CLEAN START scheduler or targeting to accommodate it. Current attempts remain ~30/60/120 seconds after boot, max 3; no periodic/resident cleaner. The static card is shown only while an actual automatic job attempt is executing, not during the waiting delay before the job.

v0.5.1 visual placement/appearance and actual real-device motionlessness are not physically accepted until Ryan tests the real Shield. GitHub must never certify those visuals.

## CLEAN START mechanism and safety retained

`STOP + VERIFY NOW` uses current-user ADB `am force-stop` for one validated selected package, then verifies no matching processes remain, Android stopped state is true, and the package remains enabled. CLEAN START targets are a private reviewed list.

AUTO CLEAN START is opt-in and bounded. Boot cleanup uses only the already-trusted loopback ADB key through `withTrustedAdb`, never a fresh RSA approval. Current resumed app is skipped. Background-only playback is not separately detected. CLEAN START is post-boot cleanup, not guaranteed pre-execution interception; deliberate manual launch releases stopped state.

Only eligible non-system user apps are targets. Preserve BOOP/Android/NVIDIA/Google-core/system exclusions. Do not broaden to blanket system killing just to maximize free RAM. HARD BLOCK remains separate and explicit. Preserve old StartupLedger undo records.

No `pm clear`, uninstall, cache/login/data deletion, broad kill-all, rooting, device-owner/bootloader work, third-party re-signing, overclocking or fake RAM score. Keep local ADB loopback-only and its private key in `noBackupFilesDir`.

Keep physically proven brightness behavior, APPS direct launch, labels, Cancel/Back and other accepted controls. Display & Sound and Accessibility remain parked.

## Testing and latest receipt

Ryan owns all real-device visuals and remote acceptance. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment or source-string visual certification. Allowed gates are functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

Latest candidate: v0.5.1/code 8, source `99c90a63a17f4a3a72b561e2c7ed3792deb41649`, run `34215598324`, job `102026489736`, success; **68 JVM tests, 21 source/security contracts, lint 0 errors/24 warnings**. Signed artifact `10051719431`, ZIP SHA-256 `2d3b9a3bd5bd04d338c5c553b3ddea9f7734af651e53477dedac14952fa688a1`; tests artifact `10051766008`, SHA-256 `8ea98b5550ab000878fb0cfbe7fce005461925b46ab33f076fda3dd643ca39b5`. APK `Shield-Turbo-v0.5.1.apk`, 2316998 bytes, SHA-256 `0b92436b50ac8cb94d3d17855a13d203c4c48bda3a7727c55f5c1167dc74de3c`. Permanent signer as above. Nonvisual install/cold/warm launch/no-fatal smoke passed. No visual tests ran.

TDD indicator receipt: RED `b9556bc1409fd11f53fd786198acb46efdfa7d73`; GREEN implementation `8e28cde4fea25a64e3c32cb5972dbb1b65af5941`; release `99c90a63a17f4a3a72b561e2c7ed3792deb41649`.

Historical evidence remains in Git history; do not repoint old checkpoints.
