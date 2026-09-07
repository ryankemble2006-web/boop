# BOOP Wall v34: caller-owned Launcher transition candidate

Updated 2026-09-07. Branch `boop-wall-free-chat-wip`.

## Current candidate

- VersionCode 34 / `0.4.14-wall-native-chat`, package `com.boop.alpha1`.
- Transition implementation commit `19731b3c223f9b94ba264f07fddd345f08b245bb`.
- Existing permanent BOOP signing identity unchanged.
- Wall's deliberate left swipe still opens the separately installed `com.boop.launcher`.
- The cross-app animation is now owned by Wall at the actual `startActivity()` call, using `ActivityOptions.makeCustomAnimation`.
- Launcher enters from the right while Wall exits left, mirroring the already physically liked Launcher -> Wall direction.
- Protected `source/MainActivity.java` remains unchanged; the transition is applied only to the materialized build output and generates two scoped animation resources.
- Launcher app code is not changed by this Wall candidate.

## Verification evidence

Full workflow run `34086706588` proved the transition patch and build path compile correctly: 169 source/Python tests passed including the focused caller-owned transition assertion; JVM harnesses passed; Android unit/build steps passed; v34 package/version/archive and permanent signer continuity passed; wake activation and chat-mode gesture/persistence smoke passed.

That run later failed an unrelated Wall polish instrumentation assertion while sampling the concurrent idle blink (`Production blink did not close at midpoint: openness=0.57552963`). This is not evidence against the cross-app transition and must not be papered over by changing blink code in this task.

This documentation commit intentionally requests the repository's existing `[boop-build-only]` route so a signed v34 artifact can be produced without rerunning the unrelated blink instrumentation. Do not describe skipped tests as passed. Final animation direction still requires Ryan's physical Pixel test.

## Preserved behavior / boundaries

- Wall package remains `com.boop.alpha1`; Launcher remains `com.boop.launcher`.
- Existing left-swipe detection, tap/hold/voice/HA behavior, chat mode, wake path, permissions and signing setup are unchanged by the transition patch.
- Launcher -> Wall right-swipe behavior is intentionally untouched.
- No checkpoint is promoted or repointed by this candidate.
- No automatic install or permission changes.

## Accepted Wall baseline remains unchanged

Physical accepted Wall checkpoint remains `595e1daa43393882a0e5de43967545ac526b8b66` / tag `checkpoint-boop-wall-595e1da`. Newer v34 chat/blink/transition work is candidate evidence only until Ryan physically accepts the relevant behavior.
