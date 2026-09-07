# BOOP Wall v34: caller-owned Launcher swipe animation

Updated 2026-09-07. Owning branch: `boop-wall-free-chat-wip`.
Package: `com.boop.alpha1`. VersionCode 34 / `0.4.14-wall-native-chat`.

## Latest user evidence

Ryan physically reported that the first Launcher-side attempt did not change the cross-app swipe animation at all. Treat that as a rejected approach. The reason is architectural: the activity that calls `startActivity()` owns the custom transition, so a receiving-Launcher OPEN override cannot reliably control the Wall -> Launcher launch.

The direction Ryan already likes remains Launcher -> Wall on a right swipe. The requested mirror is Wall -> Launcher on a left swipe: Launcher should enter from the right while Wall exits left.

## Current scoped fix

Transition implementation commit: `19731b3c223f9b94ba264f07fddd345f08b245bb`.

Protected `source/MainActivity.java` remains byte-for-byte untouched. `scripts/materialize-android.sh` patches only the materialized build output at the existing Wall `startActivity(launcherIntent)` call and supplies `ActivityOptions.makeCustomAnimation` with:

- `boop_launcher_enter_from_right`: 100% -> 0% X, 220 ms;
- `boop_wall_exit_to_left`: 0% -> -100% X, 220 ms.

This keeps the Wall and Launcher packages independent and does not alter the already-liked Launcher -> Wall right-swipe path. Existing tap/hold/voice/HA/chat/wake behavior, permissions and signing setup are not intentionally changed by this transition patch.

## Verification and signed artifact

Focused/full run `34086706588` verified the transition source assertion plus the normal Wall source/JVM/build/sign path, but later failed an unrelated idle-blink midpoint instrumentation assertion (`openness=0.57552963`). Do not modify blink behavior as part of this animation task and do not describe that full run as green.

A separate explicit build-only run was triggered by documentation commit `593abaf232727b62cd3d49f0e7fb53ea7f572214` using the repository's existing `[boop-build-only]` route:

- GitHub Actions run: `34087312865`
- conclusion: success
- stable-signed Wall v34 build: success
- package/version/archive/signer continuity: success
- emulator/instrumentation/tests deliberately skipped in this build-only run
- artifact: `BOOP-Wall-Native-Chat-candidate`
- artifact ID: `10005662085`
- extracted APK SHA-256: `82e01c9a68cb104882b6401803584069ba9085f5a450fe354af6b210c470b5e6`
- existing permanent BOOP signer unchanged

Physical animation acceptance is still pending Ryan's Pixel test. Do not call this transition physically fixed until he confirms the visual direction changed.

## Preservation

The accepted Wall physical checkpoint remains `595e1daa43393882a0e5de43967545ac526b8b66` / `checkpoint-boop-wall-595e1da`. Do not move it. v33 faster-blink history remains in repository history/docs; Ryan previously reported v32 blinking worked physically. The independent blink work is not part of this transition change.

Launcher remains a separate app (`com.boop.launcher`). Preserve its accepted fullscreen, drawer and reverse-swipe behavior. No automatic installation or permission change was performed.

## Next safe step

Install the signed Wall v34 candidate over the current Wall and physically check only the left-swipe Wall -> Launcher visual transition. Expected: Launcher enters from the right and Wall exits left. If it still uses Android's default same-direction animation, capture that as Pixel runtime evidence and investigate task/window transition policy rather than changing Launcher again.
