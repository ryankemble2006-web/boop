# Shield Launcher Now Playing + Headphones BOOP Design

## Status

Approved by Ryan on 2026-09-08 for the standalone `boop-shield-clean-launcher` validation lane. This design is intentionally **not** merged into `boop-unified` yet.

## Goal

Add a generic Android media-session Now Playing surface to BOOP Shield Home and place the existing approved headphones BOOP as a separate launcher-owned puppet layer above it, while preserving the physically-green Shield HOME replacement behavior.

## Product boundary

- Standalone package remains `com.boop.shieldhome`.
- AIO `com.boop.alpha1` remains untouched until Ryan explicitly approves a later merge.
- The protected Accessibility HOME override, reboot re-arm and native double-Home Recent Apps behavior must not change.
- Apps drawer presentation remains the physically accepted floating square-icon design.
- Existing HOME banner geometry/chrome remains unchanged by this feature.
- No ADB, root, Shizuku, laptop or privileged package manipulation may be required.

## Media source

Now Playing is generic Android media-session integration, not Deezer-specific.

Compatible players may include Deezer, Kodi and any other application that publishes a usable Android `MediaSession`.

The launcher reads active sessions through Android's supported `MediaSessionManager` route. For a normal third-party app this requires Notification Listener special access, so BOOP exposes a plain one-time route in Launcher Settings. The launcher remains fully usable if that access is never granted.

Notification payloads are not read, stored or cancelled. The notification-listener service exists only to receive authority to query active media sessions.

## Session selection

Launcher Settings exposes:

- `Automatic` (default): prefer an active playing session; otherwise retain/use an eligible paused session.
- a preferred installed media player package selected by the user. If that preferred package has an eligible session, it wins; otherwise Automatic fallback keeps the launcher useful.

Stopped, destroyed or otherwise inactive sessions do not keep a stale Now Playing panel visible.

Paused media remains visible.

## Now Playing content

The panel appears **above Favourite apps** and pushes the favourite row downward. It does not cover the favourites row.

When an eligible session exists, show where supplied:

- artwork;
- title;
- artist/subtitle;
- playing/paused state;
- progress and duration.

Artwork must come from the player/session itself. Prefer published `ART`, then `ALBUM_ART`, then display icon metadata. Local `content://`, `file://` or `android.resource://` artwork URIs supplied by the session may be decoded. BOOP does not invent, recolour, scrape or network-fetch cover art.

When no eligible session exists, the panel collapses and reclaims its layout space.

## Transport controls

The panel provides remote-selectable controls in this order:

1. Previous
2. Rewind
3. Play/Pause
4. Fast-forward
5. Next

Each control is enabled only when the selected session advertises the corresponding transport action. Play/Pause uses the appropriate command for the current state.

Selecting the media identity/artwork area reopens the source application through Android's normal launch intent when available.

## Progress

Session metadata/state updates publish the base position, duration, playback speed and update time. While playing, the view may advance the displayed progress locally between media-session callbacks. It must not poll or rebuild the whole HOME screen once per second.

## Launcher Settings

Rename the user-facing `Home rows` page/button to **Launcher Settings**.

Add a Now Playing section containing:

- media access state and one-click route to Android Notification Listener settings;
- preferred player: Automatic or a chosen installed media player.

Existing Shield Home override/recovery and optional-row controls remain available.

## Independent headphones BOOP layer

The existing approved `boop_headphones.png` artwork is reused exactly. Do not regenerate or recolour it.

The puppet is a separate transparent, non-focusable launcher-owned layer above both the launcher UI and Now Playing panel. It is **not** a system overlay and requires no overlay permission.

State behavior:

- no eligible media: puppet hidden;
- paused media: headphones BOOP visible at rest;
- playing media: gentle existing BOOP music sway/bounce;
- track/session change: brief acknowledgement/perk before returning to rest/groove.

The layer must never intercept D-pad focus or clicks. Animation respects the existing BOOP principle of not changing Android global animation scales and should stop when the launcher is not visible or power/animation policy says not to animate.

Exact size/placement is manual physical-visual acceptance by Ryan. Automated tests verify state/routing behavior only, not appearance.

## Architecture

Use focused responsibilities:

1. `NowPlayingSelectionPolicy`: pure deterministic session selection and eligibility.
2. `NowPlayingSnapshot` / state bus: immutable selected-session metadata exposed to the UI.
3. `ShieldNowPlayingManager`: owns Android media controllers, callbacks, current selected controller and transport commands.
4. `ShieldNowPlayingListenerService`: minimal `NotificationListenerService` lifecycle bridge; ignores notification content.
5. `ShieldNowPlayingView`: remote-first panel, metadata, progress and controls.
6. `ShieldNowPlayingPuppetView` + pure motion/policy: independent headphones BOOP renderer.
7. `ShieldHomeStore`: persists preferred player package (`""` means Automatic).
8. `ShieldLauncherActivity`: subscribes once, forwards snapshots to the current HOME view/puppet, and opens media-access settings/source apps.

Media callbacks must update the Now Playing subview directly rather than rerendering favourites, so track/progress events cannot steal HOME focus.

## Testing contract

Non-visual automated coverage must include:

- Automatic session selection prefers playing and retains eligible paused media;
- preferred player wins when eligible and falls back safely when absent;
- stopped/inactive sessions collapse Now Playing;
- transport-action enablement policy;
- preferred player persistence;
- listener service/manifest contract;
- HOME/Settings callbacks expose Now Playing actions;
- puppet mode policy for hidden/rest/groove;
- existing launcher tests, HOME/Leanback, Accessibility override, signer and APK integrity remain green.

Do not add screenshot tests, golden images, hierarchy visual grading or aesthetic source-string assertions.

## Physical acceptance

Ryan owns final Shield acceptance. The first candidate should be tested for:

- access setup succeeds without ADB;
- Deezer/Kodi metadata and artwork appear when their sessions publish it;
- pause keeps the panel visible;
- stop/session destruction collapses it;
- supported transport buttons work;
- player area reopens source app;
- headphones BOOP appears independently and does not steal remote focus;
- single Home still returns to BOOP;
- double Home still opens native Recent Apps;
- reboot takeover remains intact;
- Apps drawer and accepted HOME banner behavior are unchanged.
