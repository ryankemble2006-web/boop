# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest physically confirmed state

Ryan has physically confirmed on the Pixel:
- `0.2.2` removed the stubborn status-bar clock;
- `0.2.3` swipe-up / swipe-down drawer navigation works perfectly;
- `0.3.1` long-press HOME menu works and page-0 swipe right returns to BOOP Wall;
- reversing left/right cross-app slide geometry in `0.3.2` / `0.3.3` did not visibly change Android's white swish;
- `0.3.4` changed strategy and physically **removed the white swish**.

Ryan's only complaint about `0.3.4`: after the accepted black handoff, BOOP Wall simply appears too abruptly.

## Current candidate — `0.3.5` / code `12`

`0.3.5` smooths that accepted black transition without reintroducing any directional slide.

Launcher -> Wall now does:
1. Launcher content fades completely to black over 120 ms.
2. Launcher starts BOOP Wall using a destination-only Android fade-in (`ActivityOptions.makeCustomAnimation(this, android.R.anim.fade_in, 0)`).
3. Launcher remains black underneath while Wall appears; it is no longer restored immediately after `startActivity()`.
4. If Wall launch fails, Launcher restores itself and shows the existing plain-English toast.

Launcher return/resume now:
- if Launcher was left fully black by the Wall handoff, `onResume()` explicitly calls `revealLauncher()`;
- Launcher content fades back from black over 180 ms.

Black-safety retained:
- Launcher OPEN/CLOSE activity transitions disabled on Android 14+;
- pure-black window background and Android 12+ splash;
- window preview disabled;
- no old directional transition resources;
- no `FLAG_ACTIVITY_NO_ANIMATION` or zero `overridePendingTransition()` cut on the Wall launch anymore, because the new destination fade replaces that abrupt reveal.

BOOP Wall v34/source is deliberately untouched.

## Build / verification

Application/source head: `56633b3c35e0ed0f01df76bb7ea15fae5c1fcde0`.

Signed build:
- package `com.boop.launcher`
- versionName `0.3.5`
- versionCode `12`
- GitHub Actions run `34090705961`
- build job: success
- black-safe fade regression: success
- unit tests and Android lint: success
- permanent-signer release build: success
- signed artifact `BOOP-Launcher-Alpha2-signed`
- artifact ID `10006748214`
- APK SHA-256 `3e8515f662cc4e83a5e23384b698b8c79687bd897de02c9ace901fe8e8178fa5`
- existing permanent BOOP signing identity unchanged

The Android 16 interaction smoke was still running when this handoff was written. That smoke verifies launcher survival/menu/return-swipe mechanics, not subjective transition appearance. Ryan's Pixel remains the authority for whether the fade feels right.

## Physical check now

Install Launcher `0.3.5` over the current Launcher while keeping the same Wall v34 installed.

Expected visual result:
- no white page/sheet swish;
- Launcher gently disappears into black;
- BOOP Wall then fades up from that black rather than popping into existence;
- returning to Launcher also fades it back from black.

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
- Pixel status bar/clock suppression physically accepted by Ryan.
- Swipe up HOME -> All Apps.
- Swipe down from top of All Apps -> HOME, physically accepted by Ryan.
- Contextual/local app search only.
- No launcher clock, At a Glance, Google search pill, dock/hotseat, Internet permission, microphone permission, or Google proprietary launcher code/assets.
- `com.boop.launcher` and permanent BOOP signing identity unchanged.

## Remaining physical checks

Primary `0.3.5` check: is the now-black-safe Wall reveal perceptually smooth enough?

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still await Ryan's on-device acceptance.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve Alpha 1 as historical fallback until Alpha 2 is fully accepted.
