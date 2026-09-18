# BOOP status

Updated 2026-09-18. Owner: `boop-wall-shield-split-v207`. Current Shield implementation: `boop-shield-fan-capability-v215`.

Shield v215 / `1.2.215-shield` is signed and ready for Ryan's physical test.

v214 proved the low-latency HA transport: the Sonoff subwoofer and Govee right-speaker light were instant. The remaining fan failure was isolated to control selection, not WebSocket latency. v215 reads HA `supported_features` and only prefers a native `fan.*` entity as a simple power tile when it advertises both current fan power flags (TURN_OFF 16 + TURN_ON 32). Otherwise BOOP falls back to the same physical device's power switch, prioritising power/on-off switches over settings such as oscillation. The direct v214 command path is otherwise unchanged and displayed state still comes from the live HA event stream.

Weather keeps its accepted chrome and 3:4:3 layout but now centres every section using the divider geometry itself. Headings and body content are centred in each box; internal padding is symmetric; footer regions also use 3:4:3 weights and centred content.

Build source `376bb700f2aedc9ffdd659c036bd1abfb14a9948`.
Successful run `35345512061`, job `105601126131`.
Artifact `10546950922`, `BOOP-Shield-v215-Wall-v207-Signed`.
Shield file `BOOP-Shield-v215.apk`, 160485741 bytes, SHA-256 `6297d060874ab7b06fc61f4b29f8e3179b40528cd53377681c6f9811ff01ffc0`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Downloaded artifact ZIP SHA-256 `b39b83515876efe3508419b326d22c7e4f84ba44eb9d39996ac4e39117e95c2f`.

Verification passed through focused tests, inherited regression, materialized integration, 18 HA unit tests with zero failures/errors, both app builds and packaged signer/native/art checks. Independent artifact extraction matched the CI receipt; all 16 native libraries remain baseline-identical.

Physical acceptance of the Govee fan and divider-centred weather remains pending. v214 stays available as the prior signed checkpoint. The superseded pre-fix v215 commit/run failed before building an APK and is not an install candidate.
