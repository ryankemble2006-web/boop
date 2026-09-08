# SHIELD TURBO handoff

Updated 2026-09-08. Owning branch: `shield-turbo-v01`. Independent package: `com.boop.shieldturbo`.

## Current physically informed state

CLEAN START presentation and cleanup are now both evidence-backed on Ryan's real Shield.

- v0.5.7 proved the brightness-style transparent full-screen overlay host is physically visible. Ryan saw the blue/cyan static CLEAN START notice for roughly one second. Diagnostic: permission YES, `DISPLAY_WINDOW_CONTEXT`, add `ADDED`, present `FRAME_COMMITTED`, about 236ms.
- That reboot subjectively felt around ten seconds overall, so v0.5.8 added timing diagnostics only. It did not change indicator geometry, target selection, force-stop verification, trusted ADB or scheduler behavior.
- v0.5.8 physical timing result: `notice=62ms`, `adbReady=113ms`, `resumed=56ms`, `stops=332ms`, `slowest=com.fork2.app:268ms`, `total=573ms`.

Conclusion: the CLEAN START job itself is sub-second on Ryan's Shield. The earlier felt ~10s delay is outside the timed Turbo job boundary. Do not optimize or parallelize the accepted ADB/stop path to chase that perception. If the longer boot disturbance matters later, instrument Shield/launcher time outside the job boundary instead.

The full-screen notice architecture is now frozen unless fresh physical evidence shows a regression. Do not return to the old small `WRAP_CONTENT` overlay and do not add timing sleeps/prerolls/artificial dwell.

## Current candidate: v0.5.8 timing evidence build

Latest signed machine-verified source: **v0.5.8 / versionCode 15** at exact commit `ea2c290b5ca66c6a88f1967db91b741e278007b7`.

Release workflow run `34242340853`, job `102115431784`, conclusion **success**:
- JVM tests: **69 passed**, 0 failures/errors/skips;
- source/API/security contracts: **29 passed**;
- lint: **0 errors, 24 warnings**;
- signed artifact `SHIELD-TURBO`: ID `10062615402`, ZIP `768599` bytes, SHA-256 `de9d9788992c65de8e665e23d850c97d59d6a9206b9046c8ba05f65bf975c468`;
- test artifact `SHIELD-TURBO-TESTS`: ID `10062679313`, ZIP `94845` bytes, SHA-256 `1d36483400561023311e9f9823ae631ce699063117c8c26ca91063f345fd78f3`;
- delivered APK `Shield-Turbo-v0.5.8.apk`, `2337506` bytes, SHA-256 `e8d61d98fc5b4603c810246babbdb1c1937ae0942b2ea5aad0a8ce44e5fdcb66`;
- package `com.boop.shieldturbo`, versionCode 15, versionName 0.5.8, Leanback launchable;
- permanent signer certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`;
- signer DN `CN=BOOP Development,O=BOOP`;
- APK ZIP integrity passed;
- nonvisual install/cold launch/process/Back/warm launch/no-package-fatal smoke passed;
- no visual tests ran. Ryan owns physical appearance and timing acceptance.

v0.5.8 implementation checkpoint before the atomic version stamp: `89a6d4bc6e3a9878c7f8c20f2b7902740684fe12` (`feat(shield-turbo): record clean start boundary timings`). The release stamp changed only version assertions in `shield-turbo/app/build.gradle` and `.github/workflows/shield-turbo.yml`.

### v0.5.8 TDD receipts

- Initial test-only timing contract: `a35660fb2d52aacecf402d3b062cf065b943019c`.
- Round-trip store test: `d9b3798c90c6bc78655f5b7b6d914be7a748f470`.
- Corrected test setup: `1dba6db8990936812f0be1da2cc51cf0b0e2e658`. RED failed because `CleanStartTimingDiagnostic`, `recordTimingDiagnostic`, `lastTimingDiagnostic`, and the boundary timing implementation did not yet exist. Signing/build was skipped.
- GREEN timing instrumentation: `89a6d4bc6e3a9878c7f8c20f2b7902740684fe12`.
- Final atomic release stamp: `ea2c290b5ca66c6a88f1967db91b741e278007b7`.

## Accepted CLEAN START mechanism and safety boundaries

Preserve these unless Ryan explicitly changes the feature:

- `STOP + VERIFY NOW` uses current-user `am force-stop` on one validated selected package and verifies package processes are gone, Android stopped state is true, and package remains enabled.
- CLEAN START target list is private/reviewed and limited to eligible non-system user apps.
- BOOP, Android, NVIDIA, Google core/system and system/updated-system packages remain excluded.
- AUTO CLEAN START is opt-in.
- Non-exported boot receiver plus one-shot JobService only when enabled and targets exist.
- Boot attempts remain roughly 30s, 60s and 120s, maximum three. No periodic job, resident cleaner, foreground service or indefinite retry.
- Boot cleanup uses trusted loopback ADB only and cannot trigger a fresh RSA approval.
- ADB key remains private in `noBackupFilesDir`.
- Current resumed app is skipped. Background-only playback is not independently detected.
- Manual launch releases stopped state.
- HARD BLOCK stays separate and explicit.
- Old StartupLedger undo records remain preserved.
- No root, device owner, bootloader changes, third-party re-signing, uninstall, `pm clear`, cache/login/data deletion, broad kill-all, overclocking or fake RAM scores.

Keep the physically proven 10-100% brightness behavior unchanged. Keep APPS direct launch, labels, Cancel/Back and remote-first UI. Display & Sound and Accessibility remain parked.

## Static CLEAN START notice lock

Exact text:
- `SHIELD TURBO · CLEAN START`
- `Tidying startup apps`

Required behavior:
- top-centre;
- static;
- non-focusable;
- non-touchable;
- no spinner, pulse, fade, slide, countdown, moving dots, progress animation, repeated layout animation, focus effect or movement;
- presentation failure fails open into cleanup.

Accepted host geometry from v0.5.7:
- transparent `FrameLayout`;
- `MATCH_PARENT x MATCH_PARENT`;
- `TYPE_APPLICATION_OVERLAY`;
- `FLAG_NOT_FOCUSABLE`, `FLAG_NOT_TOUCHABLE`, `FLAG_LAYOUT_IN_SCREEN`, `FLAG_LAYOUT_NO_LIMITS`, `FLAG_HARDWARE_ACCELERATED`;
- static card as a top-centre child;
- attach-gated Android 10+ frame-commit tracking;
- 500ms maximum presentation wait/fail-open.

## Presentation history

- v0.5.1: card flashed only at the end.
- v0.5.2: fixed 500ms preroll, no visible card.
- v0.5.3: no card and nearly eight seconds, rejected.
- v0.5.4: no card, navigation quickly restored.
- v0.5.5: invisible; diagnostic `DRAWN` ~54ms exposed false-positive draw evidence.
- v0.5.6: invisible; `FRAME_COMMITTED` ~103ms; small-window architecture rejected.
- v0.5.7: full-screen brightness-style host physically visible for about one second; `FRAME_COMMITTED` ~236ms.
- v0.5.8: same presentation architecture plus local boundary timings; physical job total **573ms**, proving Turbo cleanup itself is not a multi-second delay source.

## Next safe step

No CLEAN START speed fix is justified by current evidence. Freeze the accepted presentation and cleanup mechanism. Continue with other Turbo features, or if Ryan specifically wants to investigate the longer perceived reboot disturbance, gather timing evidence outside `CleanStartJobService` before changing code.

The branch briefly received an accidental one-word placeholder file while preparing this documentation. It was immediately deleted in normal history; commit `c777ea2f14b7b7cd26a9f397e25d29fcba07a052` restores the exact v0.5.8 release tree SHA `1c3320acc6921fb1140a8c5997f284f06444f24f`. No app code or private data was involved and no history was force-rewritten.

`main` remains separate and unchanged by Turbo work.
