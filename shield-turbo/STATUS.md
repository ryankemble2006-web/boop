# SHIELD TURBO status

Updated 2026-09-08. Branch `shield-turbo-v01`; package `com.boop.shieldturbo`.

## Physical state

CLEAN START's core result is physically confirmed on Ryan's Shield: selected Kodi forks are force-closed after boot. Recents/task-manager cards may remain from the previous session, but the apps are not loaded and reload only when focused. Normal deliberate launch still works.

Presentation remains unaccepted. v0.5.1 flashed only at the end; v0.5.2 showed nothing; v0.5.3 also showed absolutely nothing and made Turbo take almost eight seconds to finish. Treat v0.5.3 as a physical presentation/timing failure, not a cleanup failure.

Earlier physical evidence retained: bedroom brightness works; corrected STANDARD maintenance items are selectable; Developer Options opens; v0.4.1 Startup Manager menu and normal manual Kodi launch work. The old v0.4 app-op startup restriction failed. Display & Sound and Accessibility remain parked.

## Current candidate

**v0.5.4 / code 11** is signed and machine-verified. Exact built source `5f50ac028eacbe64b1578466a535cef73f10b957`.

v0.5.4 replaces the cold-boot overlay's plain application-context WindowManager path with an Android 11+ primary-display-bound window context created via `createDisplayContext(...).createWindowContext(TYPE_APPLICATION_OVERLAY, null)`. It keeps the committed-frame/on-draw presentation signal but reduces the wait from 3000 ms to **500 ms max**. If the card is not confirmed presented within that bound, it is hidden/abandoned and CLEAN START proceeds. No animation or movement was added.

CLEAN START targets, force-stop/read-back semantics, trusted ADB, current-app skip and the 30/60/120-second max-three scheduler remain unchanged.

Expected physical result: the static `SHIELD TURBO · CLEAN START` / `Tidying startup apps` card appears before cleanup and remains motionless until cleanup completes. If the Shield still rejects the card, the failed notice must no longer impose v0.5.3's multi-second extra wait. Real-device visibility and duration remain pending Ryan's reboot.

## Exact v0.5.4 verification

Run `34224066450`, job `102053781796`, conclusion **success**. 68 JVM tests passed with zero failures/errors/skips; source/API/security contracts passed; lint **0 errors / 24 warnings**; permanent signer/package/version/archive checks passed; nonvisual cold/warm launch/no-fatal smoke passed.

Signed artifact `10055037532`, ZIP `760722` bytes, SHA-256 `3b45611dfc5f9fd2795b66e55db7be73e2a369e48b9287f035b969452a2e0f56`. Test artifact `10055084368`, ZIP `93595` bytes, SHA-256 `c00df4b9b7a1dccb4091de41c4342b6e627c3cde74c9a73788320293aef45e55`.

Delivered APK `Shield-Turbo-v0.5.4.apk`, `2319510` bytes, SHA-256 `f3a685845a0e0a230ef81ae52935e15afb4db94d81197907c84ee4a2d12c476d`. Permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

Downloaded artifact receipt/digest, APK digest, built source, package/version and signer were independently checked after CI. **No GitHub visual confirmation ran.** Ryan owns real-device appearance, timing and motionlessness acceptance.
