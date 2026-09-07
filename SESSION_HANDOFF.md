# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest state

Ryan physically confirmed the `0.2.2` fullscreen build removed the stubborn Pixel status-bar clock and physically confirmed `0.2.3` swipe-up / swipe-down drawer navigation works perfectly.

`0.3.0` added the approved widget/page plumbing, but Ryan physically found the entry point was dead: holding empty HOME produced no menu. `0.3.1` fixed that long-press menu and added Launcher page-0 swipe right -> BOOP Wall.

Ryan then physically found the Wall/Launcher cross-app transition still looked the same despite deliberately reversing the Launcher animation in `0.3.2` and `0.3.3`. Most importantly, he confirmed `0.3.3` still showed the same unwanted white swish. Treat that physical result as authoritative: changing left/right resource geometry did not solve the visible transition.

`0.3.4` / code `11` therefore changes strategy completely. It no longer tries to steer the Android cross-app slide. Instead, Launcher uses a black-safe handoff:
- Launcher activity OPEN/CLOSE system transitions are disabled on Android 14+;
- Launcher uses a pure-black window background and black Android 12+ splash background;
- window preview is disabled;
- the old directional animation XML resources have been removed;
- Launcher -> Wall fades Launcher content to black over 110 ms, launches Wall with `FLAG_ACTIVITY_NO_ANIMATION`, and requests a zero pending transition;
- when Launcher becomes visible it fades its own content in from black over 140 ms.

BOOP Wall v34/source is deliberately untouched. The purpose of this build is specifically to test whether replacing the cross-app slide with a black fade/cut removes the visible white swish on Ryan's Pixel.

Application/source head before documentation: `61606736fea597e86c37c9c9a5259fdb54617abb`.

Signed build:
- package `com.boop.launcher`
- versionName `0.3.4`
- versionCode `11`
- GitHub Actions run `34089318255`
- unit tests and Android lint: success
- permanent-signer release build: success
- signed artifact `BOOP-Launcher-Alpha2-signed`
- artifact ID `10006292363`
- APK SHA-256 `412cd7162e6c7562a93ce62534559ab255bf6f5b040145941c586c743b4474e9`
- existing permanent BOOP signing identity unchanged

The first Android 16 smoke attempt for run `34089318255` failed in emulator infrastructure after a successful install and cold launch: UIAutomator returned a null root node while trying to dump the HOME hierarchy, so the smoke could not continue to its menu/swipe assertions. No Launcher fatal crash was shown in that failure. The smoke job was explicitly retried; check the latest run/job state before claiming full smoke-green status.

## Physical check now

Install Launcher `0.3.4` over the current Launcher while keeping Wall v34 unchanged.

Expected visual experiment:
- no directional white cross-app slide should be requested by Launcher;
- Launcher -> Wall should briefly fade into the existing pure-black background, then reveal Wall;
- Wall -> Launcher should reveal Launcher from black rather than using the old Launcher slide resource.

This is intentionally a different animation family, not another left/right reversal. Pixel physical observation remains the authority for whether the white swish is actually gone.

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

Primary `0.3.4` check: does the new black fade/cut path remove the white swish on the Pixel in both directions?

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still await Ryan's on-device acceptance.

## Remaining polish

Drawer transition is still not full Launcher3 direct-finger/spring physics. Third-party widget rotation/process-death quirks remain physical-test territory. CI/build-green and physically accepted remain separate states.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve Alpha 1 as historical fallback until Alpha 2 is fully accepted.
