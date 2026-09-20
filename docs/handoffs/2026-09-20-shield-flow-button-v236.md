# Shield v236 Flow button

2026-09-20. Ryan requested replacing the Now Playing Close player button with Deezer Flow, labelled exactly Flow. Baseline is user-accepted v235; remote/source were verified at7db7295186c7b4a5ce02e2ee5f652c38cbd8d73c before editing. Existing task-owned worktree: .worktrees/boop-deezer-invisible-v235. Primary checkout and concurrent work are untouched.

## Change

The existing130x44dp slot, margins, focus traversal and accent styling are preserved. The Flow callback replaces the old Close player callback in that button only. Close media remains available through its existing separate route. Lyrics, favourite/dislike source, invisible heart helper, artwork, transport geometry and audio/voice/HA logic are untouched.

Flow sends the existing https://www.deezer.com/flow URI directly through the selected native Deezer MediaController.playFromUri with a non-null extras Bundle. This URI was already used in BOOP's native music/voice route. No new display/helper, UI launch, network lookup, shell call, permission, login or dependency is needed for Flow.

The request is restricted to an eligible selected native deezer.android.app session with freshly advertised ACTION_PLAY_FROM_URI. Cast, other players, missing sessions or unsupported URI control produce an unavailable message rather than a visible fallback or the wrong command. A1.2second same-session repeat guard prevents accidental duplicate launches. Dispatch is logged as REQUESTED, not claimed to be provider-confirmed playing. Actual physical Flow playback remains to be tested on the newly built APK.

## Verification before source publication

Two tests were observed failing first: the old button still said Close player, and the native Flow controller did not exist. Both now pass. The real controller executes18 behavioural assertions against test-only Android boundaries: correct Flow URI, non-null extras, missing/wrong/Cast players, live capability loss, paused playback, duplicate clicks, failed dispatch/retry and fresh sessions. A source integration contract checks callback wiring, unchanged slot dimensions and separate Close media preservation.

Ten focused Flow/favourite/invisible-heart/alignment tests passed locally. Production diff and git whitespace checks were reviewed. Review is self-review, not an independent review. New tests are added to the full signed build workflow and the lightweight Deezer checks. Full signed CI, actual APK verification and physical button use are pending this source commit. No new emulator run or live recording. No app install or media action yet for this change.

Shield version236 /1.2.236-shield. Wall version207 unchanged; the shared build is not a separate requested Wall update. Existing permanent signer and packaged native/art checks remain required. Keep the accepted v235 favourite round trip as a regression checkpoint.

## Verified v236 build, installation and physical Flow-button test

Source/build commit5bfb8d91f3351cbe1c089fb5e0a2566334db2553.
Signed workflow35533035925/job106137012994 succeeded through focused, inherited v206, split, HA, both APK builds and packaged signature/native/art verification. Lightweight Deezer checks35533036024/job106137012988 also passed.
Artifact10612281598: BOOP-Shield-v236-Wall-v207-Signed.
Archive SHA256 d3c2ea4229b9a7bce8a5edc24760b19a748a0b3fca6029873bc7d37729ad6ed5.
Shield APK BOOP-Shield-v236.apk,160534893 bytes, SHA25693608fae1f3566943fe11e37147a37b5d0bb48bf6938ff711c5ecd054bee06ec.
Permanent signer f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde.

Downloaded ZIP/APK CRC, source receipt, size and SHA256 were independently verified. All16 native libraries and all18 packaged assets match accepted v235 byte-for-byte. Full cryptographic apksigner verification also passed on the laptop. The exact APK was copied to Desktop/APKBOOP and installed with adb install -r on the verified Shield. Installed version236/1.2.236-shield was read back. Accepted v235 desktop rollback was hash-checked and retained. No data wipe, permission changes, native Deezer modification or Wall installation.

A private one-shot check located the actual visible BOOP button by exact text Flow plus content description Start your Deezer Flow, and invoked its accessibility click once. No coordinate guessing, recording or UI polling loop. The installed app logged Native Deezer Flow requested. In the after-state the native Deezer player was PLAYING a different track with a fresh queue and active item0, replacing the previous101-item queue. The exact same BOOP ShieldLauncherActivity remained resumed on the primary display before and after, and remote focus stayed on display0. This is physical evidence of the installed Flow button dispatching and changing native playback without leaving BOOP, not just a compile/stub pass. Ryan subsequently reported that Flow works perfectly; his acceptance and intended listening workflow are recorded below.

The one-shot test disconnected its inspection session and removed its temporary on-device JAR. No extra next-track attention skip was sent: the Flow test itself changed playback once. No emulator or live recording was started. Raw test metadata remains private on the laptop and was not published. Review was self-review; no independent reviewer claimed.

## User-accepted v236 Flow and album-to-Flow use case: 2026-09-20

Ryan reports that the installed Flow button works perfectly. His stated use case is to browse to an album by selecting the Now Playing artwork, listen to the album, then press Flow when it finishes without digging through Deezer's menus. Preserve the direct Flow shortcut alongside album-art browsing and the accepted favourite controls; this is the reason Flow replaced Close player.

This records user acceptance of the installed Flow feature and the described listening workflow. It does not request automatic Flow at album completion, establish cold-start/Cast support, or add a separate measured end-of-queue test. Source5bfb8d91f3351cbe1c089fb5e0a2566334db2553 and the verified v236 APK are unchanged. This feedback update is documentation-only: no build, install, permissions, recording, device input or playback change.
