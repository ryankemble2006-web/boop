# BOOP Shield clean launcher handoff

Updated 2026-09-08. Authoritative branch: `boop-shield-clean-launcher`.

## Product boundary

This remains a standalone Nvidia Shield clean-HOME launcher for physical testing. It is not part of unified/AIO yet.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate and untouched)
- Stock Android TV Home remains installed/enabled as recovery and as the Accessibility override trigger.
- Normal users must not need ADB, developer options, laptop, root or Shizuku.
- Merge into AIO only after Ryan explicitly approves the standalone result.

## Protected physically-green HOME mechanism: 0.8

Build head `af8ebe1147bd56cc952b874c2e4180bd6a44d15d`, version 8 / `0.8.0-reboot-rearm`, workflow `34239594403` SUCCESS, artifact `10061456035`, APK SHA-256 `7088b4be9dca7cb47bd67c740aaea940d71fd2471da623fb3f0c9fe23d5b2ff0`.

Physically confirmed on real Shield:

- banners good;
- grab/reorder works ("the booger moved :)");
- BOOP Home Override can be enabled manually in Shield Accessibility settings;
- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override remains ON across reboot;
- stock Android TV Home does not reclaim the visible HOME surface after reboot.

Ryan's core acceptance message: **"we beat it :)"**.

Treat 0.8's Accessibility override, `onServiceConnected()` reboot re-arm and untouched native double-Home behavior as protected. Visual polish must not modify that mechanism.

## Floating-card visual work

### 0.9.0 floating cards

Build head `79919976adebf5f989a0efd86bef525b6273ed44`, workflow `34244270100` SUCCESS, artifact `10063361724`, APK SHA-256 `7abd913b51329a3c2cef556fa853bfbb8a78007b046d131a12b639daa9bd589b`.

0.9 introduced shared floating card chrome:

- HOME keeps real installed Android TV wide banners with icon fallback;
- Apps drawer keeps real installed square app icons;
- no recolouring/tinting/replacement of installed artwork;
- idle cards have no dark plate;
- focused/selected/grabbed cards show the dark rounded plate;
- focus/grab scale timing unchanged;
- background remains pure black;
- protected HOME override/reboot code untouched.

Physical result from Ryan:

- **Apps drawer: "great". Treat the 0.9 Apps drawer floating-square-icon presentation as physically accepted and preserve it.**
- HOME floating-banner direction is liked, but **the Home favourite banners are spaced too far apart**. 0.9 Home spacing is not physically accepted.

### Current visual candidate: 0.9.1 Home spacing

0.9.1 changes Home favourites only. The Apps drawer is intentionally untouched.

- Build head: `2ddeed52674eb1c8c82025fc43ededdc82c4f12f`
- Version: 10 / `0.9.1-home-spacing`
- Workflow: `34246775470` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10064387830`
- APK SHA-256: `940aedfe7783f1405357d76a0546653476189175282243b4366b9f4e6c879faf`
- Artifact ZIP SHA-256: `46f2153b753a975fe1a7e1db791e1e6de51d9be9b5fbd3a2daf9720b8b46343d`
- Permanent BOOP signer reused and verified.
- Existing focused Shield HOME functional suite passed.
- Signed assembly, exact package/version, HOME/Leanback, Accessibility service/router, signer, APK integrity and artifact upload all passed.

0.9.1 implementation scope:

- Home favourite card slot width tightened from the roomy 0.9 value to sit close around the existing 230 dp banner artwork.
- Home favourite inter-card margin reduced substantially.
- Home-only horizontal card padding reduced so the banner fills the tighter slot naturally.
- Normal Apps card padding is explicitly restored by normal `bind(...)`, keeping the accepted Apps drawer geometry unchanged.
- Focus scale, grabbed scale, banner dimensions, supplied artwork, grab/reorder, Accessibility override, reboot re-arm and native Recent Apps are unchanged.

Per `BOOP_RULES.md`, there is no GitHub visual/golden/layout acceptance test. Ryan judges this spacing manually on the real Shield.

**0.9.1 is CI/signer green but Home-spacing physical acceptance is pending.**

## Background / Ambient Mode boundary

BOOP remains pure black in 0.9.x. Shield/Google Ambient Mode is the existing idle/screensaver layer, not a supported general launcher-wallpaper feed. Leave it separate. Future background work can consider Black as the zero-network default plus a user-chosen local image; online providers need separate approval.

## Setup reality

Known working first-time setup on this Shield firmware:

**Shield Settings -> Accessibility -> Services -> BOOP Home Override -> ON**

Once enabled, the 0.8 mechanism proved it persists across reboot without a retoggle.

## LOCKED Shield contract

**Remove the crap, preserve Shield behavior.**

Keep:

- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- wide Home banners and grab/reorder;
- physically accepted floating square Apps drawer from 0.9;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- no ad, Shop, Discover or sponsored provider.

## Next physical test

Install/update to 0.9.1. Judge only the HOME favourite row spacing first. The banners should now read as one tidy row with a small gap rather than isolated islands, while the focused card can still enlarge cleanly. Apps drawer should look exactly like the physically liked 0.9 drawer. If Home spacing is accepted, recheck grab once plus Home/double-Home/reboot before promoting the visual candidate.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
