# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest state

Ryan physically confirmed the `0.2.2` fullscreen build removed the stubborn Pixel status-bar clock and physically confirmed `0.2.3` swipe-up / swipe-down drawer navigation works perfectly.

`0.3.0` added the approved widget/page plumbing, but Ryan physically found the entry point was dead: holding empty HOME produced no menu. `0.3.1` fixed that long-press menu and added Launcher page-0 swipe right -> BOOP Wall. CI run `34084595483` proved the menu appears and the Wall handoff path fires.

Ryan then reported that the cross-app transition still looked visually the same in both directions. Launcher `0.3.2` tried an enter-from-right/exit-left OPEN transition and Wall v34 later tried caller-owned transition control, but Ryan still saw no visual difference.

`0.3.3` / code `10` is the requested clean opposite experiment on the Launcher only. BOOP Wall v34 is left untouched. Launcher OPEN is now literally reversed from the 0.3.2 geometry:
- Launcher enters from LEFT (`-100%p -> 0`);
- previous activity exits RIGHT (`0 -> 100%p`).

Application/source test head: `86a3971d173887c2b641f95d175e5fd30426b59f`.

Signed build:
- package `com.boop.launcher`
- versionName `0.3.3`
- versionCode `10`
- GitHub Actions run `34088140401`
- unit tests and Android lint: success
- permanent-signer release build: success
- signed artifact `BOOP-Launcher-Alpha2-signed`
- artifact ID `10005926195`
- APK SHA-256 `359dd04b3268d16d88feac1ebfcb9030ed48057feed2c06d9df81f7334599310`
- existing permanent BOOP signing identity unchanged

The first 0.3.3 CI attempt failed only because the old transition regression still asserted the previous 0.3.2 direction. Production compilation had succeeded. The regression was then updated to assert the user-requested reverse geometry and run `34088140401` passed unit tests, lint, release build and signing.

## Physical check now

Install Launcher `0.3.3` over the current Launcher while keeping Wall v34 unchanged. This creates a clean A/B test: same Wall build, opposite Launcher transition geometry.

CI proves the requested resources and wiring exist; it does not prove what Pixel's cross-app animation compositor will visibly present. Ryan's device observation remains the authority for the visual result.

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

Primary 0.3.3 check: does Wall swipe-left -> Launcher now visibly move opposite to Launcher swipe-right -> Wall?

Widget move/resize/remove, real third-party widget behavior, and page spill/persistence still await Ryan's on-device acceptance.

## Remaining polish

Drawer transition is still not full Launcher3 direct-finger/spring physics. Third-party widget rotation/process-death quirks remain physical-test territory. CI-green and physically accepted remain separate states.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve Alpha 1 as historical fallback until Alpha 2 is fully accepted.
