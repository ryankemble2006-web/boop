# Shield Launcher Now Playing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add generic Android media-session Now Playing plus an independent approved headphones-BOOP puppet layer to the standalone Shield clean launcher.

**Architecture:** A notification-listener-authorized media manager observes Android `MediaController`s, selects one session through a pure policy, publishes immutable snapshots, and owns transport commands. HOME renders only the Now Playing subview from media callbacks, while a separate transparent puppet view renders the existing headphones artwork without touching launcher focus or the protected Home-override mechanism.

**Tech Stack:** Android Java 17, `MediaSessionManager`, `MediaController`, `MediaMetadata`, `PlaybackState`, `NotificationListenerService`, existing custom Android Views, JUnit 4, GitHub Actions.

**Spec:** `docs/superpowers/specs/2026-09-08-shield-launcher-now-playing-design.md`

## Global Constraints

- Branch: `boop-shield-clean-launcher`.
- Package: `com.boop.shieldhome`.
- Do not modify `com.boop.alpha1` / `boop-unified`.
- Preserve single Home -> BOOP, double Home -> native Recent Apps, reboot re-arm and Accessibility override.
- Preserve accepted Apps drawer and current HOME banner geometry/chrome.
- Reuse exact existing `boop_headphones.png`; do not regenerate/recolour.
- No ADB/root/Shizuku/laptop requirement.
- No visual/golden/screenshot acceptance automation.
- CI/signing green is not physical acceptance.

---

### Task 1: Pure media selection, action and preference contracts

**Files:**
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/NowPlayingSelectionPolicyTest.java`
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/NowPlayingActionPolicyTest.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/NowPlayingSelectionPolicy.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/NowPlayingActionPolicy.java`
- Modify/Test: `unified/shield-home/src/test/java/com/boop/shieldhome/ShieldHomeStoreTest.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeStore.java`

**Interfaces:**
- `NowPlayingSelectionPolicy.Candidate(long id, String packageName, int playbackState)`
- `static long select(List<Candidate> candidates, long currentId, String preferredPackage)`
- `static boolean eligible(int state)`
- `NowPlayingActionPolicy` exposes boolean helpers for previous/rewind/playPause/fastForward/next from action bits/current state.
- `ShieldHomeStore.nowPlayingPlayerPackage()` and `setNowPlayingPlayerPackage(String)`; empty string means Automatic.

- [ ] Write RED tests proving Automatic prefers playing, keeps paused, preferred player wins, missing preferred falls back, stopped is ineligible, action helpers match advertised bits, and player preference round-trips.
- [ ] Run the standalone launcher workflow and verify the failures are only missing Now Playing contracts.
- [ ] Implement the smallest pure policies/store methods.
- [ ] Run workflow and verify green before proceeding.

### Task 2: Snapshot bus, Android media manager and listener access

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/NowPlayingSnapshot.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/NowPlayingState.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingManager.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingListenerService.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/NowPlayingAccessSettingsPlan.java`
- Create/Test: `unified/shield-home/src/test/java/com/boop/shieldhome/NowPlayingStateTest.java`
- Create/Test: `unified/shield-home/src/test/java/com/boop/shieldhome/NowPlayingAccessSettingsPlanTest.java`
- Modify: `shield-clean-launcher/app/src/main/AndroidManifest.xml`

**Interfaces:**
- `NowPlayingState.subscribe(Listener)` returns idempotent unsubscribe `Runnable` and immediately delivers current snapshot.
- `ShieldNowPlayingManager.get(Context)` exposes `state()`, `refreshAccess()`, `openAccessSettings(Activity)`, `setPreferredPackage(String)`, transport methods, and `openSource(Activity)`.
- Listener service calls manager lifecycle methods and ignores notification posted/removed payloads.

- [ ] Write RED state/access-route tests.
- [ ] Verify RED in CI.
- [ ] Implement state bus and Notification Listener access plan based on the already-proven Shield Deezer access route.
- [ ] Implement manager session reconciliation: query active sessions with listener component; register callbacks; read metadata/playback state; select using Task 1 policy; publish only meaningful state changes.
- [ ] Artwork order: `ART`, `ALBUM_ART`, `DISPLAY_ICON`, then local content/file/android-resource URI metadata. Never network-fetch artwork.
- [ ] Implement transport calls on the currently selected controller and source-app reopening.
- [ ] Add listener service manifest entry.
- [ ] Run full launcher tests green.

### Task 3: Remote-first Now Playing panel and HOME integration

**Files:**
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingView.java`
- Modify/Test: `unified/shield-home/src/test/java/com/boop/shieldhome/ShieldLauncherViewsTest.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java`

**Interfaces:**
- Extend `ShieldHomeView.Callbacks` with media transport/source actions.
- `ShieldHomeView.render(..., NowPlayingSnapshot snapshot, Callbacks)` plus backward-compatible overload if useful.
- `ShieldHomeView.setNowPlaying(NowPlayingSnapshot)` updates only the media subview.
- `ShieldNowPlayingView.bind(snapshot, callbacks)` collapses on null/inactive.

- [ ] Write RED reflection/callback contract tests for the Now Playing view and HOME callbacks.
- [ ] Verify RED.
- [ ] Implement panel above Favourite apps with artwork, title/subtitle, state, horizontal progress and Previous/Rewind/Play-Pause/Fast-forward/Next buttons.
- [ ] Disable unsupported controls rather than sending unsupported actions.
- [ ] Update progress locally only while playing; do not rerender favourites on progress ticks.
- [ ] Subscribe Activity to manager state and forward snapshots only to the current HOME media view.
- [ ] Verify tests and signed build remain green.

### Task 4: Independent headphones BOOP puppet layer

**Files:**
- Create: `unified/shield-home/src/test/java/com/boop/shieldhome/NowPlayingPuppetPolicyTest.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/NowPlayingPuppetPolicy.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/NowPlayingPuppetMotion.java`
- Create: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldNowPlayingPuppetView.java`
- Add existing blob as: `shield-clean-launcher/app/src/main/res/drawable-nodpi/boop_headphones.png`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java`

**Interfaces:**
- `NowPlayingPuppetPolicy.Mode { HIDDEN, REST, GROOVE }`
- `mode(NowPlayingSnapshot)` returns hidden/no-media, rest/paused, groove/playing.
- Puppet view is a transparent full-screen sibling above launcher content, not focusable/clickable.
- `setSnapshot(snapshot)` handles mode and brief track/session-change acknowledgement.

- [ ] Write RED puppet-mode tests.
- [ ] Verify RED.
- [ ] Reuse the exact `boop_headphones.png` repository blob in standalone resources.
- [ ] Implement launcher-specific motion from the existing proven BOOP music groove/settle ideas without importing overlay permissions or overlay window code.
- [ ] Add puppet view to `ShieldLauncherActivity.root` above content only on HOME; hide/detach on Apps/Settings/no-media.
- [ ] Stop repeating animation when not visible or when animations/power policy disables it.
- [ ] Verify tests/build green. Visual size/placement remains Ryan's physical call.

### Task 5: Launcher Settings media setup and player preference

**Files:**
- Modify/Test: `unified/shield-home/src/test/java/com/boop/shieldhome/ShieldLauncherViewsTest.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeSettingsView.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java`
- Modify: `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldLauncherActivity.java`

**Interfaces:**
- Rename user-facing `Home rows` navigation/page title to `Launcher Settings`.
- Settings callbacks add `onOpenNowPlayingAccess()` and `onChooseNowPlayingPlayer()`.
- Player chooser offers Automatic plus installed launchable apps; saved package is fed back to manager.

- [ ] Write RED settings callback/name/access route contracts where behavior can be tested without visual assertions.
- [ ] Verify RED.
- [ ] Add Now Playing access status/action and player preference controls.
- [ ] Use the proven Notification Listener detail-settings route first, generic listener settings fallback second, and plain Shield Settings only if Android exposes neither.
- [ ] Verify existing Home override and optional-row settings remain functional.

### Task 6: Release, verification and continuity

**Files:**
- Modify: `shield-clean-launcher/app/build.gradle`
- Modify: `.github/workflows/build-shield-clean-launcher.yml`
- Modify: `SESSION_HANDOFF.md`
- Modify: `BOOP_STATUS.md`

**Release identity:**
- versionCode `15`
- versionName `0.10.0-now-playing`

- [ ] Re-fetch live standalone/main heads before release edits.
- [ ] Bump package version and workflow verifier.
- [ ] Add non-visual verifier checks for `ShieldNowPlayingListenerService`, HOME/Leanback, existing Accessibility service/router, signer, APK integrity and packaged headphones resource.
- [ ] Run exact final workflow to SUCCESS.
- [ ] Capture workflow ID, artifact ID, APK SHA-256, artifact ZIP SHA-256, signer/package/version receipt and build head.
- [ ] Download exact artifact, independently hash/check ZIP/APK, and expose the extracted APK.
- [ ] Update handoff/status: 0.8 protected mechanism, accepted Apps drawer and 0.9.4 HOME geometry remain protected; 0.10 Now Playing/puppet is CI/signer green only until Ryan tests it.
- [ ] Re-fetch standalone/main HEADs and confirm AIO/main app code remained untouched.

## Manual physical test for 0.10

After installation on Shield:

1. Enable the BOOP Now Playing Notification Listener special access through Launcher Settings.
2. Play Deezer, return Home, verify artwork/title/artist/progress and controls.
3. Pause, confirm panel + headphones BOOP remain visible at rest.
4. Resume/change track, confirm puppet groove/brief acknowledgement.
5. Stop/destroy session, confirm panel and puppet collapse.
6. Repeat with Kodi or another media-session app.
7. Verify source-app reopen, Previous/Rewind/Play-Pause/Fast-forward/Next where supported.
8. Confirm favourites/Apps look unchanged and grab/reorder still works.
9. Confirm single Home -> BOOP, double Home -> native Recent Apps, and reboot takeover still work.
