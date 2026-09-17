# BOOP current handoff

Updated 2026-09-17. Current owner branch: `boop-wall-shield-split-v207`.

## Current Shield

Shield package: `com.boop.shieldoverlay`.
Current release line: v212 / `1.2.212-shield`.

v209 fixed Close player / Close media after the Wall-Shield package split by using the running application's package identity for the private close marker. Ryan subsequently reported the close behavior working.

v210 introduced the idle weather surface in the existing 182dp Now Playing slot. Ryan's first physical test found that the panel loaded no weather information and its background did not match Now Playing.

v211 added the missing Shield `android.permission.INTERNET` and changed the weather card to the exact Now Playing chrome: RGB 16/16/16 fill, 14dp corner radius, and RGB 48/48/48 1dp stroke. Ryan physically tested v211 on Shield and approved the colour match. His screenshot also proved the Open-Meteo fetch was working because the footer showed live wind, sunrise/sunset and update age. The main weather content was nevertheless blank.

The v211 blank-content cause was a layout bug, not setup: the three weighted current/hourly/daily columns were created with height `0`, so only the footer and dividers could render. v212 changes those three column heights to `MATCH_PARENT` while retaining their existing 3:4:3 width weights. No weather source, location, focus behaviour, hero-slot size, favourites position, Now Playing behaviour or approved card chrome changed.

Signed v212 build source: `aef9b05605b2d271d7df9f2698f8431dd42fb97e`.
GitHub run: `35229524975` (success).
Artifact: `10500159898`, `BOOP-Shield-v212-Wall-v207-Signed`.
Target APK: `BOOP-Shield-v212.apk`, SHA-256 `01cd1a53a2fc9b9eedffcc6b6601390ab204af6369dbcdd754a4eb9f1c7a3ff1`.
The signed APK passed the weather layout regression, exact chrome check, packaged INTERNET-permission check, inherited v206 functional checks, split integration checks, package/version/signer verification, and frozen native/art verification. Ryan owns manual installation and visual/physical acceptance. v212 is not yet recorded as visually accepted; v211's colour match is accepted and its network/data plumbing was physically demonstrated.

## Wall

Wall remains package `com.boop.alpha1`, release line v207. No Wall feature change was made by the Shield weather repairs.

## Repository cleanup

The former repository workflow-rule/context stack was retired on 2026-09-17. Root AGENTS / START_HERE / CONTEXT / RULES / BUILD_ON_GITHUB files and source-preservation allowlists are no longer part of the active app workflow. Functional tests, package checks, signer checks and app-specific regression tests remain.
