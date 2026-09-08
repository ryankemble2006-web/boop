# SHIELD TURBO durable decisions

Updated 2026-09-08. SESSION_HANDOFF.md owns exact current evidence/receipts; STATUS.md is the concise view. Fresh physical evidence overrides old pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Keep work scoped to Turbo and its workflow. Check live Turbo and main heads before publication, preserve concurrent work, never force-push, and verify remote HEAD afterward. GitHub publication is not Windows synchronization or Shield deployment.

Use only the established secret-backed `boop-dev` signer. Certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Physical CLEAN START decision

Ryan has four Kodi forks he does not want lingering after boot. The old v0.4 app-op mechanism failed and manual swipe/force-close noticeably improved Shield performance. v0.5.0 replaced that with real force-stop/read-back CLEAN START.

The CLEAN START core is now physically positive: after reboot Ryan checked Shield Apps and confirmed the selected apps had been force-closed. Preserve that result. The remaining issue is presentation during the noticeable roughly three-second Home-screen freeze, not whether the stop actually occurred.

## Static startup indicator lock

Ryan requires visible feedback during CLEAN START and **no movement whatsoever**. The static overlay text remains `SHIELD TURBO · CLEAN START` and `Tidying startup apps`.

It must remain non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, animation, repeated layout updates or focus effects. It appears, remains fixed, then disappears. If overlay permission is unavailable, cleanup continues normally without it.

v0.5.1 physically failed only the notice timing: the Shield froze while startup/cleanup occurred and the card was visible only for a microsecond at the end. Root cause is ordering/render opportunity: `indicator.show()` was immediately followed by submitting the background cleanup worker.

v0.5.2/code 9 adds exactly a **500 ms worker-thread preroll** after `indicator.show()` and before constructing `LocalBridge` / starting ADB force-stop work. This deliberately gives Android time to present the static overlay. Do not move the delay to the main/UI thread. Do not add animation. Do not alter the existing bounded boot scheduler to solve this UI timing issue.

Physical acceptance still belongs to Ryan: expected order is notice first -> cleanup/freeze underneath -> notice disappears. Machine tests verify code ordering and no known movement APIs, not what the TV physically shows.

## CLEAN START mechanism and safety retained

`STOP + VERIFY NOW` uses current-user ADB `am force-stop` for one validated selected package, then verifies no matching processes remain, Android stopped state is true, and the package remains enabled. CLEAN START targets are a private reviewed list.

AUTO CLEAN START is opt-in and bounded. Boot cleanup uses only the already-trusted loopback ADB key through `withTrustedAdb`, never a fresh RSA approval. Attempts remain ~30/60/120 seconds after boot, max 3; no periodic/resident cleaner. Current resumed app is skipped. Background-only playback is not separately detected. CLEAN START is post-boot cleanup, not guaranteed pre-execution interception; deliberate manual launch releases stopped state.

Only eligible non-system user apps are targets. Preserve BOOP/Android/NVIDIA/Google-core/system exclusions. Do not broaden to blanket system killing just to maximize free RAM. HARD BLOCK remains separate and explicit. Preserve old StartupLedger undo records.

No `pm clear`, uninstall, cache/login/data deletion, broad kill-all, rooting, device-owner/bootloader work, third-party re-signing, overclocking or fake RAM score. Keep local ADB loopback-only and its private key in `noBackupFilesDir`.

Keep physically proven brightness behavior, APPS direct launch, labels, Cancel/Back and other accepted controls. Display & Sound and Accessibility remain parked.

## Testing and latest receipt

Ryan owns all real-device visuals and remote acceptance. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment or source-string visual certification. Allowed gates are functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

Latest candidate: v0.5.2/code 9, source `6df33fa4e62688e09d1a5ee33c0ca7128afd6ed4`, run `34218659116`, job `102036355692`, success; **68 JVM tests, 22 source/security contracts, lint 0 errors/24 warnings**. Signed artifact `10052914776`, ZIP SHA-256 `0f59a1d4412b6b9671977d4fe18a874d02a1ed75c334b54d611ddf703f82c875`; tests artifact `10052958031`, SHA-256 `601f53247802e45c8099827c855c31587598832fbf6677065c2adff3e75a7fd8`. APK `Shield-Turbo-v0.5.2.apk`, 2317430 bytes, SHA-256 `e2920b1d6c0a5ea36a5f93829729a7391ca7a038f0c9241d891d3bd75b71fd51`. Permanent signer as above. Nonvisual install/cold/warm launch/no-fatal smoke passed. No visual tests ran.

v0.5.2 TDD: RED `8e1c022bc14bb4165e7b00e2e0d3bebb828a6f7e` left all 68 JVM tests green and failed only the new preroll ordering contract. GREEN implementation `4a2e15b42be5f336f0e68bea3271ca52241b08c4` passed 68 JVM tests, all 22 contracts and lint before release stamping. Final release source is `6df33fa4e62688e09d1a5ee33c0ca7128afd6ed4`.

Historical v0.5.1 indicator receipt and older startup work remain in Git history; do not repoint old checkpoints.
