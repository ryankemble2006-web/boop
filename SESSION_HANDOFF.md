# BOOP current handoff

Updated 2026-09-17. Current owner branch: `boop-wall-shield-split-v207`.

## Current Shield

Shield package: `com.boop.shieldoverlay`.
Current release line: v210 / `1.2.210-shield`.

v209 fixed Close player / Close media after the Wall-Shield package split by using the running application's package identity for the private close marker. Ryan subsequently reported the close behavior working.

v210 adds the idle weather surface to the existing 182dp Now Playing slot. Weather uses Open-Meteo without an API key, refreshes on a 30-minute cadence, keeps a bounded stale cache, and yields immediately to Now Playing whenever eligible media is present. The favourites row retains its existing position and spacing.

Signed v210 build source: `a3618c613fd53e75577e8d9a608bb1b2b732e007`.
GitHub run: `35226569308`.
Artifact: `10499332037`, `BOOP-Shield-v210-Wall-v207-Signed`.
Ryan received `BOOP-Shield-v210.apk` for manual installation/testing.

## Wall

Wall remains package `com.boop.alpha1`, release line v207. No Wall feature change was made by the Shield weather work.

## Repository cleanup

The former repository workflow-rule/context stack was retired on 2026-09-17. Root AGENTS / START_HERE / CONTEXT / RULES / BUILD_ON_GITHUB files and source-preservation allowlists are no longer part of the active app workflow. Functional tests, package checks, signer checks and app-specific regression tests remain.
