# v164 music bounce: work in progress

Ryan reports v163 did not move and regressed his accepted Lyrics button. He reverted himself and explicitly requests a fresh v164 with actual music bounce, based on accepted v162. Do not reuse the v161 Music Lab tree or v163 integration. Preserve v162's native lyrics and all confirmed features. Deliver a signed APK for Ryan's manual test, not a hosted visual test. No installation or merge into the accepted branch is performed by this new build task.

Base: boop-unified-native-lyrics-v162 at 112d09b5b446d6582954a6d89b3700fe16298ecb. Accepted build source 1e0136ffa9732353035f88ca7a7cb131f7481557, APK cef4527510d9856b669889abfc01c00e6f1a3819e1a28b5e05cee5d472df7171. Task branch: boop-unified-v164-music-bounce.

Design: Android Visualizer output-mix waveform samples on a short-lived worker only while the media puppet is visible/playing and permission is granted; DC-rejected level -> bounded fast-attack/soft-release vertical offset. The offset is applied inside the Now Playing GL viewport without altering the accepted shared renderer, shaders or blink clock. Reuse the conditional permission entry, prompt once in a foreground Activity if needed, never open a physical microphone or change audio focus/volume. Stop/release on pause/hide/detach; no synthetic beat fallback. Useful real Deezer readings require Ryan's test.

Test-first checkpoint: new envelope/wiring/v162-preservation tests added before production implementation. No success or signed artifact yet.
