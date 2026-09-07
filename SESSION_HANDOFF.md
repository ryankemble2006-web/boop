# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest state

Ryan physically confirmed the `0.2.2` fullscreen build removed the stubborn Pixel status-bar clock and physically confirmed `0.2.3` swipe-up / swipe-down drawer navigation works perfectly.

`0.3.0` added the approved widget/page plumbing, but Ryan physically found the entry point was dead: holding the empty black HOME screen produced no menu, so the widget functionality was unreachable. Do not treat `0.3.0` as accepted.

The failure was reproduced in GitHub Actions run `34084147192`: the signed app launched and survived, CI performed a 900 ms HOME hold, then the UI hierarchy did not contain `Add widget`.

`0.3.1` / code `8` fixes that interaction and adds the approved inverse Wall gesture.

Application source commit: `9f49ccfdc23b8dddb8a0173e1f369c79dc051b96` (`fix: restore Home menu and add Wall return swipe`).

Signed build:
- package `com.boop.launcher`
- versionName `0.3.1`
- versionCode `8`
- GitHub Actions run `34084595483`
- signed artifact `BOOP-Launcher-Alpha2-signed`
- artifact ID `10004790724`
- APK SHA-256 `294206b2ff3f50c7c0f880b952f454980582bb7970bda47430036bfb4fe86d88`
- existing permanent BOOP signing identity unchanged

## 0.3.1 interaction fix

The HOME long-press callback already existed, but its `PopupMenu` was anchored to the entire fullscreen workspace. `0.3.1` anchors the menu to a tiny temporary view at the actual hold point instead, then removes that anchor when the menu closes. This lets the contextual menu appear where Android can actually place it.

The green CI smoke now:
1. installs and launches the signed APK on Android 16;
2. confirms the launcher process survives;
3. performs a 900 ms hold on empty HOME;
4. dumps the UI hierarchy and requires visible `Add widget` text;
5. dismisses the menu;
6. swipes right on HOME;
7. requires the `BOOP_WALL_SWIPE` handoff path to fire;
8. scans for BOOP launcher fatal crashes.

Run `34084595483` passed all of those checks plus unit tests, lint, release build and permanent signing.

## Wall / Launcher gesture loop

Approved contract:
- BOOP Wall (`com.boop.alpha1`) -> deliberate swipe left -> Launcher;
- Launcher page 0 -> swipe right -> BOOP Wall.

Launcher uses Android's launch intent for `com.boop.alpha1` and brings it forward without merging the apps. If Wall is absent, Launcher stays open and shows a plain-English message. Extra Launcher content pages keep normal page navigation: right swipe from a later page returns toward page 0; only right swipe from page 0 exits to Wall.

## Widget/page implementation retained from 0.3.0

- Real `AppWidgetHostView` rendering on HOME.
- Widget pick, bind, configure and cancellation cleanup.
- Pending widget flow persists across launcher recreation.
- Stale/missing providers and orphaned host IDs are cleaned up.
- Normal widget controls remain usable until deliberate long-press editing.
- Long-press + drag moves widgets.
- Long-press from bottom-right + drag resizes widgets with a temporary grip.
- Widget move/size/page data persists.
- Top remove band deletes widgets and releases host IDs.
- Persisted page field is active; horizontal swipes move between existing content pages.
- New pages appear only when content cannot fit on the current page.
- Empty page gaps compact away after removals.

## Protected behavior retained

- Pure-black fullscreen HOME.
- Pixel status bar/clock suppression physically accepted by Ryan.
- Swipe up HOME -> All Apps.
- Swipe down from top of All Apps -> HOME, physically accepted by Ryan.
- Contextual/local app search only.
- No launcher clock, At a Glance, Google search pill, dock/hotseat, Internet permission, microphone permission, or Google proprietary launcher code/assets.
- `com.boop.launcher` and permanent BOOP signing identity unchanged.

## Physical checklist for 0.3.1

Ryan is still the final acceptance test. Check:
- hold empty black HOME -> menu visibly appears;
- tap Add widget -> Android widget picker/config flow opens;
- add a real widget and confirm it renders;
- ordinary widget controls work;
- long-press move and bottom-right resize work;
- widget remove leaves no ghost;
- page spill/navigation persists;
- from launcher page 0 swipe right -> BOOP Wall;
- from BOOP Wall swipe left -> Launcher;
- fullscreen and drawer gestures remain intact.

## Remaining polish

Drawer transition is still not full Launcher3 direct-finger/spring physics. Third-party widget rotation/process-death quirks remain physical-test territory. CI-green and physically accepted remain separate states.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve Alpha 1 as historical fallback until Alpha 2 is fully accepted.
