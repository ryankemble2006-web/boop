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
- HOME floating-banner direction is liked, but spacing needed refinement.

### 0.9.1 Home spacing: physical FAIL

Build head `2ddeed52674eb1c8c82025fc43ededdc82c4f12f`, version 10 / `0.9.1-home-spacing`, workflow `34246775470` SUCCESS, artifact `10064387830`, APK SHA-256 `940aedfe7783f1405357d76a0546653476189175282243b4366b9f4e6c879faf`.

Ryan physically tested 0.9.1 and supplied a real Shield/TV photo. Verdict: **"they got further apart..."**. Root cause was a 230 dp visible banner still centred inside a 250 dp card plus 6 dp margin, leaving large invisible spacing.

### 0.9.2 packed Home row: physical FAIL

Build head `85054f38ca584ba200308db2448441633a9347ec`, version 11 / `0.9.2-home-pack`, workflow `34249370093` SUCCESS, artifact `10065417245`, APK SHA-256 `a42afcaf533023fd7605c317b2bc668f02265ab7ab6c1d394957a56b74e4802c`.

0.9.2 fixed the idle spacing by making the HOME card hug the banner: 234 dp card, 230 dp artwork, 2 dp horizontal padding and 2 dp inter-card margin.

Ryan physically tested it and supplied a real Shield/TV photo. Verdict: **"better but we now have overlap."** Treat 0.9.2 as a physical HOME visual failure.

Root cause: HOME still inherited the roomy Apps-card focus scale (`1.08`) and grabbed scale (`1.14`). With a tightly packed 234 dp HOME card and tiny gap, focus enlargement had nowhere to go except into the neighbour.

### Current visual candidate: 0.9.3 focus clearance

0.9.3 changes HOME focus geometry only. The physically accepted Apps drawer remains untouched.

- Build head: `99900d761a8dc11c7c17d6989898aee7e9582796`
- Version: 12 / `0.9.3-focus-clearance`
- Workflow: `34251324746` SUCCESS
- Artifact: `BOOP-Shield-Clean-Launcher`, ID `10066192982`
- APK SHA-256: `99c5896c78a760de8ff22e9ccae792d8c6245854ca5fb772f009ce8feca20295`
- Artifact ZIP SHA-256: `b5a6d672df6c51fdfe8c653d5d77774caa4500ef35105b5edc111ca1b207563d`
- Permanent BOOP signer reused and verified.
- Existing focused Shield HOME functional suite passed.
- Signed assembly, exact package/version, HOME/Leanback, Accessibility service/router, signer, APK integrity and artifact upload all passed.

0.9.3 implementation scope:

- HOME card remains tightly packed at 234 dp around the existing 230 dp banner.
- HOME inter-card margin is 4 dp rather than 2 dp.
- HOME gets its own gentle focus scale (`1.02`) and grab scale (`1.03`) so focus stays clear of neighbours.
- Apps drawer keeps the existing stronger shared `1.08` / `1.14` scale and accepted geometry.
- Artwork, labels, grab/reorder logic, Accessibility override, reboot re-arm and native Recent Apps are unchanged.

Per `BOOP_RULES.md`, there is no GitHub visual/golden/layout acceptance test. Ryan judges this manually on the real Shield.

**0.9.3 is CI/signer green only; HOME visual acceptance is pending.**

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
- wide Home banners and grab/reorder;
- physically accepted floating square Apps drawer from 0.9;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- no ad, Shop, Discover or sponsored provider.

## Next physical test

Install/update to 0.9.3. Judge HOME spacing and focused-card clearance by eye first. The idle row should stay tightly packed, while the focused/selected plate should no longer collide with the next banner. Apps drawer should remain exactly as the physically accepted 0.9 drawer. If accepted, recheck grab once plus single Home, double Home and reboot before promoting the visual candidate.

Do not merge into unified until Ryan explicitly approves the standalone behavior.
