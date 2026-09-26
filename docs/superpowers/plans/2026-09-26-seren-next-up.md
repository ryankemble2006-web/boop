# Shield Seren Next Up implementation

Ryan approved the in-chat layout and implementation on 26 September: Weather/Now Playing, favourite apps, Seren posters, then HA controls off the initial screen. One click starts the exact episode using existing Seren autoplay and prescanning. Preserve art, voice, settings, and all phone work.

Base: live `boop-pocket-polish-v251` at `34612d7`; physical Shield251 hash matches its delivery. The older Shield owner handoff is superseded by the installed v251 receipt. Work is isolated on `boop-shield-seren-v252`.

The read-only probe returned 79 episodes through Kodi's existing loopback JSON-RPC TCP server, with matching first three shows. Use that local connection for the shortest path, with no new HA configuration, web server, credentials, or exposed ports. HA keeps its existing controls/session below the row. Laptop forwarding is diagnostic only and is not a runtime dependency.

- [x] Confirm live base, installed version, immutable rollback APK, and private preferences/settings backup.
- [x] Add behaviour tests for real Kodi JSON frames, safe Seren episode parsing, artwork URLs, resume and single-dispatch playback.
- [x] Implement loopback RPC, persistent last-good row and bounded artwork caching; refresh in background without stealing focus. Keep existing cached content when Kodi is stopped or unavailable.
- [x] Add native poster row and move HA into the same scroll content below the initial viewport. Preserve favourites and hero geometry where space allows; keep D-pad navigation, Back, focus chrome and artwork readable.
- [x] Cold playback starts Kodi, waits for its existing RPC socket, then sends one episode command. Never repeat an uncertain playback dispatch.
- [x] Test source behaviour, Android compilation, and real view geometry/focus in a TV fixture. Validate current settings and startup behaviour.
- [x] Build permanent-signed Shield252 via existing GitHub workflow; verify APK signature, frozen assets/libraries and rollback before in-place Shield-only install.
- [x] Test real row, focus, HA access and selected-episode playback. Record measured timings separately from user visual acceptance. Publish source/handoff and signed APK with factual validation.

Failure cases: interrupted refresh must not replace good cached content; malformed/plugin-directory rows must not play; double click must dispatch once; Kodi cold start must have a deadline; remote source failures must not produce a second playback attempt; artwork failure must retain a usable labelled card; leaving/recreating Home must not deliver stale callbacks or move focus.
