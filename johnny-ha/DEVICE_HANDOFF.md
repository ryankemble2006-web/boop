# Johnny HA Lab v15 — fast HA reaction source ready, device test pending

Updated 2026-09-16. Owner `johnny-ha-native-v8`. Final source `8f8b7b0b9c1242020cd6049aa557606403a9714a`; CI `35060140950` SUCCESS, artifact `10432192360` (`johnny-native-arm64`, SHA256 digest `b431a1873fec201ff590bf5ab6b4132aee8a700740f472d7921344c2bff9d4d5`). This source is not yet packaged/installed on Shield, so v14 remains the physically accepted installed checkpoint.

Ryan asked for fan/HA reactions to start much faster and cancel ordinary Johnny activity as soon as practical. Root cause was the Java HA snapshot client polling every2seconds; native ADS/walk pre-emption already exits ordinary routines at its existing safe frame/cleanup boundary. Healthy polling is now250ms. Provider-unavailable backoff preserves the prior roughly10-second retry cadence instead of hammering BOOP during an outage. The accepted eight-second wind episode, OI timing/pixels, music reactions and native cancellation/ownership logic are unchanged.

TDD evidence: test-only commit `16a1420bc1bafb1077a75e6ef430789bc94bc2a9`, run `35060022954`, failed exactly because `JohnnyPollPolicy.java` did not exist. Final run passed HA edge/poll policy tests, native policy tests, pinned-source patch tests,100-cycle ADS cancellation/ownership sanitizer coverage, arm64 native build, Android View compilation and artifact upload. Expected healthy detection delay is now bounded by the250ms polling cadence plus provider/main-thread/native frame-boundary work; no end-to-end physical latency claim until Ryan tests it on Shield.

# Johnny HA Lab v14 — original-pixel music reactions

Updated 2026-09-14. Owner `johnny-ha-native-v8`. Source `cf6977a047ccafed3feb936003ce54fc5532f442`; CI `34884205277` SUCCESS, artifact `10364182755`. Native SHA256 `9b73985f83e8f085447a860f8fdf7029026e4c1b8e35d2069d646c8ec9d15b94`.

Signed and installed Shield APK SHA256 `8c7f4a95dcca4bc23da2e46e8bc45ac34a10dac92a9504ad28bc555c7a2daf2c` matches installed base.apk; existing signer retained. All110 packaged private assets match approved packaging copies (108 reaction PNGs and2 original resources). Both HA preferences unchanged and active DIRECT44100 output4565 retained. V13 rollback retained privately.

Confirmed play/pause edges produce original-resource cameos:4.8-second JOHNWALK shuffle/hops,2.6-second MJTELE puzzled shrug/question. No generated artwork or audio capture.900ms stable-state debounce, same-kind8sec cooldown, global1.5sec gap. Startup/new sessions establish a baseline without a reaction. Metadata cannot restart it; buffering cancels candidates while retaining the prior confirmed state. Native events expire after1.5sec; wind/OI preempt and clear music without stale replay. Existing eight-second wind and3150ms OI remain unchanged.

Technical evidence: Java edge tests, native policies/timing, actual ADS cleanup under sanitizers, Android/native builds passed; independent Java/native review found no blockers. Installed preview running with live HA and NowPlaying. User physically tested pause/play on Shield and accepted the reactions as “perfectly janky”. Keep the deliberate original-pixel character. Combined music with wind/OI interruption is technically tested/reviewed; a separate physical combined test has not been reported.

## Prior accepted v13 behavior and evidence

Updated 2026-09-14. Owner `johnny-ha-native-v8`. Source `b3f507db6435988adc4c7ac8d8cec46e3dea0c67`; CI `34882049647` SUCCESS, artifact `10362924070`. Native SHA256 `62b663294e3f771cfc895f357d4fec6bf280e489d7728babe1749c0a5cc921d2`.

Installed Shield APK SHA256 `e0b83545bd706e995b37395af5550c4229080901fb450134f04a2e77241e8323` matches actual base.apk. Existing signer retained. All108 packaged reaction PNGs match approved private copies:102 corrected wind poses plus6 OI assets. No regeneration/public artwork upload or Windows-demo edits. Both HA preferences unchanged; DIRECT44100 output4565 and animator scale0 preserved. NowPlaying remains intact. Accepted v10 and v12 rollback APKs retained privately.

Confirmed fan-on now starts one eight-second total cameo:850ms lift,6300ms wind,850ms lowering, then normal activity while power remains on. This supersedes both v11 sustained wind and v12 five-second duration at the user's request. Repeated polls/same-state reconnect do not rearm; confirmed off/on does. Early off lowers smoothly, unknown holds the last confirmed target. A new player session synchronizes an initially confirmed-on fan with one cameo. Default rightward wind never derives direction from oscillation.

OI retains its exact3150ms timeline and private pixels. It takes sole actor ownership, clears wind effects, then wind may resume only within its original episode deadline; OI never renews the duration. Confirmed all-off changes to original night; on cancels OI/restores day. Initial/reconnect light snapshots do not replay OI; unavailable is not off. One Johnny and original tree anchor preserved.

Technical verification: test-first timing regressions; level/edge/reconnect, deadline/rearm/reversal/OI-expiry/clock-wrap tests; synthetic compositor,100 sanitizer ownership cycles, arm64/Java/dex compile; independent review no blocker. Installed hash/assets/preferences/audio checked separately from user acceptance.

Physical results: OI night/reaction/normal-night/day accepted in v10; user reported immediate sunshine after returning lights. v11 lowering accepted. v12 one-cameo/no-repeat behavior accepted, then user requested three seconds longer. v13 duration accepted as almost perfect with small visual quirks fitting the original style. Live v13 fan off/on produced one wind sequence, then windActive0 while fan stayed on, with advancing frames and no queued OI. Small visual quirks remain user-accepted; no claim of perfectly seamless animation.
Combined physical check: user reports quick changes cancelled the wind for night/OI, then confirms normal activity resumed with bubble/wind effects gone and lights-on restored daylight. Additional live off/on at night returned to windActive0 while fan stayed on, with advancing frames and no queued OI. Small visual quirks remain user-accepted; no claim of perfectly seamless animation.

Cleanup complete: selected `local.johnnycastaway.halab/local.johnnycastaway.shield.JohnnyDream`. Other installed Johnny variants shield/storypreview/remaster removed for user0 with `-k`; data and verified APK backups retained. Only HA Lab remains installed. BOOP removed the old stale tile; an existing HA Lab favourite was confirmed, so no duplicate added. Local projects/scene labels/original resources preserved. BOOPv176 lyrics album navigation is physically accepted and unchanged by this work.
