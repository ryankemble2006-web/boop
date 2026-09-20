# BOOP durable project memory

## User-accepted v235 favourites: 2026-09-20

Ryan physically tested the installed v235 and reports that the favourites button works exactly as desired, including Android added/removed confirmation messages. He favourited a track in native Deezer, returned to BOOP HOME and saw the heart fill; he then unfavourited it on BOOP's Lyrics screen and verified that the removal was reflected in native Deezer. This is acceptance of real cross-app favourite-state propagation and add/remove operation on the installed build, not merely CI success or the earlier standalone probe. Preserve this working checkpoint and its invisible operation.

Scope of acceptance: favourites add/remove, confirmation messages, Deezer-to-HOME filled state, and Lyrics-to-Deezer removal. The separate dislike-and-skip button and a fresh change of the accent slider were not explicitly tested in this report; do not infer those results or an unrestricted every-app/window guarantee. The selected-accent requirement remains unchanged. The earlier hardware-binding failure remains historical evidence; this report does not diagnose it. No new app code, build, install, permission change, recording, input or ready-track skip was performed to record this feedback. Existing source d6a7957d57a94fb7fc2a25266478e7c4d376f220 and the verified v235 APK are unchanged.

Updated 2026-09-20.

The consumer apps remain split shells around shared BOOP code: Wall `com.boop.alpha1` stays version207; Shield `com.boop.shieldoverlay` is v234. The live Shield iteration branch stays `boop-shield-weather-focus-v221` so branch-scoped Gradle cache entries remain reusable; `boop-shield-hour-temp-nudge-v222` is the v222 snapshot branch. The owning split lineage remains `boop-wall-shield-split-v207`.

## Shield room-panel control rules

The accepted low-latency architecture remains:
- discovery/registry/state loading is connection/background work, never a pre-click gate;
- validate current generation/room/cached card locally;
- keep one long-lived `state_changed` stream as the display truth;
- do not invent optimistic state;
- reject stale room/generation work and duplicate pending actions.

Sonoff and light controls proved instant on the v214/v215 direct `call_service` path and must remain there.

### Fan control rule from v216

v215 capability-aware entity selection remains required: native fan entities need the current HA power flags when feature metadata is known, otherwise choose the same device's actual power/on-off switch rather than oscillation/settings entities.

When the physical tile is semantically a fan, first use Home Assistant's WebSocket `conversation/process` with a room-scoped natural command. Accept only an `action_done` response with no failed targets. If conversation cannot act, immediately fall back to the v215 direct service route.

## HA icon rule from v216

Icons represent the physical device, not the implementation entity:
- semantic fan -> fan-blade vector even if backing control is `switch.*`;
- semantic sub/subwoofer -> speaker cabinet with woofer vector;
- light -> bulb;
- remaining switches -> power symbol.

## Weather centring rule from v216

Weather centring is locked to the actual divider/cell geometry. Do not reintroduce per-section padding that changes the region centre. Weather glyphs keep the accepted +2dp optical correction.

## Grab/reorder rule from v217

HOME favourites and HA controls share one remote model: hold to grab, left/right to move, OK/Enter to drop.

- Favourite ordinary focus keeps accepted artwork geometry; active grab alone gets 1.14x + Z-depth.
- HA tiles get 1.10x + Z-depth only while grabbed.
- Normal HA clicks remain normal device toggles and must not gain network latency.
- HA control order persists per room; vanished IDs drop and new IDs append.
- Room changes cancel an in-progress grab.

Ryan accepted this movement/customisation behavior as perfect on 2026-09-18.

## Now Playing alignment rule from v219

Ryan physically accepted v219 as perfect on 2026-09-18.

Preserve:
- transport row visual translation `-4dp`, making unfocused Prev align with the progress track;
- progress bar right margin `8dp`, ending on Close player's visible right edge;
- title/artist/Playing positions, Lyrics/Close-player layout, art and mascot bay unchanged.

## Launcher accent-colour rule from v220

Launcher highlight colour is user-configurable from **Launcher Settings**, immediately below **Smart home panel**.

- Store hue as `accent_hue_v1`, integer 0-359.
- Default is hue 204 and must map exactly to existing `#4DB8FF`.
- A single hue slider is the user control; it previews via its label and thumb.
- `BoopTvChrome.accentColor(Context)` is the canonical runtime accent source.
- Shared focus borders, text-only artist focus, Now Playing progress, HA active text/icons and other `FocusChrome` consumers must use that source.
- HOME navigation vector icons are runtime-tinted from the same source.
- Weather wind/current icon/rain accents use the same source.
- Add favourites already resolves through `FocusChrome`.
- Do not change charcoal fills, white body text, layout geometry or HA behavior when changing accent.
- The setting persists and new launcher surfaces should resolve the saved hue on construction/focus rather than hardcode cyan.

## Shield weather/focus rule from v221

Ryan's physical reference point for forecast alignment is the weather glyph's optical centre, not merely the weighted cell centre. Keep the accepted +2dp glyph correction on the associated time/day, temperature/high-low and rain rows as one aligned stack.

The 3-day region uses a +9dp container translation to balance visible whitespace from its middle divider to the card's outer right border while preserving the 3:4:3 divider geometry.

When Shield HOME regains foreground from an external app, including a Shield task-manager return, focus belongs on Favourite entry 1. Close media also returns focus to Favourite entry 1. Internal short Back retains the same destination.

## Shield hourly temperature optical rule from v223

Relative to v222, the four **Next 4 hours** temperature values alone move 2 more physical pixels right after the shared +2dp weather optical correction: `temp.setTranslationX(dp(2)+3f)`. Times, glyphs, rain percentages and the 3-day forecast remain unchanged.

The active Shield CI workflow uses writable Gradle caching with `--build-cache` and cancels superseded rapid UI builds. Keep tiny sequential Shield UI iterations on the cache-hot active branch instead of creating a fresh version branch for every pixel tweak; use version branches as snapshots when useful. v222 proved the cache was restored and reused 77 app-build tasks, with the Gradle app build completing in 26 seconds.

## Now Playing transport rule from v224

The transport buttons remain **Prev / Play-Pause / Next**. Rew and Fwd are intentionally absent. v234 adds a separate favourite toggle after Next, without repositioning those three buttons.

Preserve:
- Prev's accepted v219 position via `controls.setTranslationX(-dp(4))`;
- existing `CONTROL_GAP_DP = 10`;
- progress bar +/-10-second seeking;
- Lyrics/Close-player access and the v219 progress geometry.

Do not recenter the three-button row.

## Lyrics presentation rule from v226

Fullscreen lyrics album art uses a hard bitmap mask in the ImageView draw path. Build a rounded `Path`, call `canvas.clipPath(...)`, draw the image, restore the canvas, then draw focus chrome. Do not rely on `clipToOutline` for this screen.

The lyric provider/licence credit remains parsed internally but is not drawn.

## Lyrics title rule from v228

Fullscreen lyrics track title is one line at its original Y position. Use:
- `setSingleLine(true)`;
- `setHorizontallyScrolling(true)`;
- `TextUtils.TruncateAt.MARQUEE`;
- `setMarqueeRepeatLimit(1)`;
- `setSelected(true)`.

Current geometry after v229: title `448f * unit`, height `42f * unit`; artist `520f * unit`; progress `583f * unit`. The earlier v228 artist Y532 is superseded by v229.

## Lyrics music-column rule from v229

The fullscreen lyrics progress bar is the master horizontal datum. Preserve:
- progress width: 397 design pixels;
- artwork centred on progress midpoint;
- title and artist laid out at progress width with centred gravity;
- artwork bottom 418 -> title top 448 = 30 design pixels;
- title bottom 490 -> artist top 520 = 30 design pixels;
- transport buttons: Prev / Play-Pause / Next;
- 3-button group centred on progress midpoint using existing 54px buttons and 17px gaps;
- progress-bar left/right remains +/-10-second seek.

v234 adds hearts outside this accepted three-button group; it does not alter the positions above.

## Lyrics title rendering rule from v230

Keep all v229 music-column geometry unchanged. The title alone uses `setIncludeFontPadding(false)` to eliminate slight glyph clipping inside its existing 42-design-pixel frame.

## Lyrics artist action from v231

Fullscreen lyrics artist behavior matches HOME:
- text-only focus chrome via `BoopTvChrome.useTextOnlyFocus(artist)`;
- artist focus is enabled only when artist/title data is available;
- artwork Down -> artist; artist Down -> progress; progress Up -> artist; artist Up -> artwork;
- click routes to the same `DeezerArtistBrowser` used by HOME;
- album and artist browser operations cancel each other to prevent overlapping lookups.

## Lyrics fallback rule from v233

Ryan accepted v233 as good to build on, 2026-09-20. Provider order stays Deezer -> LRCLIB synced LRC.

Timing/search rules:
- Deezer primary window: 2.5 s;
- LRCLIB gets its own 6 s fallback window;
- total owner timeout: 9 s;
- LRCLIB final search uses `track_name` plus `q`;
- LRCLIB search 404 is a clean miss, not UNKNOWN;
- title/artist matching tolerates harmless leading “The” and trailing bracket/parenthesis qualifiers;
- duration remains bounded within 3.5 s when known;
- plain untimed lyrics are still never converted into fake synced lyrics.

## Historical v234 favourite hearts (superseded roles)

Ryan approved explicit lyrics REMOVE on the left of the existing transport buttons, ADD on the right, and one toggle heart in HOME Now Playing. Lyrics hearts are 54-design-pixel controls at transportLeft-71 and transportLeft+213, preserving the existing three positions. HOME heart is 42dp after Next. Focus uses the chosen launcher accent, with explicit D-pad navigation.

One lifecycle-bound controller shares provider state between both screens. Native `deezer.android.app` only. Send HEART ratings only when RATING_HEART and ACTION_SET_RATING are advertised; otherwise accept only an exact track-favourite-labelled published custom action and its actual identifier. Never reinterpret dislike, thumbs, artist or playlist actions. Cast favourites are not supported by this candidate.

Unknown state is not unsaved. Toggle refuses unknown; explicit add/remove do not become blind toggles. Only a provider state receipt confirms success. Requests are single-flight, bounded to three seconds, rechecked against current session and title/artist/album/duration/media ID and invalidated by track changes. These are immediate application-side guards; Android rating commands are not atomic against provider-side track changes. No new permission, audio focus, credential, API account or provider launch.

The installed Deezer app's actual capability, favourite round trip and v234 layout remain physically UNVERIFIED. If the heart is unknown or an unavailable message appears, inspect the actual provider controls rather than pretending the feature is working. CI tests use deterministic test-only Android boundaries, not the installed Deezer application. No automatic installation or Windows sync was performed.

## Historical v234 artifact

Build source `ccbd42cf3c4dc77614425f675e80fa8a3f146d20`.
Successful signed run `35525392054`, job `106116533781`; artifact `10609691434`, `BOOP-Shield-v234-Wall-v207-Signed`.
Deliver `BOOP-Shield-v234.apk`, 160518509 bytes, SHA-256 `58d151cd8a91142a3372e7efacdf59edc771dad8a7eddd69aa892b7d4f3876b9`.
Permanent signer `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Archive SHA-256 `5871cfe13020d5e4f9cf50abf9177195403978c3704bd1c5569548236ad10533`.

70 favourite behavioural assertions and 94 focused source checks passed; favourite run `35525392074` passed. Full inherited/split/HA/build/package pipeline passed. Downloaded APK hash/size, v2 certificate fingerprint, 16 native libraries and frozen art independently checked against the receipt/baseline. Full apksigner verification ran in CI. Wall stays version207; its shared-code rebuild is not asserted byte-identical or delivered as a requested Wall update.

Accepted v233 rollback: build source `9e319d7336e7b52d54c080ed8d3bd596c805ae3d`, base documentation `52afeddb66dcca23aaf9ec5c17aa3218cc1eeb9c`, artifact `10608914071`, APK SHA-256 `dfcc8522ca8137f3755abefe1e8b23eecab68a9122816089cec7f544700920d0`. Preserve its accepted lyrics/audio behavior.

## Invisible native hearts: v235 installed, 2026-09-20

## Approved UI and actual native mechanism

Lyrics LEFT: crossed-out dislike-and-immediate-skip icon, outlined with ordinary focus highlighting only.
Lyrics RIGHT: favourites toggle. HOME Now Playing: same favourites toggle after Next.
ONLY those two favourites toggles fill, and only when confirmed saved. Colour is resolved from FocusChrome.accentColor(context) when drawn, following the user's slider, not hardcoded orange. Three transport positions, artwork, text, progress and accepted v233 lyric fallback remain unchanged.

Native Deezer301000101 has no advertised rating/custom heart commands. A source-built, short-lived shell helper creates its own destroy-on-removal virtual display and runs the real signed-in Deezer player there. It invokes actual accessibility ACTION_CLICK on verified offscreen nodes, with no synthetic touch/key input and no visible fallback. Version, capability, track title/artist/album/duration/mediaID/session and native control geometry are checked. A small in-memory glyph crop provides the native fill receipt; it is not a recording and writes no screenshot files. The user's normal accessibility services remain enabled via FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES.

The existing authenticated HA ADB route is reused. An own-app private nonce marker proves the target hardware and supports cancellation; credentials from Deezer are never read. No new login, permissions, permanent service or laptop dependency. State discovery runs once per track/screen entry, not every progress callback. Single-flight actions; helper12s/app30s deadlines; stale/ambiguous/unconfirmed state fails closed without optimistic fill.


## Final build, install and live receipts

Source d6a7957d57a94fb7fc2a25266478e7c4d376f220. Signed workflow35531643785/job106133185445 succeeded through all inherited, split, HA, build and package verification stages; favourite workflow35531643786/job106133185094 also succeeded. Artifact10611029896, BOOP-Shield-v235-Wall-v207-Signed; ZIP SHA25637d99acc99912d5f306b1f2aa58367583e2581a0d19f2e4cc22e98023c600ba4.

Delivered Shield APK160534893 bytes, SHA25631d0161a2b3a534c0858ccde3b2aeb0f67474379352b366dc21a8f3cd9119d43; unchanged signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde. Downloaded archive CRC/source/hash/size independently verified. All16 native libraries and8 frozen PNG/shader assets match v234. Full cryptographic apksigner verification also passed on the laptop. Copied to Desktop/APKBOOP and installed via adb install -r; installed235/1.2.235-shield read back. No app-data wipe, new grants or Wall install.

Installed BOOP HOME was brought forward. Bounded post-action BOOP logs show read OK saved0, toggle OK saved1 at confirmation, and several subsequent read OK results. One preceding hardware-binding IOException remains an observed transient failure with unestablished cause. The final native route therefore has positive real installed-app evidence; do not claim every operation or first attempt was faultless. Ryan subsequently accepted actual v235 favourite add/remove and cross-app state propagation, as recorded above; separate dislike+skip and a fresh accent-slider change remain unreported. The earlier native reversible probe is separate evidence, not a replacement for user acceptance.

A single normal media-next was sent as the authorized ready signal after install/live receipts. No dislike used for attention. The task-owned read-only TV emulator was stopped after its synthetic fixture installation did not complete; no visual emulator pass is asserted. It had already established missing secondary-activity support on that image, unlike the physical Shield. Laptop memory remained healthy, about20GB free after lab shutdown. Private test signing material is only for the unused emulator fixture, never BOOP's permanent key or the native provider. No live recording restarted.
## Flow shortcut from v236 (installed, physically checked and user-accepted)

Now Playing's Close player button is replaced by Flow in the same130x44dp slot, margins and focus route. Preserve current accent styling and separate Close media access. Flow uses the established native Flow URI through the selected Deezer MediaController with fresh ACTION_PLAY_FROM_URI capability, non-null extras and a1.2s same-session repeat guard. No HA/ADB/offscreen work is needed for this new Flow action; the existing accepted heart helper is untouched. Unsupported native sessions/Cast report unavailable, not a visible cold-launch fallback.

Signed source5bfb8d91f3351cbe1c089fb5e0a2566334db2553, run35533035925/job106137012994, artifact10612281598. Ten focused local tests plus full signed CI passed. APK236 was hash/signature verified, copied to Desktop/APKBOOP and installed preserving data; v235 rollback retained. APK SHA25693608fae1f3566943fe11e37147a37b5d0bb48bf6938ff711c5ecd054bee06ec. A single actual labelled Flow-button click produced fresh native PLAYING playback/queue while the same BOOP HOME activity and display0 focus stayed in place. Ryan subsequently confirmed that the button works perfectly; preserve his album-art browsing -> album listening -> manual Flow shortcut workflow. No recording, new emulator run, new grants, extra ready skip or Wall install. Detailed receipt: docs/handoffs/2026-09-20-shield-flow-button-v236.md.

## User-accepted v236 Flow and album-to-Flow use case: 2026-09-20

Ryan reports that the installed Flow button works perfectly. His stated use case is to browse to an album by selecting the Now Playing artwork, listen to the album, then press Flow when it finishes without digging through Deezer's menus. Preserve the direct Flow shortcut alongside album-art browsing and the accepted favourite controls; this is the reason Flow replaced Close player.

This records user acceptance of the installed Flow feature and the described listening workflow. It does not request automatic Flow at album completion, establish cold-start/Cast support, or add a separate measured end-of-queue test. Source5bfb8d91f3351cbe1c089fb5e0a2566334db2553 and the verified v236 APK are unchanged. This feedback update is documentation-only: no build, install, permissions, recording, device input or playback change.

## Queue investigation, not implemented: 2026-09-20

Ryan is interested in a normal album/playlist Queue, explicitly excluding Flow. Live read-only inspection found an actual published Flow marker (`com.deezer.METADATA_KEY_STREAM_CONTEXT_TYPE=flow_partner`, listen type SMART_RADIO), so hiding it need not rely on queue length. Matched-provider mapping also defines album_partner and playlist_partner; those modes have not yet been captured live in this task. The current Flow list is listening history through the current item, not upcoming suggestions. Queue titles/artists and unique numeric queue IDs are available; standard item media IDs are absent. Long playlists use a limited published window.

The provider implements a queue-jump handler but does not advertise SKIP_TO_QUEUE_ITEM in the inspected live state. A direct normal-album row selection still needs a physical test before shipment. Proposed Queue panel/entry is not yet implemented or layout-approved. Hide during Flow/radio/unknown context, use event-driven native metadata/queue callbacks, and preserve v236 Flow/hearts/Lyrics. No playback, app source, APK, permissions, recording or emulator changes were made. Details: docs/handoffs/2026-09-20-deezer-queue-flow-exclusion-investigation.md.

## In progress: v237 approved Queue panel

Ryan approved Queue beside Lyrics, hidden during Flow. The physical native album queue jump is now confirmed, preserving album order. The v237 source candidate adds the charcoal remote list, real current-track indication, same-queue selection and Flow exclusion with stale-row/late-metadata guards. Twelve focused tests pass, including34 native queue assertions; full CI and the new installed panel remain pending. No working heart/Lyrics/audio code was altered. Details: docs/handoffs/2026-09-20-shield-queue-v237.md. The earlier investigation below is historical and its layout/jump questions are superseded by this approval and probe.
