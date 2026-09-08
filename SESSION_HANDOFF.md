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

This supersedes the earlier Home chrome direction. The black Home focus plate is now rejected. Home focus must not scale the whole card or move/cover neighbours.

### Current visual candidate: 0.9.4 Home artwork focus

0.9.4 implements the newly approved Home-only contract while leaving the physically accepted Apps drawer unchanged.

- Build head: `a8a207c97ddddc9b8b36ef99fa7b8718d91588c0`
- Version: 13 / `0.9.4-home-artwork-focus`
- Workflow: `34253939244` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10067129563`
- APK SHA-256: `8840c68a834e7c65b8473631dca5c0e05929a3f931621c4bc232a96155e17909`
- Artifact ZIP SHA-256: `8534bd664588ae27227b9719766475026cede17885a5b1804f5f3ee47db070cd`
- Permanent BOOP signer reused and verified.
- Existing focused Shield HOME functional suite passed.
- Signed assembly, exact package/version, HOME/Leanback, Accessibility service/router, signer, APK integrity and artifact upload all passed.

0.9.4 Home contract:

- HOME never shows the dark focus/selection/grab plate.
- Normal HOME favourite star badges are removed.
- Grab still shows the `↔` indicator so reorder state remains explicit.
- HOME focus/grab leaves the card and label at fixed 1.0 scale and enlarges only the banner artwork slightly (`1.03`).
- HOME banner artwork remains the installed Android TV banner, untinted and unrecoloured.
- HOME reserves a fixed 240 dp card lane around the 230 dp banner with a 6 dp inter-card margin so the artwork can grow without intentionally crossing a neighbour boundary.
- Apps drawer presentation and its existing square-icon focus/plate behavior are unchanged from the physically accepted 0.9 result.
- Grab/reorder logic, Accessibility override, reboot re-arm, Back handling and native double-Home Recent Apps are unchanged.

TDD receipt for this change:

- Red contract commit `e3af797c60e6ce921f91008dba64198a4a865e21` failed at the unit-test gate with exactly the missing Home-only chrome/badge/artwork-emphasis policy APIs.
- Green feature source passed the focused functional suite and signed/package checks before the version bump.
- Final versioned 0.9.4 run `34253939244` is fully green.

Per `BOOP_RULES.md`, there is no GitHub visual/golden/layout acceptance test. Ryan judges this manually on the real Shield.

**0.9.4 is CI/signer green only; HOME visual acceptance is pending.**

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
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- no ad, Shop, Discover or sponsored provider.

## Next physical test

Install/update to 0.9.4 and judge HOME only first:

- idle banners have a tidy gap;
- focused HOME item has no black plate;
- normal favourite stars are gone from HOME;
- the banner itself grows only slightly on focus;
- labels and adjacent banners stay fixed and do not overlap;
- Apps drawer remains exactly as the physically accepted 0.9 drawer.

If the Home visual is accepted, recheck grab once plus single Home, double Home and reboot before promoting the visual candidate.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
