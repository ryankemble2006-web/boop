# Deezer puppet verification and Shield handoff

This directory verifies the Deezer-only H1 checkpoint without granting Android
access, controlling playback, or touching a Shield. The feature is opt-in and
defaults to **Off**, including after an update to an existing installation.

## User-visible states and boundaries

The BOOP preference and Android notification-listener grant are separate:

- **Off** releases media observation immediately and restores the original eyes.
  It does not revoke Android's broader notification-access grant.
- **Access needed** means the preference is On but Android has not granted the BOOP
  listener. BOOP cannot grant access itself.
- **Connecting** means the preference and grant are present while the listener is
  waiting for Android to connect it.
- **On** means the listener is connected and eligible to observe Deezer playback.

Android's grant is broader than the feature needs. BOOP observes only media-session
state from the exact package `deezer.android.app`. Notification callbacks are no-ops:
it does not enumerate, parse, retain, or cancel notifications, and it never reads or
stores titles, artists, or notification contents. Session tokens are read and held
transiently in memory to track session identity; they are never logged or persisted.
It does not issue playback commands.

The notification-access settings activity was absent on the tested Shield. If the
access action is unavailable, Settings explains that one-time computer-assisted setup
is needed; ordinary eyes continue to work. A later grant must target the exact BOOP
component and Android user without replacing other listeners. Switching the feature
Off stops observation but deliberately leaves that separate grant unchanged.

Expected rendering states are:

| Condition | Expected result |
| --- | --- |
| Off, access missing, listener disconnected, no Deezer session, or stopped/null/unknown/error state | Original eyes; no repeating H1 frames; a later session starts fresh |
| Deezer playing | H1 headphones; gentle three-second nod cycle (1.2x preview pace) |
| Deezer paused | H1 holds its current pose |
| Buffering or connecting | H1 holds its current pose; neutral only for an initial resting session |
| Short seek/skip transition | H1 holds its pose until the next confirmed state |
| Home visible, overlay hidden/detached, or display off | No overlay animation frames |
| Authoritative Android animator-duration setting is zero or public Power Saver is active | Static H1 |
| Duration/power state cannot be read, or animator-setting/Power Saver observation cannot register | Static H1, conservatively |

The duration setting and Power Saver state are observed through supported Android
callbacks and public APIs, not a polling loop. A vendor may impose additional unexposed
animation policy, or may allow animation despite public Power Saver; those
hardware-specific cases remain a physical test boundary.

## Local candidate verification

Run commands from the repository root in PowerShell. Supply the installed Java 17,
JUnit, Hamcrest, Python runtime, Node, Gradle, Android SDK, and task-owned cache paths
for the machine under test. Keep the Python runtime and its `DLLs` directory at the
front of the process-local `PATH`; the source suite launches a copied Python child.
Do not change global configuration or print signing environment values.

```powershell
$javaHome = '<Java-17-home>'
$junitPath = '<junit-4.13.2.jar>'
$hamcrestPath = '<hamcrest-core-1.3.jar>'

& .\tools\deezer-puppet\test-core.ps1 `
  -JavacPath "$javaHome\bin\javac.exe" `
  -JavaPath "$javaHome\bin\java.exe" `
  -JunitPath $junitPath `
  -HamcrestPath $hamcrestPath
```

The audited candidate passed 58 focused pure-Java tests.

```powershell
$pythonRuntime = '<bundled-python-runtime>'
$taskWork = '<task-work-directory-containing-python3.exe>'
$env:PYTHONHOME = $pythonRuntime
$env:PATH = "$taskWork;$pythonRuntime;$pythonRuntime\DLLs;$env:PATH"
& "$pythonRuntime\python.exe" -B -m unittest discover -s tests -p 'test_*.py' -q
```

The audited candidate passed all 42 source regressions.

The following two checks depend on preserved, unpublished local-preview files under
`tools/media-motion-preview`. They validated this worktree, but they are intentionally
outside the release file set and must not be promised from a fresh checkout:

```powershell
& .\tools\media-motion-preview\test-motion.ps1 `
  -JavacPath "$javaHome\bin\javac.exe" `
  -JavaPath "$javaHome\bin\java.exe" `
  -JunitPath $junitPath `
  -HamcrestPath $hamcrestPath

& '<node.exe>' .\tools\media-motion-preview\test-preview-clock.cjs
```

The local preview checks passed seven motion/export tests, exported 90 music plus 300
cinema poses, and passed the preview-clock pause/resume and hidden-page timing check.

Use a task-owned Gradle cache and Android user home. This builds a **local debug audit
candidate only**:

```powershell
$env:JAVA_HOME = $javaHome
$env:PATH = "$javaHome\bin;$env:PATH"
$env:GRADLE_USER_HOME = '<task-owned-gradle-home>'
$env:ANDROID_HOME = '<Android-SDK>'
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
$env:ANDROID_USER_HOME = '<task-owned-debug-android-home>'
$gradle = '<Gradle-9.6.0>\bin\gradle.bat'

& $gradle -p .\shield-overlay `
  :app:testDebugUnitTest :app:assembleDebug `
  --offline --no-daemon --console=plain --rerun-tasks
& $gradle -p .\shield-overlay :app:lintDebug `
  --offline --no-daemon --console=plain --rerun-tasks
```

The audited candidate passed 153 Android unit tests in 30 XML suites and assembled.
`lintDebug` is **not green**: it exits 1 with three errors and 22 warnings. The three
errors are the pre-existing `GestureBackNavigation`, `MissingTvBanner`, and
`ImpliedTouchscreenHardware` findings. The exact-HEAD baseline has the same 25 semantic
issues; no lint suppression or unrelated fix belongs in this checkpoint.

## Packaged APK audit and trust labels

Audit the packaged APK rather than trusting source declarations:

```powershell
$boopApk = (Resolve-Path .\shield-overlay\app\build\outputs\apk\debug\app-debug.apk).Path
$boopAapt = "$env:ANDROID_HOME\build-tools\36.0.0\aapt.exe"
$boopAapt2 = "$env:ANDROID_HOME\build-tools\36.0.0\aapt2.exe"
$boopApksigner = "$env:ANDROID_HOME\build-tools\36.0.0\apksigner.bat"

& $boopAapt dump badging $boopApk
& $boopAapt dump permissions $boopApk
& $boopAapt2 dump xmltree --file AndroidManifest.xml $boopApk
& $boopApksigner verify --print-certs $boopApk
Get-FileHash -LiteralPath $boopApk -Algorithm SHA256
```

Require package `com.boop.shieldoverlay`, minimum SDK 26, target SDK 36, the existing
non-exported `BoopOverlayService`, and a non-exported
`DeezerMediaListenerService` protected by
`android.permission.BIND_NOTIFICATION_LISTENER_SERVICE` with the
`android.service.notification.NotificationListenerService` action. Audit the packaged
H1 PNG as RGBA with real transparency and compare its pixels with the approved source.

Use these trust labels:

- **NON-INSTALLABLE LOCAL DEBUG CANDIDATE** — useful only for tests and package audit.
  If its public certificate SHA-256 differs from the committed expected fingerprint,
  never install it over working BOOP and never publish it as an update.
- **SIGNED CI CANDIDATE — UNVERIFIED UNTIL AUDITED** — after reviewed source is
  committed and the approved stable-signing workflow runs, independently repeat the
  package, permission, certificate, and SHA-256 checks on the downloaded artifact.
- **APPROVED INSTALLABLE UPDATE** — only after the public certificate matches
  `shield-overlay/signing/boop-dev-cert-sha256.txt` (currently
  `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`), the
  installed package/version has been checked, a data-preserving recovery APK is
  retained, and installation is separately authorized.

Do not create a signing key, substitute a signer, push, dispatch CI, install, grant or
revoke notification access, uninstall, or clear app data as part of local verification.

## Non-destructive physical checkpoint gate

Before any authorized update, record the installed package version and signer, retain
a verified known-good stable-signed recovery APK and its hash, and agree a recovery
route that does not clear pairing, room, authentication, preferences, or app data.
Install and test one step at a time, recording the result before continuing:

1. Install only an approved correctly signed update, without clearing data. Verify
   pairing and room remain; with the feature Off, ordinary eyes, Home, and Routines
   still work.
2. After a separately agreed notification-access step, enable the feature. Start
   Deezer and verify transparent, unclipped H1 headphones nod at the approved pace;
   the remote must still control Deezer.
3. Pause, resume, and skip. Verify rest, continuation from the held phase, and no
   fallback flash or repeated restart.
4. Open BOOP Home and return. Verify hide/show recovery, then exercise a real Home
   favourite and a Routine without room, authentication, focus, or navigation regressions.
5. Stop playback/session, switch the feature Off, and restore it. Verify ordinary-eye
   fallback and a fresh recovery without re-pairing. Treat grant revocation/reconnection
   as a separate explicitly authorized test with documented cleanup.
6. Exercise display off/on, available resolution and HDR transitions, and a BOOP
   process restart. Check clipping and recovery. Observe ten minutes of playback plus
   a paused interval for stutter or runaway frame work; counters must show no repeating
   frames while paused or hidden.

None of these physical steps, the ten-minute observation, HDR coverage, or a new
checkpoint is established by the local build. Seal a uniquely named checkpoint only
after the applicable steps pass, with source commit, APK hash, public certificate,
device, permission state, test results, and accepted limitations recorded. Never move
the existing Home, Routines, or Wall checkpoint tags.
