# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched by this APK)
- Version: 3 / `0.3.0-input-routing`
- Green build head: `821369cd2e51f82922b8f7cfbc71aa3edb93fb0e`
- Workflow: `34223973168` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10054934429`
- APK SHA-256: `5c38cdb2b56397cf55e7d2f889e5b5c66b5f2719f556cb7087c3eea5fe2ee3e8`
- Artifact ZIP SHA-256: `e7ab325772bc1240eb7d38ba0cd86411d565267a8c315b773578272910355a8a`
- Permanent BOOP signer reused and verified

This remains intentionally standalone for Shield testing before any later AIO merge.

## Physical evidence

Ryan physically confirmed the 0.2 TV banners look good. Preserve them.

0.2 grab movement physically failed: after long-press, pressing Right moved ordinary navigation/focus one position while the favourite/icon itself stayed put. The old card-level D-pad listener is therefore not accepted.

Ryan locked new Back semantics: **single Back -> favourite item 1; long Back -> real Shield/Android Settings**.

## 0.3 repair

Grab input now intercepts at `ShieldHomeView.dispatchKeyEvent()` before Android focus navigation. While grabbed, Left/Right move the selected component through the row; Up/Down cannot escape; initiating centre-button repeat events are swallowed; later Select/Enter drops/persists.

Back is intercepted at `ShieldLauncherActivity.dispatchKeyEvent()`. Short release returns HOME/focus to favourite item 1 and cancels/restores an active grab. A timed long hold opens `Settings.ACTION_SETTINGS` and suppresses the short action on release.

TDD evidence: `34223227200` RED on missing Back gesture controller; `34223430669` RED with 37 tests / exactly 3 expected missing input-routing methods. Implementation workflow `34223747577` passed. Final versioned workflow `34223973168` passed focused tests, signing, signed build, package/version, HOME/Leanback categories, APK integrity and artifact upload.

Default HOME remains favourites-first. Favourite HOME cards preserve Android TV banners with icon fallback. Optional Play Next/app-content rows remain independently OFF by default. Ads, sponsored content, Shop and Discover have no provider. Disabled rows do not instantiate/fetch providers. Top-right Settings still opens real system Settings; launcher-only controls remain under `Home rows`.

LOCKED: **remove the crap, preserve Shield behavior.** Physical acceptance must preserve double-tap Home -> Recent Apps/task switcher, single Back -> favourite item 1, long Back -> real Settings, volume/CEC, system shortcuts, app switching and system animations.

0.3 is CI/signer green, **not yet physically accepted**. Ryan must confirm actual tile movement and Back behavior on the real Shield before any merge into unified.
