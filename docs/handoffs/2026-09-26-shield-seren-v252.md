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

## Delivery state

Permanent-signed CI and in-place Shield installation are pending at this source checkpoint. Physical rollback APK SHA256: `916ad43ec539b5b8cd2b2d3c15f0fa9ee92bd0ac91379d3ba4bd608554191947`. Private preferences and Kodi/Seren settings were backed up locally; no private snapshots are committed. Final source, CI, APK hashes and physical results will be added after verification.
