# BOOP unified handoff

Updated 2026-09-08. Canonical app branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current unified candidate

The clean Nvidia Shield HOME replacement is implemented in the canonical unified lineage and has a fully green non-visual GitHub build.

Candidate code: `e2c938ed0a035913b6fb8499aad1c3b89eb3aaac`.
Version: 46 / `1.2.0-unified-shield-home`.
Successful workflow: `34215725283`.
Artifact: `BOOP-Unified`, ID `10051749294`.
APK SHA-256: `94f0046a93797606176fdd247c328aa189adb161cb6468346d26f69b8f71cb54`.
Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `e3fb91691c0f828edba8469e15677814fb48ce1fc2955693eb793343356e5cd4`.

Fresh workflow evidence: 5 non-visual integration contracts passed; Launcher lint passed; 58 Shield focused tests, 26 Shield HOME focused tests and 74 unified focused tests all completed with zero failures/errors/skips; signed APK assembly passed; package `com.boop.alpha1`, version, internal `com.boop.shieldhome.ShieldLauncherActivity`, exported `UnifiedEntryActivity`, HOME category, permanent signer and ZIP integrity were verified. No emulator/device launch, screenshots, golden tests or visual acceptance were performed.

Physical acceptance of this candidate is still pending Ryan's real Shield test.

## Shield clean HOME behavior

Default Shield HOME is deliberately small and quiet:

- favourite apps only by default;
- Apps and launcher Settings remain available;
- Play Next and app content rows are optional and independently OFF by default;
- disabled optional rows do not create/fetch their provider;
- advertising, sponsored surfaces, Shop and Discover have no provider/restore path;
- favourites can be added, removed and reordered with the remote;
- package catalogue is cached and refreshed only for relevant package changes;
- focus/page animation is local to BOOP and does not alter Android global animation scales;
- stock Android TV launcher remains installed and selectable as the recovery HOME.

`UnifiedEntryActivity` remains the sole exported HOME/LAUNCHER doorway. On Shield, a HOME intent routes to `com.boop.shieldhome.ShieldLauncherActivity`; an ordinary BOOP app launch still routes to the existing Shield puppet.

## LOCKED Shield muscle-memory contract

Ryan's rule: **remove the crap, preserve Shield behavior**.

The replacement HOME must not intentionally take ownership of Nvidia/system behaviors that are useful outside the launcher. Preserve Shield muscle memory wherever Android/Nvidia owns it. Physical acceptance specifically includes:

- single Home returns to BOOP HOME when BOOP is selected as launcher;
- double-tap Home must continue to open Nvidia/Android Recent Apps / task switcher;
- Back behavior remains normal;
- CEC, volume and system remote shortcuts remain system-owned;
- Nvidia/Android system Settings remain reachable;
- app switching and normal system animations remain intact.

If a system shortcut breaks on real hardware, fix that narrow break later rather than expanding BOOP into a global remote-key interceptor or reimplementing Shield OS behavior. Do not disable/replace the stock launcher package, intercept Home globally, change secure settings, or alter global animation scales merely to make BOOP HOME work.

## Other current BOOP state to preserve

The approved paired black-lidded eye master is now materialized into the unified phone/Wall and Shield build path. Preserve the approved eye geometry/alpha, iris-only user hue behavior, existing blink curve/timing/lifecycle gates, headphones/puppetry and five-digit yellow hands. Ryan owns visual acceptance.

HA naming and Home control buttons were physically accepted earlier; preserve that path. Room changes must tear down previous-room state before rebuilding and Shield density scaling must remain idempotent, never cumulative or system-wide.

Custom wake naming now includes the local five-utterance enrolment materialization that landed concurrently before the successful candidate run. Do not infer physical wake acceptance from CI alone.

Assistant selection/remote microphone remains a separate physical boundary. Use supported Android assistant routes only, with no overlay microphone, competing recorder, Google-disable/default hacks, Button Mapper, privileged/ADB ownership or OpenAI API requirement. Success still requires actual remote-button invocation plus audio from THAT remote and clean return behavior.

## Physical test order for the Shield HOME candidate

1. Install the normal signed unified APK without removing the stock launcher.
2. Explicitly select BOOP as HOME through Android's supported HOME selection flow.
3. Verify single Home returns reliably to the clean favourites screen.
4. Immediately verify **double-tap Home still opens Recent Apps/task switcher**.
5. Verify Back, volume/CEC, system Settings and ordinary app switching remain normal.
6. Verify default HOME has favourites only, no ad/Shop/Discover space, and smooth local focus/scroll behavior.
7. Verify favourite launch/add/remove/reorder with the Shield remote.
8. Toggle optional rows on/off and confirm state survives restart.
9. Repeatedly return to HOME and confirm no cumulative UI shrinking.
10. Confirm stock launcher can still be selected again.

CI-green and signed does not equal physical acceptance. Record real-device failures individually and repair only what breaks.

## Protected contracts

Keep package `com.boop.alpha1` and the permanent signer. Keep private photos, credentials, device IPs and raw diagnostics out of this public repository. No automatic installs/grants or claims of Windows synchronization. Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` unless Ryan explicitly promotes a newer physically accepted checkpoint.
