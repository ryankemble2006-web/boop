# Shield Clean Launcher Design

## Status

Approved by Ryan on 2026-09-08 for implementation on the canonical `boop-unified` lineage.

## Goal

Create a fast, stock-feeling Shield TV HOME replacement that defaults to favourite apps only, removes advertising and promotional surfaces entirely, preserves smooth remote-first animation, and lets users opt back into useful non-advertising home rows later.

## Product boundary

This is a separate launcher implementation inside the unified BOOP lineage. It does not modify, patch, replace-sign, uninstall, disable, or overwrite Google's stock Android TV launcher APK. The stock launcher remains installed as a recovery path during physical testing.

The canonical BOOP package and permanent signer remain unchanged. Existing Wall and Shield puppet behavior are outside this launcher's functional scope unless integration requires a minimal route into the new launcher activity.

## Default home experience

The initial HOME screen contains only the user's favourite applications.

Default visible content:

- one horizontally navigable favourite-app row;
- large TV-friendly app cards;
- the user's chosen order;
- a route to the full Apps screen;
- a route to launcher settings;
- no other content rows enabled by default.

The background remains visually quiet. Removing a row must reclaim its layout space rather than leave a blank advertising-sized area.

## Explicit removals

The launcher must contain no implementation for:

- advertising banners;
- sponsored tiles or placements;
- Shop surfaces;
- Discover surfaces;
- promotional autoplay;
- ad-feed or promotional-feed fetching;
- hidden placeholders for removed promotional modules.

Advertising content must not be fetched and then hidden. The relevant feed/provider integration must simply not exist in the launcher.

## Optional home rows

Useful non-advertising rows may be supported as optional modules, but every such row starts OFF.

The launcher settings screen contains a simple `Optional Home Rows` section where users can independently enable or disable supported rows such as:

- Continue Watching / Play Next, where a compatible Android TV source is available;
- app-provided recommendation/content channels, where supported;
- future local-only BOOP rows that are separately approved.

Enabling or disabling one row must not affect the others.

A disabled row must not be instantiated, subscribed, polled, or fetched merely to remain hidden. The saved enabled/disabled state persists across launcher restarts and device reboots.

Advertising, Shop, Discover and sponsored content are not restorable options.

## Favourite apps behavior

Users can choose which installed launchable apps appear on HOME and arrange their order.

Required interactions:

- D-pad left/right moves focus across favourites;
- Select launches the focused app;
- long-press opens a compact edit action for the focused favourite;
- the edit action supports moving/reordering and removing the app from favourites;
- removing an app from favourites does not uninstall it;
- the Apps screen shows installed launchable TV apps and allows adding an app to favourites;
- favourite order and membership persist.

The UI must remain usable with the standard Shield remote. Touch must never be required.

## Motion and focus behavior

Do not disable Android system animations or change global animator/transition scales.

The launcher should preserve the familiar Android TV feel through local UI animation:

- focused cards enlarge smoothly;
- row movement scrolls smoothly;
- entering and leaving Apps or Settings uses short transitions;
- animation must not block D-pad input or make repeated navigation feel sluggish.

No visual acceptance automation, screenshot/golden tests, aesthetic source-string checks or emulator appearance grading may be added. Ryan performs visual acceptance on real hardware.

## Performance design

The launcher should do substantially less work than a promotional launcher by default.

On the favourite-only home screen:

- do not initialize disabled optional rows;
- do not bind to promotional/ad services;
- do not start periodic content refresh work for disabled rows;
- avoid repeated package scans on every focus movement or redraw;
- cache the installed-app/favourite model and refresh it on relevant package changes or explicit lifecycle reloads;
- keep rendering logic independent from content discovery so a slow provider cannot stall basic D-pad navigation.

Performance claims must be based on functional/device evidence. Do not claim a specific speedup percentage without measurement.

## Architecture

Use four clear responsibilities inside the Shield launcher body:

1. **Launcher activity / navigation shell**
   - owns HOME, Apps and Launcher Settings navigation;
   - exposes a valid Android TV HOME intent entry point;
   - does not own package discovery or preference persistence logic.

2. **App catalogue and favourites model**
   - discovers launchable TV applications;
   - stores favourite membership and ordering;
   - updates on package add/remove/change events;
   - exposes a stable model to the UI.

3. **Optional-row registry**
   - defines only approved non-advertising rows;
   - persists enabled/disabled state;
   - creates a row provider only when that row is enabled;
   - contains no advertising/Shop/Discover provider.

4. **Remote-first views/adapters**
   - render app cards and optional rows;
   - own focus scaling and local transitions;
   - remain presentation-only and do not perform network/content discovery directly.

These boundaries should allow the favourite-only path to run without creating optional row providers at all.

## Recovery and launcher selection

Installing the BOOP APK must not silently change the system's selected HOME app.

Physical rollout should remain reversible:

- install/update the normal signed BOOP APK;
- explicitly choose the BOOP launcher through Android's supported HOME selection flow during testing;
- keep the stock launcher installed;
- provide a clear route or documented Android path to select the stock launcher again.

Do not disable Google's launcher, use privileged shell hacks, silently alter secure settings, or rely on replacing Google's signature.

## Integration with unified BOOP

The unified routing contract remains intact for existing bodies. The new Shield launcher belongs to the canonical `boop-unified` source and must not fork a new long-lived app lineage merely for convenience.

Implementation must preserve:

- package `com.boop.alpha1`;
- permanent signer;
- existing physically accepted HA naming/control behavior;
- working blink behavior and its timing/lifecycle gates;
- idempotent Shield density behavior;
- existing Wall/Shield contracts unrelated to HOME.

Any manifest change required to expose a HOME activity must be narrowly scoped and must not break the existing unified entry activity or Shield puppet routes.

## Error and edge behavior

- If a favourite app is uninstalled, remove it cleanly from the rendered favourite list without crashing.
- If no favourites remain, show a simple remote-selectable action to open Apps and add one.
- If an optional provider fails, HOME favourite navigation must remain functional; the failing optional row may be omitted for that session with a plain-English non-blocking indication in settings if needed.
- If stored favourite data references an unavailable package, ignore/prune the stale entry during model reconciliation.
- If launcher preference storage is unreadable or absent, recover to safe defaults: favourite-only layout, all optional rows OFF.

## Testing contract

Automated verification should cover behavior, not appearance.

Required non-visual checks:

- HOME intent/activity manifest contract;
- favourite add/remove/reorder persistence;
- stale/uninstalled favourite reconciliation;
- optional rows default OFF;
- enabling/disabling rows persists independently;
- disabled rows do not create/start their provider;
- no advertising/Shop/Discover provider or dependency is part of the launcher module;
- Apps catalogue refresh on package changes;
- D-pad/select/long-press command routing at the logic level where practical;
- existing unified, Shield and signing/package integrity tests remain green.

Do not add GitHub visual checks, screenshot/golden comparisons, aesthetic source-string tests, emulator install/launch acceptance, or global animation-setting changes.

## Physical acceptance

Ryan owns device acceptance on the Nvidia Shield.

A candidate is not physically accepted until he confirms at minimum:

- BOOP can be selected as HOME and returns reliably when Home is pressed;
- default HOME contains only favourites plus minimal navigation affordances;
- adverts, Shop and Discover are absent;
- no blank advert-sized space remains;
- focus zoom and scrolling feel smooth;
- favourites launch correctly;
- add/remove/reorder works with the Shield remote;
- optional rows can be enabled and disabled and remain remembered;
- stock launcher can still be restored;
- repeated launcher opens do not reproduce cumulative UI scaling/shrinking.

CI-green and signed do not imply this physical acceptance.

## Out of scope

This design does not include:

- modifying Google's launcher APK;
- decompiling or redistributing Google launcher source/assets;
- disabling system animation;
- a new visual theme or icon skin;
- widgets enabled by default;
- advertising of any kind;
- automatic default-launcher takeover;
- changes to BOOP eyes, blink, HA behavior, assistant selection, microphones, signing or unrelated Shield Turbo features.
