# Shield v215: fan capability selection and divider-centred weather

Date: 2026-09-18
Branch: `boop-shield-fan-capability-v215`
Verified build source: `376bb700f2aedc9ffdd659c036bd1abfb14a9948`

## Physical evidence from v214

Ryan reported:
- weather was substantially improved, but wanted all content centred according to the visible divider boxes;
- Sonoff subwoofer control was instant;
- Govee right-speaker light control was instant;
- Govee fan tile did not operate the fan;
- asking BOOP on the phone to turn the fan off still worked immediately.

This proves the v214 direct WebSocket transport is working for other devices and points to fan entity selection/capability rather than general HA latency.

## Home Assistant fan model

Current Home Assistant fan entities advertise TURN_OFF and TURN_ON in `supported_features` (numeric flags 16 and 32). BOOP previously ranked any `fan.*` entity ahead of switches on the same device without inspecting that capability data.

v215 parses `attributes.supported_features` from the state snapshot and stores it on EntityState/EntityCard. A known fan feature mask must contain both power bits (48) to be actionable as BOOP's binary fan tile. If not, device collapsing selects another actionable entity. Switch ranking now prefers entity IDs/names containing power/on-off, preventing an oscillation/settings switch from winning a fallback tie.

Unknown feature metadata keeps legacy behaviour. There is no brand-specific Govee rule.

The v214 immediate `call_service` action path and session-wide `state_changed` source of truth are unchanged.

## Weather geometry

The 3:4:3 dividers are now the centring coordinate system. All three columns use symmetric 12dp padding and centred headings. The current icon + text stack is centred as a combined group. The footer changed from equal 1:1:1 thirds to 3:4:3 so its wind/sun/source centres line up with the boxes above.

## Verification

Corrected GitHub Actions run `35345512061`, job `105601126131`: SUCCESS.
Artifact `10546950922`: `BOOP-Shield-v215-Wall-v207-Signed`.

Shield:
- `BOOP-Shield-v215.apk`
- `com.boop.shieldoverlay`
- version 215 / `1.2.215-shield`
- 160485741 bytes
- SHA-256 `6297d060874ab7b06fc61f4b29f8e3179b40528cd53377681c6f9811ff01ffc0`
- permanent signer SHA-256 `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`

Downloaded artifact ZIP SHA-256: `b39b83515876efe3508419b326d22c7e4f84ba44eb9d39996ac4e39117e95c2f`.

Focused room/weather tests, inherited checks, split materialization/integration, HA unit tests, both app builds and actual APK signer/native/art checks passed. Independent extraction matched the receipt. Four copied HA test suites contain 18 tests, zero failures/errors/skips. All 16 native libraries match the accepted v206 baseline.

## Superseded failed run

Initial code commit `4b5ab35dccbf72c53528025129af862edc6a2846` had a Java constructor delegation typo caught before acceptance. Run `35345462718` failed the first focused test gate and did not build/upload an APK. Commit `376bb700...` corrects that typo and is the verified source above.

## Physical acceptance

Install only the verified v215 Shield APK. Confirm the Govee fan now acts immediately, Sonoff/light remain instant, and weather appears centred relative to the divider lines.
