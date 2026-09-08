# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

This remains a standalone Nvidia Shield clean-HOME launcher for physical testing. It is not part of unified/AIO yet.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Android TV Home remains installed/enabled as recovery and as the Accessibility override trigger.
- Normal users must not need ADB, developer options, laptop, root or Shizuku.
- Merge into AIO only after Ryan explicitly approves the standalone result.

## Protected physically-green HOME mechanism: 0.8

Build head `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`, version 8 / `0.8.0-reboot-rearm`, workflow `34239594403` SUCCESS, artifact `10061456035`, APK SHA-256 `7088b4be9dca7cb47bd67c740aaea940d71fd2471da623fb3f0c9fe23d5b2ff0`.

Physically confirmed on real Shield:

- banners good;
- grab/reorder works;
- BOOP Home Override can be enabled manually in Shield Accessibility settings;
- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override remains ON across reboot;
- stock Android TV Home does not reclaim the visible HOME surface after reboot.

Treat 0.8's Accessibility override, `onServiceConnected()` reboot re-arm and untouched native double-Home behavior as protected. Visual/media work must not modify that mechanism.

## Floating-card visual baseline

### 0.9.0 floating cards

Build head `79919976adebf5f989a0efd86bef525b6273ed44`, workflow `34244270100` SUCCESS, artifact `10063361724`, APK SHA-256 `7abd913b51329a3c2cef556fa853bfbb8a78007b046d131a12b639daa9bd589b`.

Physical result: Apps drawer **physically accepted** by Ryan ("app drawer great"). Preserve its floating square-icon presentation exactly.

### 0.9.1 / 0.9.2 / 0.9.3 HOME attempts

- 0.9.1 `2ddeed52674eb1c8c82025fc43ededdc82c4f12f`: physical FAIL, spacing looked wider.
- 0.9.2 `85054f38ca584ba200308db2448441633a9347ec`: physical FAIL, focus overlapped neighbours.
- 0.9.3 `99900d761a8dc11c7c17d6989898aee7e9582796`: physical FAIL, black focus plate/whole-card emphasis rejected.

### 0.9.4 HOME geometry/chrome: physically good

Build head `a8a207c97ddddc9b8b36ef99fa7b8718d91588c0`, version 13 / `0.9.4-home-artwork-focus`, workflow `34253939244` SUCCESS, artifact `10067129563`, APK SHA-256 `8840c68a834e7c65b8473631dca5c0e05929a3f931621c4bc232a96155e17909`.

Ryan: **"awesome.. make them pop out a few more pixels when highlighted. almost perfect"**.

Preserve:
- fixed 240 dp Home card lane;
- 230 dp installed banner artwork base size;
- 6 dp inter-card margin;
- fixed labels/positions;
- no Home black focus plate;
- no normal Home favourite stars;
- focus changes artwork only, not the whole card/label;
- Apps drawer remains its accepted 0.9 presentation.

### 0.9.5 stronger Home focus pop

Build head `66a15f89969c547224ed22d962f609243effae6e`, version 14 / `0.9.5-home-focus-pop`, workflow `34255507581` SUCCESS, artifact `10067748314`, APK SHA-256 `e42d2f9c85244a52ec3124dd9d68d6cac6bcf56878d122c7743f7e9ea942d00e`, artifact ZIP SHA-256 `556f9aedb15ef4ad3b7a4effc8ce5ecbc4465b36588eeaf1de1b08d2272bc36b`.

0.9.5 changes only focused HOME banner artwork scale from `1.03` to `1.05`; grabbed artwork remains `1.03`. It is CI/signer green; Ryan's final physical focus-pop verdict has not yet been recorded. 0.10 inherits this launcher visual baseline.

## Current candidate: 0.10.0 Now Playing + headphones BOOP

Release identity and receipt:

- Version: 15 / `0.10.0-now-playing`
- Build head: `f06cee260c98b2b03ddaa67ed19e505078bf3ac1`
- Workflow: `34277141969` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10076198619`
- APK SHA-256: `69cdf3d136c004c2cfb7ad377f8533a2992cb383eeb82b7926c8284e1985cc88`
- Artifact ZIP SHA-256: `658e3d61a8d944bfbe7815221e1d9a3137aaf1a4450a9b57ca43b5f032a656f6`
- Permanent BOOP signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

### Implemented behavior

Generic Android MediaSession integration, not Deezer-specific:

- Notification Listener special access is used only as Android's supported authority to query active media sessions.
- `ShieldNowPlayingListenerService` intentionally ignores notification posted/removed payloads. BOOP does not read, store or cancel notifications.
- Automatic session selection prefers an eligible playing session, otherwise retains/uses an eligible paused session.
- A saved preferred installed launchable app wins when it has an eligible session; otherwise selection falls back safely to Automatic.
- Stopped/destroyed/inactive sessions do not keep stale Now Playing visible; paused media remains visible.
- HOME renders Now Playing above Favourite apps and pushes favourites down rather than covering them.
- Player-supplied artwork, title, subtitle, playback state, progress/duration and advertised transport actions are shown by the dedicated media subview.
- Controls are Previous, Rewind, Play/Pause, Fast-forward and Next and are enabled only when the MediaSession advertises the corresponding action.
- Media identity can reopen the source package through Android's normal launch intent.
- Progress advances locally while playing; media callbacks update only the Now Playing subview, not the Favourite apps row.
- Artwork preference is MediaSession `ART`, `ALBUM_ART`, `DISPLAY_ICON`, then local `content://`, `file://` or `android.resource://` URI metadata. There is no network artwork fetch.

Launcher Settings:

- user-facing `Home rows` is renamed `Launcher Settings`;
- Now Playing section exposes `Media access: ON/OFF` and routes to Android Notification Listener settings;
- player selection offers Automatic plus installed launchable apps;
- existing BOOP Home Override, stock Home recovery/info and optional-row controls remain present.

Independent headphones BOOP layer:

- exact existing approved `boop_headphones.png` is reused unchanged; repository blob `b2112ec156668cc165747d8778a8e564e268b184`;
- transparent launcher-owned sibling view, not a system overlay;
- non-focusable and non-clickable, so it cannot steal D-pad focus/clicks;
- hidden with no eligible media;
- visible at rest when paused;
- gentle groove while playing;
- brief acknowledgement on track/session change;
- hidden and animation-stopped on Apps/Launcher Settings while retaining the media snapshot so returning HOME resumes correctly;
- respects platform animator/power-save policy and does not change Android global animation scales.

### Verification

Workflow `34277141969` passed:

- complete standalone `com.boop.shieldhome.*` unit-test suite;
- signed standalone assembly;
- exact package `com.boop.shieldhome`;
- exact versionCode 15 / versionName `0.10.0-now-playing`;
- HOME and Leanback entry presence;
- protected Accessibility service/router presence;
- Now Playing Notification Listener service presence;
- packaged `boop_headphones` resource presence;
- permanent BOOP signer match;
- APK ZIP integrity;
- artifact upload.

The downloaded artifact was independently unpacked after CI and its APK SHA-256 matched the workflow receipt exactly: `69cdf3d136c004c2cfb7ad377f8533a2992cb383eeb82b7926c8284e1985cc88`.

**0.10 is CI/signer/package green only. It is not physically accepted until Ryan tests the real Shield.**

## Background / Ambient Mode boundary

The launcher background remains pure black. Shield/Google Ambient Mode remains a separate idle/screensaver layer and was not changed by 0.10.

## Setup reality

Known working HOME override setup on this Shield firmware:

**Shield Settings -> Accessibility -> Services -> BOOP Home Override -> ON**

0.10 adds a separate one-time Launcher Settings -> Media access route for Android Notification Listener special access. This is not the Home override and does not require ADB/root/Shizuku.

## LOCKED Shield contract

**Remove the crap, preserve Shield behavior.**

Keep:

- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- real wide Home banners and grab/reorder;
- physically accepted floating square Apps drawer from 0.9;
- Home has no black focus plate and no normal favourite stars;
- Home focus enlarges artwork only, never the whole card/label;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- no ad, Shop, Discover or sponsored provider.

## Next physical test

Install/update to `0.10.0-now-playing` and test on the real Shield:

1. Confirm HOME still has the accepted 0.9.4 geometry; judge the inherited 0.9.5 5% focus pop and verify it does not touch/cover adjacent banners.
2. Open Launcher Settings -> Media access and grant BOOP Now Playing Notification Listener special access through Android settings.
3. Play Deezer, return HOME and verify artwork/title/artist/progress plus only the transport controls that Deezer advertises.
4. Pause: panel and headphones BOOP should remain visible at rest. Resume: puppet should groove. Change track/session: brief acknowledgement. Stop/destroy the session: panel and puppet should collapse.
5. Repeat with Kodi or another app publishing a usable Android MediaSession.
6. Test Player selection: Automatic plus a chosen app; chosen app should win when eligible and fall back safely when absent.
7. Select media identity/artwork and verify the source app reopens.
8. Open Apps and Launcher Settings while media is active: headphones BOOP must disappear and must not steal focus; returning HOME should restore the current media state.
9. Recheck favourite grab/reorder and the physically accepted Apps drawer.
10. Recheck single Home -> BOOP, double Home -> native Recent Apps, and reboot takeover/override persistence.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
