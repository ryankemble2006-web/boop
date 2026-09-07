# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest physically confirmed state

Ryan has physically confirmed on the Pixel:
- `0.2.2` removed the stubborn status-bar clock;
- `0.2.3` swipe-up / swipe-down drawer navigation works perfectly;
- `0.3.1` long-press HOME menu works and page-0 swipe right returns to BOOP Wall;
- reversing left/right cross-app slide geometry in `0.3.2` / `0.3.3` did not visibly change Android's white swish;
- `0.3.4` physically removed the white swish;
- `0.3.5` smoothed the handoff, but Ryan physically reported that the Android system bars returned during the transition.

## Current candidate — `0.3.6` / code `13`

Ryan chose to remove both cross-app transition effects completely for now and design one matched swoosh for both directions later.

`0.3.6` therefore uses an intentionally plain black-safe handoff:
- no Launcher fade-out;
- no Launcher fade-in;
- no destination `ActivityOptions` fade;
- Wall launch uses `FLAG_ACTIVITY_NO_ANIMATION` plus `overridePendingTransition(0,0)`;
- Launcher continues to disable OPEN/CLOSE activity transitions on Android 14+;
- Launcher explicitly re-hides system bars in `onResume()`, `onNewIntent()`, window-focus return, and immediately before launching Wall;
- Launcher content is restored immediately at alpha 1 rather than animated;
- pure-black window, disabled preview, and black Android 12+ splash remain intact.

BOOP Wall source remains untouched. The future matched swoosh is intentionally deferred until the no-animation bridge is physically accepted.

## Build / verification

Application/source head: `bdc88de44290470e958ea6e6b53cad6026dc964e`.

Signed build:
- package `com.boop.launcher`
- versionName `0.3.6`
- versionCode `13`
- GitHub Actions run `34091910098`
- no-animation immersive-handoff regression: success
- unit tests and Android lint: success
- permanent-signer release build: success
- signed artifact `BOOP-Launcher-Alpha2-signed`
- artifact ID `10007159008`
- existing permanent BOOP signing identity unchanged

The Android 16 interaction smoke may continue after artifact creation. Ryan's Pixel remains the authority for system-bar visibility and subjective transition appearance.

## Physical check now

Install Launcher `0.3.6` over the current Launcher while keeping the same Wall build installed.

Expected result:
- no white swish;
- no fade;
- no deliberate cross-app animation in either direction;
- status/navigation bars remain suppressed by Launcher;
- Wall and Launcher simply switch instantly.

If that is physically clean, keep this as the stable bridge and design a matched custom swoosh for both directions later.

## Wall / Launcher gesture loop

Approved contract:
- BOOP Wall (`com.boop.alpha1`) -> deliberate swipe left -> Launcher;
- Launcher page 0 -> swipe right -> BOOP Wall.

Extra Launcher content pages keep normal page navigation: right swipe from a later page returns toward page 0; only right swipe from page 0 exits to Wall.

## Widget/page implementation retained

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
- Pixel status bar/clock suppression physically accepted before transition experiments.
- Swipe up HOME -> All Apps.
- Swipe down from top of All Apps -> HOME, physically accepted by Ryan.
- Contextual/local app search only.
- No launcher clock, At a Glance, Google search pill, dock/hotseat, Internet permission, microphone permission, or Google proprietary launcher code/assets.
- `com.boop.launcher` and permanent BOOP signing identity unchanged.

## Remaining physical checks

Primary `0.3.6` check: are the bars gone again with both cross-app animations removed?

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still await Ryan's on-device acceptance.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve Alpha 1 as historical fallback until Alpha 2 is fully accepted.
