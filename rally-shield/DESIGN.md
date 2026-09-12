# BOOP Rally: Network Q collection

User request: combine the two existing portable Network Q rally games into one launcher, move them to Shield, install and test; use the BOOP GitHub repository on a separate branch.

This is an adjacent game application, not a Unified/Shield HOME change. Own branch `boop-rally-shield`, source `rally-shield/`, package `com.boop.rally`. Base shared main: af0db837bf9dde26d16c632fe75d8b5bca1f7fc0.

One offline Android TV APK contains a two-choice launcher and native ARM64 DOSBox Pure. No RetroArch app, PC streaming, WebView, account, network, broad storage or accessibility permission. The original DOS executables run under emulation; this is not a native-source rewrite of the games.

Build and sign on GitHub using the existing BOOP development signer. Pin DOSBox Pure 1.0-preview6 at a4a0bab7f8931433588f2fcad9045c85b277373d and retain its GPL-2.0-or-later notices and complete corresponding-source build instructions.

Proprietary game files never enter the public repository, CI or artifacts. A local preparation script copies the existing accepted desktop game folders into deterministic bundles. The APK reads them from its own external files directory after ADB provisioning, or from user-selected ZIP imports. Preserve the desktop originals and separate per-game save directories. Reject unsafe paths and unexpected bundle launch configuration.

Keep RAC Rally at its existing 12000-cycle setting. Championship uses the accepted Browser-Test copy, 16 MB RAM, SB16 at 220/7/1/5, dynamic CPU and max cycles. Initial output pacing is 30 FPS; this does not guarantee simulation speed or hardware performance.

TV D-pad selects games. During gameplay D-pad/stick steers and supplies arrow keys, triggers accelerate/brake, A/OK selects, B supplies DOS Escape, X supplies Space. Back/Start opens an application pause/menu with Resume, DOS Escape, DOS Enter, keyboard help and Return to collection. Never trap Android Home. Release all held keys and silence/pause emulation on focus loss.

A separate game process owns one native core, one emulation thread, surface presentation and low-latency audio. Return requests cooperative unload/save, then finishes the game process; the launcher and unrelated apps are untouched. No native work or joins block the UI thread.

Verify with behavioral tests, source build, manifest/signer/package checks and actual Shield installation. Inspect both real games, attempt a race in each, test input/menu/return and reopening, inspect scoped crash logs. Agent-observed device behavior is not user visual/acoustic acceptance. No GitHub screenshot/golden/appearance tests.
