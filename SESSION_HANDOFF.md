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
- grab/reorder works;
- BOOP Home Override can be enabled manually in Shield Accessibility settings;
- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override remains ON across reboot;
- stock Android TV Home does not reclaim the visible HOME surface after reboot.

Treat 0.8's Accessibility override, `onServiceConnected()` reboot re-arm and untouched native double-Home behavior as protected. Visual polish must not modify that mechanism.

## Floating-card visual work

### 0.9.0 floating cards

Build head `79919976adebf5f989a0efd86bef525b6273ed44`, workflow `34244270100` SUCCESS, artifact `10063361724`, APK SHA-256 `7abd913b51329a3c2cef556fa853bfbb8a78007b046d131a12b639daa9bd589b`.

Physical result:

- Apps drawer: **physically accepted** by Ryan ("app drawer great"). Preserve its floating square-icon presentation exactly.
- HOME floating-banner direction is liked, but spacing/focus needed refinement.

### 0.9.1 Home spacing: physical FAIL

Build head `2ddeed52674eb1c8c82025fc43ededdc82c4f12f`, version 10 / `0.9.1-home-spacing`, workflow `34246775470` SUCCESS, artifact `10064387830`, APK SHA-256 `940aedfe7783f1405357d76a0546653476189175282243b4366b9f4e6c879faf`.

Ryan supplied a real Shield/TV photo and reported: **"they got further apart..."**. The visible banner was still centred in an oversized card shell.

### 0.9.2 packed Home row: physical FAIL

Build head `85054f38ca584ba200308db2448441633a9347ec`, version 11 / `0.9.2-home-pack`, workflow `34249370093` SUCCESS, artifact `10065417245`, APK SHA-256 `a42afcaf533023fd7605c317b2bc668f02265ab7ab6c1d394957a56b74e4802c`.

Idle spacing improved, but Ryan's physical verdict was: **"better but we now have overlap."** Whole-card focus scaling expanded into neighbouring banners.

### 0.9.3 focus clearance: physical FAIL

Build head `99900d761a8dc11c7c17d6989898aee7e9582796`, version 12 / `0.9.3-focus-clearance`, workflow `34251324746` SUCCESS, artifact `10066192982`, APK SHA-256 `99c5896c78a760de8ff22e9ccae792d8c6245854ca5fb772f009ce8feca20295`.

Ryan physically tested 0.9.3 and supplied another real Shield/TV photo. Verdict: **"nope. need spacing.. remove the black box behind icons, then when scrolled on make the icon slightly bigger without breaking the adjacent icons boundaries"**.

This superseded the earlier Home chrome direction. The black Home focus plate is rejected. Home focus must not scale the whole card or move/cover neighbours.

### 0.9.4 Home artwork focus: physical result GOOD, pop slightly too subtle

Build head `a8a207c97ddddc9b8b36ef99fa7b8718d91588c0`, version 13 / `0.9.4-home-artwork-focus`, workflow `34253939244` SUCCESS, artifact `10067129563`, APK SHA-256 `8840c68a834e7c65b8473631dca5c0e05929a3f931621c4bc232a96155e17909`.

0.9.4 changed HOME only:

- HOME never shows the dark focus/selection/grab plate;
- normal HOME favourite stars are removed;
- grab still shows `↔`;
- card and label stay fixed at 1.0 scale;
- only installed banner artwork grows on focus/grab;
- HOME uses a fixed 240 dp lane around the 230 dp banner with a 6 dp inter-card margin;
- Apps drawer remains the physically accepted 0.9 presentation;
- protected Home override/reboot/Recent Apps code unchanged.

Ryan physically tested 0.9.4 and reported: **"awesome.. make them pop out a few more pixels when highlighted. almost perfect"**.

Treat 0.9.4's spacing, no-overlap geometry, no-black-box rule, no-star rule and fixed labels as physically successful. The only requested follow-up is a slightly stronger **focused banner artwork** pop. This is not yet the final visual checkpoint because Ryan explicitly requested that refinement.

### Current visual candidate: 0.9.5 stronger Home focus pop

0.9.5 changes exactly one runtime value from 0.9.4: HOME focused banner artwork grows from `1.03` to `1.05`. HOME grab artwork remains `1.03`.

- Build head: `66a15f89969c547224ed22d962f609243effae6e`
- Feature source head before version bump: `bec0cb94c8127e8ce54fb22d73445cd0d9c42097`
- Version: 14 / `0.9.5-home-focus-pop`
- Workflow: `34255507581` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10067748314`
- APK SHA-256: `e42d2f9c85244a52ec3124dd9d68d6cac6bcf56878d122c7743f7e9ea942d00e`
- Artifact ZIP SHA-256: `556f9aedb15ef4ad3b7a4effc8ce5ecbc4465b36588eeaf1de1b08d2272bc36b`
- Permanent BOOP signer reused and verified.
- Existing Shield HOME functional suite passed.
- Signed assembly, exact package/version, HOME/Leanback, Accessibility service/router, signer, APK integrity and artifact upload all passed.

0.9.5 scope is deliberately frozen around the accepted 0.9.4 geometry:

- same 240 dp Home card lane;
- same 230 dp installed banner artwork base size;
- same 6 dp inter-card margin;
- same labels and positions;
- still no Home black plate;
- still no normal Home favourite star;
- grab remains `↔` and keeps its 1.03 artwork emphasis;
- Apps drawer unchanged from physically accepted 0.9;
- grab/reorder, Accessibility override, reboot re-arm, Back handling and native double-Home Recent Apps unchanged.

Per `BOOP_RULES.md`, there is no GitHub visual/golden/layout acceptance test. Ryan owns the visual call on the real Shield. No fake automated assertion was added to claim that 5% looks right.

**0.9.5 is CI/signer green only; final HOME visual acceptance is pending Ryan's physical test.**

## Background / Ambient Mode boundary

BOOP remains pure black in 0.9.x. Shield/Google Ambient Mode stays a separate idle/screensaver layer. Future background work is separate.

## Setup reality

Known working first-time setup on this Shield firmware:

**Shield Settings -> Accessibility -> Services -> BOOP Home Override -> ON**

## LOCKED Shield contract

**Remove the crap, preserve Shield behavior.**

Keep:

- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- real wide Home banners and grab/reorder;
- physically accepted floating square Apps drawer from 0.9;
- Home has no black focus plate and no normal favourite stars;
- Home focus enlarges artwork only, never the whole card/label;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- no ad, Shop, Discover or sponsored provider.

## Next physical test

Install/update to 0.9.5 and judge the focused HOME banner pop only. Everything else should look exactly like the physically good 0.9.4 layout. Confirm the selected banner now pops enough without touching or covering adjacent banners. If accepted, recheck grab once plus single Home, double Home and reboot before promoting the final standalone visual checkpoint.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
