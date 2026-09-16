# v206 idle Home host repair - 2026-09-16

Owner: `boop-hand-colour-v191`. Ryan has physically accepted v205 Home spacing, including the left alignment. His new instruction is to fix only the large idle BOOP's bottom-right placement; preserve all approved spacing and controls.

Live v205 no-media screenshot confirms Now Playing disappears and favourites stay parked, but idle BOOP is small and overlaps the right-hand favourite artwork. The device hierarchy confirms the idle host was assigned media-bay dimensions/offsets instead of its caller-specified Home parameters.

Root cause: `ShieldNowPlayingPuppetView.onAttachedToWindow()` invokes `lockToMascotBay()` for every FrameLayout parent. v204 moved the independent Home puppet from LinearLayout into a FrameLayout; that legacy mutator now overwrites its Home size, gravity and margins. The intended Home parameters in `ShieldHomeView` are already correct.

Bounded correction: guard the legacy attach-time mutator by `presentationOwner == HOME_NOW_PLAYING`. The idle host retains the caller's parameters. Do not change `ShieldHomeView`, `TvAppCardView`, the renderer, saved choices, permissions, voice or media behavior.

Regression: execute the actual production mutator against recording View parameter stand-ins, checking idle-host isolation, unchanged media allocation, idempotent media attachment, non-FrameLayout and null parents at three densities. This is a host ownership logic test, not rendering or visual certification. RED run `35098539710` at `83b74db6732ec93f8b58cd2b68c296daa5e885c4`: 1 failed, 21 passed. Exact failure: idle dimensions 360x220 and bottom gravity replaced with 230x154 and top gravity.

Next: run the same focused gate followed by the existing signed GitHub build, install the verified v206 candidate on the already-authorized Shield, read back version/hash, and capture the no-media Home screen. Ryan retains final physical acceptance. No phone operations or local app-source edits/builds.
