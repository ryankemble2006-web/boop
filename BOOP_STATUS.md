# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer presentation: 0.9 floating square icons
- 0.9.4 HOME geometry/chrome: physically good; focus pop slightly too subtle
- Current HOME visual candidate: version 14 / `0.9.5-home-focus-pop`
- 0.9.5 build head: `66a15f89969c547224ed22d962f609243effae6e`
- 0.9.5 workflow: `34255507581` SUCCESS
- 0.9.5 artifact ID: `10067748314`
- 0.9.5 APK SHA-256: `e42d2f9c85244a52ec3124dd9d68d6cac6bcf56878d122c7743f7e9ea942d00e`
- 0.9.5 artifact ZIP SHA-256: `556f9aedb15ef4ad3b7a4effc8ce5ecbc4465b36588eeaf1de1b08d2272bc36b`
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
- 0.9.1 HOME: **physical FAIL**, spacing looked wider.
- 0.9.2 HOME: **physical FAIL**, idle spacing improved but focused card overlapped neighbours.
- 0.9.3 HOME: **physical FAIL**, black focus plate/whole-card emphasis rejected.
- 0.9.4 HOME: **physically good geometry/chrome**. Ryan: **"awesome.. make them pop out a few more pixels when highlighted. almost perfect"**. Preserve its spacing, fixed labels, no-overlap result, no black Home plate and no normal Home stars.

## Current 0.9.5 candidate

0.9.5 changes only the focused Home banner artwork amount:

- focused HOME artwork scale: `1.05` instead of `1.03`;
- grabbed HOME artwork stays `1.03`;
- fixed 240 dp Home lane unchanged;
- 230 dp banner base size unchanged;
- 6 dp inter-card margin unchanged;
- labels/positions unchanged;
- no Home plate and no normal Home star;
- Apps drawer unchanged from its accepted 0.9 presentation;
- artwork remains original/untinted;
- grab/reorder, Accessibility override, reboot re-arm and Home/Recent Apps behavior unchanged.

Existing non-visual functional/package/signer/integrity checks are green. Per BOOP visual rules, no automated test claims that the stronger pop looks correct.

**0.9.5 HOME appearance is awaiting Ryan's physical verdict.**

## Locked behavior

**Remove the crap, preserve Shield behavior.**

Keep unchanged:
- single Home -> BOOP;
- double Home -> native Recent Apps/task switcher;
- BOOP Home Override across reboot;
- stock Android TV Home installed/enabled as recovery/trigger;
- real wide Home banners;
- physically accepted floating square Apps drawer;
- no Home black focus plate;
- no normal Home favourite stars;
- Home focus enlarges artwork only, not card/label;
- grab/reorder;
- single Back -> favourite item 1;
- long Back/top-right Settings -> real Shield Settings;
- volume/CEC/system shortcuts and animations;
- pure black background for current 0.9.x builds.

## Next gate

Install/update to 0.9.5 and judge only the extra Home focus pop. Everything else should remain the physically good 0.9.4 layout. If accepted, recheck grab plus single Home, double Home and reboot before promoting the final standalone visual checkpoint.
