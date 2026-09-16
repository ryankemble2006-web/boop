# v206 idle Home host repair - 2026-09-16

Owner: `boop-hand-colour-v191`. Ryan has physically accepted v205 Home spacing, including the left alignment. His new instruction is to fix only the large idle BOOP's bottom-right placement; preserve all approved spacing and controls.

Live v205 no-media screenshot confirms Now Playing disappears and favourites stay parked, but idle BOOP is small and overlaps the right-hand favourite artwork. The device hierarchy confirms the idle host was assigned media-bay dimensions/offsets instead of its caller-specified Home parameters.

Root cause: `ShieldNowPlayingPuppetView.onAttachedToWindow()` invokes `lockToMascotBay()` for every FrameLayout parent. v204 moved the independent Home puppet from LinearLayout into a FrameLayout; that legacy mutator now overwrites its Home size, gravity and margins. The intended Home parameters in `ShieldHomeView` are already correct.

Bounded correction: guard the legacy attach-time mutator by `presentationOwner == HOME_NOW_PLAYING`. The idle host retains the caller's parameters. Do not change `ShieldHomeView`, `TvAppCardView`, the renderer, saved choices, permissions, voice or media behavior.

Regression: execute the actual production mutator against recording View parameter stand-ins, checking idle-host isolation, unchanged media allocation, idempotent media attachment, non-FrameLayout and null parents at three densities. This is a host ownership logic test, not rendering or visual certification. RED run `35098539710` at `83b74db6732ec93f8b58cd2b68c296daa5e885c4`: 1 failed, 21 passed. Exact failure: idle dimensions 360x220 and bottom gravity replaced with 230x154 and top gravity.


## Completed implementation and verification

The published app change is exactly the four-line owner guard. Source `9d57019d9370dbe3f47061b6e8b0ce8ed5134715` preserves `ShieldHomeView` and `TvAppCardView` byte-for-byte, including v205 top alignment, parked favourites and equal gaps. Only version/build identity metadata and its test gate accompany the behavior correction.

The new executable regression was RED in run `35098539710` before the guard and GREEN in signed run `35099151524` afterward, including the materialized-source checks. The full existing build/test/signing/package workflow passed. The separate Home-layout, TV/Voice and voice-profile workflows also passed for the candidate.

Artifact: `10447197742`, `BOOP-Unified-v206-Idle-Home-Corner`. Build commit: `9d57019d9370dbe3f47061b6e8b0ce8ed5134715`. Installed package: `com.boop.alpha1`, `206` / `1.2.206-idle-home-corner`. GitHub artifact and pulled-back installed base APK both have SHA-256 `b5f7b0570eccb171bcda7a2ad4e58e79cb397768ec12851e110f04b238713bca`. The existing permanent signer was retained and `adb install -r` returned Success. No app data, permissions, saved settings, phones or other packages were changed.

Runtime verification: after installation, playback was active and the small Now Playing puppet occupied its normal slot. Returned Home and activated the live Close player control once. Fresh screenshot and view hierarchy confirmed Now Playing invisible, its puppet gone, favourites unchanged, and the 360x220dp idle Home puppet bottom-right. Another Home entry and fresh screenshot confirmed that attachment no longer overwrites that placement. The strip remained in exactly the same position in both states.

Acceptance boundary at the implementation receipt: the assistant inspected the live before/after screenshots; Ryan had accepted v205 spacing and retained final physical acceptance of this corner correction. Other app/Cast/end-of-track exit routes were not individually exercised. No claim of new voice/global-focus acceptance. Screenshots and raw diagnostics remain private. Historical dirty v203 documentation in the laptop checkout remains untouched; GitHub owns this current receipt.

## Subsequent user acceptance

After the reload Ryan confirmed: "tested after you reloaded.. perfection. thanks boops". The tested Home/corner correction is therefore physically accepted alongside his v205 spacing/alignment acceptance. No further Home change is implied. This does not establish final Voice/global TV focus acceptance or an exhaustive test of every external media exit route.

The next agreed task is the latest-source Wall/Shield split, gated by completing and preserving current Voice/UI work. Context: `docs/handoffs/2026-09-16-wall-shield-split-prerequisites.md`.
