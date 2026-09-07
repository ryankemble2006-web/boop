# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest state

Ryan physically confirmed the `0.2.2` fullscreen build removed the stubborn Pixel status-bar clock, and then physically confirmed the `0.2.3` inverse drawer gesture works perfectly: swipe up opens All Apps, swipe down from the top closes it back to HOME.

Ryan then explicitly requested completion of the previously approved Alpha 2 widget/page scope, with no emulator/UI testing for this pass and immediate signing for his own physical test.

Application source commit: `11adc4cbe7df0c63cfb772c9986a0e6f45c0d054` (`feat: finish Alpha 2 widgets and dynamic pages`).

Signed build:
- package `com.boop.launcher`
- versionName `0.3.0`
- versionCode `7`
- GitHub Actions run `34083701591`
- signed artifact `BOOP-Launcher-Alpha2-signed`
- artifact ID `10004513657`
- APK SHA-256 `0ed19b064c56cead4b47c735c59665eb741faab6b8e5b25c581a0d0802eca3a6`
- existing permanent BOOP signing identity unchanged

The existing GitHub signing workflow's unavoidable compile/lint stage completed successfully before the signed artifact was taken. Per Ryan's explicit request, this pass was not held for emulator/UI validation. Treat `0.3.0` as signed and ready for Ryan's physical verification, not as a new physical checkpoint.

## What 0.3.0 completes

### Widgets

- Real `AppWidgetHostView` instances are rendered on HOME instead of placeholder records.
- System widget picker flow allocates host IDs and supports provider binding, provider configuration, cancellation cleanup, and persisted pending state across launcher recreation.
- Cancelled widget flows release their allocated host IDs rather than leaving ghosts.
- Missing/uninstalled widget providers are pruned from the workspace and their IDs released.
- Host IDs no longer referenced by the persisted workspace are cleaned up.
- Normal widget child interaction remains available until the user deliberately long-presses the widget.
- Long-press + drag moves a widget.
- Long-press from the widget's bottom-right area + drag resizes it; a temporary resize grip appears during that interaction.
- Moving or resizing persists normalized position and size.
- Dragging a held widget into the existing top remove band deletes the widget and releases its host ID.
- Widget size options are updated after placement/resize so providers can adapt their layout.

### Dynamic HOME pages

- Existing persisted `page` data is now active rather than dormant.
- HOME renders only the current page.
- Horizontal swipe on empty HOME changes between existing content pages without adding permanent page chrome/dots.
- Adding an app/widget first tries the current page; if it does not fit, a new page is created automatically and becomes current.
- Removing content compacts empty page gaps so there is no permanent empty carousel.
- App and widget overlap checks now share the same page-aware placement model.

## Protected behavior retained

- Pure-black fullscreen HOME.
- Pixel status bar/clock suppression that Ryan physically accepted.
- Swipe up from HOME opens All Apps.
- Swipe down from the top of All Apps returns HOME and was physically accepted.
- App-drawer local/on-demand search remains contextual only.
- No launcher clock, At a Glance, Google search pill, dock/hotseat, Internet permission, microphone permission, or Google proprietary launcher code/assets.
- `com.boop.launcher` and the permanent BOOP signing identity remain unchanged.

## Physical checklist for 0.3.0

Ryan is the acceptance test for this build. Check:
- long-press empty HOME -> Add widget -> choose/configure a widget;
- widget actually renders and remains interactive normally;
- long-press widget and drag moves it;
- long-press its bottom-right area and drag resizes it;
- remove a widget through the top remove band;
- add enough content to spill to another page, then swipe horizontally between content pages;
- relaunch and confirm widget/page placement persists;
- confirm fullscreen and up/down drawer gestures remain intact.

## Remaining polish, not blockers for this physical pass

The drawer transition is still not full Launcher3 direct-finger/spring physics. Rotation/process-death behavior for every third-party widget cannot be called physically accepted until Ryan exercises it. Do not manufacture a physical checkpoint from the signed build.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve Alpha 1 as historical fallback until Alpha 2 is fully accepted.
