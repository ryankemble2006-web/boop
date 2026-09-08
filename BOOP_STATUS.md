# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer presentation: 0.9 floating square icons
- 0.9.4 HOME geometry/chrome: physically good; focus pop slightly too subtle
- 0.9.5 HOME focus-pop baseline: CI/signer green, physical verdict still pending
- Current feature candidate: version 15 / `0.10.0-now-playing`
- 0.10 build head: `f06cee260c98b2b03ddaa67ed19e505078bf3ac1`
- 0.10 workflow: `34277141969` SUCCESS
- 0.10 artifact ID: `10076198619`
- 0.10 APK SHA-256: `69cdf3d136c004c2cfb7ad377f8533a2992cb383eeb82b7926c8284e1985cc88`
- 0.10 artifact ZIP SHA-256: `658e3d61a8d944bfbe7815221e1d9a3137aaf1a4450a9b57ca43b5f032a656f6`
- Permanent BOOP signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`
- 0.10 is CI/signer green only; real-Shield acceptance is pending Ryan

## Physical state

Core mechanism physically green from 0.8:

- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override remains ON across reboot;
- stock Android TV Home does not reclaim the visible HOME surface;
- banners and grab/reorder work.

Visual state:

- 0.9 Apps drawer: **physically accepted** by Ryan ("app drawer great"). Preserve it exactly.
- 0.9.1 HOME: **physical FAIL**, spacing looked wider.
- 0.9.2 HOME: **physical FAIL**, focused card overlapped neighbours.
- 0.9.3 HOME: **physical FAIL**, black focus plate/whole-card emphasis rejected.
- 0.9.4 HOME: **physically good geometry/chrome**. Ryan: **"awesome.. make them pop out a few more pixels when highlighted. almost perfect"**.
- 0.9.5 retains 0.9.4 geometry/chrome and raises focused banner artwork scale from `1.03` to `1.05`; its final physical focus-pop verdict is still pending.
- 0.10 is built on that launcher baseline and adds Now Playing + the headphones BOOP layer; no automated test claims visual acceptance.

## 0.10 Now Playing candidate

Implemented generic Android MediaSession support, not a Deezer-only path:

- Notification Listener special access is used only as Android's supported authority to query active media sessions.
- Notification posted/removed payloads are intentionally ignored; BOOP does not read, store or cancel notifications.
- Automatic player selection prefers an eligible playing session, otherwise retains/uses eligible paused media.
- Launcher Settings can save a preferred installed launchable app; if it has no eligible session BOOP falls back to Automatic.
- HOME shows a Now Playing panel above Favourite apps, with player-supplied artwork, title/subtitle, state, progress and supported Previous/Rewind/Play-Pause/Fast-forward/Next actions.
- Media callbacks update the Now Playing subview directly rather than rebuilding Favourite apps.
- Selecting the media identity can reopen the source app.
- Artwork uses MediaSession bitmaps first and local `content://`, `file://` or `android.resource://` metadata only; there is no network artwork fetch.
- The approved existing headphones BOOP asset is packaged unchanged (Git blob `b2112ec156668cc165747d8778a8e564e268b184`).
- Headphones BOOP is a launcher-owned, transparent, non-focusable/non-clickable sibling layer: hidden with no media, resting when paused, gently grooving when playing, and hidden/stopped on Apps/Launcher Settings while retaining media state for return to HOME.
- User-facing `Home rows` is now `Launcher Settings`, with media-access and player-choice controls alongside the existing Home override/recovery and optional-row controls.

## Verification state

Workflow `34277141969` passed all current standalone launcher unit tests, signed assembly, exact package/version checks, HOME/Leanback entry checks, protected Accessibility service/router checks, Now Playing listener-service manifest presence, packaged `boop_headphones` resource presence, permanent signer verification and APK ZIP integrity.

This proves build/package/contract integrity only. It does not prove how the new panel or puppet looks on the real Shield and does not promote 0.10 to a physical checkpoint.

## Locked behavior

**Remove the crap, preserve Shield behavior.**

Keep unchanged:
- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- real wide Home banners;
- physically accepted floating square Apps drawer;
- no Home black focus plate;
- no normal Home favourite stars;
- Home focus enlarges artwork only, not card/label;
- grab/reorder;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- pure black launcher background.

## Next gate

Physically install/update to `0.10.0-now-playing` on the Shield and verify:

1. Existing HOME visuals still preserve the accepted 0.9.4 geometry and the 0.9.5 stronger focus pop does not overlap neighbours.
2. Launcher Settings -> Media access opens Android Notification Listener special access without ADB/root/Shizuku/laptop setup.
3. Deezer playback shows player-supplied artwork/title/artist/progress and supported transport controls; pause keeps the panel visible and stop/session destruction collapses it.
4. Repeat with Kodi or another app that publishes an Android MediaSession.
5. Player selection works with Automatic and a chosen player, with safe fallback when the chosen player has no active session.
6. Approved headphones BOOP appears independently, rests paused, grooves playing, does not steal D-pad focus, and disappears on Apps/Launcher Settings.
7. Recheck favourite grab/reorder, Apps drawer, single Home, double Home -> native Recent Apps, and reboot takeover.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
