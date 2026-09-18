# BOOP status

Updated 2026-09-18. Owner: `boop-wall-shield-split-v207`. Current Shield implementation: `boop-shield-weather-icon-fan-v216`.

Shield v216 / `1.2.216-shield` is signed and ready for Ryan's physical test.

Weather now uses the visible divider geometry as the complete horizontal coordinate system. Hourly/daily labels, weather glyphs, temperatures and rain percentages each occupy the full width of their cell and are explicitly centred; section horizontal insets are zero. Weather glyphs have a +2dp optical correction for font side bearing, addressing the screenshot where numbers looked right-shifted beneath cloud icons. The footer remains aligned to the same 3:4:3 regions.

Fan routing now tries Home Assistant's WebSocket `conversation/process` with an explicit room-scoped fan command first, matching the HA intent resolver already proven by BOOP on the phone. If HA does not return an action result, v215's capability-aware direct `call_service` route remains the fallback. Sonoff and light tiles retain their existing direct path. Live `state_changed` remains authoritative for display state.

Room-control icons are semantic: fan devices show a fan vector even when the selected control entity is a switch; sub/subwoofer devices show a speaker/woofer vector; light/switch icons otherwise remain unchanged.

Build source `e60a3521524e1ccdb19fcac73f4ad4c3033bf618`.
Successful run `35347668181`, job `105607970217`.
Artifact `10547503847`, `BOOP-Shield-v216-Wall-v207-Signed`.
Shield file `BOOP-Shield-v216.apk`, 160485741 bytes, SHA-256 `a81dfd6f143ebe32952e48924184b52325644ed64b8ed217443cb1a6dd460004`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded artifact ZIP SHA-256 `3cfaf36dd7936ecf4a57d5c5c57ffa16d0d2347837fa388020d13e13f23fa293`.

Verification passed through focused checks, inherited regression, materialized split integration, 20 HA unit tests with zero failures/errors/skips, both app builds, and packaged signer/native/art verification. Independent extraction matched the CI receipt and all 16 native libraries remain baseline-identical.

Physical weather/fan/icon acceptance remains pending. Wall stays v207; voice/audio, permissions and signing material were not changed.
