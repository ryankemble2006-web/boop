# BOOP v192 Home assistant corner handoff — 2026-09-16

## Status

Source of truth is live GitHub branch `boop-hand-colour-v191`. Accepted v191 remains the rollback checkpoint. The Home-presence change is intentionally versioned as v192 so the physically accepted v191 APK is not silently redefined.

Built code commit: `d9f7a94e11c2e0f0da1d6048ef94e0341f8e75bc`.
GitHub Actions run: `35061033124` (`Build BOOP v192 home assistant corner`) completed successfully.
Artifact: `BOOP-Unified-v192-Home-Assistant-Corner`.
Artifact digest: `sha256:a02f54a8df2e9250492b3bf6bb7121af822f4fec9d68592b948757c2fb2c4209`.

No Shield install, reinstall, permission change, or device-state mutation was performed in this work. Ryan still owns the physical visual verdict and exact corner/scale tuning.

## Requested behaviour

Ryan reported that **Close media** already performs its media shutdown correctly, but the BOOP associated with media should not remain in its old media position. After media is gone, BOOP should remain visible on the Shield Home screen as a reminder that BOOP is an assistant, positioned at the top right.

The Close media mechanism itself was deliberately left unchanged.

## Implementation

`unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java` now hosts one idle canonical BOOP at the far right of the Home navigation/header row, after Shield Settings.

The Home assistant presence reuses `ShieldNowPlayingPuppetView`; no new puppet renderer or artwork was introduced. It uses:

- presentation owner `BoopState.Owner.NONE`, matching the no-media state;
- a synthetic `PlaybackState.STATE_BUFFERING` snapshot, which maps through the existing policy to the canonical REST / `idle` animation;
- `setHomeVisible(!visible)` so the idle Home presence is hidden whenever the Now Playing surface is active.

The existing media puppet continues to use `HOME_NOW_PLAYING` ownership during eligible media states. This preserves one visible canonical BOOP rather than two competing faces.

Current Home assistant layout request in source is `135dp x 90dp`, rightmost in the nav row with a `12dp` left margin. This is a starting physical placement, not a claimed visual acceptance result. Tune only after Ryan views it on the actual Shield.

## TDD / verification

A focused RED test was first added to `tests/test_now_playing_bay_ownership.py`; run `35060491690` failed specifically because the Home idle assistant host did not yet exist.

After implementation, the v192 run passed the Home ownership/materialization checks along with the existing v191 preservation suite, Android compilation, permanent signing, APK identity checks, and artifact upload.

An existing v169 historical production-change guard initially rejected the intentional `ShieldHomeView.java` modification. Logs proved that was the only reason for that run failure. Its allowlist was updated explicitly for the v192 Home surface change, then the complete workflow was rerun successfully.

## Files intentionally changed from v191 branch checkpoint

- `unified/shield-home/src/main/java/com/boop/shieldhome/ShieldHomeView.java`
- `unified/app-build.gradle` — versionCode 192, versionName `1.2.192-home-assistant-corner`
- `tests/test_now_playing_bay_ownership.py`
- `tests/test_playback_dance_v169.py` — historical change allowlist only
- `.github/workflows/build-boop-v191-hand-colour.yml` — retained filename, now builds/verifies the v192 candidate on this branch

No accepted v191 appearance assets or hand/eye/felt rig files were changed.

## Next physical test

Install only when Ryan explicitly requests it. On the Shield:

1. Confirm normal Now Playing still shows the existing media BOOP and controls unchanged.
2. Press **Close media** and confirm media shuts down exactly as before.
3. Confirm the media BOOP disappears and the idle assistant BOOP is visible at the Home screen top right.
4. Ryan decides whether `135dp x 90dp` and the header position are visually correct; adjust only that Home presentation geometry if needed.
