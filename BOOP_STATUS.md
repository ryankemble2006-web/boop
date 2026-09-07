# BOOP Launcher Alpha 2 status

## Current

Launcher Alpha 2 now has a physically confirmed pure-black fullscreen HOME, swipe-up All Apps, and swipe-down-to-HOME navigation. Ryan confirmed the `0.2.2` fullscreen fix removed the persistent Pixel clock/status bar and confirmed the `0.2.3` swipe-down behavior works perfectly.

`0.3.0` / code `7` completes the approved widget and dynamic-page fundamentals and is signed for Ryan's physical test.

Application source: `11adc4cbe7df0c63cfb772c9986a0e6f45c0d054`.
GitHub Actions run: `34083701591`.
Signed artifact ID: `10004513657`.
APK SHA-256: `0ed19b064c56cead4b47c735c59665eb741faab6b8e5b25c581a0d0802eca3a6`.

## 0.3.0 implementation

- Real Android `AppWidgetHostView` rendering on HOME.
- Widget pick, bind, configure and cancellation cleanup.
- Pending widget flow survives launcher recreation.
- Stale/missing widget providers and orphaned host IDs are cleaned up.
- Normal widget child controls stay usable until deliberate long-press editing.
- Long-press + drag moves widgets.
- Long-press bottom-right + drag resizes widgets with a temporary resize grip.
- Widget move/size/page data persists in the existing workspace schema.
- Top remove band deletes widgets and releases host IDs.
- Persisted page field is active: horizontal swipes move between existing content pages.
- New pages are created only when adding content cannot fit on the current page.
- Empty page gaps compact away after removals; no permanent empty page carousel or page furniture.
- App/widget overlap handling is page-aware.

## Verification level

The existing GitHub workflow's compile/lint gate completed successfully and the permanent BOOP signer produced the artifact. Ryan explicitly requested no emulator/UI testing for this pass, so `0.3.0` is signed and ready for physical verification, not physically accepted yet.

## Physically confirmed from prior builds

- pure-black fullscreen HOME;
- persistent Pixel status-bar clock removed;
- swipe up opens All Apps;
- swipe down from the top of All Apps returns HOME.

## Physical checks for 0.3.0

- add/configure a real widget and confirm it renders;
- confirm ordinary widget controls work;
- long-press move and bottom-right long-press resize;
- remove widget and confirm no ghost remains;
- overflow content to another page and swipe between pages;
- relaunch and confirm widget/page placement persists;
- confirm fullscreen and drawer gestures remain intact.

## Remaining polish

- Drawer motion is still not full Launcher3 direct-finger/spring physics.
- Third-party widget rotation/process-death quirks remain physical-test territory.

## Protect

- Preserve `boop-launcher-alpha1` as historical fallback until Alpha 2 is fully accepted.
- Keep package `com.boop.launcher` and the existing BOOP signing identity.
- Do not publish signing keys/private certificates.
- Keep Wall and Shield app lineages independent.
- Signed and physically accepted are separate states.
