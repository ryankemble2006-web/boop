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
- HOME floating-banner direction is liked, but the Home favourite banners were spaced too far apart.

### 0.9.1 Home spacing: physical FAIL

Build head `2ddeed52674eb1c8c82025fc43ededdc82c4f12f`, version 10 / `0.9.1-home-spacing`, workflow `34246775470` SUCCESS, artifact `10064387830`, APK SHA-256 `940aedfe7783f1405357d76a0546653476189175282243b4366b9f4e6c879faf`.

Ryan physically tested 0.9.1 and supplied a real Shield/TV photo. Verdict: **"they got further apart..."**. Treat 0.9.1 Home spacing as a physical failure.

Root cause: the visible HOME banner is 230 dp wide, but 0.9.1 still centred it inside a 250 dp card container with a 6 dp right margin. That left roughly 26 dp of deliberate invisible spacing between visible banner edges. Reducing the outer slot without making the card hug the banner was the wrong abstraction.

### Current visual candidate: 0.9.2 packed Home row

0.9.2 changes HOME favourites only. The physically accepted Apps drawer remains untouched.

- Build head: `85054f38ca584ba200308db2448441633a9347ec`
- Version: 11 / `0.9.2-home-pack`
- Workflow: `34249370093` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10065417245`
- APK SHA-256: `a42afcaf533023fd7605c317b2bc668f02265ab7ab6c1d394957a56b74e4802c`
- Artifact ZIP SHA-256: `f1e360246d1d248e3a8228434a1827a41f87e5114b8c1f76e2472e2c01fbe8fc`
- Permanent BOOP signer reused and verified.
- Existing focused Shield HOME functional suite passed.
- Signed assembly, exact package/version, HOME/Leanback, Accessibility service/router, signer, APK integrity and artifact upload all passed.

0.9.2 implementation scope:

- HOME banner artwork remains 230 dp wide.
- HOME card container now hugs that artwork at 234 dp wide with 2 dp horizontal card padding.
- HOME inter-card right margin is 2 dp.
- Focus may grow slightly over the tiny neighbour gap, matching the intended floating-card treatment.
- Apps drawer code/geometry remains unchanged from the physically liked 0.9 presentation.
- Focus scale, grabbed scale, supplied artwork, grab/reorder, Accessibility override, reboot re-arm and native Recent Apps are unchanged.

Per `BOOP_RULES.md`, there is no GitHub visual/golden/layout acceptance test. Ryan judges this spacing manually on the real Shield.

**0.9.2 is CI/signer green only; HOME spacing physical acceptance is pending.**

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

Install/update to 0.9.2 and judge the HOME favourite row spacing by eye first. The banners should now sit almost shoulder-to-shoulder, with the focused card allowed to enlarge over the tiny gap. Apps drawer should remain exactly as the physically liked 0.9 drawer. If Home spacing is accepted, recheck grab once plus single Home, double Home and reboot before promoting the visual candidate.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
