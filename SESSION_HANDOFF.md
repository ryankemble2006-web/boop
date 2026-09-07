# BOOP Launcher Alpha 2 handoff — 2026-09-07

Owner: Ryan's Launcher work. Authoritative branch: `boop-launcher-alpha2`.
Project: `launcher/`; package remains `com.boop.launcher`.

## Latest state

Ryan physically confirmed the `0.2.2` fullscreen build removed the stubborn Pixel status-bar clock and physically confirmed `0.2.3` swipe-up / swipe-down drawer navigation works perfectly.

`0.3.0` added the approved widget/page plumbing, but Ryan physically found the entry point was dead: holding empty HOME produced no menu. `0.3.1` fixed that long-press menu and added Launcher page-0 swipe right -> BOOP Wall. CI run `34084595483` proved the menu appears and the Wall handoff path fires.

Ryan then physically reported one remaining navigation-polish bug in `0.3.1`: Launcher -> Wall looked correct, but Wall -> Launcher reused the same left-to-right page transition instead of mirroring the swipe direction.

`0.3.2` / code `9` fixes only that transition direction on the Launcher side. Wall source/branches were deliberately left untouched because Wall has concurrent work.

Application source/version commit: `3d2066d8bafa2ba69be70984ce3f060c79ff3b78` (`build: bump Launcher to 0.3.2`).

Signed build:
- package `com.boop.launcher`
- versionName `0.3.2`
- versionCode `9`
- GitHub Actions run `34085524812` — success
- signed artifact `BOOP-Launcher-Alpha2-signed`
- artifact ID `10005093325`
- APK SHA-256 `83a1ead52fb1ccdce8fb59912101e80a6fd3c98d85c07fc12002c772af0667bb`
- existing permanent BOOP signing identity unchanged

## Transition-direction fix

Android 14+ Launcher OPEN transitions now explicitly enter Launcher from the right while the previous activity exits left. This mirrors the already-good Launcher -> Wall direction so Wall swipe-left -> Launcher visually travels right-to-left instead of replaying the opposite transition.

TDD evidence:
- red run `34085403670` failed the new transition-direction regression before production code/resources existed;
- green run `34085524812` passes that regression, unit tests, lint, permanent-signer release build, and the existing Android 16 interaction smoke.

The Android 16 smoke still verifies launcher survival, empty-HOME long press -> visible `Add widget`, menu dismissal, and Launcher page-0 right swipe -> `BOOP_WALL_SWIPE` handoff path with no launcher fatal crash.

The animation direction itself remains a physical visual check for Ryan; CI verifies the mirrored resource geometry and Launcher transition wiring, not subjective device motion.

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

## Physical checklist for 0.3.2

Primary new check:
- Wall swipe left -> Launcher should animate right-to-left;
- Launcher page-0 swipe right -> Wall should keep the already-good opposite direction.

Widget/page checks from 0.3.1 remain pending until Ryan exercises them on-device.

## Remaining polish

Drawer transition is still not full Launcher3 direct-finger/spring physics. Third-party widget rotation/process-death quirks remain physical-test territory. CI-green and physically accepted remain separate states.

## Cross-app boundaries

Wall remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`; Shield remains `com.boop.shieldoverlay`. Keep apps independent. Main owns shared cross-project contracts; this branch owns Launcher implementation state. Preserve Alpha 1 as historical fallback until Alpha 2 is fully accepted.
