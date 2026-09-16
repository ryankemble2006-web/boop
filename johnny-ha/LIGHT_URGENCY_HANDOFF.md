# Johnny HA light urgency

Updated 2026-09-16. Owner branch `johnny-ha-native-v8`.

Ryan reported that Johnny HA Lab v15 reacts to fan changes quickly after the 250 ms polling change, but lights on/off still took roughly 4–5 seconds to visibly switch day/night. Diagnosis: lights and fan already share the same 250 ms BOOP snapshot polling path, so the remaining delay was native. A real light edge updated `night`, but unlike fan urgency it was not an independent native pre-emption reason; an original routine could therefore keep the old visual frame during a long interruptible engine wait.

Implemented in code commit `342491f3b103fd8e3827eed4bdd786024cbb5df5`: real day/night edges create one environment-generation interrupt, wake ordinary native waits and use the existing safe ADS/TTM cleanup boundary. Duplicate steady-state light samples do not pre-empt. Active fan/wind ownership remains protected. Lights-on retains OI cancellation. No hard reset, art change or engine-source replacement was introduced.

Initial TDD RED: `1ade672f619fc47cc4dfa66c4625de16b216f1ee`, Actions `35063451010`, failed in Native policy tests because environment-edge controls did not yet exist. GREEN: code `342491f3b103fd8e3827eed4bdd786024cbb5df5`, Actions `35063839059`, full native job SUCCESS.

Shield install evidence for that candidate: the installed v15 base was backed up first with SHA256 `3810ac838ecb0c9e7d10a61f38551b44a794b33d9b239ed29d8d8786e5ddc370`. The green artifact digest matched GitHub and declared built commit `342491f3b103fd8e3827eed4bdd786024cbb5df5`. Packaging preserved every untouched v15 ZIP entry byte-for-byte and replaced only `classes.dex` and `lib/arm64-v8a/libjohnny.so`; same signer fingerprint `785ce5d046b1947a48dd40fbbd4e83aa1e69665d5b89426af5d9abc215c05215`. Installed candidate SHA256 `2db3dc544dbcfe9e2f795ea1d79793f6af85f24680a6c3ad8cf92b48eb69f70d` matched a fresh pull from the Shield exactly. Johnny remained selected/enabled screensaver and JohnnyMusicListener remained authorized.

Physical follow-up: Ryan reports **lights ON is immediate**, but lights OFF still continues the current animation for a few seconds before OI appears. That proves the day restore path is fast and isolates the remaining asymmetry to lights-off OI scheduling: night mode and the OI gag are currently requested in the same Android callback, allowing OI pre-emption to compete with the first dark render.

Follow-up design now in progress: set night immediately and delay only the OI request by a tiny tested render window so the environment-only interrupt can publish darkness first. The delay policy target is 150 ms, with lifecycle/session guards; if lights return on before the delay expires, native OI rejection keeps the stale gag from starting. This is intentionally a Java scheduling fix, not another C-engine state machine. Do not claim physical success until the new candidate passes CI, is installed, and Ryan confirms lights-off behavior.
