# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer presentation: 0.9 floating square icons
- Current HOME visual candidate: version 12 / `0.9.3-focus-clearance`
- 0.9.3 build head: `99900d761a8dc11c7c17d6989898aee7e9582796`
- 0.9.3 workflow: `34251324746` SUCCESS
- 0.9.3 artifact ID: `10066192982`
- 0.9.3 APK SHA-256: `99c5896c78a760de8ff22e9ccae792d8c6245854ca5fb772f009ce8feca20295`
- 0.9.3 artifact ZIP SHA-256: `b5a6d672df6c51fdfe8c653d5d77774caa4500ef35105b5edc111ca1b207563d`
- Permanent BOOP signer reused and verified

## Physical state

Core mechanism physically green from 0.8:

- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override remains ON across reboot;
- stock Android TV Home does not reclaim the visible HOME surface;
- banners and grab/reorder work.

Visual state:

- 0.9 Apps drawer: **physically accepted** by Ryan ("app drawer great"). Preserve it exactly.
- 0.9 HOME: directionally liked, too much spacing.
- 0.9.1 HOME: **physical FAIL**, spacing looked even wider.
- 0.9.2 HOME: **physical FAIL**, spacing became much better but focused card overlapped its neighbour. Ryan's verdict: **"better but we now have overlap."**

0.9.2 root cause: the tightly packed HOME row still inherited Apps-card focus/grab scales, so the card expanded into adjacent banners.

## Current 0.9.3 candidate

0.9.3 changes HOME focus clearance only:

- HOME banner/card packing remains tight;
- inter-card margin is increased slightly from 0.9.2;
- HOME focus/grab enlargement is reduced substantially;
- Apps drawer keeps its accepted stronger focus/grab scale and geometry;
- artwork, grab/reorder, Accessibility override, reboot re-arm and Home/Recent Apps behavior are unchanged.

CI/signer/package/integrity checks are green. Per BOOP visual rules, **0.9.3 HOME appearance is not physically accepted until Ryan judges it on the real Shield**.

## Locked behavior

**Remove the crap, preserve Shield behavior.**

Keep unchanged:
- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- real wide Home banners;
- physically accepted floating square Apps drawer;
- grab/reorder;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- pure black background for current 0.9.x builds.

## Next gate

Install/update to 0.9.3 and judge HOME idle spacing plus focused-card clearance by eye. Apps drawer should remain visually unchanged from the accepted 0.9 drawer. If Home is accepted, recheck grab plus single Home, double Home and reboot before promoting the visual candidate.
