# SHIELD TURBO durable decisions

Updated 2026-09-08. SESSION_HANDOFF.md owns exact current evidence/receipts; STATUS.md is the concise view. Fresh physical evidence overrides old pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Keep work scoped to Turbo and its workflow. Check live Turbo and main heads before publication, preserve concurrent work, never force-push, and verify remote HEAD afterward. GitHub publication is not Windows synchronization or Shield deployment.

Use only the established secret-backed `boop-dev` signer. Certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Physical CLEAN START decision

Ryan has four Kodi forks he does not want lingering after boot. The old v0.4 app-op mechanism failed and manual swipe/force-close noticeably improved Shield performance. v0.5.0 replaced that with real force-stop/read-back CLEAN START.

The CLEAN START core is physically positive. Stale Recents/task-manager cards may persist after force-stop, but the target apps are not loaded and reload only when focused. Preserve the interpretation: cards are history; deliberate normal launch releases stopped state. Do not change the accepted stop/read-back mechanism to solve presentation.

## Static startup indicator lock

Ryan requires visible feedback during CLEAN START and **no movement whatsoever**. Static text remains `SHIELD TURBO · CLEAN START` and `Tidying startup apps`.

It must remain non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, animation, repeated layout animation or focus effects. It appears, remains fixed, then disappears. If overlay permission or presentation is unavailable, cleanup continues normally without it.

Physical history matters:
- v0.5.1 flashed the card only at the end;
- v0.5.2's 500 ms guessed preroll showed nothing;
- v0.5.3's committed-frame wait also showed nothing and caused almost eight seconds total Turbo completion time on Ryan's Shield.

Therefore do not solve this with another arbitrary delay. v0.5.3 is not a physical checkpoint.

v0.5.4/code 11 changes the cold-boot overlay architecture. On Android 11+ the static card now uses a primary-display-bound `TYPE_APPLICATION_OVERLAY` window context created through `DisplayManager`, `createDisplayContext` and `createWindowContext`. The first-frame commit/on-draw signal remains, but the worker waits only **500 ms max**. If presentation is not confirmed, the card is hidden/abandoned and cleanup proceeds. This fail-open bound exists specifically so presentation cannot recreate v0.5.3's multi-second delay.

Physical acceptance still belongs to Ryan. Expected success order is notice presented first -> cleanup/freeze underneath -> notice disappears. Machine checks prove only the implementation, API/safety contracts, build/signing and nonvisual launch behavior, not what the TV physically shows.

## CLEAN START mechanism and safety retained

`STOP + VERIFY NOW` uses current-user ADB `am force-stop` for one validated selected package, then verifies no matching processes remain, Android stopped state is true, and the package remains enabled. CLEAN START targets are a private reviewed list.

AUTO CLEAN START is opt-in and bounded. Boot cleanup uses only the already-trusted loopback ADB key through `withTrustedAdb`, never a fresh RSA approval. Attempts remain ~30/60/120 seconds after boot, max 3; no periodic/resident cleaner. Current resumed app is skipped. Background-only playback is not separately detected. CLEAN START is post-boot cleanup, not guaranteed pre-execution interception; deliberate manual launch releases stopped state.

Only eligible non-system user apps are targets. Preserve BOOP/Android/NVIDIA/Google-core/system exclusions. Do not broaden to blanket system killing just to maximize free RAM. HARD BLOCK remains separate and explicit. Preserve old StartupLedger undo records.

No `pm clear`, uninstall, cache/login/data deletion, broad kill-all, rooting, device-owner/bootloader work, third-party re-signing, overclocking or fake RAM score. Keep local ADB loopback-only and its private key in `noBackupFilesDir`.

Keep physically proven brightness behavior, APPS direct launch, labels, Cancel/Back and other accepted controls. Display & Sound and Accessibility remain parked.

## Testing and latest receipt

Ryan owns all real-device visuals and remote acceptance. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment or source-string visual certification. Allowed gates are functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

Latest candidate: v0.5.4/code 11, source `5f50ac028eacbe64b1578466a535cef73f10b957`, run `34224066450`, job `102053781796`, success; **68 JVM tests, source/API/security contracts passed, lint 0 errors/24 warnings**. Signed artifact `10055037532`, ZIP SHA-256 `3b45611dfc5f9fd2795b66e55db7be73e2a369e48b9287f035b969452a2e0f56`; tests artifact `10055084368`, SHA-256 `c00df4b9b7a1dccb4091de41c4342b6e627c3cde74c9a73788320293aef45e55`. APK `Shield-Turbo-v0.5.4.apk`, 2319510 bytes, SHA-256 `f3a685845a0e0a230ef81ae52935e15afb4db94d81197907c84ee4a2d12c476d`. Permanent signer as above. Nonvisual install/cold/warm launch/no-fatal smoke passed. No visual tests ran.

v0.5.4 TDD: RED `29829ac2ff6232b50ccc8a5765cdbe1380c38100` left all 68 JVM tests green and failed the new boot-safe overlay/fail-open contract. GREEN implementation `afcebf98a330d1a25245324c5b39b576b8d5678e` passed unit/source/lint/build/signer and nonvisual launch gates. Final stamped release source is `5f50ac028eacbe64b1578466a535cef73f10b957`.

Historical v0.5.3/v0.5.2/v0.5.1 indicator receipts and older startup work remain in Git history; do not repoint old checkpoints.
