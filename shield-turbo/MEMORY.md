# SHIELD TURBO durable decisions

Updated 2026-09-08. `SESSION_HANDOFF.md` owns exact current evidence/receipts; `STATUS.md` is the concise view. Fresh physical evidence overrides old pending-test notes.

## Identity and continuity

Independent utility `shield-turbo/`, branch `shield-turbo-v01`, package `com.boop.shieldturbo`; not unified BOOP. Check live Turbo and `main` heads before publication, preserve concurrent work, never force-push, and verify remote HEAD afterward. GitHub publication is not Windows synchronization or Shield deployment.

Use only the established secret-backed `boop-dev` signer. Certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Physical CLEAN START decision

Ryan has four Kodi forks he does not want lingering after boot. The old v0.4 app-op mechanism failed; v0.5.0 replaced it with real force-stop/read-back CLEAN START.

The CLEAN START core is physically positive from earlier real-Shield tests. Stale Recents/task-manager cards may persist after force-stop, while target apps are not loaded and reload only when focused. Cards are history; deliberate manual launch releases stopped state. Do not change the accepted stop/read-back mechanism to solve presentation.

## Static startup indicator lock

Ryan requires visible feedback during CLEAN START and **no movement whatsoever**. Static text remains `SHIELD TURBO · CLEAN START` and `Tidying startup apps`. It must remain non-focusable and non-touchable. No spinner, pulse, fade, slide, countdown, moving dots, animation, repeated layout animation or focus effects. If presentation is unavailable, cleanup continues normally.

Physical history:
- v0.5.1 flashed only at the end;
- v0.5.2 showed nothing;
- v0.5.3 showed nothing and caused almost eight seconds total completion time;
- v0.5.4 still showed nothing but restored fast navigation with the 500ms fail-open;
- v0.5.5 still showed nothing and reported `permission=yes`, `DISPLAY_WINDOW_CONTEXT`, `ADDED`, `DRAWN`, about 54ms;
- v0.5.6 kept navigation quick, caused only a microsecond-looking Home refresh, showed no card, and reported `FRAME_COMMITTED` in about 103ms.

## Durable interpretation of v0.5.6

`FRAME_COMMITTED` is **not physical visual acceptance**. Android's API contract says the content has been rendered into a frame and submitted to the swap chain, but that frame may not currently be visible on the display. Android also explicitly reserves the ability to change `TYPE_APPLICATION_OVERLAY` position, size or visibility.

Therefore:
- permission/add/render timing is no longer the useful problem boundary;
- the small boot-time `WRAP_CONTENT` application-overlay window is physically unreliable on Ryan's Shield;
- do not add another sleep, preroll, timeout extension, draw callback or commit callback tweak;
- the next experiment belongs at window-surface geometry/visibility.

The physically proven in-app reference is `BrightnessService`, which uses a `MATCH_PARENT x MATCH_PARENT` `TYPE_APPLICATION_OVERLAY` with `FLAG_LAYOUT_NO_LIMITS` and is visibly effective on the Shield.

If startup notice work continues, the next architecture is therefore locked as a **brightness-style transparent full-screen host** containing the same static card at top-centre. Change geometry only first. Preserve non-touch/non-focus, no movement, the 500ms fail-open and all CLEAN START/ADB behavior. This is evidence-driven architecture testing, not another timing guess.

## CLEAN START mechanism and safety retained

`STOP + VERIFY NOW` uses current-user ADB `am force-stop` for one validated selected package, then verifies no matching processes remain, Android stopped state is true, and the package remains enabled. CLEAN START targets are a private reviewed list.

AUTO CLEAN START is opt-in and bounded. Boot cleanup uses only the already-trusted loopback ADB key through `withTrustedAdb`, never a fresh RSA approval. Attempts remain ~30/60/120 seconds after boot, max 3; no periodic/resident cleaner. Current resumed app is skipped. Background-only playback is not separately detected. Deliberate manual launch releases stopped state.

Failed trusted boot ADB should retain the actual bounded exception class/message in CLEAN START item detail and expose it as `LAST CLEAN START DETAIL:`. Retry scheduling itself remains unchanged.

Only eligible non-system user apps are targets. Preserve BOOP/Android/NVIDIA/Google-core/system exclusions. HARD BLOCK remains separate and explicit. Preserve old StartupLedger undo records.

No `pm clear`, uninstall, cache/login/data deletion, broad kill-all, rooting, device-owner/bootloader work, third-party re-signing, overclocking or fake RAM score. Keep local ADB loopback-only and its private key in `noBackupFilesDir`.

Keep physically proven brightness behavior, APPS direct launch, labels, Cancel/Back and other accepted controls. Display & Sound and Accessibility remain parked.

## Testing boundary and latest receipt

Ryan owns all real-device visuals and remote acceptance. Never add GitHub screenshots, UI hierarchy dumps, golden/image/layout/focus/appearance/motion judgment or source-string visual certification. Allowed gates are functional/API/protocol/security tests, compilation, lint, signer/package/archive integrity and basic nonvisual crash smoke.

Latest candidate: **v0.5.6/code 13**, source `5870742c83b193251b323a48e12b0ef6c5b8b5ad`, run `34230235524`, job `102074235862`, success: **68 JVM tests, source/API/security contracts passed, lint 0 errors/24 warnings**. Signed artifact `10057548249`, ZIP SHA-256 `1c601a47fd94d002f8a4e5d5722444dc81f256057fe444cd86e16223d560a4c3`; tests artifact `10057598695`, SHA-256 `ed2d1f902e577a538c5fe8d041c79fc4c30f75930eb7780fcc0c2902a03e9c4a`. APK `Shield-Turbo-v0.5.6.apk`, 2330594 bytes, SHA-256 `e462db094cf3f09fa4815949b492ed85f0fa12a57a53635951ce875c8c48cd78`. Permanent signer as above. Nonvisual install/cold/warm launch/no-fatal smoke passed. No visual tests ran.

TDD/evidence chain: RED `53ef056100e63e04787d0b4d6dfe251dd7443dec` (run `34228754199`, job `102069273962`); attach-gated frame implementation `57247a23908d755d37683e7bf3d2684ded656198`; combined GREEN `d290042b650e63dc54d251118ce022fbe8c21637` (run `34229523488`, job `102071858015`, success); final atomic release stamp `5870742c83b193251b323a48e12b0ef6c5b8b5ad`.

Historical v0.5.5/v0.5.4/v0.5.3/v0.5.2/v0.5.1 receipts remain in Git; never repoint historical checkpoints.

A temporary preparation branch `shield-turbo-v01-stamp-temp` points to the already-green `d290042...` implementation. It is not canonical. Delete it later only with a normal safe branch deletion if/when tooling permits; never move the live Turbo branch to clean it up.

## Next safe step

No further small-window/timing change. If Ryan continues notice work, use TDD first for the brightness-style transparent full-screen host. Keep CLEAN START core untouched and require real Shield acceptance.
