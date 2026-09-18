# BOOP status

Updated 2026-09-18. Owner: `boop-wall-shield-split-v207`. Current Shield implementation: `boop-shield-accent-colour-v220`.

Shield v220 / `1.2.220-shield` is signed and ready for Ryan's physical test.

Ryan physically accepted the v219 Now Playing alignment as perfect. Preserve its -4dp transport visual correction and 8dp progress right margin.

v220 adds user-selectable launcher highlight colour in **Launcher Settings**, directly below **Smart home panel**:
- one remote-focusable hue slider, range 0-359;
- default hue 204 maps exactly to BOOP's existing `#4DB8FF` cyan, so untouched installs look unchanged;
- setting persists in Shield launcher preferences;
- slider label/thumb preview the selected hue while it moves;
- the shared accent source now drives TV focus chrome, artist focus colour, Now Playing progress, HA on-state/icons, Add favourites, launcher navigation icons and weather accent details;
- the launcher icon vectors are tinted at runtime, so their old baked cyan no longer locks the visible colour;
- layout, v217 favourite/HA reordering, HA latency/control paths, v219 Now Playing geometry, voice/audio, permissions and signing were not changed.

Build source `a009b921bf23d018f7edc9ebf2c64a9f89bf8ddd`.
Successful run `35355454656`, job `105634013385`.
Artifact `10552105353`, `BOOP-Shield-v220-Wall-v207-Signed`.
Shield file `BOOP-Shield-v220.apk`, 160485741 bytes, SHA-256 `2ac14d16983a7662b09f5338e18f7f43b749cea0666e4af676d86b8e087d34ad`.
Permanent signer SHA-256 remains `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Uploaded artifact ZIP SHA-256 `1a6fa09706d06b2bba52becc4123f7640fee15068147fd3b8b38288a11d4cdf1`.

Verification passed through 77 focused checks, inherited v206 regression, materialized split integration with 100 checks, HA room/latency tests, both app builds, and packaged signer/native/art verification. All 16 native libraries remain baseline-identical.

Physical acceptance of the colour slider and accent propagation remains pending. v219 is the prior accepted signed checkpoint. Wall stays v207.
