# BOOP current handoff

Updated 2026-09-17. Current owner branch: `boop-wall-shield-split-v207`.

## Current Shield

Shield package: `com.boop.shieldoverlay`.
Current release line: v211 / `1.2.211-shield`.

v209 fixed Close player / Close media after the Wall-Shield package split by using the running application's package identity for the private close marker. Ryan subsequently reported the close behavior working.

v210 introduced the idle weather surface in the existing 182dp Now Playing slot. Ryan's first physical test found that the panel loaded no weather information and its background did not match Now Playing.

v211 repairs that candidate without changing the approved layout: the Shield shell now declares `android.permission.INTERNET` so the existing keyless Open-Meteo repository can actually fetch weather data, and the weather panel uses the exact Now Playing card chrome: RGB 16/16/16 fill, 14dp corner radius, and RGB 48/48/48 1dp stroke. The weather panel remains non-focusable, keeps its 30-minute refresh and bounded stale cache, yields immediately to eligible Now Playing media, and leaves the favourites row parked in its existing position.

Signed v211 build source: `3d1c62b965fbe02aee58e8d39dd6649721611fef`.
GitHub run: `35228355008` (success).
Artifact: `10500275652`, `BOOP-Shield-v211-Wall-v207-Signed`.
Target APK: `BOOP-Shield-v211.apk`, SHA-256 `cd059052f7dfe1b398b8d1aeba21f49d333d4e637b455e87776367578ea6874d`.
The signed APK passed package/version/signer checks, the packaged INTERNET-permission check, inherited v206 functional checks, split integration checks, and the frozen native/art verification. Ryan owns manual installation and visual/physical acceptance; v211 is not yet recorded as installed or visually accepted.

## Wall

Wall remains package `com.boop.alpha1`, release line v207. No Wall feature change was made by the Shield weather repair.

## Repository cleanup

The former repository workflow-rule/context stack was retired on 2026-09-17. Root AGENTS / START_HERE / CONTEXT / RULES / BUILD_ON_GITHUB files and source-preservation allowlists are no longer part of the active app workflow. Functional tests, package checks, signer checks and app-specific regression tests remain.
