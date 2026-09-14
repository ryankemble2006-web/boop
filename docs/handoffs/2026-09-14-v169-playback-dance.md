# v169 native bounce with playback fallback: installed on Shield

Updated 2026-09-14. Owner remains `boop-dance-levels-v167`; branch name does not determine version.

## User verdict and current behavior

Ryan rejected v168's diagnostic/onset dance as late and missing beats. He said the original visualizer bounce had responded in time. His earlier explicit fallback choice was playback-driven dancing if diagnostic levels proved unsuitable.

v169 removes the diagnostic reader, its parser and the onset detector from the app. When Android Visualizer works, the original 33 ms sampling and raw waveform-to-level mapping are restored, retaining the stronger 2x visible lift. When capture fails or stays silent, a smooth playback-driven curve is evaluated on BOOP's own frame clock; native capture is retried only every 10 seconds while fallback is active. It stops with the existing playback/visibility/permission lifecycle. This fallback is explicitly independent of a song's actual beat or tempo.

Keep the accepted automatic routing: Deezer/native music at 44.1 kHz, video at normal 48 kHz. Keep BOOP animation independent of Android's zero animation scales. Audio routing, voice repair, artwork, renderer, saved settings and other devices were not changed. No diagnostic polling remains in the dance path.

## Exact installed candidate

- Package/version: `com.boop.alpha1`, `169 / 1.2.169-playback-dance`.
- Signed source: `01f0053b922a8c344d5534b17cf342c1c657703c`.
- Successful GitHub run: `34866309663`.
- Artifact: `10357665694`, `BOOP-Unified-v169-Playback-Dance`.
- ZIP SHA256: `ab5e14b98618abb2f0c4efe3fd80df72ff93086ee111ac2370e15a5867b32098`.
- APK SHA256: `c8390c15fe6dec6ae1048472bdbc79d5df81658d03131846f4992d199b64472e`.
- Permanent signer: `f5af40378ef06445b43f6001ae602fc18ce16eefbabdefd23afe178a47b5cdde`.

## Verification

RED run 34866199744 failed because delayed diagnostic polling was still present. GREEN passed 12 real worker/behavior scenarios plus 1,429 curve bounds checks (1,441 total), covering exact original native mapping/cadence, blocked fallback, no native work on render calls, pause/resume, native recovery, stale samples, revoked permission and silent routes. Existing envelope/renderer, voice (14 scenarios twice), natural-voice, lyrics, animation and ownership tests passed; inherited one-time lyrics source-freeze skips are not passes. Production preservation gate passed. Signed compile completed successfully. Read-only source review found no blocking issue.

Downloaded package/version, signer, hash and built-source receipt independently checked. Shield update succeeded; installed readback hash matches. All saved preference-file hashes remain unchanged across installation. Live logs report playback-driven fallback on Deezer's blocked direct output. Pause changed media state to PAUSED and removed the sampler worker; resume changed it to PLAYING and reactivated fallback. Deezer still uses direct 44.1 kHz; Android animator scale remains zero. Music left playing on BOOP Home/Now Playing.

No local app source builds/tests, emulator/hosted visual tests, permission/role/settings changes or phone operations. Historical v166/v167/v168 APKs are retained privately for recovery; raw logs and UI captures are not published. Previous v168 diagnostic attempt and its rejected verdict remain in its historical receipt.

## Pending acceptance

Asked Ryan whether v169 movement is smooth and enjoyable, explicitly explaining that fallback is not beat-synchronized. His physical verdict is pending. Do not call fallback beat sync or final visual acceptance proven. The accepted v166 speech fix remains inherited and covered by regression tests. Phone remains v166. No unrelated merge or main/other-owner advancement.
