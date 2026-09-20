# BOOP current handoff

Updated 2026-09-20. Owner branch: `boop-wall-shield-split-v207`. Current Shield implementation branch: `boop-shield-weather-focus-v221`.

## Current Shield: v221, signed and ready for Ryan's physical test

Package `com.boop.shieldoverlay`, version `221` / `1.2.221-shield`.

v219's Now Playing alignment remains physically accepted as perfect and is unchanged. v220's user-selectable highlight colour remains unchanged.

### v221 weather alignment

Forecast rows now use the weather glyph's accepted optical centre as the anchor. Time/day labels, temperature/high-low and rain percentage receive the same +2dp optical correction as the weather glyph, so each stack is centred on the icon point rather than merely the weighted cell.

The 3-day forecast region is shifted +9dp inside its 3/10 card region. This compensates for the card's 18dp outer inset so the visible empty space from the middle divider to the first forecast and from the last forecast to the outer border is balanced.

No Now Playing, HA control, accent-colour, voice/audio or assistant geometry was changed.

### v221 return-focus behaviour

Whenever the Shield launcher regains focus from an external app, HOME requests Favourite entry 1. This covers ordinary Back returns and returns through Shield's task manager. HOME intents also rebuild with first-favourite focus.

The launcher Close media action now returns focus to Favourite entry 1 after issuing the existing close-media command. Existing internal short-Back behaviour retains the same first-favourite destination.

## Verified signed artifact

Production/build source: `9bf329a24631310261843b654f9fe0716a59aa2e`.
Successful GitHub Actions run `35515284079`, job `106090068972`.
Artifact `10606343049`: `BOOP-Shield-v221-Wall-v207-Signed`.

Deliver **BOOP-Shield-v221.apk**, 160485741 bytes.
Shield APK SHA-256: `d0e5111efd97dc948890b2181e3212ce20e67b4e43cadc0df900a06969295f8a`.
Permanent certificate SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `20a5fbf892d2139d4e45239515bfcee0f2f2773af4c86f0543c42440e2dc664c`.

Verification passed the focused Shield checks, inherited v206 checks, split materialization/integration, HA room/latency unit tests, both app builds, and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Wall remains v207.

## Physical acceptance pending

Ryan is the visual/ADB tester. Check:
1. Next 4 hours: time, temperature and rain percentage visually centre on each weather glyph.
2. 3 day forecast: each label/high-low/rain stack centres on its glyph and the whole panel has balanced left/right visible whitespace.
3. Back out of an app and return through Shield task manager: focus lands on Favourite entry 1.
4. Close media: focus lands on Favourite entry 1.
5. v219 Now Playing alignment and v220 accent behaviour remain unchanged.

Detailed record: `docs/handoffs/2026-09-20-shield-weather-focus-v221.md`.
