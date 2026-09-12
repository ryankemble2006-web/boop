# BOOP Rally Shield handoff

Updated 2026-09-12. Adjacent game project, not Unified or HOME work.
Owning branch/worktree: `boop-rally-shield` / `.worktrees/boop-rally-shield`.
Project: `rally-shield/`. Package: `com.boop.rally`. App label: BOOP Rally.
Base: main `af0db837bf9dde26d16c632fe75d8b5bca1f7fc0`.

## User request and boundaries

Combine the two existing portable Network Q rally games in one Shield launcher, build through GitHub, install and test. Preserve all original game folders, concurrent work, Unified, standalone launcher and other installed packages. No HOME, permission, system audio or screensaver setting changes are part of this task.

RAC Rally uses the original portable RALLY directory. Championship uses the accepted Browser-Test directory, NOT its older RALLY/Installed copies. The native ARM64 DOSBox Pure core is pinned to 1.0-preview6 at `a4a0bab7f8931433588f2fcad9045c85b277373d`. No separate emulator application or PC streaming is required.

Game files and private receipts remain local. Public GitHub source and APK contain only the launcher/host/emulator and permitted notices. Private prepared packs were installed separately. Use the existing permanent BOOP signing workflow, never export or replace its key.

## Installed candidate

Source `71c71157fd9b6931b3c7d320409fa32d94f72197`, version 1 / 1.0.
Successful Actions run `34688081241`; artifact `10296216335` / BOOP-Rally-Shield.
APK SHA-256 `cb3b37efd9dfe4b5ea1850c4fe3ff9155e3c319ecefa1855ee1ae6f408e192d8`.
Certificate SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
APK size 1,602,630 bytes. Positively identified Shield installation succeeded.

Desktop output folder: `Network Q Rally Shield`. It contains `release/BOOP-Rally.apk`, corresponding GPL source and receipts, `games/rac93.zip`, `games/rac96.zip`, the preparation receipt, vendor source and private Evidence. Both installed pack hashes matched their local receipts; preparation verified the originals unchanged.

## What is actually verified

12 behavioral tests passed, including 22 controls assertions and native input/audio queues. Android compilation, permanent signature, APK/package/content checks passed. Tests rerun successfully from the downloaded corresponding-source artifact. Read `rally-shield/VERIFICATION.md` for the precise scope.

Launcher opened. Both original games booted with advancing frames and nonzero PCM output. Championship keyboard/options/back navigation responded. Configured output is 30 Hz, not a demonstrated race-performance guarantee. User acoustic/visual acceptance is not inferred.

## Remaining device work

Actual driving in BOTH games, physical controller steering/acceleration/braking, audible sound, in-game save then clean return/reopen, repeated game switching and background/resume still require verification. Do not mark the request fully tested. Inspect shutdown/relaunch ownership under repeated launches; this was identified during source review but not resolved by a hardware test.

The separate installed package `local.networkq.rally` has different UI and is NOT this build. It was sometimes foreground, as was Johnny Castaway. Leave both untouched. Check resumed package before and after every device test action. MainActivity task restoration can reopen a previous game; a requested launch extra alone is not evidence of the selected game. Select through the visible collection for controlled tests.

## Resume without repeating setup

Remote Desktop Commander file/command requests and ping stopped answering late in testing. GitHub remained accessible. Final documentation is being committed directly through the GitHub connector, so the local worktree must fetch/check live `origin/boop-rally-shield` and be fast-forwarded only if clean before further work. Do not call it synchronized merely because earlier code was pushed.

Recheck connection, device identity and installed APK hash. Resume from the existing installed candidate and packs, not a fresh port or game search. The source archive rebuild caveat and exact unfinished tests are in VERIFICATION.md. No app code changed after the installed source commit. Documentation-only commits may trigger another CI run; they do not imply another APK was installed.
