# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Current visual candidate: version 10 / `0.9.1-home-spacing`
- 0.9.1 build head: `2ddeed52674eb1c8c82025fc43ededdc82c4f12f`
- 0.9.1 workflow: `34246775470` SUCCESS
- 0.9.1 artifact ID: `10064387830`
- 0.9.1 APK SHA-256: `940aedfe7783f1405357d76a0546653476189175282243b4366b9f4e6c879faf`
- 0.9.1 artifact ZIP SHA-256: `46f2153b753a975fe1a7e1db791e1e6de51d9be9b5fbd3a2daf9720b8b46343d`
- Permanent BOOP signer reused and verified

## Physical state

Core mechanism physically green from 0.8:

- single Home -> BOOP;
- double Home -> native Nvidia/Shield Recent Apps;
- BOOP Home Override remains ON across reboot;
- stock Android TV Home does not reclaim the visible HOME surface;
- banners and grab/reorder work.

0.9 visual result:

- **Apps drawer physically liked/accepted by Ryan: "app drawer great". Preserve its floating square-icon presentation exactly.**
- HOME floating banners are directionally right but physically failed spacing review because the icons/banners are too far apart.

0.9.1 is the Home-only spacing correction. It tightens Home favourite card width, gap and Home-specific horizontal padding while leaving the Apps drawer untouched. It does not change artwork, focus scale, grab behavior, Accessibility override, reboot re-arm or Home/Recent Apps behavior.

Per BOOP visual rules, physical appearance is judged only by Ryan on the real Shield. 0.9.1 is CI/signer green; **Home spacing physical acceptance is pending**.

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

Install/update to 0.9.1 and judge the HOME favourite spacing by eye. The Apps drawer should remain visually unchanged from the physically liked 0.9 drawer. If Home spacing is accepted, recheck grab plus Home/double-Home/reboot before promoting the visual candidate.
