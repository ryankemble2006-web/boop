# BOOP Shield clean launcher status

Updated 2026-09-08. Standalone branch `boop-shield-clean-launcher`.

- Standalone package: `com.boop.shieldhome`
- Unified/AIO package: `com.boop.alpha1` (separate, untouched)
- Protected physically-green HOME mechanism: version 8 / `0.8.0-reboot-rearm`
- Physically accepted Apps drawer presentation: 0.9 floating square icons
- Current HOME visual candidate: version 13 / `0.9.4-home-artwork-focus`
- 0.9.4 build head: `a8a207c97ddddc9b8b36ef99fa7b8718d91588c0`
- 0.9.4 workflow: `34253939244` SUCCESS
- 0.9.4 artifact ID: `10067129563`
- 0.9.4 APK SHA-256: `8840c68a834e7c65b8473631dca5c0e05929a3f931621c4bc232a96155e17909`
- 0.9.4 artifact ZIP SHA-256: `8534bd664588ae27227b9719766475026cede17885a5b1804f5f3ee47db070cd`
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
- 0.9.3 HOME: **physical FAIL**. Ryan's photo/feedback: **"nope. need spacing.. remove the black box behind icons, then when scrolled on make the icon slightly bigger without breaking the adjacent icons boundaries"**.

The accepted next design removes the Home focus plate entirely and stops scaling the whole Home card.

## Current 0.9.4 candidate

0.9.4 changes HOME chrome/focus only:

- no dark plate on HOME, including focus/selection/grab;
- no normal favourite star badge on HOME;
- grab still exposes `↔`;
- HOME card and label stay fixed at 1.0 scale;
- only the installed banner artwork grows slightly on focus/grab;
- fixed Home lane and explicit spacing reserve clearance for that artwork growth;
- Apps drawer keeps its physically accepted square-icon presentation and existing focus/plate behavior;
- artwork remains original/untinted;
- grab/reorder, Accessibility override, reboot re-arm and Home/Recent Apps behavior are unchanged.

TDD red `e3af797c60e6ce921f91008dba64198a4a865e21` failed only on the deliberately missing Home-only chrome APIs. The final 0.9.4 release run is fully green for tests, signed assembly, exact identity, signer, manifest and APK integrity.

Per BOOP visual rules, **0.9.4 HOME appearance is not physically accepted until Ryan judges it on the real Shield**.

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

Install/update to 0.9.4 and judge HOME by eye: tidy idle spacing, no Home black plate, no Home favourite stars, slight banner-only focus growth, no neighbour overlap. Apps drawer should remain visually unchanged from the accepted 0.9 drawer. If Home is accepted, recheck grab plus single Home, double Home and reboot before promoting the visual candidate.
