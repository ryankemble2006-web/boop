# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer presentation: 0.9 floating square icons
- Current HOME visual candidate: version 11 / `0.9.2-home-pack`
- 0.9.2 build head: `85054f38ca584ba200308db2448441633a9347ec`
- 0.9.2 workflow: `34249370093` SUCCESS
- 0.9.2 artifact ID: `10065417245`
- 0.9.2 APK SHA-256: `a42afcaf533023fd7605c317b2bc668f02265ab7ab6c1d394957a56b74e4802c`
- 0.9.2 artifact ZIP SHA-256: `f1e360246d1d248e3a8228434a1827a41f87e5114b8c1f76e2472e2c01fbe8fc`
- Permanent BOOP signer reused and verified

## Physical state

Core mechanism physically green from 0.8:

- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override remains ON across reboot;
- stock Android TV Home does not reclaim the visible HOME surface;
- banners and grab/reorder work.

Visual state:

- 0.9 Apps drawer: **physically accepted** by Ryan ("app drawer great"). Preserve its floating square-icon presentation exactly.
- 0.9 HOME: directionally liked, spacing physically failed as too wide.
- 0.9.1 HOME: **physical FAIL**. Ryan supplied a real Shield/TV photo and reported: **"they got further apart..."**.

0.9.1 root cause: the 230 dp visible banner was still centred inside a 250 dp card plus 6 dp margin, leaving about 26 dp of invisible inter-banner spacing. The fix must make the HOME card hug the banner rather than only shrinking the outer slot.

## Current 0.9.2 candidate

0.9.2 changes HOME favourites only:

- visible banner remains 230 dp;
- HOME card width is 234 dp;
- HOME horizontal card padding is 2 dp;
- HOME inter-card margin is 2 dp;
- focused card may overlap the tiny gap when it scales;
- Apps drawer remains unchanged;
- artwork, grab/reorder, Accessibility override, reboot re-arm and Home/Recent Apps behavior are unchanged.

CI/signer/package/integrity checks are green. Per BOOP visual rules, **0.9.2 HOME appearance is not physically accepted until Ryan judges it on the real Shield**.

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

Install/update to 0.9.2 and judge HOME spacing by eye. Apps drawer should remain visually unchanged from the accepted 0.9 drawer. If Home spacing is accepted, recheck grab plus single Home, double Home and reboot before promoting the visual candidate.
