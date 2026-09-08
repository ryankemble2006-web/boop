# BOOP unified handoff

Updated 2026-09-08. Canonical app branch `boop-unified`; package `com.boop.alpha1`; permanent signer unchanged. Always re-fetch live `boop-unified` and `main` before edits and preserve concurrent work.

## Current unified candidate

The current signed unified candidate adds local five-sample custom wake-name training on top of the clean Nvidia Shield HOME work.

Candidate code: `aad1e20aae1bbd15423a7bf0f307d364039cf506`.
Version: 47 / `1.2.1-unified-wake-training`.
Successful workflow: `34216093167`.
Artifact: `BOOP-Unified`, ID `10051904537`.
APK SHA-256: `b99a83873a44a5dd3a4ac2fea8e32633db71a14ef38fac8bcc7b6cbf6970b6b2`.
Permanent signer SHA-256: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.
Artifact ZIP SHA-256: `12140acd259093ed4c1a48b6a3be169d46635b7e613f08f0afc589de00b1c104`.

Fresh workflow evidence: 5 non-visual integration contracts passed; Launcher lint passed; 58 Shield focused tests, 26 Shield HOME focused tests and 74 unified focused tests all completed with zero failures/errors/skips; signed APK assembly passed; package `com.boop.alpha1`, version 47 / `1.2.1-unified-wake-training`, internal `com.boop.shieldhome.ShieldLauncherActivity`, exported `UnifiedEntryActivity`, HOME category, permanent signer and APK ZIP integrity were verified. The downloaded artifact ZIP and extracted APK were re-hashed locally and matched the workflow receipt. No emulator/device launch, screenshots, golden tests, visual acceptance or acoustic wake acceptance were performed.

Physical acceptance of the custom-name acoustics is still pending Ryan's real Pixel test. Do not describe wake accuracy as fixed until he confirms it on hardware.

## Custom wake-name training, implemented in v47

Ryan approved moving arbitrary-name training to the user rather than pretending one generated keyword model fits everybody.

- `BOOP` permanently remains a valid wake name and does not require user training.
- A custom name is additive, never a replacement for the BOOP fallback.
- Setting a non-BOOP name verbally or in Voice Settings now prompts the user to say that name five times. Voice Settings also exposes a manual `Train wake name · say it 5 times` action.
- Training uses the existing `BoopWakeWordController` microphone ownership. It does **not** open a second competing `AudioRecord` or create another background listener.
- Five spoken examples are segmented from the same 16 kHz PCM stream, trimmed to active speech and converted into amplitude-normalised local pronunciation features. A compact centroid/threshold/duration profile is stored in BOOP preferences; raw PCM enrolment audio is not persisted.
- An obviously inconsistent training example is rejected and the user is asked to say it again rather than silently poisoning the profile.
- At runtime Sherpa remains first. The trained local matcher is an additional route for the custom spoken name and fails safely back to the permanent BOOP/Sherpa path if its profile cannot be used.
- Changing the selected custom name clears the old pronunciation profile because a profile belongs to the name it was trained for.
- Custom names now receive the same full 33 natural wake forms used by BOOP, including bare name plus `hey`, `ey`, `hi`, `hello`, `yo`, `oi`, `ok`, `okay`, `morning`, `good morning`, `evening`, `good evening`, `wake up`, `come on`, `you there`, `listen`, `excuse me` and established prefix/suffix combinations.
- Existing post-wake command capture, coordinator reload/re-arm ownership, charging policy and tap-to-talk policy remain intact.

The acoustic profile is intentionally lightweight and local. CI verifies its deterministic feature/profile/codec contracts, not whether a particular human voice in a particular room crosses the right real-world threshold. Tune only from physical evidence.

## Wake-name physical test order

1. Install the normal signed v47 unified APK on the Pixel 7 Pro test body without changing unrelated permissions/settings.
2. With the phone in its normal continuous-wake condition, choose a custom name such as `Steve`.
3. Let BOOP prompt for five examples; say the same name naturally five times with a short pause between examples.
4. Confirm training reports completion and the selected name remains after reopening BOOP.
5. Try the bare custom name repeatedly from normal listening distance.
6. Try several established wrappers, especially `Hey Steve`, `Oi Steve`, `Morning Steve`, `Steve wake up`, `Listen Steve` and `Excuse me Steve`.
7. Confirm plain `BOOP` still wakes BOOP after custom training.
8. Try ordinary conversation, TV/music and similar-sounding words to watch for false wakes.
9. Repeat at quieter/louder levels and a little farther away before changing thresholds.
10. Undock the phone and confirm the existing tap-to-talk policy still wins where continuous wake is intentionally disabled.

Record misses and false wakes separately. Do not tune from one lucky or unlucky utterance.

## Shield clean HOME behavior to preserve

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

The approved paired black-lidded eye master is materialized into the unified phone/Wall and Shield build path. Preserve the approved eye geometry/alpha, iris-only user hue behavior, existing blink curve/timing/lifecycle gates, headphones/puppetry and five-digit yellow hands. Ryan owns visual acceptance.

Blink is user-confirmed working; do not reopen that defect. HA naming and Home control buttons were physically accepted earlier; preserve that path. Room changes must tear down previous-room state before rebuilding and Shield density scaling must remain idempotent, never cumulative or system-wide.

Assistant selection/remote microphone remains a separate unresolved physical boundary. Use supported Android assistant routes only, with no overlay microphone, competing recorder, Google-disable/default hacks, Button Mapper, privileged/ADB ownership or OpenAI API requirement. Success still requires actual remote-button invocation plus audio from THAT remote and clean return behavior.

## Protected contracts

Keep package `com.boop.alpha1` and the permanent signer. Keep private photos, credentials, device IPs and raw diagnostics out of this public repository. No automatic installs/grants or claims of Windows synchronization. GitHub performs functional/non-visual verification only; Ryan owns visual, animation, device and acoustic acceptance. Protected historical rollback remains `e746affbb82b577cef2f1cf6e731dff186c8f881` unless Ryan explicitly promotes a newer physically accepted checkpoint.
