# BOOP durable project memory

Updated 2026-09-20.

The consumer apps remain split shells around shared BOOP code: Wall `com.boop.alpha1` stays v207; Shield `com.boop.shieldoverlay` is v222 on `boop-shield-hour-temp-nudge-v222`. The owning split branch remains `boop-wall-shield-split-v207`.

## Shield room-panel control rules

The accepted low-latency architecture remains:
- discovery/registry/state loading is connection/background work, never a pre-click gate;
- validate current generation/room/cached card locally;
- keep one long-lived `state_changed` stream as the display truth;
- do not invent optimistic state;
- reject stale room/generation work and duplicate pending actions.

Sonoff and light controls proved instant on the v214/v215 direct `call_service` path and must remain there.

### Fan control rule from v216

v215 capability-aware entity selection remains required: native fan entities need the current HA power flags when feature metadata is known, otherwise choose the same device's actual power/on-off switch rather than oscillation/settings entities.

When the physical tile is semantically a fan, first use Home Assistant's WebSocket `conversation/process` with a room-scoped natural command. Accept only an `action_done` response with no failed targets. If conversation cannot act, immediately fall back to the v215 direct service route.

## HA icon rule from v216

Icons represent the physical device, not the implementation entity:
- semantic fan -> fan-blade vector even if backing control is `switch.*`;
- semantic sub/subwoofer -> speaker cabinet with woofer vector;
- light -> bulb;
- remaining switches -> power symbol.

## Weather centring rule from v216

Weather centring is locked to the actual divider/cell geometry. Do not reintroduce per-section padding that changes the region centre. Weather glyphs keep the accepted +2dp optical correction.

## Grab/reorder rule from v217

HOME favourites and HA controls share one remote model: hold to grab, left/right to move, OK/Enter to drop.

- Favourite ordinary focus keeps accepted artwork geometry; active grab alone gets 1.14x + Z-depth.
- HA tiles get 1.10x + Z-depth only while grabbed.
- Normal HA clicks remain normal device toggles and must not gain network latency.
- HA control order persists per room; vanished IDs drop and new IDs append.
- Room changes cancel an in-progress grab.

Ryan accepted this movement/customisation behavior as perfect on 2026-09-18.

## Now Playing alignment rule from v219

Ryan physically accepted v219 as perfect on 2026-09-18.

Preserve:
- transport row visual translation `-4dp`, making unfocused Prev align with the progress track;
- progress bar right margin `8dp`, ending on Close player's visible right edge;
- title/artist/Playing positions, Lyrics/Close-player layout, art and mascot bay unchanged.

## Launcher accent-colour rule from v220

Launcher highlight colour is user-configurable from **Launcher Settings**, immediately below **Smart home panel**.

- Store hue as `accent_hue_v1`, integer 0-359.
- Default is hue 204 and must map exactly to existing `#4DB8FF`.
- A single hue slider is the user control; it previews via its label and thumb.
- `BoopTvChrome.accentColor(Context)` is the canonical runtime accent source.
- Shared focus borders, text-only artist focus, Now Playing progress, HA active text/icons and other `FocusChrome` consumers must use that source.
- HOME navigation vector icons are runtime-tinted from the same source.
- Weather wind/current icon/rain accents use the same source.
- Add favourites already resolves through `FocusChrome`.
- Do not change charcoal fills, white body text, layout geometry or HA behavior when changing accent.
- The setting persists and new launcher surfaces should resolve the saved hue on construction/focus rather than hardcode cyan.

## Shield weather/focus rule from v221

Ryan's physical reference point for forecast alignment is the weather glyph's optical centre, not merely the weighted cell centre. Keep the accepted +2dp glyph correction on the associated time/day, temperature/high-low and rain rows as one aligned stack.

The 3-day region uses a +9dp container translation to balance visible whitespace from its middle divider to the card's outer right border while preserving the 3:4:3 divider geometry.

When Shield HOME regains foreground from an external app, including a Shield task-manager return, focus belongs on Favourite entry 1. Close media also returns focus to Favourite entry 1. Internal short Back retains the same destination.

## Shield hourly temperature optical rule from v222

The four **Next 4 hours** temperature values alone receive one extra physical-pixel nudge to the right after the shared +2dp weather optical correction: `temp.setTranslationX(dp(2)+1f)`. Do not apply that extra pixel to times, glyphs, rain percentages or the 3-day forecast unless Ryan explicitly requests it.

The active Shield CI workflow uses writable Gradle caching with `--build-cache` and cancels superseded rapid UI builds. v222 proved the cache was restored and reused 77 app-build tasks, with the Gradle app build completing in 26 seconds.

## Latest verified artifact

Build source `5bab9dab597161a0a53ab5e6f85ef0f3d913bee0`.
Run `35516142829`, job `106092308080`; artifact `10606488920`, `BOOP-Shield-v222-Wall-v207-Signed`.
Deliver `BOOP-Shield-v222.apk`, 160485741 bytes, SHA-256 `3eee980f533a64b09d4fa0a25177c9c13bcbab2df17ec46f01c407f4c8f98bc5`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde` unchanged.
Artifact ZIP SHA-256 `649641d807fbcab638ad2b65ef5ea35aed5aefd0a70d34e7a4e9c6036667fe30`.
Focused checks, inherited v206 checks, materialized split integration, HA tests, both builds and packaged signer/native/art verification passed; all 16 native libraries remain baseline-identical.

v219 Now Playing alignment remains physically accepted. v220 accent-colour behaviour and v221 return-focus/weather geometry remain retained. Voice/provider/pitch work remains deferred and untouched. Wall stays v207.
