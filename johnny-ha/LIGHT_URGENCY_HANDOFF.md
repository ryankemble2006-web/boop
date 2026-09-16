# Johnny HA light urgency

Updated 2026-09-16. Owner branch `johnny-ha-native-v8`.

Ryan reported that Johnny HA Lab v15 reacts to fan changes quickly after the 250 ms polling change, but lights on/off still took roughly 4–5 seconds to visibly switch day/night. Diagnosis: lights and fan already share the same 250 ms BOOP snapshot polling path, so the remaining delay was native. A real light edge updated `night`, but unlike fan urgency it was not an independent native pre-emption reason; an original routine could therefore keep the old visual frame during a long interruptible engine wait.

Implemented in code commit `342491f3b103fd8e3827eed4bdd786024cbb5df5`: real day/night edges now create one environment-generation interrupt, wake ordinary native waits and use the existing safe ADS/TTM cleanup boundary. Duplicate steady-state light samples do not pre-empt. The interrupt is consumed once when native cleanup takes ownership, allowing the following frame/routine to use the existing live island refresh rather than immediately aborting again. A light edge also cancels a normal music cameo. Active fan/wind ownership remains protected from the new light pre-emption; wind already reads live night state. Lights-on retains OI cancellation. No hard reset, art change or engine-source replacement was introduced.

TDD RED: test-only commit `1ade672f619fc47cc4dfa66c4625de16b216f1ee`, Actions run `35063451010`, failed in `Native policy tests` exactly because the new environment-edge control functions did not yet exist; HA state-edge tests passed first.

GREEN: code commit `342491f3b103fd8e3827eed4bdd786024cbb5df5`, Actions run `35063839059`, job `native` SUCCESS. Passed HA state-edge tests, native light/fan policy tests including interruptible-wait wake-up and fan ownership protection, pinned-source fetch/patch, cancellation/ownership sanitizers, pinned NDK install, silent arm64 native engine build, Android View compilation and artifact upload.

Device boundary: the Shield remains on installed/verified Johnny HA Lab v15 (`versionCode 15`, `1.14-reborn-ha-fast-poll`) from the preceding handoff. This light-urgency candidate has not been packaged/installed or physically accepted yet. Do not claim the 4–5 second visible symptom is fixed until Ryan installs and tests it on the Shield.
