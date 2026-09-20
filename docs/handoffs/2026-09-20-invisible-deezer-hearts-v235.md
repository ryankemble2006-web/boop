# Shield v235: invisible native Deezer hearts, build candidate

2026-09-20. Based on verified live Shield branch 46ef2fd7642a4a46b012424592521f19d1282b6f. Ryan authorizes the invisible implementation, emulator/laptop use, automatic verified Shield APK installation and a normal next-track ready signal. No live recording or continuous UI polling.

## Physical feasibility evidence

A short-lived shell-owned virtual display opened the installed native Deezer player separately from display0. An accessibility ACTION_CLICK on the actual offscreen now-playing node opened its player. A bounded read of the real transport geometry and a native heart glyph crop established the state. A reversible favourite test then confirmed saved -> unsaved -> saved on the same track. Original favourite state was restored. The test returned NATIVE_FAVOURITE_ROUND_TRIP_CONFIRMED. Primary remote focus was display0 afterward, no probe displays remained, and the original Deezer process continued playing.

A prior exploratory synthetic touch targeted to the offscreen display stole focus and did not navigate. Production therefore has NO synthetic touch/key injection; it invokes only the actual offscreen accessibility nodes. Original display creation alone did not steal focus. The source-owned helper never captures the main display or records video.

The existing Android TV API36 emulator was started with a read-only data overlay, 2 CPU cores and 2GB memory. Its system image lacks activities_on_secondary_displays; it cannot validate this native offscreen path. The physical Shield SDK30 advertises and supports it. The helper fails closed on platforms without this feature rather than falling back to a visible launch.

## Candidate implementation

Lyrics LEFT is dislike+skip, outlined with normal focus only. Lyrics RIGHT and HOME Now Playing are favourite toggles; both fill using FocusChrome.accentColor(context) resolved at draw time, not an orange constant. The accepted three-button positions, text, art and progress geometry stay intact.

The existing selected media-session path remains available when a provider actually advertises heart controls. This installed Deezer instead uses the new source-built DeezerHeartBridge through the existing authenticated Home Assistant androidtv ADB service. The owning BOOP app writes a private per-request nonce marker; every target is checked against that marker, and the native helper rechecks it immediately before a requested action. The laptop is not part of the shipped control path. No new login, permission, provider modification, credential extraction or permanent background helper.

The helper is pinned to the inspected native Deezer version301000101 and requires secondary-display support. It creates a destroy-on-removal offscreen surface, checks native track title/artist/album/duration/mediaID/session token, verifies the navigation/transport layout, and only then invokes the favourite or dislike node. A 41px in-memory native glyph classifier distinguishes the provider's filled/outlined heart and rejects ambiguity. Provider-purple classification is NOT BOOP's accent colour. No screenshot files are written by the production helper.

Read-only state discovery happens once per selected track or screen re-entry, shared between both screens, not on every playback tick. Actions are single-flight and cancellable on changed ownership. Unknown, stale native state, unsupported versions, ambiguous geometry and unconfirmed receipts never manufacture a saved fill. The shell helper has a 12s watchdog, the app a 30s deadline. Closing the helper releases the offscreen display. UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES is used; no user accessibility configuration is changed.

## Checks before publication

The new pure policy test was observed failing before implementation, and the UI contract test failed on the old remove/add roles before its correction. Six focused tests now pass: 70 retained favourite assertions, 27 native-rule assertions, the live-source UI contract, and the actual controller against deterministic Android/async-backend boundaries covering coalescing, shared replies, toggle/dislike distinction and cancellation. The source-owned Android bridge compiles/dexes against installed API36 tools. Git whitespace check is clean. Review is self-review, not an independent reviewer.

The physical round trip above used a throwaway probe, not the final APK. A manual standalone production-helper diagnostic was blocked by a tool safety check and was not retried through another route. Full signed CI, emulator presentation and the final app-to-HA-to-native route still require validation. No v235 installation or ready skip has happened at source publication. Do not conflate the successful feasibility probe with final APK acceptance.

Private raw images, reconstructed provider files, network addresses and diagnostics remain off GitHub. Preserve the accepted v233 fallback/layout/audio/voice/HA behavior and permanent BOOP signer. Wall remains version207; it is built by the shared pipeline but is not requested for delivery or installation.
## Final build, install and live receipts

Source d6a7957d57a94fb7fc2a25266478e7c4d376f220. Signed workflow35531643785/job106133185445 succeeded through all inherited, split, HA, build and package verification stages; favourite workflow35531643786/job106133185094 also succeeded. Artifact10611029896, BOOP-Shield-v235-Wall-v207-Signed; ZIP SHA25637d99acc99912d5f306b1f2aa58367583e2581a0d19f2e4cc22e98023c600ba4.

Delivered Shield APK160534893 bytes, SHA25631d0161a2b3a534c0858ccde3b2aeb0f67474379352b366dc21a8f3cd9119d43; unchanged signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde. Downloaded archive CRC/source/hash/size independently verified. All16 native libraries and8 frozen PNG/shader assets match v234. Full cryptographic apksigner verification also passed on the laptop. Copied to Desktop/APKBOOP and installed via adb install -r; installed235/1.2.235-shield read back. No app-data wipe, new grants or Wall install.

Installed BOOP HOME was brought forward. Bounded post-action BOOP logs show read OK saved0, toggle OK saved1 at confirmation, and several subsequent read OK results. One preceding hardware-binding IOException remains an observed transient failure with unestablished cause. The final native route therefore has positive real installed-app evidence; do not claim every operation or first attempt was faultless. User acceptance of actual v235 unfavourite, dislike+skip and both-screen accent/state behavior remains pending. The earlier native reversible probe is separate evidence, not a replacement for user acceptance.

A single normal media-next was sent as the authorized ready signal after install/live receipts. No dislike used for attention. The task-owned read-only TV emulator was stopped after its synthetic fixture installation did not complete; no visual emulator pass is asserted. It had already established missing secondary-activity support on that image, unlike the physical Shield. Laptop memory remained healthy, about20GB free after lab shutdown. Private test signing material is only for the unused emulator fixture, never BOOP's permanent key or the native provider. No live recording restarted.