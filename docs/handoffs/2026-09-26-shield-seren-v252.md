# Shield252: Seren Next Up on Home

Ryan approved a native Seren Next Up poster row in BOOP on Shield, with HA controls below the initial screen. Clicking a poster opens the exact episode through the existing Seren autoplay and pre-scrape configuration. The implementation uses Kodi's existing loopback JSON-RPC TCP server; the diagnostic laptop forward is not a runtime dependency. No HA, Kodi, source-provider or Android permission settings are changed.

Branch `boop-shield-seren-v252` starts from current `boop-pocket-polish-v251` at `34612d7b3346a638bfbe6cf0edd022956ad152e3`. Physical Shield251 was verified against its accepted signed APK. The phone's Wall220 source is retained; this task deploys only Shield. The former weather-focus owner is older than the installed Shield and was not used as the implementation base.

## Behaviour

- Weather/Now Playing and favourite apps retain their existing positions. The poster row uses the remaining first-screen space; Down scrolls to HA, Up returns to the selected poster. Artwork is clipped clear of the idle face.
- `Files.GetDirectory` reads `plugin://plugin.video.seren/?action=showsNextUp`. Exact opaque episode URLs are retained, including Seren's encoded arguments. `Player.Open` adds Seren's forced-resume flag to avoid another prompt.
- Last good content and bounded artwork caches survive app/Kodi restarts. Malformed or unavailable feeds retain saved content. Valid empty results clear the row. Transient poster errors keep usable labels and retry on later visibility/refresh.
- Refreshes preserve focus by episode, show or nearest index. Cold playback opens Kodi, polls its local readiness with a deadline, and sends at most one playback request. Busy/double clicks do not dispatch twice. A playback failure never retries an uncertain command.
- The feature is enabled when Kodi is installed. It uses no account credentials and opens no server port. Existing optional provider rows and settings remain available.

## Verification before signed delivery

- Read-only physical Kodi probe returned 79 episodes, with the first shows matching Ryan's Seren screenshot, in 2.626 seconds. This is directory loading, not playback latency.
- JVM tests execute the actual parser, fragmented UTF-8 socket transport, cache replacement and single-dispatch playback classes. Regression cases include malformed nonempty feeds, cancelled/duplicate requests, failed launches, RPC errors and truncated frames.
- Android36 TV fixture at Shield's effective density/font scale passes: fully visible posters, HA initially offscreen, D-pad traversal in both directions, exact clicked URL, HA action routing, focused-show advancement, empty-feed focus, status changes and real HTTP poster failure/retry. Earlier fixture runs failed before the relevant fixes.
- Existing optional Home-row focus/visibility/selection/return and tall HA fixtures also pass. Local Android compilation and preview APK build pass. Preview uses a separate emulator-only package and test signer.
- Full bare pytest is not the maintained release gate: candidate run reported 58 failures / 498 passes / 2 skips. Every distinct failing test also fails in an untouched base-source snapshot under the same environment; no candidate-only failure IDs. Historical tests depend on superseded contracts, Linux tools, or generated source. The preview's intentionally separate manifest also fails production manifest assertions. The signed CI runs the maintained inherited sequence and clean production materialization.
- Independent code review findings were fixed and re-reviewed with no remaining actionable blockers. Physical playback/resume, final installed UI and Ryan's visual acceptance remain separate from these checks.

## Signed delivery and physical acceptance

Source `768bfb0d743b336fb617ce11439e49326e9a42cc` passed the complete maintained [CI36217003838](https://github.com/ryankemble2006-web/boop/actions/runs/36217003838), including clean materialization, inherited tests, HA unit tests, both signed APKs and frozen assets/native verification. [PR12](https://github.com/ryankemble2006-web/boop/pull/12) is based on the current phone/Shield owner; no main/owner branch merge was performed.

Shield252 is installed in place. Its installed APK SHA256 matches the downloaded signed artifact: `15526fc514b71ea1b8d0b9fd9b70dbec8db19171acd6122b6bbffb501e595e9e`. Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`. All 29 assets and 16 native libraries match physical Shield251 byte for byte. UID10117 and first-install history are unchanged; every shared-preferences file was byte-identical immediately after installation. Only Shield was installed; no phone or sender deployment occurred. The exact signed APK also passes the Seren Android fixture.

Physical screenshots and UI hierarchy confirm real Seren posters, HA off the initial screen, Down revealing all four living-room controls, and Up returning to the selected poster. No real HA device was toggled during validation. Cached posters remained visible after Kodi was stopped.

One press on President Curtis S1E9 reached the exact episode in Kodi. Initial item/player recognition was12.411seconds; that early metric preceded buffering/fullscreen completion. The stronger cold test started with Kodi force-stopped, sent one poster press, and reached fullscreen moving video in14.374seconds (including ADB input overhead). No source-selection or resume click was sent. Ryan directly confirmed: **"its playing from a cold start"**. This confirms physical cold-start playback, separately from the fixture and signed-install evidence. Startup speed still depends on Seren's source resolution/buffering.

An automated warm repetition timed out during a display transition; Android logged a missing-focused-window ANR in that interval. Ryan corrected the interpretation: **"second kaunch didnt stall. the refresh rate changed"**. Treat that automated repetition as inconclusive transition timing, not a proven Seren or fullscreen defect. Subsequent cold playback passed with the same APK. No speculative fullscreen fix or refresh-rate setting change was made.

Deliverables are in `C:/Users/ryank/Documents/Codex/2026-09-26/lau/outputs/BOOP-Shield-Seren-v252`: APK, installation/verification receipt, playback observations and actual Home/HA screenshots. Private preference snapshots and logs remain under task `work/`. Physical rollback APK SHA256: `916ad43ec539b5b8cd2b2d3c15f0fa9ee92bd0ac91379d3ba4bd608554191947`.
